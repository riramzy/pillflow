package com.riramzy.pillfllow.ui.screens.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.ui.components.custom.PillFlowButton
import com.riramzy.pillfllow.ui.components.onboarding.PillFlowOnboardingScreenContent
import com.riramzy.pillfllow.ui.components.onboarding.PillFlowOnboardingTopBar
import com.riramzy.pillfllow.ui.components.onboarding.medication.PillFlowOnboardingMedicationHero
import com.riramzy.pillfllow.ui.components.onboarding.result.PillFlowOnboardingResultHero
import com.riramzy.pillfllow.ui.components.onboarding.time.PillFlowOnboardingTimeHeroCard
import com.riramzy.pillfllow.ui.components.onboarding.together.PillFlowOnboardingTogetherHero
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import kotlinx.coroutines.launch
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.next

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onFinishOnboarding: () -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    val onNext: () -> Unit = {
        if (pagerState.currentPage < 3) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        } else {
            onFinishOnboarding()
        }
    }

    Scaffold(
        topBar = {
            PillFlowOnboardingTopBar(
                progress = pagerState.currentPage + 1,
                modifier = Modifier.padding(15.dp)
            )
        },
        bottomBar = {
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
                    text = if (pagerState.currentPage == 3) "Get Started" else "Next",
                    withIcon = true,
                    icon = Res.drawable.next,
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth()
                )

                if (pagerState.currentPage < 3) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onFinishOnboarding() }
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.statusBarsPadding()
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) { page ->
            when (page) {
                0 -> PillFlowOnboardingScreenContent(
                    hero = { PillFlowOnboardingMedicationHero(modifier = Modifier.padding(top = 20.dp)) },
                    title = "Medication,\nmade easier.",
                    subtitle = "A smarter way to stay on track\nwith every dose."
                )

                1 -> PillFlowOnboardingScreenContent(
                    hero = {
                        PillFlowOnboardingTimeHeroCard(
                            modifier = Modifier
                                .padding(
                                    start = 25.dp,
                                    end = 25.dp,
                                    top = 20.dp
                                ),
                            isActive = pagerState.currentPage == 1
                        )
                    },
                    title = "Know when it’s time.",
                    subtitle = "PillFlow brings your next dose\nto you when you need it."
                )

                2 -> PillFlowOnboardingScreenContent(
                    hero = {
                        PillFlowOnboardingTogetherHero(
                            modifier = Modifier.padding(15.dp),
                            isActive = pagerState.currentPage == 2,
                        )
                    },
                    title = "Never care alone.",
                    subtitle = "Connect with family or caregivers\nfor gentle nudges, live updates, and\neffortless peace of mind."
                )

                3 -> PillFlowOnboardingScreenContent(
                    hero = {
                        PillFlowOnboardingResultHero(
                            modifier = Modifier
                                .padding(
                                    start = 30.dp,
                                    end = 30.dp,
                                    top = 20.dp
                                ),
                            isActive = pagerState.currentPage == 3
                        )
                    },
                    title = "Celebrate your\nprogress.",
                    subtitle = "Track monthly compliance trends, build lasting\nstreaks, and share verified reports with your\ndoctor."
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    PillFlowTheme {
        OnboardingScreen()
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun OnboardingScreenPreviewDark() {
    PillFlowTheme {
        OnboardingScreen()
    }
}