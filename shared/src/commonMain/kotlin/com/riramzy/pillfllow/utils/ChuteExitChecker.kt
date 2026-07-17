package com.riramzy.pillfllow.utils

import com.riramzy.pillfllow.data.local.entity.PillEntity
import com.riramzy.pillfllow.domain.physics.Vector2D

fun checkChuteExit(
    pill: PillEntity,
    jarCenter: Vector2D,
    jarRadius: Float,
    onPillLogged: (String) -> Unit
) {
    val boundaryY = jarCenter.y + jarRadius
    if (pill.position.y >= (boundaryY + pill.radius)) {
        onPillLogged(pill.id)
    }
}