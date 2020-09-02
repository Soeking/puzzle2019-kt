package com.puzzle.dcs.type

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.puzzle.dcs.Core

class Start:Block() {
    companion object {
        val sprite = Sprite(Texture(Gdx.files.internal("images/player.png"))).apply {
            setOrigin(0f, 0f)
            setScale(Core.gridSize2 / width / 1.5f)
            setOrigin(width / 2f, height / 2f)
        }
    }

    override fun getSprite():Sprite {
        return sprite
    }
}