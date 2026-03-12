package com.quran.tathbeet.app

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.quran.tathbeet.domain.model.QuranReadingTarget

interface QuranExternalLauncher {
    fun openReadingTarget(target: QuranReadingTarget): QuranLaunchResult

    fun openQuranAndroidInstallPage()

    fun openReadingTargetOnWeb(target: QuranReadingTarget)
}

enum class QuranLaunchResult {
    LaunchedInApp,
    ShowFallbackOptions,
}

class AndroidQuranExternalLauncher(
    context: Context,
) : QuranExternalLauncher {
    private val appContext = context.applicationContext

    override fun openReadingTarget(target: QuranReadingTarget): QuranLaunchResult {
        val quranIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("quran://${target.startSurahId}/${target.startAyah}"),
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val canResolve = quranIntent.resolveActivity(appContext.packageManager) != null
        return if (!canResolve) {
            QuranLaunchResult.ShowFallbackOptions
        } else {
            try {
                appContext.startActivity(quranIntent)
                QuranLaunchResult.LaunchedInApp
            } catch (_: ActivityNotFoundException) {
                QuranLaunchResult.ShowFallbackOptions
            } catch (_: SecurityException) {
                QuranLaunchResult.ShowFallbackOptions
            }
        }
    }

    override fun openQuranAndroidInstallPage() {
        val marketIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$QURAN_ANDROID_PACKAGE_NAME"),
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (marketIntent.resolveActivity(appContext.packageManager) != null) {
            appContext.startActivity(marketIntent)
            return
        }

        openUri(
            Uri.parse("https://play.google.com/store/apps/details?id=$QURAN_ANDROID_PACKAGE_NAME"),
        )
    }

    override fun openReadingTargetOnWeb(target: QuranReadingTarget) {
        openUri(
            Uri.Builder()
                .scheme("https")
                .authority("quran.com")
                .appendPath(target.startSurahId.toString())
                .appendPath(target.startAyah.toString())
                .build(),
        )
    }

    private fun openUri(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            appContext.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
        }
    }

    private companion object {
        private const val QURAN_ANDROID_PACKAGE_NAME = "com.quran.labs.androidquran"
    }
}
