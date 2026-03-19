package com.quran.tathbeet.sync

import android.content.Context

fun interface CloudBackendFactory {
    fun create(context: Context): CloudBackend
}
