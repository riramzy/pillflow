package com.riramzy.pillfllow.ui.components.onboarding.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.medication.IndicatorColor
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.award
import pillfllow.shared.generated.resources.streak

@Composable
fun PillFlowAccomplishmentCard(
    modifier: Modifier= Modifier,
    icon: DrawableResource = Res.drawable.award,
    iconColor: Color = Color(0xFFFFCC00),
    title: String = "98% Adherence",
    subtitle: String = "30 of 31 On Time"
) {
    Box(
        modifier = modifier
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier
                .widthIn(min = 140.dp)
                .align(Alignment.Center)
                .offset(x = 0.dp, y = 15.dp)
                .dropShadow(
                    shape = RoundedCornerShape(32.dp),
                    shadow = Shadow(
                        radius = 15.dp,
                        spread = 4.dp,
                        offset = DpOffset(x = 0.dp, y = 8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(0.8f),
                    )
                ),
        ) {
            Column(
                modifier = Modifier
                    .padding(15.dp)
                    .widthIn(min = 140.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 0.dp, y = (-35).dp)
                .size(47.dp)
                .background(
                    color = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .padding(4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowAccomplishmentCardPreview() {
    PillFlowTheme {
        Surface {
            Column {
                PillFlowAccomplishmentCard(
                    modifier = Modifier.padding(15.dp),
                    title = "98% Adherence",
                    subtitle = "30 of 31 On Time",
                    icon = Res.drawable.award
                )

                PillFlowAccomplishmentCard(
                    modifier = Modifier.padding(15.dp),
                    title = "21-Day Streak",
                    subtitle = "Personal Best!",
                    icon = Res.drawable.streak,
                    iconColor = IndicatorColor.RED.color
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PillFlowAccomplishmentCardPreviewDark() {
    PillFlowTheme {
        Surface {
            Column {
                PillFlowAccomplishmentCard(
                    modifier = Modifier.padding(15.dp),
                    title = "98% Adherence",
                    subtitle = "30 of 31 On Time",
                    icon = Res.drawable.award
                )

                PillFlowAccomplishmentCard(
                    modifier = Modifier.padding(15.dp),
                    title = "21-Day Streak",
                    subtitle = "Personal Best!",
                    icon = Res.drawable.streak,
                    iconColor = IndicatorColor.RED.color
                )
            }
        }
    }
}