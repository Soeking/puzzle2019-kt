package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class Title(private val game: Core) : Screen {
    private val batch: SpriteBatch
    private val font: BitmapFont

    init {
        batch = SpriteBatch()
        font = BitmapFont()
        font.color = Color.WHITE
        font.data.setScale(10f)
    }

    override fun render(delta: Float) {
        if (Gdx.input.justTouched()) {
            game.screen=GameMain(game) //画面遷移
        }

        batch.begin()
        Gdx.gl.glClearColor(0.1f, 0.25f, 0.2f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        font.draw(batch, "TITLE", Gdx.graphics.width / 2f, Gdx.graphics.height / 2f)
        batch.end()
    }

    override fun show() {

    }

    override fun hide() {

    }

    override fun resize(width: Int, height: Int) {

    }

    override fun resume() {

    }

    override fun dispose() {
        batch.dispose()
        font.dispose()
    }

    override fun pause() {

    }
}