package io.synapse.ai.features.add_pdf.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.synapse.ai.R

data class LanguageOption(val code: String)

val LANGUAGES = listOf(
    LanguageOption("en"),
    LanguageOption("ar"),
    LanguageOption("es"),
    LanguageOption("fr"),
    LanguageOption("de"),
    LanguageOption("pt"),
    LanguageOption("it"),
    LanguageOption("ja"),
    LanguageOption("ko"),
    LanguageOption("zh"),
    LanguageOption("hi"),
    LanguageOption("bn"),
    LanguageOption("ru"),
    LanguageOption("vi"),
    LanguageOption("uk"),
    LanguageOption("pl"),
    LanguageOption("nl"),
    LanguageOption("tr"),
    LanguageOption("th"),
    LanguageOption("sv"),
    LanguageOption("da"),
    LanguageOption("fi"),
    LanguageOption("no"),
    LanguageOption("cs"),
    LanguageOption("el"),
    LanguageOption("he"),
    LanguageOption("id"),
    LanguageOption("ms"),
    LanguageOption("ro"),
    LanguageOption("hu"),
)

@Composable
fun LanguageOption.label(): String = stringResource(
    when (code) {
        "en" -> R.string.lang_en
        "ar" -> R.string.lang_ar
        "es" -> R.string.lang_es
        "fr" -> R.string.lang_fr
        "de" -> R.string.lang_de
        "pt" -> R.string.lang_pt
        "it" -> R.string.lang_it
        "ja" -> R.string.lang_ja
        "ko" -> R.string.lang_ko
        "zh" -> R.string.lang_zh
        "hi" -> R.string.lang_hi
        "bn" -> R.string.lang_bn
        "ru" -> R.string.lang_ru
        "vi" -> R.string.lang_vi
        "uk" -> R.string.lang_uk
        "pl" -> R.string.lang_pl
        "nl" -> R.string.lang_nl
        "tr" -> R.string.lang_tr
        "th" -> R.string.lang_th
        "sv" -> R.string.lang_sv
        "da" -> R.string.lang_da
        "fi" -> R.string.lang_fi
        "no" -> R.string.lang_no
        "cs" -> R.string.lang_cs
        "el" -> R.string.lang_el
        "he" -> R.string.lang_he
        "id" -> R.string.lang_id
        "ms" -> R.string.lang_ms
        "ro" -> R.string.lang_ro
        "hu" -> R.string.lang_hu
        else -> R.string.lang_en
    }
)

fun getLanguageLabelByCode(code: String): String = when (code) {
    "en" -> "English"
    "ar" -> "Arabic"
    "es" -> "Spanish"
    "fr" -> "French"
    "de" -> "German"
    "pt" -> "Portuguese"
    "it" -> "Italian"
    "ja" -> "Japanese"
    "ko" -> "Korean"
    "zh" -> "Chinese"
    "hi" -> "Hindi"
    "bn" -> "Bengali"
    "ru" -> "Russian"
    "vi" -> "Vietnamese"
    "uk" -> "Ukrainian"
    "pl" -> "Polish"
    "nl" -> "Dutch"
    "tr" -> "Turkish"
    "th" -> "Thai"
    "sv" -> "Swedish"
    "da" -> "Danish"
    "fi" -> "Finnish"
    "no" -> "Norwegian"
    "cs" -> "Czech"
    "el" -> "Greek"
    "he" -> "Hebrew"
    "id" -> "Indonesian"
    "ms" -> "Malay"
    "ro" -> "Romanian"
    "hu" -> "Hungarian"
    else -> "English"
}
