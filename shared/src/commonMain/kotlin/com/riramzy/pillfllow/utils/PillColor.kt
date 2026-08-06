package com.riramzy.pillfllow.utils

import androidx.compose.ui.graphics.Color
import com.riramzy.pillfllow.ui.theme.citrusGold
import com.riramzy.pillfllow.ui.theme.coralRed
import com.riramzy.pillfllow.ui.theme.mintGreen
import com.riramzy.pillfllow.ui.theme.skyBlue
import com.riramzy.pillfllow.ui.theme.softPurple

enum class PillColor(val label: String, val color: Color) {
    CORAL_RED("Coral Red", coralRed),
    CITRUS_GOLD("Citrus Gold", citrusGold),
    SOFT_PURPLE("Soft Purple", softPurple),
    SKY_BLUE("Sky Blue", skyBlue),
    MINT_GREEN("Mint Green", mintGreen)
}