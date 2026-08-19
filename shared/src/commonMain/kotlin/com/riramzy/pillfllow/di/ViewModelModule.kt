package com.riramzy.pillfllow.di

import com.riramzy.pillfllow.ui.viewmodel.CaregiverDashboardViewModel
import com.riramzy.pillfllow.ui.viewmodel.PatientDashboardViewModel
import com.riramzy.pillfllow.ui.viewmodel.auth.AuthViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule: Module = module {
    viewModel { AuthViewModel(authRepo = get()) }
    viewModel { PatientDashboardViewModel(medicationRepo = get(), userRepo = get()) }
    viewModel { CaregiverDashboardViewModel(pairingRepo = get(), medicationRepo = get()) }

}