package com.riramzy.pillfllow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.platform.clearFocusOnTap

@Composable
fun App() {
    PillFlowTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clearFocusOnTap()
        ) {
            NavApp()
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    PillFlowTheme {
        App()
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun AppPreviewDark() {
    PillFlowTheme {
        App()
    }
}