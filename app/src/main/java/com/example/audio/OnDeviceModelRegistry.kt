package com.example.audio

import android.content.Context
import androidx.compose.runtime.Immutable

enum class ClassificationSource(val badgeLabel: String, val detailLabel: String) {
    HEURISTIC_ACOUSTIC(
        badgeLabel = "HEURISTIC ACOUSTIC",
        detailLabel = "Heuristic acoustic analysis (no trained neural genre model bundled)"
    ),
    LITERT_STYLE_MODEL(
        badgeLabel = "LITERT STYLE MODEL",
        detailLabel = "Quantized on-device LiteRT style classifier"
    )
}

@Immutable
data class OnDeviceModelSpec(
    val id: String,
    val title: String,
    val assetName: String,
    val architecture: String,
    val inputShape: String,
    val outputLabels: List<String>,
    val licenseAndProvenance: String,
    val knownLimitations: String,
    val isBundledAndVerified: Boolean,
    val classificationSource: ClassificationSource? = null
)

/**
 * Documents every on-device ML asset and probes asset presence truthfully so
 * WAVIQ never claims a neural genre classifier when operating in heuristic mode.
 */
object OnDeviceModelRegistry {

    const val MASTERING_GATE_ASSET = "ai_audio_mastering_model.tflite"
    const val OPTIONAL_STYLE_CLASSIFIER_ASSET = "style_classifier.tflite"

    val masteringGateModelSpec: OnDeviceModelSpec = OnDeviceModelSpec(
        id = "mastering_gate_mlp",
        title = "Block Dynamic Gate & Gain MLP",
        assetName = MASTERING_GATE_ASSET,
        architecture = "Compact fully-connected TFLite float MLP (LiteRT XNNPACK CPU)",
        inputShape = "[1, 4] block acoustic features: [rms, peak, zcr, hfEnergy]",
        outputLabels = listOf("denoiseStrength (0..1)", "dynGainLinear (0.75..1.45)"),
        licenseAndProvenance = "Bundled local WAVIQ DSP calibration weights; offline zero-network inference",
        knownLimitations = "Operates only as a per-block noise-floor gate and soft dynamic stabilizer; does not classify genre or separate stems.",
        isBundledAndVerified = true
    )

    fun detectStyleClassificationSource(context: Context?): ClassificationSource {
        if (context == null) return ClassificationSource.HEURISTIC_ACOUSTIC
        val hasValidatedStyleAsset = runCatching {
            val assets = context.assets.list("") ?: emptyArray()
            assets.contains(OPTIONAL_STYLE_CLASSIFIER_ASSET)
        }.getOrDefault(false)

        return if (hasValidatedStyleAsset) {
            ClassificationSource.LITERT_STYLE_MODEL
        } else {
            ClassificationSource.HEURISTIC_ACOUSTIC
        }
    }

    fun getRegisteredModels(context: Context?): List<OnDeviceModelSpec> {
        val styleSource = detectStyleClassificationSource(context)
        val styleModelSpec = OnDeviceModelSpec(
            id = "genre_style_classifier",
            title = "Audio Style / Genre Classifier",
            assetName = OPTIONAL_STYLE_CLASSIFIER_ASSET,
            architecture = if (styleSource == ClassificationSource.LITERT_STYLE_MODEL) {
                "Quantized on-device LiteRT spectrogram classifier"
            } else {
                "Acoustic Heuristic Scorer (no .tflite genre classifier bundled)"
            },
            inputShape = "Windowed BS.1770 / spectral balance / crest / onset features",
            outputLabels = listOf("multi-label music_taxonomy IDs", "unknown_mixed fallback"),
            licenseAndProvenance = "Local deterministic WAVIQ acoustic scoring against music_taxonomy.json",
            knownLimitations = "Heuristic multi-label estimator only; confidence < 0.50 falls back to Unknown / Mixed.",
            isBundledAndVerified = styleSource == ClassificationSource.LITERT_STYLE_MODEL,
            classificationSource = styleSource
        )
        return listOf(masteringGateModelSpec, styleModelSpec)
    }
}
