package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.google.gson.Gson
import java.lang.Exception
import kotlin.math.max
import kotlin.math.min


class StageSelect(private val game: Core) : Screen {
    private val stage: Stage = Stage()
    private val spriteBatch = SpriteBatch()

    private var stageSelectX: Int
    private var oldStageSelectX: Int
    private val stageSelectMaxX: Int
    private var stageSelectImage: ArrayList<Pixmap> = ArrayList()
    private var stageSelectImageTexture: ArrayList<Texture> = ArrayList()
    private val stageSelectFile: ArrayList<FileHandle> = ArrayList()
    private val json = Gson()
    private val wall: Texture
    private val square: Texture
    private val triangle: Texture
    private val ladder: Texture
    private val player: Texture
    private val goal: Texture
    private val change: Texture
    private val onePixel: Int
    private val previewPixel: Int
    private val fontGenerator: FreeTypeFontGenerator = FreeTypeFontGenerator(Gdx.files.internal("fonts/meiryo.ttc"))
    private val bitmapFont: BitmapFont
    private val fontGenerator2: FreeTypeFontGenerator
    private val bitmapFont2: BitmapFont
    private val previewWidthAndHeight: Int = 10

    private var frame: Pixmap
    private var frameTexture: Texture
    private var frame2: Pixmap
    private var frameTexture2: Texture

    private var firstTouch: Vector2?
    private var isTap: Boolean
    private var checkTap: Boolean

    private val sound: Sound

    init {

        // create fonts
        val param = FreeTypeFontGenerator.FreeTypeFontParameter()
        param.size = Gdx.graphics.height / 25
        param.color = Color.BLACK
        param.incremental = true
        bitmapFont = fontGenerator.generateFont(param)

        fontGenerator2 = FreeTypeFontGenerator(Gdx.files.internal("fonts/meiryo.ttc"))
        val param2 = FreeTypeFontGenerator.FreeTypeFontParameter()
        param2.size = Gdx.graphics.height / 20
        param2.color = Color.RED
        param2.incremental = true
        bitmapFont2 = fontGenerator.generateFont(param2)

        //stage preview start
        onePixel = (Gdx.graphics.height / 5.0 / previewWidthAndHeight * 2.0).toInt()
        previewPixel = (Gdx.graphics.height / 5.0 * 2).toInt()

        wall = Texture(Gdx.files.internal("images/puzzle cube.png"))
        square = Texture(Gdx.files.internal("images/puzzle cubepattern.png"))
        triangle = Texture(Gdx.files.internal("images/puzzle cubepatternT.png"))
        ladder = Texture(Gdx.files.internal("images/ladder.png"))
        player = Texture(Gdx.files.internal("images/player.png"))
        goal = Texture(Gdx.files.internal("images/warphole.png"))
        change = Texture(Gdx.files.internal("images/change.png"))

        val files = Gdx.files.internal("stages/").list()

        files.forEach {
            if (it.file().name.endsWith(".json")) {
                stageSelectFile.add(it)
            }
        }

        oldStageSelectX = 0
        stageSelectX = 0
        stageSelectMaxX = min(0, Gdx.graphics.width - Gdx.graphics.height / 5 * 2 * ((stageSelectFile.size + 1) / 2))

        firstTouch = null
        isTap = false
        checkTap = true

        val th = DrawButtonThread(this)
        th.start()

        //stage preview end

        //create frame start
        frame = Pixmap(previewPixel, previewPixel, Pixmap.Format.RGBA8888)
        frame.setColor(0.0f, 0.0f, 0.0f, 0.0f)
        frame.fill()
        frame.setColor(1.0f, 1.0f, 1.0f, 1.0f)
        frame.fillRectangle(0, 0, onePixel, previewPixel)
        frame.fillRectangle(0, 0, previewPixel, onePixel)
        frame.fillRectangle(0, previewPixel - onePixel, previewPixel, onePixel)
        frame.fillRectangle(previewPixel - onePixel, 0, onePixel, previewPixel)
        frameTexture = Texture(frame)

        frame2 = Pixmap(previewPixel, previewPixel, Pixmap.Format.RGBA8888)
        frame2.setColor(0.0f, 0.0f, 0.0f, 0.0f)
        frame2.fill()
        frame2.setColor(1.0f, 0.0f, 0.0f, 1.0f)
        frame2.fillRectangle(0, 0, onePixel, previewPixel)
        frame2.fillRectangle(0, 0, previewPixel, onePixel)
        frame2.fillRectangle(0, previewPixel - onePixel, previewPixel, onePixel)
        frame2.fillRectangle(previewPixel - onePixel, 0, onePixel, previewPixel)
        frameTexture2 = Texture(frame2)

        sound = Gdx.audio.newSound(Gdx.files.internal("sounds/cymbal.mp3"))

        val mu = InputMultiplexer()
        mu.addProcessor(Touch())
        Gdx.input.inputProcessor = mu
    }


    class DrawButtonThread(private val stageselect: StageSelect) : Thread() {
        override fun run() {
            stageselect.createPreview()
            super.run()
        }
    }

    fun createPreview() {
        stageSelectFile.forEach { sf ->
            if (sf.exists()) {
                val stageData: StageData = json.fromJson(sf.readString(), StageData::class.java)
                val pixmap = Pixmap(previewPixel, previewPixel, Pixmap.Format.RGBA8888)
                stageData.wall.forEach {
                    drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), wall, 0)
                }
                stageData.square.forEach {
                    drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), square, 0)
                }
                stageData.triangle.forEach {
                    drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), triangle, it.rotate)
                }
                stageData.ladder.forEach {
                    drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), ladder, it.rotate)
                }
                stageData.start.let {
                    drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), player, it.gravity + 1)
                }
                stageData.goal.let {
                    drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), goal, it.gravity + 1)
                }
                stageData.gravityChange.forEach {
                    drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), square, it.setGravity + 1)
                    drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), change, it.setGravity + 1)
                }
                stageSelectImage.add(pixmap)
            } else {
                dispose()
                game.screen = StageSelect(game)
            }
        }
    }

    private fun drawPixmap(pixmap: Pixmap, x1: Int, y1: Int, texture: Texture, rotation: Int) {
        if (x1 >= previewWidthAndHeight - 1 || y1 >= previewWidthAndHeight - 1) return
        texture.textureData.prepare()
        val pixmap2: Pixmap = texture.textureData.consumePixmap()
        for (x in 0..onePixel) {
            for (y in 0..onePixel) {
                try {
                    when (rotation % 4) {
                        0 -> pixmap.drawPixel(x + x1 * onePixel, previewPixel - (y + y1 * onePixel), pixmap2.getPixel((texture.width.toFloat() / onePixel * x).toInt(), texture.height - (texture.height.toFloat() / onePixel * y).toInt()))
                        3 -> pixmap.drawPixel(y + x1 * onePixel, previewPixel - ((onePixel - x) + y1 * onePixel), pixmap2.getPixel((texture.width.toFloat() / onePixel * x).toInt(), texture.height - (texture.height.toFloat() / onePixel * y).toInt()))
                        1 -> pixmap.drawPixel((onePixel - y) + x1 * onePixel, previewPixel - (x + y1 * onePixel), pixmap2.getPixel((texture.width.toFloat() / onePixel * x).toInt(), texture.height - (texture.height.toFloat() / onePixel * y).toInt()))
                        2 -> pixmap.drawPixel((onePixel - x) + x1 * onePixel, previewPixel - ((onePixel - y) + y1 * onePixel), pixmap2.getPixel((texture.width.toFloat() / onePixel * x).toInt(), texture.height - (texture.height.toFloat() / onePixel * y).toInt()))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        pixmap2.dispose()
    }

    private var finishedTexture: Int = 0

    private var loadingTime: Int = 0
    private var loadingString: String = "loading"

    override fun render(delta: Float) {
        spriteBatch.begin()
        Gdx.gl.glClearColor(0.7f, 0.9f, 0.3f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        touch()

        for (it in 0 until stageSelectImageTexture.size) {
            if (isTap && firstTouch != null) {
                if (firstTouch!!.x in (previewPixel * (it / 2).toFloat() + stageSelectX)..(previewPixel * (it / 2).toFloat() + stageSelectX + previewPixel) && firstTouch!!.y in (previewPixel - previewPixel * (it % 2).toFloat())..(previewPixel - previewPixel * (it % 2).toFloat() + previewPixel)) {
                    sound.play()
                    spriteBatch.draw(frameTexture2, previewPixel * (it / 2).toFloat() + stageSelectX, previewPixel - previewPixel * (it % 2).toFloat())
                } else {
                    spriteBatch.draw(frameTexture, previewPixel * (it / 2).toFloat() + stageSelectX, previewPixel - previewPixel * (it % 2).toFloat())
                }
            } else {
                spriteBatch.draw(frameTexture, previewPixel * (it / 2).toFloat() + stageSelectX, previewPixel - previewPixel * (it % 2).toFloat())
            }
            spriteBatch.draw(stageSelectImageTexture[it], previewPixel * (it / 2).toFloat() + stageSelectX, previewPixel - previewPixel * (it % 2).toFloat())
            bitmapFont.draw(spriteBatch, stageSelectFile[it].name().substring(0, stageSelectFile[it].name().length - 5), previewPixel * (it / 2).toFloat() + stageSelectX, previewPixel - previewPixel * (it % 2).toFloat() + Gdx.graphics.height / 25.0f)

            if (isTap && firstTouch != null) {
                if (firstTouch!!.x in (previewPixel * (it / 2).toFloat() + stageSelectX)..(previewPixel * (it / 2).toFloat() + stageSelectX + previewPixel) && firstTouch!!.y in (previewPixel - previewPixel * (it % 2).toFloat())..(previewPixel - previewPixel * (it % 2).toFloat() + previewPixel)) {
                    isTap = false
                    firstTouch = null
                    sound.dispose()
                    game.screen = PlayScreen(game, stageSelectFile[it].name())
                }
            }
        }
        for (it in stageSelectImageTexture.size until stageSelectFile.size) {
            spriteBatch.draw(frameTexture, previewPixel * (it / 2).toFloat() + stageSelectX, previewPixel - previewPixel * (it % 2).toFloat())
            loadingTime += (Gdx.graphics.deltaTime * 1000.0f / (stageSelectFile.size - stageSelectImageTexture.size)).toInt()
            loadingTime %= 1000
            when (loadingTime) {
                in 0..250 -> loadingString = "loading"
                in 250..500 -> loadingString = "loading."
                in 500..750 -> loadingString = "loading.."
                in 750..1000 -> loadingString = "loading..."
            }
            bitmapFont2.draw(spriteBatch, loadingString, previewPixel * (it / 2).toFloat() + stageSelectX + Gdx.graphics.height / 15.0f, previewPixel - previewPixel * (it % 2).toFloat() + Gdx.graphics.height / 20.0f + Gdx.graphics.height / 10.0f)
        }

        if (finishedTexture < stageSelectImage.size) {
            stageSelectImageTexture.add(Texture(stageSelectImage[finishedTexture++]))
            stageSelectImage[finishedTexture - 1].dispose()
        }

        spriteBatch.end()

    }

    private fun touch() {
        if (touchCoordinate[0] != null) {
            if (firstTouch == null) {
                firstTouch = Vector2(touchCoordinate[0]!!.x, touchCoordinate[0]!!.y)
                oldStageSelectX = stageSelectX
            }
            if (firstTouch != null) {
                if (checkTap) {
                    if (!(firstTouch!!.x - touchCoordinate[0]!!.x in -2.0f..2.0f && firstTouch!!.y - touchCoordinate[0]!!.y in -2.0f..2.0f)) {
                        checkTap = false
                    }
                } else {
                    stageSelectX = min(0, max(stageSelectMaxX, oldStageSelectX + (touchCoordinate[0]!!.x - firstTouch!!.x).toInt()))
                }
            }
        } else {
            if (firstTouch != null) {
                if (checkTap) isTap = true
                else {
                    isTap = false
                    checkTap = true
                    firstTouch = null
                }
            }
        }
    }

    override fun resize(width: Int, height: Int) {

    }

    override fun show() {

    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun hide() {
        stage.dispose()
        spriteBatch.dispose()
        sound.dispose()
        wall.dispose()
        square.dispose()
        triangle.dispose()
        ladder.dispose()
        player.dispose()
        goal.dispose()
        change.dispose()
        frame.dispose()
        frameTexture.dispose()
        frame2.dispose()
        frameTexture2.dispose()
    }

    override fun dispose() {
        sound.dispose()
        spriteBatch.dispose()
        stage.dispose()
    }
}