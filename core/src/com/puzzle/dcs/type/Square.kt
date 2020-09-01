package com.puzzle.dcs.type

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.puzzle.dcs.Core

class Square : MovableBlock() {
    companion object {
        val sprite = Sprite(Texture(Gdx.files.internal("images/puzzle cubepattern.png"))).apply {
            setOrigin(0.0f, 0.0f)
            setScale(Core.gridSize2 / width)
            setOrigin(width / 2.0f, height / 2.0f)
        }
    }

    override fun getSprite():Sprite {
        return sprite
    }
}