package com.riramzy.pillfllow.domain.usecase.auth

import com.riramzy.pillfllow.data.local.database.PillFlowDatabase
import com.riramzy.pillfllow.domain.hardware.PlatformNotifier
import com.riramzy.pillfllow.domain.repo.AuthRepo
import com.riramzy.pillfllow.domain.session.SessionManager
import com.riramzy.pillfllow.utils.app.Result
import com.riramzy.pillfllow.utils.app.safeCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class LogoutUseCase(
    private val authRepo: AuthRepo,
    private val sessionManager: SessionManager,
    private val database: PillFlowDatabase,
    private val platformNotifier: PlatformNotifier
) {
    suspend operator fun invoke(): Result<Unit> {
        return safeCall {
            platformNotifier.cancelAllReminders()

            sessionManager.clearUser()

            withContext(Dispatchers.IO) {
                database.clearDatabase()
            }

            authRepo.signOut()
        }
    }
}