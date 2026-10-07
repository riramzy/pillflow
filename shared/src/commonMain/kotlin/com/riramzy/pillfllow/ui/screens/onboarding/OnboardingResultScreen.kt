package com.riramzy.pillfllow.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.ui.components.custom.PillFlowButton
import com.riramzy.pillfllow.ui.components.onboarding.PillFlowOnboardingTopBar
import com.riramzy.pillfllow.ui.components.onboarding.result.PillFlowOnboardingResultHero
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.next

@Composable
fun OnboardingResultScreen(
    modifier: Modifier = Modifier,
    onNextClick: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            PillFlowOnboardingTopBar(
                progress = 4,
                modifier = Modifier.padding(15.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PillFlowOnboardingResultHero(
                modifier = Modifier
                    .padding(
                        start = 30.dp,
                        end = 30.dp,
                        top = 20.dp
                    )
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Celebrate your\nprogress.",
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Track monthly compliance trends, build lasting\nstreaks, and share verified reports with your\ndoctor.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            PillFlowButton(
                text = "Get Started",
                withIcon = true,
                icon = Res.drawable.next,
                onClick = onNextClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 15.dp,
                        end = 15.dp,
                        bottom = 20.dp
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingResultScreenPreview() {
    PillFlowTheme {
        OnboardingResultScreen()
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun OnboardingResultScreenPreviewDark() {
    PillFlowTheme {
        OnboardingResultScreen()
    }
}