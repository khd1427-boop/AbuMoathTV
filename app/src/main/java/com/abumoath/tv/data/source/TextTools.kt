package com.abumoath.tv.data.source

import java.text.Normalizer
import java.util.Locale

/** Arabic/English text normalisation and fuzzy matching for the smart search. */
object TextTools {
    private val diacritics = Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED\\u0640]")
    private val latinMarks = Regex("\\p{Mn}+")
    private val nonWord = Regex("[^\\p{L}\\p{N}]+")

    fun normalize(text: String?): String {
        if (text.isNullOrBlank()) return ""
        var s = Normalizer.normalize(text.lowercase(Locale.ROOT), Normalizer.Form.NFKD)
        s = s.replace(diacritics, "").replace(latinMarks, "")
        val sb = StringBuilder(s.length)
        for (c in s) sb.append(
            when (c) {
                'إ', 'أ', 'آ', 'ٱ' -> 'ا'
                'ى', 'ئ' -> 'ي'
                'ة' -> 'ه'
                'ؤ' -> 'و'
                in '٠'..'٩' -> '0' + (c - '٠')
                else -> c
            }
        )
        return sb.toString().replace(nonWord, " ").trim()
    }

    fun tokens(text: String): List<String> =
        normalize(text).split(' ').filter { it.isNotEmpty() }.map { if (it.length > 3 && it.startsWith("ال")) it.substring(2) else it }

    fun levenshtein(a: String, b: String, max: Int): Int {
        if (kotlin.math.abs(a.length - b.length) > max) return max + 1
        var prev = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            val cur = IntArray(b.length + 1)
            cur[0] = i
            var rowMin = i
            for (j in 1..b.length) {
                cur[j] = minOf(prev[j] + 1, cur[j - 1] + 1, prev[j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1)
                if (cur[j] < rowMin) rowMin = cur[j]
            }
            if (rowMin > max) return max + 1
            prev = cur
        }
        return prev[b.length]
    }

    /** Relevance score of `name` (already normalised) for a query; 0 = no match. */
    fun score(queryNorm: String, queryTokens: List<String>, nameNorm: String): Int {
        if (nameNorm.isEmpty() || queryNorm.isEmpty()) return 0
        if (nameNorm == queryNorm) return 100
        if (nameNorm.startsWith(queryNorm)) return 70
        if (nameNorm.contains(queryNorm)) return 50
        val nameTokens = nameNorm.split(' ').map { if (it.length > 3 && it.startsWith("ال")) it.substring(2) else it }
        var total = 0
        for (q in queryTokens) {
            var best = 0
            for (t in nameTokens) {
                val allowed = if (q.length >= 8) 2 else if (q.length >= 4) 1 else 0
                best = maxOf(best, when {
                    t == q -> 3
                    q.length >= 2 && t.startsWith(q) -> 2
                    allowed > 0 && levenshtein(q, t, allowed) <= allowed -> 1
                    else -> 0
                })
            }
            if (best == 0) return 0
            total += best
        }
        return 10 + total * 5
    }

    fun countryName(code: String, lang: String): String =
        Locale("", code).getDisplayCountry(Locale(lang)).ifBlank { code }

    fun languageName(code: String, lang: String): String =
        Locale(code).getDisplayLanguage(Locale(lang)).ifBlank { code }
}
