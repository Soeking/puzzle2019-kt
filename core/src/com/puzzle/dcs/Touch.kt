package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.math.Vector2

public var touchCoordinate: Array<Vector2?> = arrayOfNulls(5)
public var ThreadEnabled: Boolean = false
public var StageLoaded = false

class Touch : InputProcessor {

    override fun keyDown(keycode: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (pointer in 0..4) {
            touchCoordinate[pointer] = Vector2(screenX.toFloat(), Gdx.graphics.height - screenY.toFloat())
            return false
        }
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (pointer in 0..4) {
            if(touchCoordinate[pointer] == null) return false
            touchCoordinate[pointer]!!.x = screenX.toFloat()
            touchCoordinate[pointer]!!.y = Gdx.graphics.height - screenY.toFloat()
            return false
        }
        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (pointer in 0..4) {
            touchCoordinate[pointer] = null
            return false
        }
        return false
    }
}