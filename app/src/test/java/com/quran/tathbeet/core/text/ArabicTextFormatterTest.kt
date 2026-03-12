package com.quran.tathbeet.core.text

import org.junit.Assert.assertEquals
import org.junit.Test

class ArabicTextFormatterTest {

    private val forms = ArabicCountForms(
        one = "آية واحدة",
        two = "آيتان",
        few = "%d آيات",
        many = "%d آية",
    )

    @Test
    fun one_uses_singular_form() {
        assertEquals("آية واحدة", formatArabicCount(1, forms))
    }

    @Test
    fun two_uses_dual_form() {
        assertEquals("آيتان", formatArabicCount(2, forms))
    }

    @Test
    fun few_uses_ayat_form() {
        assertEquals("4 آيات", formatArabicCount(4, forms))
        assertEquals("9 آيات", formatArabicCount(9, forms))
    }

    @Test
    fun many_uses_ayah_form() {
        assertEquals("11 آية", formatArabicCount(11, forms))
        assertEquals("40 آية", formatArabicCount(40, forms))
    }
}
