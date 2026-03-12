package com.quran.tathbeet.test

import com.quran.tathbeet.app.QuranExternalLauncher
import com.quran.tathbeet.app.QuranLaunchResult
import com.quran.tathbeet.domain.model.QuranReadingTarget

class RecordingQuranExternalLauncher : QuranExternalLauncher {
    var quranAppInstalled: Boolean = true
    val openReaderRequests = mutableListOf<QuranReadingTarget>()
    val installRequests = mutableListOf<Unit>()
    val openWebRequests = mutableListOf<QuranReadingTarget>()

    override fun openReadingTarget(target: QuranReadingTarget): QuranLaunchResult {
        return if (quranAppInstalled) {
            openReaderRequests += target
            QuranLaunchResult.LaunchedInApp
        } else {
            QuranLaunchResult.ShowFallbackOptions
        }
    }

    override fun openQuranAndroidInstallPage() {
        installRequests += Unit
    }

    override fun openReadingTargetOnWeb(target: QuranReadingTarget) {
        openWebRequests += target
    }
}
