package com.abumoath.tv.data.source

import android.content.Context
import androidx.media3.common.MimeTypes
import com.abumoath.tv.data.model.StreamDto
import com.abumoath.tv.data.model.SubtitleDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.URLEncoder
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Resolves the playable files of an Internet Archive item when the user opens it (small JSON request).
 * Nothing is downloaded except subtitle text when the user changes subtitle sync.
 */
@Singleton
class ArchiveResolver @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("base") private val client: OkHttpClient
) {
    data class Resolved(val streams: List<StreamDto>, val subtitles: List<SubtitleDto>)

    private val cache = object : LinkedHashMap<String, Resolved>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Resolved>?) = size > 20
    }

    private fun enc(path: String) = path.split('/').joinToString("/") { URLEncoder.encode(it, "UTF-8").replace("+", "%20") }

    suspend fun resolve(identifier: String): Resolved = withContext(Dispatchers.IO) {
        synchronized(cache) { cache[identifier] }?.let { return@withContext it }
        val json = client.newCall(Request.Builder().url("https://archive.org/metadata/${enc(identifier)}").build()).execute().use { r ->
            if (!r.isSuccessful) error("HTTP ${r.code}")
            Json.parseToJsonElement(r.body!!.string()).jsonObject
        }
        val files = json["files"]?.jsonArray?.map { it.jsonObject } ?: emptyList()
        val base = "https://archive.org/download/${enc(identifier)}/"
        fun JsonObject.s(k: String) = (this[k] as? JsonPrimitive)?.contentOrNull

        // MP4 renditions (H.264), one per resolution, best first, max 1080p (device limit).
        val byHeight = LinkedHashMap<Int, JsonObject>()
        files.filter { (it.s("name") ?: "").endsWith(".mp4", true) && (it.s("format")?.contains(Regex("h\\.264|mpeg4", RegexOption.IGNORE_CASE)) != false) }
            .forEach { f ->
                val h = f.s("height")?.toIntOrNull() ?: if ((f.s("format") ?: "").contains("512Kb", true)) 360 else 480
                if (h > 1080) return@forEach
                val prev = byHeight[h]
                if (prev == null || (f.s("size")?.toLongOrNull() ?: 0) > (prev.s("size")?.toLongOrNull() ?: 0)) byHeight[h] = f
            }
        val streams = byHeight.entries.sortedByDescending { it.key }.take(3).mapIndexed { i, (h, f) ->
            StreamDto(id = i.toLong(), url = base + enc(f.s("name")!!), protocol = "mp4", quality = if (h >= 1000) "FHD" else if (h >= 700) "HD" else "SD", height = h)
        }
        val subs = files.filter { val n = it.s("name") ?: ""; n.endsWith(".srt", true) || n.endsWith(".vtt", true) }.take(6)
            .mapIndexed { i, f ->
                val name = f.s("name")!!
                val lang = subtitleLang(name)
                SubtitleDto(id = i.toLong(), language = lang, label = Locale(lang).getDisplayLanguage(Locale.getDefault()).ifBlank { name },
                    mimeType = if (name.endsWith(".vtt", true)) MimeTypes.TEXT_VTT else MimeTypes.APPLICATION_SUBRIP, url = base + enc(name))
            }
            .sortedByDescending { it.language == "ar" }
        Resolved(streams, subs).also { synchronized(cache) { cache[identifier] = it } }
    }

    private fun subtitleLang(name: String): String {
        val n = name.lowercase()
        fun has(vararg k: String) = k.any { Regex("(^|[._\\- ])$it([._\\- ]|$)").containsMatchIn(n) }
        return when {
            has("ar", "ara", "arabic") -> "ar"
            has("fr", "fra", "french") -> "fr"
            has("es", "spa", "spanish") -> "es"
            has("de", "ger", "german") -> "de"
            else -> "en"
        }
    }

    // ---------- Subtitle sync (re-timing) ----------
    private val textCache = HashMap<String, String>()

    /** Returns a local WebVTT copy shifted by `offsetMs` (positive = later). */
    suspend fun shifted(sub: SubtitleDto, offsetMs: Long): SubtitleDto = withContext(Dispatchers.IO) {
        val text = textCache[sub.url] ?: client.newCall(Request.Builder().url(sub.url).build()).execute().use { it.body!!.string() }
            .also { textCache[sub.url] = it }
        val dir = File(context.cacheDir, "subs").apply { mkdirs() }
        val f = File(dir, "sub_${sub.id}_${offsetMs}.vtt")
        f.writeText(SubtitleShift.toVtt(text, offsetMs))
        sub.copy(url = f.toURI().toString(), mimeType = MimeTypes.TEXT_VTT)
    }

    fun clearSubtitleFiles() { File(context.cacheDir, "subs").deleteRecursively(); textCache.clear() }
}

/** SRT / WebVTT parsing and re-timing. */
object SubtitleShift {
    private val time = Regex("(?:(\\d{1,2}):)?(\\d{1,2}):(\\d{2})[.,](\\d{1,3})")

    private fun toMs(t: String): Long? {
        val m = time.find(t) ?: return null
        val (h, mi, s, ms) = m.destructured
        return (((h.ifEmpty { "0" }.toLong() * 60 + mi.toLong()) * 60 + s.toLong()) * 1000) + ms.padEnd(3, '0').toLong()
    }

    private fun fmt(v: Long): String {
        val ms = v.coerceAtLeast(0)
        return String.format(Locale.US, "%02d:%02d:%02d.%03d", ms / 3600000, (ms % 3600000) / 60000, (ms % 60000) / 1000, ms % 1000)
    }

    fun toVtt(content: String, offsetMs: Long): String {
        val out = StringBuilder("WEBVTT\n\n")
        content.removePrefix("﻿").replace("\r\n", "\n").replace('\r', '\n').split(Regex("\n{2,}")).forEach { block ->
            val lines = block.split('\n').filter { it.isNotEmpty() }
            val idx = lines.indexOfFirst { it.contains("-->") }
            if (idx < 0) return@forEach
            val parts = lines[idx].split("-->")
            val s = toMs(parts[0]) ?: return@forEach
            val e = toMs(parts.getOrElse(1) { "" }) ?: return@forEach
            val body = lines.drop(idx + 1).joinToString("\n").trim()
            if (body.isEmpty() || e + offsetMs <= 0) return@forEach
            out.append(fmt(s + offsetMs)).append(" --> ").append(fmt(e + offsetMs)).append('\n').append(body).append("\n\n")
        }
        return out.toString()
    }
}
