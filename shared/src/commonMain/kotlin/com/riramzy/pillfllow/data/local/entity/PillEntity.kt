package com.riramzy.pillfllow.data.local.entity

import com.riramzy.pillfllow.domain.physics.Vector2D

data class PillEntity(
    val id: String,
    val name: String,
    val colorHex: String,
    val radius: Float,
    var position: Vector2D,
    var velocity: Vector2D = Vector2D(0f, 0f)
)