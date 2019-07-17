package com.puzzle.dcs

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class Title:ApplicationAdapter() {
    internal lateinit var batch: SpriteBatch
    internal lateinit var font:BitmapFont

    override fun create() {
        batch=SpriteBatch()
        font=BitmapFont()
        font.color= Color.WHITE
    }

    override fun render() {
        batch.begin()
        Gdx.gl.glClearColor(0.1f,0.25f,0.2f,0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        font.draw(batch,"TITLE",500f,500f)
        batch.end()
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