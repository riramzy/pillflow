package com.riramzy.pillfllow.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.riramzy.pillfllow.data.local.dao.MedicationDao
import com.riramzy.pillfllow.domain.hardware.PlatformNotifier
import com.riramzy.pillfllow.utils.medication.DoseReminderStage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class BootReceiver : BroadcastReceiver() {
    private val medicationDao: MedicationDao by inject(MedicationDao::class.java)
    private val platformNotifier: PlatformNotifier by inject(PlatformNotifier::class.java)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val now = System.currentTimeMillis()
                    val medications = medicationDao.getAllMedicationsOnce().associateBy { it.id }
                    val pendingDoses = medicationDao.getAllPendingScheduledDosesOnce()
                        .filter { !it.isTaken && it.scheduledTime > now }

                    pendingDoses.forEach { dose ->
                        val medName = medications[dose.medicationId]?.name ?: "Medication"

                        val tMinus30 = (dose.scheduledTime - 30 * 60 * 1000L).coerceAtLeast(now + 1000L)
                        platformNotifier.scheduleDoseReminder(
                            context = context,
                            doseId = dose.id,
                            pillName = medName,
                            triggerTimeMillis = tMinus30,
                            stage = DoseReminderStage.ADVANCE_30MIN
                        )

                        platformNotifier.scheduleDoseReminder(
                            context = context,
                            doseId = dose.id,
                            pillName = medName,
                            triggerTimeMillis = dose.scheduledTime,
                            stage = DoseReminderStage.DUE_NOW
                        )

                        val tPlus15 = dose.scheduledTime + 15 * 60 * 1000L
                        if (tPlus15 > now) {
                            platformNotifier.scheduleDoseReminder(
                                context = context,
                                doseId = dose.id,
                                pillName = medName,
                                triggerTimeMillis = tPlus15,
                                stage = DoseReminderStage.PRE_EXPIRY_15MIN
                            )
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}