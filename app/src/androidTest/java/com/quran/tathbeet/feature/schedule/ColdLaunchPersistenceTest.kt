package com.quran.tathbeet.feature.schedule

import com.quran.tathbeet.test.BaseMainActivityUiTest
import org.junit.Test

class ColdLaunchPersistenceTest : BaseMainActivityUiTest() {

    @Test
    fun relaunch_after_saving_schedule_opens_review_instead_of_onboarding() {
        launchMainActivity().use {
            assertIntroVisible()
            tapNext()
            openJuzTab()
            selectVisibleJuz(1)
            tapNext()
            saveSchedule()
            assertReviewVisible()
        }

        launchMainActivity().use {
            assertReviewVisible()
        }
    }
}
