package com.puzzle.dcs.type

import com.badlogic.gdx.graphics.g2d.Sprite

open class MovableBlock : Block() {
    val gravityID = 0
    var gravity = 0
    var rotate = 3

    override fun getSprite(): Sprite {
        return Sprite()
    }
}