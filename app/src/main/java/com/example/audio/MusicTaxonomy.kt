package com.example.audio

import android.content.Context
import androidx.compose.runtime.Immutable
import com.example.model.MusicGenre
import org.json.JSONArray
import org.json.JSONObject

@Immutable
data class TaxonomyEntry(
    val id: String,
    val displayName: String,
    val parentGenre: String,
    val subgenre: String? = null,
    val aliases: List<String> = emptyList(),
    val relatedGenres: List<String> = emptyList(),
    val fusionGenres: List<String> = emptyList(),
    val regionTags: List<String> = emptyList(),
    val eraTags: List<String> = emptyList(),
    val productionStyleTags: List<String> = emptyList(),
    val vocalStyleTags: List<String> = emptyList(),
    val moodEnergyTags: List<String> = emptyList(),
    val instrumentationRhythmTags: List<String> = emptyList(),
    val masteringFamilyHint: String = "transparent_headroom",
    val mappedMusicGenre: MusicGenre? = null
)

@Immutable
data class ScoredTaxonomyTag(
    val taxonomyId: String,
    val displayName: String,
    val parentGenre: String,
    val score: Float,
    val isHeuristic: Boolean = true,
    val relatedIds: List<String> = emptyList(),
    val fusionLabels: List<String> = emptyList(),
    val masteringFamilyHint: String = "transparent_headroom"
)

data class LoadedMusicTaxonomy(
    val version: Int,
    val entries: List<TaxonomyEntry>
)

object MusicTaxonomyRepository {

    const val ASSET_FILE_NAME = "music_taxonomy.json"
    const val UNKNOWN_MIXED_ID = "unknown_mixed"

    @Volatile
    private var cachedTaxonomy: LoadedMusicTaxonomy? = null

    fun load(context: Context?): LoadedMusicTaxonomy {
        cachedTaxonomy?.let { return it }
        if (context != null) {
            val loaded = runCatching {
                context.assets.open(ASSET_FILE_NAME).bufferedReader().use { it.readText() }
            }.mapCatching { parseJson(it) }.getOrNull()
            if (loaded != null && loaded.entries.isNotEmpty()) {
                cachedTaxonomy = loaded
                return loaded
            }
        }
        return fallbackTaxonomy().also { cachedTaxonomy = it }
    }

    fun parseJson(jsonText: String): LoadedMusicTaxonomy {
        val root = JSONObject(jsonText)
        val version = root.optInt("version", 1)
        val arr = root.optJSONArray("entries") ?: JSONArray()
        val entries = ArrayList<TaxonomyEntry>(arr.length())
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val mappedName = if (obj.isNull("mappedMusicGenre")) null else obj.optString("mappedMusicGenre")
            val mappedGenre = mappedName?.let { name ->
                MusicGenre.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
            }
            val sub = if (obj.isNull("subgenre")) null else obj.optString("subgenre").takeIf { it.isNotBlank() }
            entries.add(
                TaxonomyEntry(
                    id = obj.getString("id"),
                    displayName = obj.getString("displayName"),
                    parentGenre = obj.optString("parentGenre", "Unclassified"),
                    subgenre = sub,
                    aliases = jsonStringList(obj.optJSONArray("aliases")),
                    relatedGenres = jsonStringList(obj.optJSONArray("relatedGenres")),
                    fusionGenres = jsonStringList(obj.optJSONArray("fusionGenres")),
                    regionTags = jsonStringList(obj.optJSONArray("regionTags")),
                    eraTags = jsonStringList(obj.optJSONArray("eraTags")),
                    productionStyleTags = jsonStringList(obj.optJSONArray("productionStyleTags")),
                    vocalStyleTags = jsonStringList(obj.optJSONArray("vocalStyleTags")),
                    moodEnergyTags = jsonStringList(obj.optJSONArray("moodEnergyTags")),
                    instrumentationRhythmTags = jsonStringList(obj.optJSONArray("instrumentationRhythmTags")),
                    masteringFamilyHint = obj.optString("masteringFamilyHint", "transparent_headroom"),
                    mappedMusicGenre = mappedGenre
                )
            )
        }
        return LoadedMusicTaxonomy(version = version, entries = entries)
    }

    fun findById(taxonomy: LoadedMusicTaxonomy, id: String): TaxonomyEntry? {
        return taxonomy.entries.firstOrNull { it.id == id }
    }

    fun findByAlias(taxonomy: LoadedMusicTaxonomy, query: String): List<TaxonomyEntry> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return taxonomy.entries.filter { entry ->
            entry.id.lowercase() == q ||
                entry.displayName.lowercase().contains(q) ||
                entry.aliases.any { alias -> alias.lowercase() == q || alias.lowercase().contains(q) } ||
                entry.fusionGenres.any { fg -> fg.lowercase().contains(q) }
        }
    }

    fun clearCacheForTest() {
        cachedTaxonomy = null
    }

    private fun jsonStringList(arr: JSONArray?): List<String> {
        if (arr == null) return emptyList()
        return List(arr.length()) { idx -> arr.optString(idx) }.filter { it.isNotBlank() }
    }

    /**
     * Built-in safety fallback when Context/AssetManager is unavailable (e.g., pure JVM tests without context).
     */
    fun fallbackTaxonomy(): LoadedMusicTaxonomy = LoadedMusicTaxonomy(
        version = 1,
        entries = listOf(
            TaxonomyEntry(
                id = UNKNOWN_MIXED_ID,
                displayName = "Unknown / Mixed",
                parentGenre = "Unclassified",
                aliases = listOf("unknown", "mixed"),
                masteringFamilyHint = "transparent_headroom",
                mappedMusicGenre = null
            ),
            TaxonomyEntry(
                id = "pop_contemporary",
                displayName = "Contemporary Pop",
                parentGenre = "Pop",
                aliases = listOf("pop", "mainstream pop", "synth pop"),
                relatedGenres = listOf("rnb_contemporary", "electronic_edm"),
                productionStyleTags = listOf("polished_mid_air", "streaming_balanced"),
                vocalStyleTags = listOf("front_center_lead"),
                moodEnergyTags = listOf("upbeat", "bright"),
                masteringFamilyHint = "streaming_balanced",
                mappedMusicGenre = MusicGenre.POP
            ),
            TaxonomyEntry(
                id = "hip_hop_modern",
                displayName = "Modern Hip-Hop",
                parentGenre = "Hip-Hop",
                aliases = listOf("hip hop", "hip-hop", "rap"),
                relatedGenres = listOf("trap_808", "boom_bap", "trap_soul"),
                productionStyleTags = listOf("punchy_transient", "center_vocal_pocket"),
                vocalStyleTags = listOf("rhythmic_rap"),
                moodEnergyTags = listOf("assertive", "rhythmic"),
                masteringFamilyHint = "low_end_punch",
                mappedMusicGenre = MusicGenre.HIP_HOP
            ),
            TaxonomyEntry(
                id = "trap_808",
                displayName = "Trap",
                parentGenre = "Hip-Hop",
                subgenre = "Trap / 808",
                aliases = listOf("trap", "808 trap", "تراب"),
                relatedGenres = listOf("drill_uk_ny", "trap_soul"),
                fusionGenres = listOf("trap_soul"),
                productionStyleTags = listOf("sub_dominant", "crisp_top_hats"),
                vocalStyleTags = listOf("melodic_rap"),
                moodEnergyTags = listOf("dark", "energetic"),
                masteringFamilyHint = "sub_controlled_modern",
                mappedMusicGenre = MusicGenre.TRAP
            ),
            TaxonomyEntry(
                id = "drill_uk_ny",
                displayName = "Drill",
                parentGenre = "Hip-Hop",
                subgenre = "UK / NY Drill",
                aliases = listOf("drill", "uk drill", "ny drill", "دريل"),
                relatedGenres = listOf("trap_808", "hip_hop_modern"),
                productionStyleTags = listOf("sliding_808_sub", "aggressive_transient_snap"),
                vocalStyleTags = listOf("hard_delivery"),
                moodEnergyTags = listOf("intense", "tense"),
                masteringFamilyHint = "transient_sub_control",
                mappedMusicGenre = MusicGenre.DRILL
            ),
            TaxonomyEntry(
                id = "rnb_contemporary",
                displayName = "R&B",
                parentGenre = "R&B",
                aliases = listOf("rnb", "r&b"),
                relatedGenres = listOf("trap_soul", "alternative_rnb"),
                fusionGenres = listOf("trap_soul", "alternative_rnb"),
                productionStyleTags = listOf("warm_mid_pocket", "smooth_top"),
                vocalStyleTags = listOf("soulful_lead"),
                moodEnergyTags = listOf("smooth", "intimate"),
                masteringFamilyHint = "warm_vocal_pocket",
                mappedMusicGenre = MusicGenre.POP
            ),
            TaxonomyEntry(
                id = "trap_soul",
                displayName = "Trap Soul",
                parentGenre = "R&B",
                subgenre = "Trap Soul",
                aliases = listOf("trap soul", "trapsoul"),
                relatedGenres = listOf("rnb_contemporary", "trap_808", "alternative_rnb"),
                fusionGenres = listOf("rnb_contemporary", "trap_808"),
                productionStyleTags = listOf("warm_filtered_pads", "deep_sub_pocket"),
                vocalStyleTags = listOf("melodic_soul_lead"),
                moodEnergyTags = listOf("moody", "emotive"),
                masteringFamilyHint = "warm_sub_vocal",
                mappedMusicGenre = MusicGenre.HIP_HOP
            ),
            TaxonomyEntry(
                id = "alternative_rnb",
                displayName = "Alternative R&B",
                parentGenre = "R&B",
                subgenre = "Alternative R&B",
                aliases = listOf("alt rnb", "alternative r&b"),
                relatedGenres = listOf("rnb_contemporary", "trap_soul"),
                productionStyleTags = listOf("wide_atmospheric", "textured_low_end"),
                vocalStyleTags = listOf("intimate_falsetto"),
                moodEnergyTags = listOf("atmospheric", "introspective"),
                masteringFamilyHint = "spacious_warm",
                mappedMusicGenre = MusicGenre.POP
            ),
            TaxonomyEntry(
                id = "electronic_edm",
                displayName = "Electronic / EDM",
                parentGenre = "Electronic",
                aliases = listOf("electronic", "edm", "electro"),
                relatedGenres = listOf("house_tech", "techno_industrial"),
                productionStyleTags = listOf("wide_stereo_field", "dense_loudness"),
                moodEnergyTags = listOf("high_energy", "euphoric"),
                masteringFamilyHint = "club_wide_impact",
                mappedMusicGenre = MusicGenre.ELECTRONIC
            ),
            TaxonomyEntry(
                id = "techno_industrial",
                displayName = "Techno",
                parentGenre = "Electronic",
                subgenre = "Techno / Industrial",
                aliases = listOf("techno", "rave", "تكنو"),
                relatedGenres = listOf("house_tech", "electronic_edm"),
                productionStyleTags = listOf("driving_kick_rumble", "wide_industrial_fx"),
                moodEnergyTags = listOf("hypnotic", "relentless"),
                masteringFamilyHint = "club_wide_impact",
                mappedMusicGenre = MusicGenre.TECHNO
            ),
            TaxonomyEntry(
                id = "rock_modern",
                displayName = "Rock / Alternative",
                parentGenre = "Rock",
                aliases = listOf("rock", "alt rock", "indie rock"),
                productionStyleTags = listOf("midrange_guitar_wall", "harmonic_bite"),
                moodEnergyTags = listOf("driving", "raw"),
                masteringFamilyHint = "warm_mid_bite",
                mappedMusicGenre = MusicGenre.ROCK
            ),
            TaxonomyEntry(
                id = "acoustic_folk",
                displayName = "Acoustic / Organic",
                parentGenre = "Acoustic",
                aliases = listOf("acoustic", "folk", "unplugged"),
                productionStyleTags = listOf("natural_room", "transparent_dynamics"),
                vocalStyleTags = listOf("intimate_dry_or_natural_room"),
                moodEnergyTags = listOf("warm", "intimate"),
                masteringFamilyHint = "natural_dynamic_clarity",
                mappedMusicGenre = MusicGenre.ACOUSTIC
            ),
            TaxonomyEntry(
                id = "classical_orchestral",
                displayName = "Classical / Orchestral",
                parentGenre = "Classical",
                aliases = listOf("classical", "orchestral", "symphony"),
                productionStyleTags = listOf("wide_crest_factor", "concert_hall_depth"),
                moodEnergyTags = listOf("expressive", "spacious"),
                masteringFamilyHint = "wide_dynamic_preservation",
                mappedMusicGenre = MusicGenre.CLASSICAL
            )
        )
    )
}
