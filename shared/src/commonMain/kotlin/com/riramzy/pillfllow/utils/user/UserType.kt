package com.riramzy.pillfllow.utils.user

enum class UserType(val label: String) {
    CAREGIVER("Caregiver"),
    PATIENT("Patient");

    companion object {
        fun fromRaw(raw: String?): UserType {
            return if (raw.equals("CAREGIVER", ignoreCase = true)) CAREGIVER else PATIENT
        }
    }
}