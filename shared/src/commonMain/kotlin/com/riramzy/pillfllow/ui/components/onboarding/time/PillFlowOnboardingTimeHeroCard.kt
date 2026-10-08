package com.riramzy.pillfllow.ui.components.onboarding.time

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.riramzy.pillfllow.ui.components.dashboard.caregiver.PillFlowPatientWeeklyOverviewCard
import com.riramzy.pillfllow.ui.components.dashboard.patient.PillFlowComplianceCard
import com.riramzy.pillfllow.ui.components.prescriptions.PillFlowPrescriptionCard
import com.riramzy.pillfllow.ui.components.prescriptions.PillFlowPrescriptionsSummaryCard
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.medication.ComplianceStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PillFlowOnboardingTimeHeroCard(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
) {
    val isPreview = LocalInspectionMode.current
    val startOffset = if (isPreview) 0f else -350f
    var hasAnimated by remember { mutableStateOf(false) }

    val drop1 = remember { Animatable(startOffset) }
    val drop2 = remember { Animatable(startOffset) }
    val drop3 = remember { Animatable(startOffset) }
    val drop4 = remember { Animatable(startOffset) }

    LaunchedEffect(isActive) {
        if (!isActive || hasAnimated || isPreview) return@LaunchedEffect
        hasAnimated = true

        val relaxedSpring = spring<Float>(
            dampingRatio = 0.70f,
            stiffness = Spring.StiffnessLow
        )

        launch { drop1.animateTo(0f, relaxedSpring) }

        launch {
            delay(200.milliseconds)
            drop2.animateTo(0f, relaxedSpring)
        }

        launch {
            delay(400.milliseconds)
            drop3.animateTo(0f, relaxedSpring)
        }

        launch {
            delay(600.milliseconds)
            drop4.animateTo(0f, relaxedSpring)
        }
    }

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(432.dp)
                .dropShadow(
                    shape = RoundedCornerShape(32.dp),
                    shadow = Shadow(
                        radius = 20.dp,
                        spread = 6.dp,
                        offset = DpOffset(x = 0.dp, y = 8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(0.8f),
                    )
                ),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {}

        PillFlowPatientWeeklyOverviewCard(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(300.dp)
                .graphicsLayer {
                    rotationZ = 7f
                    translationY = drop4.value.dp.toPx()
                }
                .offset(x = 60.dp, y = 325.dp)
                .dropShadow(
                    shape = RoundedCornerShape(25.dp),
                    shadow = Shadow(
                        radius = 20.dp,
                        spread = 6.dp,
                        offset = DpOffset(x = 0.dp, y = 6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(0.3f),
                    )
                )
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(25.dp)
                ),
        )

        PillFlowComplianceCard(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(320.dp)
                .graphicsLayer {
                    rotationZ = -6f
                    translationY = drop3.value.dp.toPx()
                }
                .offset(x = (-35).dp, y = 235.dp)
                .dropShadow(
                    shape = RoundedCornerShape(25.dp),
                    shadow = Shadow(
                        radius = 20.dp,
                        spread = 6.dp,
                        offset = DpOffset(x = 0.dp, y = 6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(0.3f),

                        )
                )
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(25.dp)
                ),
            status = ComplianceStatus.MISSED,
            title = "Overdue: Vitamin C",
            subtitle = "Was due at 2:00 PM (35 mins ago)",
            badgeText = "Grace Expired"
        )

        PillFlowPrescriptionsSummaryCard(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .graphicsLayer {
                    rotationZ = 6f
                    translationY = drop2.value.dp.toPx()
                }
                .offset(x = 30.dp, y = 155.dp)
                .dropShadow(
                    shape = RoundedCornerShape(25.dp),
                    shadow = Shadow(
                        radius = 20.dp,
                        spread = 6.dp,
                        offset = DpOffset(x = 0.dp, y = 6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(0.3f),

                        )
                )
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(25.dp)
                ),
            isForOnboarding = true
        )

        PillFlowPrescriptionCard(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .graphicsLayer {
                    rotationZ = -8f
                    translationY = drop1.value.dp.toPx()
                }
                .offset(x = (-10).dp, y = 14.dp)
                .dropShadow(
                    shape = RoundedCornerShape(25.dp),
                    shadow = Shadow(
                        radius = 20.dp,
                        spread = 6.dp,
                        offset = DpOffset(x = 10.dp, y = 6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(0.3f),
                        )
                )
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(25.dp)
                )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowOnboardingTimeHeroCardPreview() {
    PillFlowTheme {
        PillFlowOnboardingTimeHeroCard(modifier = Modifier.padding(50.dp))
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, backgroundColor = 0xFF000000)
@Composable
fun PillFlowOnboardingTimeHeroCardPreviewDark() {
    PillFlowTheme {
        PillFlowOnboardingTimeHeroCard(modifier = Modifier.padding(35.dp))
    }
}