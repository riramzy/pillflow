package com.riramzy.pillfllow.di

import com.riramzy.pillfllow.ui.viewmodel.auth.AuthViewModel
import com.riramzy.pillfllow.ui.viewmodel.dashboard.CaregiverDashboardViewModel
import com.riramzy.pillfllow.ui.viewmodel.dashboard.PatientDashboardViewModel
import com.riramzy.pillfllow.ui.viewmodel.history.HistoryViewModel
import com.riramzy.pillfllow.ui.viewmodel.prescriptions.CaregiverPrescriptionsViewModel
import com.riramzy.pillfllow.ui.viewmodel.prescriptions.PatientPrescriptionsViewModel
import com.riramzy.pillfllow.ui.viewmodel.settings.CaregiverSettingsViewModel
import com.riramzy.pillfllow.ui.viewmodel.settings.PatientSettingsViewModel
import com.riramzy.pillfllow.ui.viewmodel.splash.SplashViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule: Module = module {
    viewModel {
        SplashViewModel(
            observeCurrentUserUseCase = get()
        )
    }

    viewModel {
        AuthViewModel(
            signInUseCase = get(),
            signUpUseCase = get()
        )
    }

    viewModel {
        PatientDashboardViewModel(
            observeCurrentUserUseCase = get(),
            getPendingDosesForUserUseCase = get(),
            logDoseTakenUseCase = get(),
            getPhysicsSensitivityUseCase = get()
        )
    }

    viewModel {
        PatientPrescriptionsViewModel(
            observeCurrentUserUseCase = get(),
            getMedicationsForUserUseCase = get(),
            getPendingDosesForUserUseCase = get(),
            savePrescriptionUseCase = get(),
            deletePrescriptionUseCase = get()
        )
    }

    viewModel {
        PatientSettingsViewModel(
            observeCurrentUserUseCase = get(),
            getPatientPairingStatusUseCase = get(),
            generatePairingCodeUseCase = get(),
            updateUserProfileUseCase = get(),
            logoutUseCase = get(),
            getPhysicsSensitivityUseCase = get(),
            setPhysicsSensitivityUseCase = get()
        )
    }

    viewModel {
        CaregiverDashboardViewModel(
            observeCurrentUserUseCase = get(),
            getCaregiverPatientsUseCase = get(),
            getPendingDosesForUserUseCase = get(),
            nudgePatientUseCase = get()
        )
    }

    viewModel {
        CaregiverPrescriptionsViewModel(
            observeCurrentUserUseCase = get(),
            getCaregiverPatientsUseCase = get(),
            getPendingDosesForUserUseCase = get(),
            getMedicationsForUserUseCase = get(),
            savePrescriptionUseCase = get(),
            deletePrescriptionUseCase = get()
        )
    }

    viewModel {
        CaregiverSettingsViewModel(
            observeCurrentUserUseCase = get(),
            getCaregiverPatientsUseCase = get(),
            initiatePairingUseCase = get(),
            confirmPairingUseCase = get(),
            unlinkPatientUseCase = get(),
            updateUserProfileUseCase = get(),
            logoutUseCase = get()
        )
    }

    viewModel {
        HistoryViewModel(
            observeCurrentUserUseCase = get(),
            getCaregiverPatientsUseCase = get(),
            getDoseHistoryForUserUseCase = get()
        )
    }
}