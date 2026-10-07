package com.riramzy.pillfllow.ui.components.onboarding.together

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.avatar1

@Composable
fun PillFlowPatientIcon(
    modifier: Modifier = Modifier,
    avatar: DrawableResource = Res.drawable.avatar1,
    name: String = "Alex",
    relationship: String = "Son",
    indicatorColor: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier
            .dropShadow(
                shape = RoundedCornerShape(45.dp),
                shadow = Shadow(
                    radius = 20.dp,
                    spread = 4.dp,
                    offset = DpOffset(x = 0.dp, y = 4.dp),
                    color = MaterialTheme.colorScheme.primary.copy(0.3f),
                )
            ),
        shape = RoundedCornerShape(45.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .border(
                        width = 5.dp,
                        color = indicatorColor,
                        shape = CircleShape
                    )
            ) {
                Image(
                    painter = painterResource(avatar),
                    contentDescription = "Patient Avatar",
                    modifier = Modifier
                        .clip(CircleShape)
                        .padding(8.dp)
                )
            }

            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 6.dp)
            )

            Text(
                text = relationship,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowPatientIconPreview() {
    PillFlowTheme {
        PillFlowPatientIcon(modifier = Modifier.padding(15.dp))
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, backgroundColor = 0xFF000000)
@Composable
fun PillFlowPatientIconPreviewDark() {
    PillFlowTheme {
        PillFlowPatientIcon(modifier = Modifier.padding(15.dp))
    }
}