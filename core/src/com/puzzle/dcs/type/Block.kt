package com.puzzle.dcs.type

import com.badlogic.gdx.graphics.g2d.Sprite

open class Block {
    var x: Float = 0f
    var y: Float = 0f

    open fun getSprite(): Sprite {
        return Sprite()
    }
}