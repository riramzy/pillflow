package com.riramzy.pillfllow.utils.app

import org.jetbrains.compose.resources.DrawableResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.avatar1
import pillfllow.shared.generated.resources.avatar2
import pillfllow.shared.generated.resources.avatar3
import pillfllow.shared.generated.resources.avatar4
import pillfllow.shared.generated.resources.avatar5
import pillfllow.shared.generated.resources.avatar6
import pillfllow.shared.generated.resources.avatar7
import pillfllow.shared.generated.resources.avatar8

object AvatarMapper {
    fun fromRaw(raw: String?): DrawableResource = when (raw?.trim()) {
        "avatar1" -> Res.drawable.avatar1
        "avatar2" -> Res.drawable.avatar2
        "avatar3" -> Res.drawable.avatar3
        "avatar4" -> Res.drawable.avatar4
        "avatar5" -> Res.drawable.avatar5
        "avatar6" -> Res.drawable.avatar6
        "avatar7" -> Res.drawable.avatar7
        "avatar8" -> Res.drawable.avatar8
        else -> Res.drawable.avatar1
    }
}