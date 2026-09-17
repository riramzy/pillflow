package com.riramzy.pillfllow.domain.usecase.patient

import com.riramzy.pillfllow.domain.session.SessionManager
import com.riramzy.pillfllow.utils.PhysicsSensitivity

class SetPhysicsSensitivityUseCase(
    private val sessionManager: SessionManager
) {
    operator fun invoke(sensitivity: PhysicsSensitivity) = sessionManager.setPhysicsSensitivity(sensitivity)
}