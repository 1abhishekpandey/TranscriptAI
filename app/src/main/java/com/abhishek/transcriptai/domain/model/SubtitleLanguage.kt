package com.abhishek.transcriptai.domain.model

/**
 * A subtitle language the user can pick on the language settings page.
 *
 * @param codes YouTube caption codes to try, in order. Some languages have more than
 *        one code on YouTube (e.g., Hebrew is "iw" on older videos and "he" on newer ones).
 * @param nativeName Name of the language written in that language (e.g., "Bahasa Indonesia")
 * @param englishName Name of the language in English, shown as a hint and used for search
 */
data class SubtitleLanguage(
    val codes: List<String>,
    val nativeName: String,
    val englishName: String
) {
    val code: String get() = codes.first()
}

/**
 * Which subtitle language the home screen asks for.
 */
enum class LanguageMode {
    /** Captions YouTube generated from the video's speech */
    AUTO,

    /** A language picked on the language settings page */
    OTHER
}

/**
 * The user's saved language choice.
 *
 * @param mode Which option is selected on the home screen
 * @param otherLanguageCode Language picked on the settings page, kept even when another mode is selected
 */
data class LanguageChoice(
    val mode: LanguageMode = LanguageMode.AUTO,
    val otherLanguageCode: String? = null
) {
    val otherLanguage: SubtitleLanguage? get() = SubtitleLanguages.find(otherLanguageCode)

    /**
     * Language codes to request from the subtitle downloader, in order of preference.
     * Falls back to auto-generated captions if "Other" is selected without a language.
     */
    fun toLanguagePreferences(): List<String> = when (mode) {
        LanguageMode.AUTO -> listOf("auto")
        LanguageMode.OTHER -> otherLanguage?.codes ?: listOf("auto")
    }
}

/**
 * Languages offered on the language settings page, sorted by English name.
 */
object SubtitleLanguages {
    val all: List<SubtitleLanguage> = listOf(
        SubtitleLanguage(listOf("ar"), "العربية", "Arabic"),
        SubtitleLanguage(listOf("bn"), "বাংলা", "Bengali"),
        SubtitleLanguage(listOf("zh-Hans", "zh-CN", "zh"), "简体中文", "Chinese (Simplified)"),
        SubtitleLanguage(listOf("zh-Hant", "zh-TW", "zh-HK"), "繁體中文", "Chinese (Traditional)"),
        SubtitleLanguage(listOf("cs"), "Čeština", "Czech"),
        SubtitleLanguage(listOf("da"), "Dansk", "Danish"),
        SubtitleLanguage(listOf("nl"), "Nederlands", "Dutch"),
        SubtitleLanguage(listOf("en"), "English", "English"),
        SubtitleLanguage(listOf("fil", "tl"), "Filipino", "Filipino"),
        SubtitleLanguage(listOf("fi"), "Suomi", "Finnish"),
        SubtitleLanguage(listOf("fr"), "Français", "French"),
        SubtitleLanguage(listOf("de"), "Deutsch", "German"),
        SubtitleLanguage(listOf("el"), "Ελληνικά", "Greek"),
        SubtitleLanguage(listOf("gu"), "ગુજરાતી", "Gujarati"),
        SubtitleLanguage(listOf("iw", "he"), "עברית", "Hebrew"),
        SubtitleLanguage(listOf("hi"), "हिन्दी", "Hindi"),
        SubtitleLanguage(listOf("hu"), "Magyar", "Hungarian"),
        SubtitleLanguage(listOf("id", "in"), "Bahasa Indonesia", "Indonesian"),
        SubtitleLanguage(listOf("it"), "Italiano", "Italian"),
        SubtitleLanguage(listOf("ja"), "日本語", "Japanese"),
        SubtitleLanguage(listOf("kn"), "ಕನ್ನಡ", "Kannada"),
        SubtitleLanguage(listOf("ko"), "한국어", "Korean"),
        SubtitleLanguage(listOf("ms"), "Bahasa Melayu", "Malay"),
        SubtitleLanguage(listOf("ml"), "മലയാളം", "Malayalam"),
        SubtitleLanguage(listOf("mr"), "मराठी", "Marathi"),
        SubtitleLanguage(listOf("ne"), "नेपाली", "Nepali"),
        SubtitleLanguage(listOf("no", "nb"), "Norsk", "Norwegian"),
        SubtitleLanguage(listOf("fa"), "فارسی", "Persian"),
        SubtitleLanguage(listOf("pl"), "Polski", "Polish"),
        SubtitleLanguage(listOf("pt"), "Português", "Portuguese"),
        SubtitleLanguage(listOf("pa"), "ਪੰਜਾਬੀ", "Punjabi"),
        SubtitleLanguage(listOf("ro"), "Română", "Romanian"),
        SubtitleLanguage(listOf("ru"), "Русский", "Russian"),
        SubtitleLanguage(listOf("es"), "Español", "Spanish"),
        SubtitleLanguage(listOf("sw"), "Kiswahili", "Swahili"),
        SubtitleLanguage(listOf("sv"), "Svenska", "Swedish"),
        SubtitleLanguage(listOf("ta"), "தமிழ்", "Tamil"),
        SubtitleLanguage(listOf("te"), "తెలుగు", "Telugu"),
        SubtitleLanguage(listOf("th"), "ไทย", "Thai"),
        SubtitleLanguage(listOf("tr"), "Türkçe", "Turkish"),
        SubtitleLanguage(listOf("uk"), "Українська", "Ukrainian"),
        SubtitleLanguage(listOf("ur"), "اردو", "Urdu"),
        SubtitleLanguage(listOf("vi"), "Tiếng Việt", "Vietnamese")
    )

    fun find(code: String?): SubtitleLanguage? = all.firstOrNull { it.code == code }
}
