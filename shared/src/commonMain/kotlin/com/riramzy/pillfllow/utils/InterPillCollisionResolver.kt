package com.riramzy.pillfllow.utils

import com.riramzy.pillfllow.data.local.entity.PillEntity

fun resolveInterPillCollision(
    pills: List<PillEntity>,
    restitution: Float
) {
    for (i in pills.indices) {
        for (j in i + 1 until pills.size) {
            val pill1 = pills[i]
            val pill2 = pills[j]
            val delta = pill2.position - pill1.position
            val distance = delta.length()
            val targetDistance = pill1.radius + pill2.radius

            if (distance < targetDistance) {
                val normal = delta.normalize()
                val overlap = targetDistance - distance
                pill1.position -= normal * (overlap * 0.5f)
                pill2.position += normal * (overlap * 0.5f)

                val relativeVelocity = pill2.velocity - pill1.velocity
                val normalVelocity = relativeVelocity.dot(normal)

                if (normalVelocity < 0) {
                    val impulse = -(1f + restitution) * normalVelocity / 2f
                    val impulseVector = normal * impulse
                    pill1.velocity -= impulseVector
                    pill2.velocity += impulseVector
                }
            }

        }
    }
}