package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class GameMain(private val game:Core) : Screen {
    val batch: SpriteBatch
    val font: BitmapFont

    init {
        batch = SpriteBatch()
        font = BitmapFont()
        font.color = Color.BLACK
        font.data.setScale(10f)
    }

    override fun render(delta: Float) {
        batch.begin()
        Gdx.gl.glClearColor(0.9f, 0.1f, 0.5f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        font.draw(batch, "GAME", Gdx.graphics.width / 2f, Gdx.graphics.height / 2f)
        batch.end()
    }

    override fun show() {

    }

    override fun hide() {

    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun resize(width: Int, height: Int) {

    }

    override fun dispose() {

    }
}
