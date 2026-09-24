package com.riramzy.pillfllow.di

import com.riramzy.pillfllow.domain.usecase.auth.LogoutUseCase
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.domain.usecase.auth.SignInUseCase
import com.riramzy.pillfllow.domain.usecase.auth.SignUpUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.ConfirmPairingUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.GetCaregiverPatientsUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.InitiatePairingUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.NudgePatientUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.UnlinkPatientUseCase
import com.riramzy.pillfllow.domain.usecase.medication.DeletePrescriptionUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetDoseHistoryForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetMedicationsForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetPendingDosesForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.LogDoseTakenUseCase
import com.riramzy.pillfllow.domain.usecase.medication.SavePrescriptionUseCase
import com.riramzy.pillfllow.domain.usecase.patient.GeneratePairingCodeUseCase
import com.riramzy.pillfllow.domain.usecase.patient.GetPatientPairingStatusUseCase
import com.riramzy.pillfllow.domain.usecase.patient.GetPhysicsSensitivityUseCase
import com.riramzy.pillfllow.domain.usecase.patient.SetPhysicsSensitivityUseCase
import com.riramzy.pillfllow.domain.usecase.patient.UpdateUserProfileUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val useCaseModule: Module = module {
    factory { SignInUseCase(authRepo = get(), sessionManager = get()) }
    factory { SignUpUseCase(authRepo = get(), sessionManager = get()) }
    factory { LogoutUseCase(authRepo = get(), sessionManager = get(), database = get(), platformNotifier = get()) }
    factory { ObserveCurrentUserUseCase(sessionManager = get(), authRepo = get()) }

    factory { GetCaregiverPatientsUseCase(pairingRepo = get(), userRepo = get(), medicationRepo = get()) }
    factory { InitiatePairingUseCase(pairingRepo = get(), userRepo = get()) }
    factory { ConfirmPairingUseCase(pairingRepo = get(), userRepo = get()) }
    factory { UnlinkPatientUseCase(pairingRepo = get(), userRepo = get()) }
    factory { NudgePatientUseCase(firestore = get()) }

    factory { GetPatientPairingStatusUseCase(pairingRepo = get()) }
    factory { GeneratePairingCodeUseCase(pairingRepo = get()) }
    factory { UpdateUserProfileUseCase(userRepo = get(), sessionManager = get()) }
    factory { GetPhysicsSensitivityUseCase(sessionManager = get()) }
    factory { SetPhysicsSensitivityUseCase(sessionManager = get()) }

    factory { GetMedicationsForUserUseCase(medicationRepo = get()) }
    factory { SavePrescriptionUseCase(medicationRepo = get()) }
    factory { DeletePrescriptionUseCase(medicationRepo = get()) }
    factory { GetPendingDosesForUserUseCase(medicationRepo = get()) }
    factory { LogDoseTakenUseCase(medicationRepo = get(), platformNotifier = get()) }
    factory { GetDoseHistoryForUserUseCase(medicationRepo = get()) }
}