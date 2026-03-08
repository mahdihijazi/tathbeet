package com.quran.tathbeet.ui.prototype

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

data class TextSpec(
    @param:StringRes val resId: Int? = null,
    val formatArgs: List<Any> = emptyList(),
    val rawText: String? = null,
) {
    init {
        require((resId != null) xor (rawText != null)) {
            "TextSpec requires either a string resource or raw text."
        }
    }
}

@Composable
fun TextSpec.asString(): String =
    rawText ?: stringResource(
        id = resId!!,
        *formatArgs.map { arg ->
            when (arg) {
                is TextSpec -> arg.asString()
                else -> arg
            }
        }.toTypedArray(),
    )
