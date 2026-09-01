package com.riramzy.pillfllow

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import com.riramzy.pillfllow.ui.theme.PillFlowTheme

@Composable
fun App() {
    PillFlowTheme {
        Surface {
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