package com.riramzy.pillfllow.utils.pill

object PillColorMapper {
    fun fromRaw(raw: String?): PillColor {
        if (raw.isNullOrBlank()) return PillColor.SKY_BLUE
        val normalized = raw.replace("_", " ").trim()

        return PillColor.entries.firstOrNull {
            it.name.equals(raw, ignoreCase = true) ||
                    it.label.equals(normalized, ignoreCase = true)
        } ?: PillColor.SKY_BLUE
    }
}