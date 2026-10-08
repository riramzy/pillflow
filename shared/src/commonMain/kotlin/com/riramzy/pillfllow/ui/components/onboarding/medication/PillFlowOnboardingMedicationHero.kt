package com.riramzy.pillfllow.ui.components.onboarding.medication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.riramzy.pillfllow.ui.components.custom.PillFlowHoveringCard
import com.riramzy.pillfllow.ui.components.dashboard.patient.PillFlowPillsDish
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.medication.IndicatorColor
import com.riramzy.pillfllow.utils.pill.PillShape
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.dot
import pillfllow.shared.generated.resources.sun
import kotlin.math.min

@Composable
fun PillFlowOnboardingMedicationHero(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        PillFlowHoveringCard(
            customTitle = "Morning Dose • 8:00 AM",
            customIcon = Res.drawable.sun
        )

        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(325.dp)
                .fillMaxWidth(0.92f)
                .widthIn(max = 355.dp)
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()
            val dishCenter = Offset(widthPx / 2f + 25f, heightPx / 2f)
            val dishRadius = min(widthPx, heightPx) * 0.38f

            PillFlowPillsDish(
                center = dishCenter,
                radius = dishRadius,
                chuteWidth = 200f,
                handleWidth = 110f,
                modifier = Modifier.align(Alignment.Center)
            )

            PillFlowHoveringPill(
                shape = PillShape.CIRCLE,
                baseColor = IndicatorColor.GREEN.color,
                size = DpSize(60.dp, 60.dp),
                baseRotation = 0f,
                cycleDurationMillis = 4000,
                phaseOffsetRad = 0f,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = (-45).dp, y = 5.dp)
            )

            PillFlowHoveringPill(
                shape = PillShape.CAPSULE,
                baseColor = IndicatorColor.YELLOW.color,
                secondaryColor = IndicatorColor.YELLOW.color.copy(0.4f),
                size = DpSize(50.dp, 85.dp),
                baseRotation = 42f,
                cycleDurationMillis = 3400,
                phaseOffsetRad = 2.09f,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 42.dp, y = (-60).dp)
            )


            PillFlowHoveringPill(
                shape = PillShape.OVAL,
                baseColor = Color(0xFF4F46E5),
                size = DpSize(50.dp, 85.dp),
                baseRotation = -35f,
                cycleDurationMillis = 4600,
                phaseOffsetRad = 4.19f,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 40.dp, y = 55.dp)
            )
        }

        PillFlowHoveringCard(
            customTitle = "3 Pills in Dish",
            customIcon = Res.drawable.dot
        )
    }
}

@Preview
@Composable
fun PillFlowOnboardingMedicationHeroPreview() {
    PillFlowTheme {
        PillFlowOnboardingMedicationHero(modifier = Modifier.padding(15.dp))
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PillFlowOnboardingMedicationHeroPreviewDark() {
    PillFlowTheme {
        PillFlowOnboardingMedicationHero(modifier = Modifier.padding(15.dp))
    }
}