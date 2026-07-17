package com.riramzy.pillfllow.domain.physics

import com.riramzy.pillfllow.data.local.entity.PillEntity
import com.riramzy.pillfllow.utils.checkChuteExit
import com.riramzy.pillfllow.utils.resolveBoundaryCollision
import com.riramzy.pillfllow.utils.resolveInterPillCollision
import kotlin.math.sqrt

data class Vector2D(val x: Float, val y: Float) {
    operator fun plus(other: Vector2D) = Vector2D(x + other.x, y + other.y)
    operator fun minus(other: Vector2D) = Vector2D(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vector2D(x * scalar, y * scalar)

    fun length() = sqrt(x * x + y * y)

    fun normalize(): Vector2D {
        val len = length()
        return if (len > 0f) Vector2D(x / len, y / len) else Vector2D(0f, 0f)
    }

    fun dot(other: Vector2D): Float = this.x * other.x + this.y * other.y
}

class PhysicsEngine(
    private val jarRadius: Float,
    private val jarCenter: Vector2D,
    private val chuteWidth: Float = 120f,
    private val onPillLogged: (String) -> Unit
) {
    private val restitution = 0.45f
    private val friction = 0.98f

    fun update(
        pills: List<PillEntity>,
        tiltX: Float,
        tiltY: Float,
        deltaTime: Float
    ) {
        val gravityScale = 9.81f * 120f
        val gravity = Vector2D(-tiltX * gravityScale, tiltY * gravityScale)

        for (i in pills.indices) {
            val pill = pills[i]
            pill.velocity = (pill.velocity + gravity * deltaTime) * friction
            pill.position += pill.velocity * deltaTime

            resolveBoundaryCollision(pill, jarRadius, jarCenter, chuteWidth, restitution)
            checkChuteExit(pill, jarCenter, jarRadius, onPillLogged)
        }

        resolveInterPillCollision(pills, restitution)
    }
}