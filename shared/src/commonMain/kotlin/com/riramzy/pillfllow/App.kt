package com.riramzy.pillfllow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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