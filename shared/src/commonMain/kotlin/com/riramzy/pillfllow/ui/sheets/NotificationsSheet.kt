package com.riramzy.pillfllow.ui.sheets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.riramzy.pillfllow.ui.components.notifications.PillFlowNotificationAlertCard
import com.riramzy.pillfllow.ui.state.notifications.NotificationAlertUiModel
import com.riramzy.pillfllow.ui.state.notifications.NotificationType
import com.riramzy.pillfllow.ui.state.notifications.NotificationsAction
import com.riramzy.pillfllow.ui.state.notifications.NotificationsState
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.ui.viewmodel.notifications.NotificationsViewModel
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.notification

@Composable
fun NotificationsSheet(
    viewModel: NotificationsViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    NotificationsSheetContent(
        state = state,
        onDismissAlert = { id -> viewModel.onAction(NotificationsAction.DismissAlert(id)) },
        onClearAll = { viewModel.onAction(NotificationsAction.ClearAll) },
        onTakeDose = { doseId -> viewModel.takeDose(doseId) },
        modifier = modifier
    )
}

@Composable
fun NotificationsSheetContent(
    state: NotificationsState,
    onDismissAlert: (String) -> Unit,
    onClearAll: () -> Unit,
    onTakeDose: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                if (state.hasUnread) {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${state.unreadCount}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (state.hasUnread) {
                Text(
                    text = "Clear All",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onClearAll() }
                )
            }
        }

        if (state.alerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        imageVector = vectorResource(Res.drawable.notification),
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                    )

                    Text(
                        text = "You're all caught up!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "No active reminders or alerts right now",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(state.alerts, key = { it.id }) { alert ->
                    PillFlowNotificationAlertCard(
                        alert = alert,
                        onDismiss = { onDismissAlert(alert.id) },
                        onTakeDose = { onTakeDose(alert.id) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsSheetPreview() {
    PillFlowTheme {
        NotificationsSheetContent(
            state = NotificationsState(
                alerts = listOf(
                    NotificationAlertUiModel(
                        id = "1",
                        title = "Overdue: Bon One",
                        description = "Bon One was due at 19:00",
                        timestampText = "1L",
                        type = NotificationType.OVERDUE_DOSE
                    )
                )
            ),
            onDismissAlert = {},
            onClearAll = {}
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, backgroundColor = 0xFF000000)
@Composable
fun NotificationsSheetPreviewDark() {
    PillFlowTheme {
        NotificationsSheetContent(
            state = NotificationsState(
                alerts = listOf(
                    NotificationAlertUiModel(
                        id = "1",
                        title = "Overdue: Bon One",
                        description = "Bon One was due at 19:00",
                        timestampText = "1L",
                        type = NotificationType.OVERDUE_DOSE
                    )
                )
            ),
            onDismissAlert = {},
            onClearAll = {}
        )
    }
}
