package com.quran.tathbeet.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeSpecTest {

    @Test
    fun light_background_prefers_dark_system_bar_icons() {
        assertTrue(prefersDarkSystemBarIcons(Color(0xFFF1E8D8)))
    }

    @Test
    fun dark_background_prefers_light_system_bar_icons() {
        assertFalse(prefersDarkSystemBarIcons(Color(0xFF121212)))
    }

    @Test
    fun theme_spec_uses_background_luminance_for_system_bar_icons() {
        val themeSpec = resolveTathbeetThemeSpec(darkTheme = false)

        assertTrue(themeSpec.useDarkSystemBarIcons)
    }
}
