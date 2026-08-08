package com.riramzy.pillfllow.utils

import androidx.compose.ui.graphics.Color
import com.riramzy.pillfllow.ui.theme.indicatorGreen
import com.riramzy.pillfllow.ui.theme.indicatorGreenContainer
import com.riramzy.pillfllow.ui.theme.indicatorRed
import com.riramzy.pillfllow.ui.theme.indicatorRedContainer
import com.riramzy.pillfllow.ui.theme.indicatorYellow
import com.riramzy.pillfllow.ui.theme.indicatorYellowContainer

enum class IndicatorColor(val color: Color) {
    GREEN(indicatorGreen),
    GREEN_CONTAINER(indicatorGreenContainer),
    YELLOW(indicatorYellow),
    YELLOW_CONTAINER(indicatorYellowContainer),
    RED(indicatorRed),
    RED_CONTAINER(indicatorRedContainer)
}