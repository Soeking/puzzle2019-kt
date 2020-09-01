package com.puzzle.dcs.type

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.puzzle.dcs.Core

class Ladder : MovableBlock() {
    companion object {
        val sprite = Sprite(Texture(Gdx.files.internal("images/ladder.png"))).apply {
            setOrigin(0f, 0f)
            setScale(Core.gridSize2 / width)
            setOrigin(width / 2f, height / 2f)
        }
    }

    override fun getSprite():Sprite {
        return sprite
    }
}