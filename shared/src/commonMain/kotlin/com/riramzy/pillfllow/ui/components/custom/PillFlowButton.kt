package com.riramzy.pillfllow.ui.components.custom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.call
import pillfllow.shared.generated.resources.settings

@Composable
fun PillFlowButton(
    modifier: Modifier = Modifier,
    text: String = "Call",
    textAlignment: TextAlign = TextAlign.Center,
    withIcon: Boolean = false,
    icon: DrawableResource? = Res.drawable.call,
    customColor: Color? = null,
    customTextColor: Color? = null,
    customTextSize: Int? = null,
    onClick: () -> Unit = {},
    isEnabled: Boolean = true
) {
    if (withIcon) {
        IconButton(
            onClick = { onClick() },
            modifier = modifier
                .widthIn(min = 100.dp)
                .heightIn(min = 50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = customColor
                    ?: MaterialTheme.colorScheme.primary
            ),
            enabled = isEnabled
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = vectorResource(icon ?: Res.drawable.settings),
                    contentDescription = "null",
                    tint = customTextColor
                        ?: MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = customTextSize?.sp ?: 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = customTextColor
                        ?: MaterialTheme.colorScheme.onPrimary,
                    textAlign = textAlignment
                )
            }
        }
    } else {
        Button(
            onClick = { onClick() },
            modifier = modifier
                .widthIn(min = 100.dp)
                .heightIn(min = 50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = customColor
                    ?: MaterialTheme.colorScheme.primary
            ),
            enabled = isEnabled
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontSize = customTextSize?.sp ?: 16.sp,
                fontWeight = FontWeight.Normal,
                color = customTextColor
                    ?: MaterialTheme.colorScheme.onPrimary,
                textAlign = textAlignment
            )
        }
    }
}

@Preview
@Composable
fun PillFlowButtonPreview() {
    PillFlowTheme {
        PillFlowButton()
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PillFlowButtonPreviewDark() {
    PillFlowTheme {
        PillFlowButton()
    }
}

