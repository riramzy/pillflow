package com.riramzy.pillfllow.domain.physics

import com.riramzy.pillfllow.utils.physics.checkChuteExit
import com.riramzy.pillfllow.utils.physics.resolveBoundaryCollision
import com.riramzy.pillfllow.utils.physics.resolveInterPillCollision

class PhysicsEngine(
    private val jarRadius: Float,
    private val jarCenter: Vector2D,
    private val chuteWidth: Float = 120f,
    private val onPillLogged: (String) -> Unit,
    private val onCollision: (() -> Unit)? = null
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

        val exitedPills = mutableListOf<String>()

        for (i in pills.indices) {
            val pill = pills.getOrNull(i) ?: continue

            pill.velocity = (pill.velocity + gravity * deltaTime) * friction
            pill.position += pill.velocity * deltaTime

            resolveBoundaryCollision(pill, jarRadius, jarCenter, chuteWidth, restitution, onCollision)

            checkChuteExit(pill, jarCenter, jarRadius) { loggedId ->
                exitedPills.add(loggedId)
            }
        }

        resolveInterPillCollision(pills, restitution, onCollision)
        exitedPills.forEach { onPillLogged(it) }
    }
}