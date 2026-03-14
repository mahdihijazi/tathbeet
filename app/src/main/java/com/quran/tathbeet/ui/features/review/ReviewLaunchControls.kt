package com.quran.tathbeet.ui.features.review

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.theme.TathbeetTheme
import com.quran.tathbeet.ui.theme.TathbeetTokens

@Composable
fun ReviewLaunchIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(TathbeetTokens.radii.sm),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .size(56.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(TathbeetTokens.radii.sm),
                )
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onClick),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.mipmap.quran),
                contentDescription = stringResource(R.string.review_open_in_quran),
                modifier = Modifier.size(42.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewLaunchIconButtonPreview() {
    TathbeetTheme {
        ReviewLaunchIconButton(onClick = {})
    }
}
