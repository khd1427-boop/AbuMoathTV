package com.abumoath.tv.data.model

/** UI models shared by all screens. Everything is produced locally on the TV (no own server needed). */

data class TitleCard(
    val id: Long,
    val type: String = "movie",
    val title: String? = null,
    val originalTitle: String? = null,
    val year: Int? = null,
    val country: String? = null,
    val rating: Double? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val quality: String? = null,
    val overview: String? = null,
    val progress: ProgressDto? = null
) {
    val displayTitle: String get() = title ?: originalTitle ?: ""
}

data class ChannelCard(
    val id: Long,
    val name: String,
    val logoUrl: String? = null,
    val country: String? = null,
    val categories: List<String> = emptyList(),
    val quality: String? = null
)

data class EpisodeRef(val id: Long = 0, val season: Int = 0, val number: Int = 0, val name: String? = null)

data class ProgressDto(
    val itemType: String,
    val itemId: Long,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val completed: Boolean = false,
    val updatedAt: Long? = null,
    val episode: EpisodeRef? = null
) {
    val fraction: Float get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}

/** A home row holds either titles or channels. `browse` = filters for the "see all" screen. */
data class HomeRow(
    val id: String,
    val title: String,
    val kind: String,                       // titles | continue | channels
    val titles: List<TitleCard> = emptyList(),
    val channels: List<ChannelCard> = emptyList(),
    val browse: Map<String, String>? = null
)

data class HomeResponse(val hero: List<TitleCard> = emptyList(), val rows: List<HomeRow> = emptyList(), val heroChannels: List<ChannelCard> = emptyList())

data class PageResponse<T>(val page: Int = 1, val size: Int = 30, val total: Int = 0, val items: List<T> = emptyList())

data class Facet(val id: String, val name: String, val count: Int = 0)

data class FacetsResponse(
    val countries: List<Facet> = emptyList(),
    val languages: List<Facet> = emptyList(),
    val genres: List<Facet> = emptyList(),
    val decades: List<Facet> = emptyList(),
    val categories: List<Facet> = emptyList()
)

data class StreamDto(
    val id: Long,
    val url: String,
    val protocol: String = "hls",
    val quality: String? = null,
    val height: Int? = null,
    val referrer: String? = null,
    val userAgent: String? = null
)

/** `url` may be http(s) or a local file (re-timed copy for subtitle sync). */
data class SubtitleDto(val id: Long, val language: String, val label: String? = null, val mimeType: String, val url: String)

data class NamedDto(val id: String, val name: String)

data class TitleDetails(
    val id: Long,
    val type: String = "movie",
    val title: String? = null,
    val originalTitle: String? = null,
    val year: Int? = null,
    val language: String? = null,
    val languageName: String? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val quality: String? = null,
    val overview: String = "",
    val runtimeMin: Int? = null,
    val genres: List<NamedDto> = emptyList(),
    val directors: List<String> = emptyList(),
    val license: String? = null,
    val sourcePage: String? = null,
    val progress: ProgressDto? = null,
    val streams: List<StreamDto> = emptyList(),
    val subtitles: List<SubtitleDto> = emptyList(),
    val qualities: List<String> = emptyList()
) {
    val displayTitle: String get() = title ?: originalTitle ?: ""
}

data class ChannelDetails(
    val id: Long,
    val name: String,
    val logoUrl: String? = null,
    val country: String? = null,
    val streams: List<StreamDto> = emptyList()
)

data class SearchResults(
    val titles: List<TitleCard> = emptyList(),
    val channels: List<ChannelCard> = emptyList(),
    val countries: List<Facet> = emptyList(),
    val categories: List<Facet> = emptyList()
) {
    val isEmpty get() = titles.isEmpty() && channels.isEmpty() && countries.isEmpty() && categories.isEmpty()
}
