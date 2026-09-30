package com.riramzy.pillfllow.domain.repo

import com.riramzy.pillfllow.ui.state.notifications.NotificationsAction
import com.riramzy.pillfllow.ui.state.notifications.NotificationsState
import kotlinx.coroutines.flow.StateFlow

interface NotificationsRepo {
    val state: StateFlow<NotificationsState>
    fun onAction(action: NotificationsAction)
    fun takeDose(doseId: String)
}