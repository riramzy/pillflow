package com.riramzy.pillfllow.ui.components.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.ui.components.custom.PillFlowButton
import com.riramzy.pillfllow.ui.state.notifications.NotificationAlertUiModel
import com.riramzy.pillfllow.ui.state.notifications.NotificationType
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.medication.IndicatorColor
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.compliance_late
import pillfllow.shared.generated.resources.compliance_missed
import pillfllow.shared.generated.resources.next
import pillfllow.shared.generated.resources.nugde

@Composable
fun PillFlowNotificationAlertCard(
    modifier: Modifier = Modifier,
    alert: NotificationAlertUiModel,
    onDismiss: (String) -> Unit = {},
    onTakeDose: (String) -> Unit = {},
) {
    var icon: DrawableResource
    var color: Color
    var backgroundColor: Color
    var actionLabel: String
    val typeLabel: String

    when(alert.type) {
        NotificationType.OVERDUE_DOSE -> {
            icon = Res.drawable.compliance_late
            color = IndicatorColor.YELLOW.color
            backgroundColor = IndicatorColor.YELLOW_CONTAINER.color
            actionLabel = "Take Dose"
            typeLabel = "Overdue"
        }

        NotificationType.ADHERENCE_ALERT -> {
            icon = Res.drawable.compliance_missed
            color = IndicatorColor.RED.color
            backgroundColor = IndicatorColor.RED_CONTAINER.color
            actionLabel = "Dismiss"
            typeLabel = "Missed"
        }

        NotificationType.UPCOMING_DOSE -> {
            icon = Res.drawable.next
            color = MaterialTheme.colorScheme.primary
            backgroundColor = MaterialTheme.colorScheme.primaryContainer
            actionLabel = "Dismiss"
            typeLabel = "Upcoming"
        }

        NotificationType.CAREGIVER_NUDGE -> {
            icon = Res.drawable.nugde
            color = MaterialTheme.colorScheme.tertiary
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
            actionLabel = "Dismiss"
            typeLabel = "Nudge"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.5f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(25.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = backgroundColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = vectorResource(icon),
                    contentDescription = "null",
                    modifier = Modifier.size(12.dp),
                    tint = color
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = alert.timestampText,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = alert.description,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                PillFlowButton(
                    text = actionLabel,
                    customColor = backgroundColor,
                    customTextColor = color,
                    onClick = {
                        if (alert.type == NotificationType.OVERDUE_DOSE) {
                            onTakeDose(alert.id)
                        } else {
                            onDismiss(alert.id)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowNotificationAlertCardPreview() {
    PillFlowTheme {
        PillFlowNotificationAlertCard(
            alert = NotificationAlertUiModel(
                id = "1",
                title = "Bon One 1000mg",
                description = "Dose One was due at 19:00",
                timestampText = "Today, at 19:00",
                type = NotificationType.CAREGIVER_NUDGE,
                actionLabel = "Take immediately"
            ),
            modifier = Modifier.padding(15.dp)
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, backgroundColor = 0xFF000000)
@Composable
fun PillFlowNotificationAlertCardPreviewDark() {
    PillFlowTheme {
        PillFlowNotificationAlertCard(
            alert = NotificationAlertUiModel(
                id = "1",
                title = "Bon One 1000mg",
                description = "Dose One was due at 19:00",
                timestampText = "Today, at 19:00",
                type = NotificationType.ADHERENCE_ALERT,
                actionLabel = "Take immediately"
            ),
            modifier = Modifier.padding(15.dp)
        )
    }
}