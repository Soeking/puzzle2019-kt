package com.puzzle.dcs

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator

class Font : ApplicationListener {
    var fontGenerator: FreeTypeFontGenerator
    var bitmapFont: BitmapFont

    var batch: SpriteBatch


    override fun create() {
        batch = SpriteBatch()

        var file = Gdx.files.local("font/NotoSansCJKjp-Bold.otf")
        fontGenerator = FreeTypeFontGenerator(file)

    }

    override fun resize(width: Int, height: Int) {

    }

    override fun render() {

    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun dispose() {

    }
}