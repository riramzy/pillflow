package com.riramzy.pillfllow.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.outfit_black
import pillfllow.shared.generated.resources.outfit_bold
import pillfllow.shared.generated.resources.outfit_light
import pillfllow.shared.generated.resources.outfit_medium
import pillfllow.shared.generated.resources.outfit_regular
import pillfllow.shared.generated.resources.outfit_semi_bold
import pillfllow.shared.generated.resources.outfit_thin

@Composable
fun outfitFontFamily() = FontFamily(
    Font(Res.font.outfit_black, FontWeight.Black),
    Font(Res.font.outfit_bold, FontWeight.Bold),
    Font(Res.font.outfit_semi_bold, FontWeight.SemiBold),
    Font(Res.font.outfit_medium, FontWeight.Medium),
    Font(Res.font.outfit_regular, FontWeight.Normal),
    Font(Res.font.outfit_light, FontWeight.Light),
    Font(Res.font.outfit_thin, FontWeight.Thin),
)

@Composable
fun appTypography(): Typography {
    val outfit = outfitFontFamily()
    val default = Typography()

    return Typography(
        displayLarge = default.displayLarge.copy(fontFamily = outfit),
        displayMedium = default.displayMedium.copy(fontFamily = outfit),
        displaySmall = default.displaySmall.copy(fontFamily = outfit),
        headlineLarge = default.headlineLarge.copy(fontFamily = outfit),
        headlineMedium = default.headlineMedium.copy(fontFamily = outfit),
        headlineSmall = default.headlineSmall.copy(fontFamily = outfit),
        titleLarge = default.titleLarge.copy(fontFamily = outfit),
        titleMedium = default.titleMedium.copy(fontFamily = outfit),
        titleSmall = default.titleSmall.copy(fontFamily = outfit),
        bodyLarge = default.bodyLarge.copy(fontFamily = outfit),
        bodyMedium = default.bodyMedium.copy(fontFamily = outfit),
        bodySmall = default.bodySmall.copy(fontFamily = outfit),
        labelLarge = default.labelLarge.copy(fontFamily = outfit),
        labelMedium = default.labelMedium.copy(fontFamily = outfit),
        labelSmall = default.labelSmall.copy(fontFamily = outfit),
    )
}