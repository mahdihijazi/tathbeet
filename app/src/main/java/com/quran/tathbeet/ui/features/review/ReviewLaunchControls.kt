package com.quran.tathbeet.ui.features.review

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.theme.TathbeetTheme

@Composable
fun ReviewLaunchIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
    ) {
        Image(
            painter = painterResource(R.mipmap.quran),
            contentDescription = stringResource(R.string.review_open_in_quran),
            modifier = Modifier.size(48.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewLaunchIconButtonPreview() {
    TathbeetTheme {
        ReviewLaunchIconButton(onClick = {})
    }
}
