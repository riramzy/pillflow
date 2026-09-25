package com.riramzy.pillfllow.utils.pill

object PillShapeMapper {
    fun fromRaw(raw: String?, default: PillShape = PillShape.CAPSULE): PillShape {
        if (raw.isNullOrBlank()) return default
        val cleaned = raw.trim()

        return PillShape.entries.firstOrNull {
            it.name.equals(cleaned, ignoreCase = true) ||
                    it.label.equals(cleaned, ignoreCase = true)
        } ?: default
    }
}