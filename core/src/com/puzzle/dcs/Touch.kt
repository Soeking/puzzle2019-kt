package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.input.GestureDetector.GestureListener


class Touch : GestureListener {

    override fun touchDown(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        Gdx.app.log("Touch", "${x}, ${y}, ${pointer}, ${button}")
        return false
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        Gdx.app.log("tap", "${x}, ${y}, ${count}, ${button}")
        return false
    }

    override fun longPress(x: Float, y: Float): Boolean {
        Gdx.app.log("longPress", "${x}, ${y}")
        return false
    }

    override fun fling(velocityX: Float, velocityY: Float, button: Int): Boolean {
        Gdx.app.log("fling", "${velocityX}, ${velocityY}, ${button}")
        return false
    }

    override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
        Gdx.app.log("pan", "${x}, ${y}, ${deltaX}, ${deltaY}")
        return false
    }

    override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        Gdx.app.log("panStop", "${x}, ${y}, ${pointer}, ${button}")
        return false
    }

    override fun zoom(originalDistance: Float, currentDistance: Float): Boolean {
        Gdx.app.log("zoom", "${originalDistance}, ${currentDistance}")
        return false
    }

    override fun pinch(initialFirstPointer: Vector2, initialSecondPointer: Vector2, firstPointer: Vector2, secondPointer: Vector2): Boolean {
        Gdx.app.log("pinch", "${initialFirstPointer}, ${initialSecondPointer}, ${firstPointer}, ${secondPointer}")
        return false
    }

    override fun pinchStop() {

    }
}