package com.quran.tathbeet.feature.schedule

import androidx.test.espresso.Espresso.pressBack
import com.quran.tathbeet.test.BaseUiFlowTest
import org.junit.Test

class ScheduleWizardBackNavigationTest : BaseUiFlowTest() {

    @Test
    fun back_from_pool_selector_returns_to_intro_during_onboarding() {
        tapNext()
        assertPoolSelectorVisible()

        pressBack()

        assertIntroVisible()
    }

    @Test
    fun back_from_pace_step_returns_to_pool_selector_during_onboarding() {
        tapNext()
        openJuzTab()
        selectVisibleJuz(1)
        tapNext()
        assertScheduleDoseVisible()

        pressBack()

        assertPoolSelectorVisible()
    }
}
