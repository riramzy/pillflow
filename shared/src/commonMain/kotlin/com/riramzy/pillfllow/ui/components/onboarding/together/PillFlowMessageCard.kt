package com.riramzy.pillfllow.ui.components.onboarding.together

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import pillfllow.shared.generated.resources.message

@Composable
fun PillFlowMessageCard(
    modifier: Modifier = Modifier,
    sender: String = "Mom",
    message: String = "Thanks Alex,\njust took it! \uD83E\uDD70"
) {
    Card(
        modifier = modifier
            .dropShadow(
                shape = RoundedCornerShape(25.dp),
                shadow = Shadow(
                    radius = 10.dp,
                    spread = 4.dp,
                    offset = DpOffset(x = 0.dp, y = 4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(0.8f),
                )
            ),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.message),
                contentDescription = "Message Icon",
                modifier = Modifier
                    .size(20.dp)
            )

            Text(
                text = "$sender: \"$message\"",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowMessageCardPreview() {
    PillFlowTheme {
        PillFlowMessageCard(modifier = Modifier.padding(15.dp))
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, backgroundColor = 0xFF000000)
@Composable
fun PillFlowMessageCardPreviewDark() {
    PillFlowTheme {
        PillFlowMessageCard(modifier = Modifier.padding(15.dp))
    }
}