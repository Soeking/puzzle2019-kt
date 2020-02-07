package com.puzzle.dcs.type

import com.badlogic.gdx.math.Vector2

enum class Scale(val scale: Vector2) {
    EAST(Vector2(1f, 0f)),
    NORTH(Vector2(0f, 1f)),
    WEST(Vector2(-1f, 0f)),
    SOUTH(Vector2(0f, -1f)),
    ELSE(Vector2(0f, 0f))
}

fun Scale.mul(value: Float) = Vector2(this.scale.x * value, this.scale.y * value)