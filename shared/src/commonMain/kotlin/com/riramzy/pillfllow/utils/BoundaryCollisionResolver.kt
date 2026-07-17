package com.riramzy.pillfllow.utils

import com.riramzy.pillfllow.data.local.entity.PillEntity
import com.riramzy.pillfllow.domain.physics.Vector2D

fun resolveBoundaryCollision(
    pill: PillEntity,
    jarRadius: Float,
    jarCenter: Vector2D,
    chuteWidth: Float,
    restitution: Float
) {
    val delta = pill.position - jarCenter
    val distance = delta.length()
    val limit = jarRadius - pill.radius

    if (distance > limit) {
        val normal = delta.normalize()
        val isAtChuteOpening = pill.position.y > (jarCenter.y + jarRadius - 45f) &&
                (pill.position.x > jarCenter.x - (chuteWidth / 2f)) &&
                (pill.position.x < jarCenter.x + (chuteWidth / 2f))

        if (!isAtChuteOpening) {
            pill.position -= normal * (distance - limit)
            val dotProduct = pill.velocity.dot(normal)
            pill.velocity = (pill.velocity - normal * 2f * dotProduct) * restitution
        }
    }
}