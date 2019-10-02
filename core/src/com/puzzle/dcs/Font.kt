package com.puzzle.dcs

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator

class Font : ApplicationListener {
    lateinit var fontGenerator: FreeTypeFontGenerator
    lateinit var bitmapFont: BitmapFont

    lateinit var batch: SpriteBatch


    override fun create() {
        batch = SpriteBatch()

        var file = Gdx.files.local("font/NotoSansCJKjp-Bold.otf")
        fontGenerator = FreeTypeFontGenerator(file)
        var param = FreeTypeFontGenerator.FreeTypeFontParameter()
        param.size = 32

        bitmapFont = fontGenerator.generateFont(param)
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