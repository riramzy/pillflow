package com.riramzy.pillfllow.domain.usecase.medication

import com.riramzy.pillfllow.data.local.dao.MedicationDao
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.domain.scheduler.MedicationScheduler
import com.riramzy.pillfllow.utils.app.Result
import com.riramzy.pillfllow.utils.app.safeCall

class SyncAndMaintainScheduleUseCase(
    private val medicationRepo: MedicationRepo,
    private val medicationDao: MedicationDao
) {
    suspend operator fun invoke(): Result<Unit> = safeCall {
        medicationRepo.syncUnsyncedData()
        val activeMeds = medicationDao.getAllMedicationsOnce().filter { !it.isArchived }
        val allDoses = medicationDao.getAllScheduledDosesOnce().groupBy { it.medicationId }

        activeMeds.forEach { med ->
            val medDoses = allDoses[med.id].orEmpty()

            val newDoses = MedicationScheduler.extendRollingWindow(
                medicationId = med.id,
                existingDoses = medDoses
            )

            if (newDoses.isNotEmpty()) {
                medicationRepo.insertScheduledDoses(newDoses)
            }
        }
    }
}