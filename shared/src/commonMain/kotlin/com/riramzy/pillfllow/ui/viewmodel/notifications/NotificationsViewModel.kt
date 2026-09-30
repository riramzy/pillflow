package com.riramzy.pillfllow.ui.viewmodel.notifications

import androidx.lifecycle.ViewModel
import com.riramzy.pillfllow.domain.repo.NotificationsRepo
import com.riramzy.pillfllow.ui.state.notifications.NotificationsAction
import com.riramzy.pillfllow.ui.state.notifications.NotificationsState
import kotlinx.coroutines.flow.StateFlow

class NotificationsViewModel(
    private val notificationsRepo: NotificationsRepo
) : ViewModel() {
    val state: StateFlow<NotificationsState> = notificationsRepo.state

    fun onAction(action: NotificationsAction) {
        notificationsRepo.onAction(action)
    }

    fun takeDose(doseId: String) {
        notificationsRepo.takeDose(doseId)
    }
}