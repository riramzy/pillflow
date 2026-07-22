package com.riramzy.pillfllow.data.local.entity

import androidx.compose.ui.graphics.Color
import com.riramzy.pillfllow.domain.physics.Vector2D
import com.riramzy.pillfllow.utils.PillShape

data class PillEntity(
    val id: String,
    val name: String,
    val color: Color,
    val shape: PillShape = PillShape.CIRCLE,
    val radius: Float,
    var position: Vector2D,
    var velocity: Vector2D = Vector2D(0f, 0f)
)