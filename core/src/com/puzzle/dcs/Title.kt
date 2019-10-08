package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator

class Title(private val game: Core) : Screen {
    private val batch: SpriteBatch
    private val font: BitmapFont
    private val fontGenerator: FreeTypeFontGenerator
    private val bitmapFont: BitmapFont

    init {
        batch = SpriteBatch()
        font = BitmapFont()
        font.color = Color.WHITE
        font.data.setScale(10f)

        //フォント生成
        var file = Gdx.files.internal("fonts/junegull rg.ttf")
        fontGenerator = FreeTypeFontGenerator(file)
        var param = FreeTypeFontGenerator.FreeTypeFontParameter()
        param.size = 200
        param.color = Color.ORANGE
        param.incremental = true
        bitmapFont = fontGenerator.generateFont(param)
    }

    override fun render(delta: Float) {
        if (Gdx.input.justTouched()) {
            game.screen=GameMain(game) //画面遷移
        }

        batch.begin()
        Gdx.gl.glClearColor(0.1f, 0.25f, 0.2f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        //font.draw(batch, "TITLE", Gdx.graphics.width / 2f, Gdx.graphics.height / 2f)
        bitmapFont.draw(batch,"MisPuzzle", Gdx.graphics.width / 10f * 1f, Gdx.graphics.height / 10f *7)
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