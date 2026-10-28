package com.riramzy.pillfllow.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.riramzy.pillfllow.data.local.dao.MedicationDao
import com.riramzy.pillfllow.data.local.dao.UserDao
import com.riramzy.pillfllow.domain.compliance.DoseStateMachine
import com.riramzy.pillfllow.domain.hardware.PlatformNotifier
import com.riramzy.pillfllow.domain.repo.AuthRepo
import com.riramzy.pillfllow.utils.medication.DoseReminderStage
import com.riramzy.pillfllow.utils.platform.currentTimeMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class BootReceiver : BroadcastReceiver() {
    private val medicationDao: MedicationDao by inject(MedicationDao::class.java)
    private val userDao: UserDao by inject(UserDao::class.java)
    private val authRepo: AuthRepo by inject(AuthRepo::class.java)
    private val platformNotifier: PlatformNotifier by inject(PlatformNotifier::class.java)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        val isSupported = action == Intent.ACTION_BOOT_COMPLETED ||
                action == Intent.ACTION_MY_PACKAGE_REPLACED ||
                action == Intent.ACTION_TIME_CHANGED ||
                action == Intent.ACTION_TIMEZONE_CHANGED

        if (!isSupported) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = currentTimeMillis()
                val activeUser = authRepo.getCurrentUser()
                val isCaregiver = activeUser?.userType.equals("caregiver", ignoreCase = true)

                val activeMeds = medicationDao.getAllMedicationsOnce()
                    .filter { !it.isArchived && !it.isPaused && !it.isPrn }
                    .associateBy { it.id }

                val activeDoses = medicationDao.getAllPendingScheduledDosesOnce()
                    .filter { dose ->
                        !dose.isTaken &&
                                activeMeds.containsKey(dose.medicationId) &&
                                (now <= dose.scheduledTime + DoseStateMachine.LATE_WINDOW_MILLIS)
                    }

                if (isCaregiver) {
                    activeDoses.forEach { dose ->
                        val med = activeMeds[dose.medicationId]
                        val medName = med?.name ?: "Medication"
                        val patientName = med?.userId?.let { userDao.getUserByIdOnce(it)?.firstName } ?: "Your patient"

                        val lateTrigger = dose.scheduledTime + DoseStateMachine.ON_TIME_WINDOW_MILLIS
                        if (lateTrigger > now) {
                            platformNotifier.scheduleCaregiverEscalation(
                                context = context,
                                doseId = dose.id,
                                pillName = medName,
                                patientName = patientName,
                                triggerTimeMillis = lateTrigger,
                                isMissed = false
                            )
                        }

                        val missedTrigger = dose.scheduledTime + DoseStateMachine.LATE_WINDOW_MILLIS
                        if (missedTrigger > now) {
                            platformNotifier.scheduleCaregiverEscalation(
                                context = context,
                                doseId = dose.id,
                                pillName = medName,
                                patientName = patientName,
                                triggerTimeMillis = missedTrigger,
                                isMissed = true
                            )
                        }
                    }
                } else {
                    activeDoses.forEach { dose ->
                        val medName = activeMeds[dose.medicationId]?.name ?: "Medication"

                        val tMinus30 = dose.scheduledTime - 30 * 60 * 1000L
                        if (tMinus30 > now) {
                            platformNotifier.scheduleDoseReminder(
                                context = context,
                                doseId = dose.id,
                                pillName = medName,
                                triggerTimeMillis = tMinus30,
                                stage = DoseReminderStage.ADVANCE_30MIN
                            )
                        }

                        val dueTrigger = if (dose.scheduledTime > now) {
                            dose.scheduledTime
                        } else {
                            (now + 1000L)
                        }
                        platformNotifier.scheduleDoseReminder(
                            context = context,
                            doseId = dose.id,
                            pillName = medName,
                            triggerTimeMillis = dueTrigger,
                            stage = DoseReminderStage.DUE_NOW
                        )

                        val tPlus = dose.scheduledTime + 15 * 60 * 1000L
                        if (tPlus > now) {
                            platformNotifier.scheduleDoseReminder(
                                context = context,
                                doseId = dose.id,
                                pillName = medName,
                                triggerTimeMillis = tPlus,
                                stage = DoseReminderStage.PRE_EXPIRY_15MIN
                            )
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}