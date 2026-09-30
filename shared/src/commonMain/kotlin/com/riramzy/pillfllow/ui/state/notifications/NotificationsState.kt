package com.riramzy.pillfllow.ui.state.notifications

enum class NotificationType {
    CAREGIVER_NUDGE,
    OVERDUE_DOSE,
    UPCOMING_DOSE,
    ADHERENCE_ALERT
}

data class NotificationAlertUiModel(
    val id: String,
    val title: String,
    val description: String,
    val timestampText: String,
    val type: NotificationType,
    val actionLabel: String? = null
)

data class NotificationsState(
    val alerts: List<NotificationAlertUiModel> = emptyList(),
    val isLoading: Boolean = false
) {
    val hasUnread: Boolean get() = alerts.isNotEmpty()
    val unreadCount: Int get() = alerts.size
}

sealed interface NotificationsAction {
    data class DismissAlert(val id: String) : NotificationsAction
    data object ClearAll : NotificationsAction
}