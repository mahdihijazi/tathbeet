package com.quran.tathbeet.ui.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.R

@Composable
fun ReminderTimeEditor(
    initialHour: Int,
    initialMinute: Int,
    onValueChanged: (Int, Int) -> Unit,
) {
    var hourValue by remember(initialHour) { mutableStateOf(timeFieldValue(initialHour)) }
    var minuteValue by remember(initialMinute) { mutableStateOf(timeFieldValue(initialMinute)) }
    val hourFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        hourFocusRequester.requestFocus()
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReminderTimeField(
                value = hourValue,
                label = stringResource(R.string.settings_reminder_hour_label),
                tag = "settings-time-hour-input",
                focusRequester = hourFocusRequester,
                maxValue = 23,
                onValueChange = { updated ->
                    hourValue = updated
                    val hour = updated.text.toIntOrNull()
                    val minute = minuteValue.text.toIntOrNull()
                    if (hour != null && minute != null) {
                        onValueChanged(hour, minute)
                    }
                },
            )
            Text(
                text = ":",
                style = MaterialTheme.typography.displayMedium,
            )
            ReminderTimeField(
                value = minuteValue,
                label = stringResource(R.string.settings_reminder_minute_label),
                tag = "settings-time-minute-input",
                maxValue = 59,
                onValueChange = { updated ->
                    minuteValue = updated
                    val hour = hourValue.text.toIntOrNull()
                    val minute = updated.text.toIntOrNull()
                    if (hour != null && minute != null) {
                        onValueChanged(hour, minute)
                    }
                },
            )
        }
    }
}

@Composable
private fun ReminderTimeField(
    value: TextFieldValue,
    label: String,
    tag: String,
    maxValue: Int,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    onValueChange: (TextFieldValue) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { updated ->
            onValueChange(normalizeTimeField(updated, maxValue))
        },
        modifier = modifier
            .width(120.dp)
            .then(
                if (focusRequester != null) {
                    Modifier.focusRequester(focusRequester)
                } else {
                    Modifier
                },
            )
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    onValueChange(value.withCursorAtEnd())
                }
            }
            .testTag(tag),
        singleLine = true,
        textStyle = MaterialTheme.typography.displayMedium.copy(
            textAlign = TextAlign.Center,
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
        ),
        supportingText = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
    )
}

private fun timeFieldValue(
    value: Int,
): TextFieldValue {
    val text = "%02d".format(value)
    return TextFieldValue(
        text = text,
        selection = TextRange(text.length),
    )
}

private fun normalizeTimeField(
    value: TextFieldValue,
    maxValue: Int,
): TextFieldValue {
    val digits = value.text.filter(Char::isDigit).take(2)
    val boundedDigits = digits
        .takeIf { candidate -> candidate.toIntOrNull()?.let { it <= maxValue } ?: true }
        ?: digits.dropLast(1)
    return TextFieldValue(
        text = boundedDigits,
        selection = TextRange(boundedDigits.length),
    )
}

private fun TextFieldValue.withCursorAtEnd(): TextFieldValue =
    copy(selection = TextRange(text.length))
