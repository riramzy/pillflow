package com.riramzy.pillfllow.di

import com.riramzy.pillfllow.ui.viewmodel.auth.AuthViewModel
import com.riramzy.pillfllow.ui.viewmodel.dashboard.CaregiverDashboardViewModel
import com.riramzy.pillfllow.ui.viewmodel.dashboard.PatientDashboardViewModel
import com.riramzy.pillfllow.ui.viewmodel.history.HistoryViewModel
import com.riramzy.pillfllow.ui.viewmodel.prescriptions.PrescriptionsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule: Module = module {
    viewModel { AuthViewModel(authRepo = get()) }

    viewModel { PatientDashboardViewModel(medicationRepo = get(), authRepo = get()) }

    viewModel {
        CaregiverDashboardViewModel(
            pairingRepo = get(),
            medicationRepo = get(),
            authRepo = get(),
            userRepo = get()
        )
    }

    viewModel {
        HistoryViewModel(
            medicationRepo = get(),
            authRepo = get(),
            pairingRepo = get(),
            userRepo = get()
        )
    }

    viewModel {
        PrescriptionsViewModel(
            medicationRepo = get(),
            authRepo = get()
        )
    }
}