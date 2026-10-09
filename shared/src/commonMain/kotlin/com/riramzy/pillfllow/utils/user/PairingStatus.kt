package com.riramzy.pillfllow.utils.user

data class PairingStatus(
    val pendingCode: String? = null,
    val hasActivePairing: Boolean = false,
    val activePairingId: String? = null,
    val caregiverId: String? = null,
    val caregiverName: String? = null,
    val relation: String? = null
)