package com.quran.tathbeet.domain.model

enum class CycleTarget(val days: Int) {
    OneWeek(7),
    TwoWeeks(14),
    OneMonth(30),
    FortyFiveDays(45),
    TwoMonths(60),
}
