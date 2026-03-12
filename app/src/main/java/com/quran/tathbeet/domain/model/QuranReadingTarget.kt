package com.quran.tathbeet.domain.model

data class QuranReadingTarget(
    val startSurahId: Int,
    val startAyah: Int,
    val endSurahId: Int,
    val endAyah: Int,
)
