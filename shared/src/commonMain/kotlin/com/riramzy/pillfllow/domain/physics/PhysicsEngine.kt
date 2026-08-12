package com.riramzy.pillfllow.domain.physics

import com.riramzy.pillfllow.utils.checkChuteExit
import com.riramzy.pillfllow.utils.resolveBoundaryCollision
import com.riramzy.pillfllow.utils.resolveInterPillCollision

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