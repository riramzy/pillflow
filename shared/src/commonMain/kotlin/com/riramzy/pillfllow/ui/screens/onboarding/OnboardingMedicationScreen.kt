package com.riramzy.pillfllow.ui.screens.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.riramzy.pillfllow.ui.components.onboarding.medication.PillFlowOnboardingMedicationHero
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.next

@Composable
fun OnboardingMedicationScreen(
    modifier: Modifier = Modifier,
    onNextClick: () -> Unit = {},
    onSkipClick: () -> Unit = {},
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            PillFlowOnboardingTopBar(
                progress = 1,
                modifier = Modifier.padding(15.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PillFlowOnboardingMedicationHero(
                modifier = Modifier.padding(top = 20.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Medication,\nmade easier.",
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "A smarter way to stay on track\nwith every dose.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(
                        start = 15.dp,
                        end = 15.dp,
                        bottom = 20.dp
                    )
            ) {
                PillFlowButton(
                    text = "Next",
                    withIcon = true,
                    icon = Res.drawable.next,
                    onClick = onNextClick,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        onSkipClick()
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingMedicationScreenPreview() {
    PillFlowTheme {
        OnboardingMedicationScreen()
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun OnboardingMedicationScreenPreviewDark() {
    PillFlowTheme {
        OnboardingMedicationScreen()
    }
}