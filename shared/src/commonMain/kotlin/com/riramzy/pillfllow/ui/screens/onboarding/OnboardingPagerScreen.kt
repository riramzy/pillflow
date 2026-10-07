package com.riramzy.pillfllow.ui.screens.onboarding

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun OnboardingPagerScreen(
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

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> OnboardingMedicationScreen(onNextClick = onNext, onSkipClick = onFinishOnboarding)
            1 -> OnboardingTimeScreen(onNextClick = onNext, onSkipClick = onFinishOnboarding)
            2 -> OnboardingTogetherScreen(onNextClick = onNext, onSkipClick = onFinishOnboarding)
            3 -> OnboardingResultScreen(onNextClick = onNext)
        }
    }
}