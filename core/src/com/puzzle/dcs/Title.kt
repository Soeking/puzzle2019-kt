package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import java.util.*
import kotlin.math.*

class Title(private val game: Core) : Screen {
    private val batch: SpriteBatch = SpriteBatch()
    private val font: BitmapFont = BitmapFont()
    private val titleImage: Sprite
    private var titleMilliseconds: Int
    private val fontGenerator: FreeTypeFontGenerator
    private val bitmapFont: BitmapFont
    private val fontGenerator2: FreeTypeFontGenerator
    private val bitmapFont2: BitmapFont
    private var first: Boolean
    private var colorR: Float
    private var colorG: Float
    private var colorB: Float
    private val version: String = "0.0000000000000000000000000000002"
    private var center1: Float = Gdx.graphics.width / 2.0f
    private var center2: Float = 0.0f

    init {
        font.color = Color.WHITE
        font.data.setScale(10f)

        titleImage = Sprite(Texture(Gdx.files.internal("images/InvertRoom.png")))
        val xSize: Float = Gdx.graphics.width / titleImage.width
        val ySize: Float = Gdx.graphics.height / titleImage.height
        titleImage.setScale(min(xSize, ySize))
        titleImage.setOriginCenter()

        fontGenerator = FreeTypeFontGenerator(Gdx.files.internal("fonts/meiryo.ttc"))
        val param = FreeTypeFontGenerator.FreeTypeFontParameter()
        param.size = Gdx.graphics.width / 25
        param.color = Color.RED
        param.incremental = true
        bitmapFont = fontGenerator.generateFont(param)

        fontGenerator2 = FreeTypeFontGenerator(Gdx.files.internal("fonts/meiryo.ttc"))
        val param2 = FreeTypeFontGenerator.FreeTypeFontParameter()
        param2.size = Gdx.graphics.width / 100
        param2.color = Color.BLUE
        param2.incremental = true
        bitmapFont2 = fontGenerator2.generateFont(param2)

        colorR = 0.1f
        colorG = 0.25f
        colorB = 0.2f

        first = true

        titleMilliseconds = 0
        Core.gridSize2 = min(Gdx.graphics.width / 20f, Gdx.graphics.height * 4f / 45f)
        Core.halfGrid2 = Core.gridSize2 / 2.0f
    }

    override fun render(delta: Float) {
        if (Gdx.input.justTouched() && titleMilliseconds >= 2000) {
            game.screen = GameMain(game)
        }

        batch.begin()

        Gdx.gl.glClearColor(colorR, colorG, colorB, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        if (titleMilliseconds <= 2000) {
            titleImage.setPosition(Gdx.graphics.width / 2.0f - titleImage.width / 2.0f, Gdx.graphics.height / 2.0f - titleImage.height / 2.0f)
            titleImage.setColor(titleImage.color.r, titleImage.color.g, titleImage.color.b, 1.0f - abs(1.0f - titleMilliseconds / 1000.0f))
            titleImage.draw(batch)
        } else if (titleMilliseconds >= 2500) {
            titleImage.setPosition(Gdx.graphics.width / 2.0f - titleImage.width / 2.0f, Gdx.graphics.height / 8.0f * 5.0f - titleImage.height / 2.0f)
            val xSize: Float = Gdx.graphics.width / titleImage.width / 1.5f
            val ySize: Float = Gdx.graphics.height / titleImage.height / 1.5f
            titleImage.setScale(min(xSize, ySize))
            titleImage.setColor(titleImage.color.r, titleImage.color.g, titleImage.color.b, 1.0f)
            titleImage.draw(batch)
            if (titleMilliseconds % 1000 < 500)
                center1 = bitmapFont.draw(batch, "tap screen to start game", Gdx.graphics.width / 2.0f - center1 / 2.0f, Gdx.graphics.height / 4.0f).width
            center2 = bitmapFont2.draw(batch, "Version: $version", Gdx.graphics.width - center2, bitmapFont2.lineHeight).width
        }

        batch.end()

        if (first) {
            first = false
            titleMilliseconds = 0
        } else {
            titleMilliseconds += (Gdx.graphics.deltaTime * 1000.0f).toInt()
        }
        if (titleMilliseconds >= 3500) {
            titleMilliseconds -= 1000
            colorR = Random().nextFloat()
            colorG = Random().nextFloat()
            colorB = Random().nextFloat()
        }
        //Gdx.app.log("times", "${titleMilliseconds}")
    }

    private fun remove() {
        fontGenerator.dispose()
        fontGenerator2.dispose()
        titleImage.texture.dispose()
        font.dispose()
        bitmapFont.dispose()
        bitmapFont2.dispose()
        batch.dispose()
        alreadyRemoved = true
    }

    override fun show() {

    }

    override fun hide() {
        remove()
    }

    override fun resize(width: Int, height: Int) {

    }

    override fun resume() {

    }

    override fun dispose() {

    }

    override fun pause() {

    }

    private var alreadyRemoved: Boolean = false

    protected fun finalize() {
        Gdx.app.log("finalize", "Title is disposed")
        if (!alreadyRemoved) {
            remove()
        }
    }
}