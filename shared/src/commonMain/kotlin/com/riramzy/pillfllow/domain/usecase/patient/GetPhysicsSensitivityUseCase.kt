package com.riramzy.pillfllow.domain.usecase.patient

import com.riramzy.pillfllow.domain.session.SessionManager
import com.riramzy.pillfllow.utils.physics.PhysicsSensitivity
import kotlinx.coroutines.flow.StateFlow

class GetPhysicsSensitivityUseCase(
    private val sessionManager: SessionManager
) {
    operator fun invoke(): StateFlow<PhysicsSensitivity> = sessionManager.physicsSensitivity
}