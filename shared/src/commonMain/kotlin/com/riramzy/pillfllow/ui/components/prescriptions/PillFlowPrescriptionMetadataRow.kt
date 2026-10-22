package com.riramzy.pillfllow.ui.components.prescriptions

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.time

@Composable
fun PillFlowPrescriptionMetadataRow(
    modifier: Modifier = Modifier,
    icon: DrawableResource = Res.drawable.time,
    customIconColor: Color? = null,
    customIconBackgroundColor: Color? = null,
    title: String = "Twice Daily, 08:00 AM, 08:00 PM",
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    color = customIconBackgroundColor ?: MaterialTheme.colorScheme.primary.copy(0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                imageVector = vectorResource(icon),
                contentDescription = "Pills Icon",
                colorFilter = ColorFilter.tint(customIconColor ?: MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .size(10.dp)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowPrescriptionMetadataRowPreview() {
    PillFlowTheme {
        PillFlowPrescriptionMetadataRow(
            title = "Twice Daily, 08:00 AM, 08:00 PM",
            icon = Res.drawable.time,
            modifier = Modifier.padding(15.dp)
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, backgroundColor = 0xFF000000)
@Composable
fun PillFlowPrescriptionMetadataRowPreviewDark() {
    PillFlowTheme {
        PillFlowPrescriptionMetadataRow(
            title = "Twice Daily, 08:00 AM, 08:00 PM",
            icon = Res.drawable.time,
            modifier = Modifier.padding(15.dp)
        )
    }
}
