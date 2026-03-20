package com.quran.tathbeet.ui.features.profiles

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.quran.tathbeet.ui.features.settings.EmailLinkDialog
import com.quran.tathbeet.ui.screenshot.ThemedScreenshotFrame

private const val ProfilesDialogPreviewWidth = 411
private const val ProfilesEditorDialogHeight = 420
private const val ProfilesDeleteDialogHeight = 300
private const val ProfilesEmailDialogHeight = 320

@PreviewTest
@Preview(
    name = "profiles_profile_editor_dialog_add",
    locale = "ar",
    widthDp = ProfilesDialogPreviewWidth,
    heightDp = ProfilesEditorDialogHeight,
    showBackground = true,
)
@Composable
fun ProfilesProfileEditorDialogAddScreenshot() {
    ThemedScreenshotFrame(darkTheme = false) {
        ProfileEditorDialog(
            editor = ProfileEditorUiState(
                profileId = null,
                name = "",
                canDelete = false,
            ),
            onNameChanged = {},
            onSave = {},
            onDismiss = {},
            onRequestDelete = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "profiles_profile_editor_dialog_add_dark",
    locale = "ar",
    widthDp = ProfilesDialogPreviewWidth,
    heightDp = ProfilesEditorDialogHeight,
    showBackground = true,
)
@Composable
fun ProfilesProfileEditorDialogAddDarkScreenshot() {
    ThemedScreenshotFrame(darkTheme = true) {
        ProfileEditorDialog(
            editor = ProfileEditorUiState(
                profileId = null,
                name = "",
                canDelete = false,
            ),
            onNameChanged = {},
            onSave = {},
            onDismiss = {},
            onRequestDelete = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "profiles_profile_editor_dialog_edit",
    locale = "ar",
    widthDp = ProfilesDialogPreviewWidth,
    heightDp = ProfilesEditorDialogHeight,
    showBackground = true,
)
@Composable
fun ProfilesProfileEditorDialogEditScreenshot() {
    ThemedScreenshotFrame(darkTheme = false) {
        ProfileEditorDialog(
            editor = ProfileEditorUiState(
                profileId = "child-1",
                name = "أحمد",
                canDelete = true,
            ),
            onNameChanged = {},
            onSave = {},
            onDismiss = {},
            onRequestDelete = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "profiles_profile_editor_dialog_edit_dark",
    locale = "ar",
    widthDp = ProfilesDialogPreviewWidth,
    heightDp = ProfilesEditorDialogHeight,
    showBackground = true,
)
@Composable
fun ProfilesProfileEditorDialogEditDarkScreenshot() {
    ThemedScreenshotFrame(darkTheme = true) {
        ProfileEditorDialog(
            editor = ProfileEditorUiState(
                profileId = "child-1",
                name = "أحمد",
                canDelete = true,
            ),
            onNameChanged = {},
            onSave = {},
            onDismiss = {},
            onRequestDelete = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "profiles_profile_delete_dialog",
    locale = "ar",
    widthDp = ProfilesDialogPreviewWidth,
    heightDp = ProfilesDeleteDialogHeight,
    showBackground = true,
)
@Composable
fun ProfilesProfileDeleteDialogScreenshot() {
    ThemedScreenshotFrame(darkTheme = false) {
        ProfileDeleteDialog(
            confirmation = ProfileDeleteConfirmationUiState(
                profileId = "child-1",
                profileName = "أحمد",
            ),
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "profiles_profile_delete_dialog_dark",
    locale = "ar",
    widthDp = ProfilesDialogPreviewWidth,
    heightDp = ProfilesDeleteDialogHeight,
    showBackground = true,
)
@Composable
fun ProfilesProfileDeleteDialogDarkScreenshot() {
    ThemedScreenshotFrame(darkTheme = true) {
        ProfileDeleteDialog(
            confirmation = ProfileDeleteConfirmationUiState(
                profileId = "child-1",
                profileName = "أحمد",
            ),
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "profiles_email_link_dialog",
    locale = "ar",
    widthDp = ProfilesDialogPreviewWidth,
    heightDp = ProfilesEmailDialogHeight,
    showBackground = true,
)
@Composable
fun ProfilesEmailLinkDialogScreenshot() {
    ThemedScreenshotFrame(darkTheme = false) {
        EmailLinkDialog(
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "profiles_email_link_dialog_dark",
    locale = "ar",
    widthDp = ProfilesDialogPreviewWidth,
    heightDp = ProfilesEmailDialogHeight,
    showBackground = true,
)
@Composable
fun ProfilesEmailLinkDialogDarkScreenshot() {
    ThemedScreenshotFrame(darkTheme = true) {
        EmailLinkDialog(
            onDismiss = {},
            onConfirm = {},
        )
    }
}
