package com.abumoath.tv.data.source

/**
 * Arabic names for well-known free channels, so they can be found by Arabic search
 * ("الجزيرة", "العربية"...) and shown first in the Arabic news row.
 * Matching is done on the English channel name published by the public index.
 * `rank` = order in the "Arabic news" row (lower = first). `news` = belongs to that row.
 */
object ChannelAliases {
    data class Alias(val pattern: Regex, val ar: String, val rank: Int, val news: Boolean)

    private fun a(p: String, ar: String, rank: Int, news: Boolean = false) = Alias(Regex(p), ar, rank, news)

    // More specific patterns first (e.g. "al jazeera mubasher" before "al jazeera").
    private val list = listOf(
        a("^al ?jazeera mubasher", "الجزيرة مباشر", 2, true),
        a("^al ?jazeera documentary", "الجزيرة الوثائقية", 40),
        a("^al ?jazeera english", "الجزيرة الإنجليزية", 30, true),
        a("^al ?jazeera balkans", "الجزيرة البلقان", 90),
        a("^al ?jazeera( arabic| channel| news)?$", "الجزيرة", 1, true),
        a("^al ?arabiya business", "العربية Business", 12, true),
        a("^al ?arabiya", "العربية", 3, true),
        a("^al ?hadath", "الحدث", 4, true),
        a("^sky news arabia", "سكاي نيوز عربية", 5, true),
        a("^al ?ekhbariya", "الإخبارية السعودية", 6, true),
        a("^asharq", "الشرق", 7, true),
        a("^al ?qahera news", "القاهرة الإخبارية", 8, true),
        a("^bbc (news )?arabic", "بي بي سي عربي", 9, true),
        a("^france ?24 arab", "فرانس 24 عربي", 10, true),
        a("^dw arab", "DW عربية", 11, true),
        a("^rt arab", "RT عربي", 13, true),
        a("^trt arab", "TRT عربي", 14, true),
        a("^al ?araby", "العربي", 15, true),
        a("^al ?mayadeen", "الميادين", 16, true),
        a("^al ?hurra", "الحرة", 17, true),
        a("^cnbc arabia", "CNBC عربية", 18, true),
        a("^al ?ghad", "الغد", 19, true),
        a("^al ?mamlaka", "المملكة", 20, true),
        a("^roya", "رؤيا", 21, true),
        a("^saudi quran|^al ?quran al ?kareem|^quran tv", "قناة القرآن الكريم", 50),
        a("^saudi sunnah|^al ?sunnah", "قناة السنة النبوية", 51),
        a("^ksa sports|^ssc", "السعودية الرياضية", 60),
        a("^saudi tv|^al ?saudiya|^saudi 1", "القناة السعودية الأولى", 22),
        a("^sbc", "SBC", 61),
        a("^dubai tv", "تلفزيون دبي", 62),
        a("^dubai sports", "دبي الرياضية", 63),
        a("^dubai one", "دبي ون", 64),
        a("^abu dhabi tv", "تلفزيون أبوظبي", 65),
        a("^ad sports|^abu dhabi sports", "أبوظبي الرياضية", 66),
        a("^sharjah tv", "تلفزيون الشارقة", 67),
        a("^kuwait tv|^ktv", "تلفزيون الكويت", 68),
        a("^qatar tv", "تلفزيون قطر", 69),
        a("^al ?kass", "الكأس", 70),
        a("^bahrain tv", "تلفزيون البحرين", 71),
        a("^oman tv", "تلفزيون عمان", 72),
        a("^al ?masriya", "المصرية", 73),
        a("^nile (tv )?news", "النيل للأخبار", 23, true),
        a("^jordan tv", "التلفزيون الأردني", 74),
        a("^al ?iraqiya", "العراقية", 75),
        a("^mbc", "MBC", 76),
        a("^spacetoon", "سبيستون", 80),
        a("^majid", "ماجد", 81),
        a("^jeem", "جيم", 82),
        a("^baraem", "براعم", 83)
    )

    private fun clean(name: String) = name.lowercase().replace(Regex("[^a-z0-9 ]"), " ").replace(Regex("\\s+"), " ").trim()

    /** Alias for an English channel name, or null. Keeps any suffix such as "2" or "HD" (e.g. "MBC 2" -> "MBC 2"). */
    fun find(englishName: String): Alias? {
        val c = clean(englishName)
        return list.firstOrNull { it.pattern.containsMatchIn(c) }
    }

    /** Arabic display name: the alias, plus the original number/variant if the English name had one. */
    fun arabicName(englishName: String): String? {
        val al = find(englishName) ?: return null
        val c = clean(englishName)
        val rest = c.replace(al.pattern, "").trim().replace(Regex("^(tv|channel|hd|arabic|news)\\b"), "").trim()
        return if (rest.isNotEmpty() && rest.length <= 12) "${al.ar} ${rest.uppercase()}" else al.ar
    }

    private val byArabic by lazy { list.associateBy { it.ar } }

    /** Rank of a stored (Arabic) channel name in the Arabic news row, or null if it is not a news alias. */
    fun newsRank(storedName: String): Int? {
        val al = byArabic[storedName] ?: byArabic.entries.firstOrNull { storedName.startsWith(it.key + " ") }?.value
        return al?.takeIf { it.news }?.rank
    }
}
