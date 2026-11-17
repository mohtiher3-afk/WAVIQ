package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.WaviqDatabase
import com.example.model.StartupCheckStatus
import com.example.model.StartupGuardReport
import com.example.model.StartupHealthState
import com.example.model.StartupSubsystemCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Pre-flight startup validator verifying TFLite asset integrity, optional hardware
 * permissions, and Room SQLite connectivity before studio initialization, producing
 * a structured fallback report for degraded-mode resilience.
 */
class StartupGuard(
    private val context: Context,
    private val modelAssetName: String = DEFAULT_MODEL_ASSET,
    private val assetOpener: (String) -> InputStream = { name -> context.assets.open(name) },
    private val permissionGrantedProvider: (String) -> Boolean = { perm ->
        ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
    },
    private val databasePing: suspend () -> Int = {
        WaviqDatabase.getInstance(context).masterTrackDao().getTrackCount()
    }
) {
    suspend fun validate(): StartupGuardReport = withContext(Dispatchers.IO) {
        val tfliteCheck = validateTfliteModel()
        val permissionCheck = validateHardwarePermissions()
        val databaseCheck = validateRoomDatabase()

        val hasCriticalFallback =
            tfliteCheck.status == StartupCheckStatus.FALLBACK ||
                databaseCheck.status == StartupCheckStatus.FALLBACK

        val healthState = when {
            hasCriticalFallback -> StartupHealthState.DEGRADED_FALLBACK
            permissionCheck.status == StartupCheckStatus.ADVISORY -> StartupHealthState.PERMISSION_ADVISORY
            else -> StartupHealthState.HEALTHY
        }

        val issues = buildList {
            if (tfliteCheck.status == StartupCheckStatus.FALLBACK) add(tfliteCheck.detail)
            if (databaseCheck.status == StartupCheckStatus.FALLBACK) add(databaseCheck.detail)
            if (permissionCheck.status == StartupCheckStatus.ADVISORY) add(permissionCheck.detail)
        }

        val summary = when (healthState) {
            StartupHealthState.HEALTHY -> "All TFLite, hardware permission, and Room DB checks passed."
            StartupHealthState.PERMISSION_ADVISORY ->
                "Studio active. Optional mic permission ungranted; file mastering and DSP playback remain operational."
            StartupHealthState.DEGRADED_FALLBACK ->
                "Degraded mode active: ${issues.joinToString(" | ")}"
            StartupHealthState.VALIDATING -> "Pre-flight validation in progress."
        }

        StartupGuardReport(
            healthState = healthState,
            tfliteCheck = tfliteCheck,
            permissionCheck = permissionCheck,
            databaseCheck = databaseCheck,
            fallbackSummary = summary,
            checkedAtMs = System.currentTimeMillis()
        )
    }

    private fun validateTfliteModel(): StartupSubsystemCheck {
        return runCatching {
            val header = assetOpener(modelAssetName).use { stream ->
                val bytes = ByteArray(MIN_TFLITE_BYTES)
                var totalRead = 0
                while (totalRead < bytes.size) {
                    val read = stream.read(bytes, totalRead, bytes.size - totalRead)
                    if (read <= 0) break
                    totalRead += read
                }
                if (totalRead < 8) {
                    error("Asset '$modelAssetName' truncated ($totalRead bytes)")
                }
                bytes.copyOf(totalRead)
            }
            val isTfl3 = header[4] == 'T'.code.toByte() &&
                header[5] == 'F'.code.toByte() &&
                header[6] == 'L'.code.toByte() &&
                header[7] == '3'.code.toByte()
            if (!isTfl3) {
                error("Invalid FlatBuffer signature in '$modelAssetName'")
            }
            StartupSubsystemCheck(
                title = "TFLITE MODEL",
                status = StartupCheckStatus.PASS,
                detail = "Verified TFL3 asset ($modelAssetName)"
            )
        }.getOrElse { err ->
            runCatching {
                Log.w("StartupGuard", "TFLite validation fallback: ${err.message}")
            }
            StartupSubsystemCheck(
                title = "TFLITE MODEL",
                status = StartupCheckStatus.FALLBACK,
                detail = "TFLite degraded (${err.message ?: "missing asset"}); using DSP spectral gate fallback"
            )
        }
    }

    private fun validateHardwarePermissions(): StartupSubsystemCheck {
        return runCatching {
            val micGranted = permissionGrantedProvider(Manifest.permission.RECORD_AUDIO)
            if (micGranted) {
                StartupSubsystemCheck(
                    title = "RECORD_AUDIO",
                    status = StartupCheckStatus.PASS,
                    detail = "Live AudioRecord hardware capture ready"
                )
            } else {
                StartupSubsystemCheck(
                    title = "RECORD_AUDIO",
                    status = StartupCheckStatus.ADVISORY,
                    detail = "Mic permission optional; grant when capturing live takes"
                )
            }
        }.getOrElse { err ->
            StartupSubsystemCheck(
                title = "RECORD_AUDIO",
                status = StartupCheckStatus.ADVISORY,
                detail = "Permission check deferred (${err.message ?: "unavailable"})"
            )
        }
    }

    private suspend fun validateRoomDatabase(): StartupSubsystemCheck {
        return runCatching {
            val count = databasePing()
            StartupSubsystemCheck(
                title = "ROOM SQLITE",
                status = StartupCheckStatus.PASS,
                detail = "Connected ($count saved master${if (count == 1) "" else "s"})"
            )
        }.getOrElse { err ->
            runCatching {
                Log.w("StartupGuard", "Room DB ping fallback: ${err.message}")
            }
            StartupSubsystemCheck(
                title = "ROOM SQLITE",
                status = StartupCheckStatus.FALLBACK,
                detail = "Room DB degraded (${err.message ?: "unavailable"}); operating in session-only fallback"
            )
        }
    }

    companion object {
        const val DEFAULT_MODEL_ASSET = "ai_audio_mastering_model.tflite"
        private const val MIN_TFLITE_BYTES = 64
    }
}
