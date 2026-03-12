package com.quran.tathbeet.core.text

import android.content.Context
import com.quran.tathbeet.R

data class ArabicCountForms(
    val one: String,
    val two: String,
    val few: String,
    val many: String,
)

fun formatArabicCount(
    count: Int,
    forms: ArabicCountForms,
): String = when (count) {
    1 -> forms.one
    2 -> forms.two
    in 3..10 -> forms.few.format(count)
    else -> forms.many.format(count)
}

fun formatAyahCount(
    context: Context,
    ayahCount: Int,
): String = formatArabicCount(
    count = ayahCount,
    forms = ArabicCountForms(
        one = context.getString(R.string.ayah_count_one),
        two = context.getString(R.string.ayah_count_two),
        few = context.getString(R.string.ayah_count_few, ayahCount),
        many = context.getString(R.string.ayah_count_many, ayahCount),
    ),
)
