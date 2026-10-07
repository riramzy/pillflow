package com.riramzy.pillfllow.ui.components.onboarding.together

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import org.jetbrains.compose.resources.painterResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.bell

@Composable
fun PillFlowNudgeCard(
    modifier: Modifier = Modifier,
    message: String = "“Hi Mom! Just checking in —\nremember to take your 8:00PM\nMetformin with water ❤”",
) {
    Card(
        modifier = modifier
            .width(240.dp)
            .dropShadow(
                shape = RoundedCornerShape(25.dp),
                shadow = Shadow(
                    radius = 10.dp,
                    spread = 4.dp,
                    offset = DpOffset(x = 0.dp, y = 4.dp),
                    color = MaterialTheme.colorScheme.primary.copy(0.3f),
                )
            ),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.bell),
                contentDescription = "Nudge Icon",
                modifier = Modifier
                    .size(20.dp)
                    .dropShadow(
                        shape = CircleShape,
                        shadow = Shadow(
                            radius = 8.dp,
                            spread = 4.dp,
                            color = Color(0xFFFFCC00),
                        )
                    ),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Gentle Nudge",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Text(
                        text = "Just Now",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.primary.copy(0.5f)
                    )
                }

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowNudgeCardPreview() {
    PillFlowTheme {
        PillFlowNudgeCard(modifier = Modifier.padding(15.dp))
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, backgroundColor = 0xFF000000)
@Composable
fun PillFlowNudgeCardPreviewDark() {
    PillFlowTheme {
        PillFlowNudgeCard(modifier = Modifier.padding(15.dp))
    }
}