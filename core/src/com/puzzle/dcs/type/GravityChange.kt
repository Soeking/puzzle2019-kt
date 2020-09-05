package com.puzzle.dcs.type

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.puzzle.dcs.Core

class GravityChange : MovableBlock() {
    var setGravity = 3

    companion object {
        var sprite = Sprite(Texture(Gdx.files.internal("images/puzzle cubepattern.png"))).apply {
            setOrigin(0f, 0f)
            setScale(Core.gridSize2 / width)
            setOrigin(width / 2f, height / 2f)
        }
        var changeSprite = Sprite(Texture(Gdx.files.internal("images/change.png"))).apply {
            setOrigin(0f, 0f)
            setScale(Core.gridSize2 / width)
            setOrigin(width / 2f, height / 2f)
        }

        fun update() {
            sprite = Sprite(Texture(Gdx.files.internal("images/puzzle cubepattern.png"))).apply {
                setOrigin(0f, 0f)
                setScale(Core.gridSize2 / width)
                setOrigin(width / 2f, height / 2f)
            }
            changeSprite = Sprite(Texture(Gdx.files.internal("images/change.png"))).apply {
                setOrigin(0f, 0f)
                setScale(Core.gridSize2 / width)
                setOrigin(width / 2f, height / 2f)
            }
        }
    }

    override fun getSprite(): Sprite {
        return sprite
    }
}