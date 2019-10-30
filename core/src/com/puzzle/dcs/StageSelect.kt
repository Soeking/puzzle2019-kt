package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.google.gson.Gson
import java.io.File
import java.lang.Exception

class StageSelect(private val game: Core) : Screen {
    private val stage: Stage
    private val spriteBatch = SpriteBatch()
    private val stageList = mutableListOf<Pair<ImageButton, String>>()

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
    private val fontGenerator: FreeTypeFontGenerator
    private val bitmapFont: BitmapFont
    private val previewWidthAndHeight: Int = 10

    private var firstTouch: Vector2?
    private var isTap: Boolean
    private var checkTap: Boolean

    init {
        stage = Stage()
        stageList.add(Pair(ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("UI/kari.png"))))), "kari.json"))
        stageList.add(Pair(ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("UI/new.png"))))), "new.json"))
        stageList.add(Pair(ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("UI/saishin.png"))))), "saishin.json"))

        for (i in stageList.indices) {
            stageList[i].first.setPosition(Gdx.graphics.width / 2f, Gdx.graphics.height / 4f * (3 - i))
            stage.addActor(stageList[i].first)
        }

        // create fonts
        fontGenerator = FreeTypeFontGenerator(Gdx.files.internal("fonts/meiryo.ttc"))
        val param = FreeTypeFontGenerator.FreeTypeFontParameter()
        param.size = Gdx.graphics.height / 25
        param.color = Color.BLACK
        param.incremental = true
        bitmapFont = fontGenerator.generateFont(param)

        //stage preview start
        onePixel = (Gdx.graphics.height / 5.0 / previewWidthAndHeight * 2.0).toInt()
        previewPixel = (Gdx.graphics.height / 5.0 * 2).toInt()

        wall = Texture(Gdx.files.internal("images/puzzle cube.png"))
        square = Texture(Gdx.files.internal("images/puzzle cubepattern.png"))
        triangle = Texture(Gdx.files.internal("images/puzzle cubepatternT.png"))
        ladder = Texture(Gdx.files.internal("images/ladder (2).png"))
        player = Texture(Gdx.files.internal("images/ball.png"))
        goal = Texture(Gdx.files.internal("images/warphole.png"))
        change = Texture(Gdx.files.internal("images/change.png"))
//        wall.textureData.prepare()
//        square.textureData.prepare()
//        triangle.textureData.prepare()
//        ladder.textureData.prepare()
//        player.textureData.prepare()
//        goal.textureData.prepare()
//        change.textureData.prepare()

//        wall.setScale(Gdx.graphics.height / 25 * 2 / wall.height)
//        square.setScale(Gdx.graphics.height / 25 * 2 / square.height)
//        triangle.setScale(Gdx.graphics.height / 25 * 2 / triangle.height)
//        ladder.setScale(Gdx.graphics.height / 25 * 2 / ladder.height)
//        player.setScale(Gdx.graphics.height / 25 * 2 / player.height)
//        goal.setScale(Gdx.graphics.height / 25 * 2 / goal.height)
//        change.setScale(Gdx.graphics.height / 25 * 2 / change.height)

        val files = Gdx.files.internal("stages/").list()

        files.forEach {
//            Gdx.app.log("files", "${it.file().name}, ${it.file().isFile}, ${it.file().name.endsWith(".json")}")
//            stageSelectFile.add(it)z
            if (it.file().name.endsWith(".json")) {
                stageSelectFile.add(it)
            }
        }

//        stageSelectFile.add(Gdx.files.internal("stages/kari.json"))
//        stageSelectFile.add(Gdx.files.internal("stages/new.json"))
//        stageSelectFile.add(Gdx.files.internal("stages/saishin.json"))

//        for (i in 0..(stageSelectFile.size - 1)) {
//            var pixmap: Pixmap = Pixmap(Gdx.graphics.width / 5 * 2, Gdx.graphics.width / 5 * 2, Pixmap.Format.RGBA8888)
//            stageSelectImage.add(pixmap)
//        }

        oldStageSelectX = 0
        stageSelectX = 0
        stageSelectMaxX = Math.max(0, Gdx.graphics.width - Gdx.graphics.height / 5 * 2 * ((stageSelectFile.size + 1) / 2))

        firstTouch = null
        isTap = false
        checkTap = true

        var th = DrawButtonThread(this)
        th.start()
//        createPreview()

        //stage preview end

        val mu = InputMultiplexer()
        mu.addProcessor(Touch())
//        mu.addProcessor(stage)
        Gdx.input.inputProcessor = mu
    }


    class DrawButtonThread(private val stageselect: StageSelect) : Thread() {
        override fun run() {
            stageselect.createPreview()
            super.run()
        }
    }

    public fun createPreview() {
        stageSelectFile.forEach() {
            if (it.exists()) {
                var stageData: StageData = json.fromJson(it.readString(), StageData::class.java)
                var pixmap: Pixmap = Pixmap(previewPixel, previewPixel, Pixmap.Format.RGBA8888)
//                    pixmap.setColor(0f, 0f, 0f, 0f)
//                    pixmap.fill()
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
                    drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), change, it.setGravity + 1)
                }
                stageSelectImage.add(pixmap)
//                stageSelectImageTexture.add(Texture(pixmap))
//                stageSelectImageTexture[stageSelectImageTexture.size-1].draw(pixmap, 0, 0)
//                pixmap.dispose()
            } else {
                dispose()
                game.screen = StageSelect(game)
            }
        }
    }

    private fun drawPixmap(pixmap: Pixmap, x1: Int, y1: Int, texture: Texture, rotation: Int) {
        if (x1 >= previewWidthAndHeight - 1 || y1 >= previewWidthAndHeight - 1) return
        texture.textureData.prepare()
        var pixmap2: Pixmap = texture.textureData.consumePixmap()
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

    override fun render(delta: Float) {
        stageList.forEach {
            if (it.first.isPressed) game.screen = PlayScreen(game, it.second)
        }

        spriteBatch.begin()
        Gdx.gl.glClearColor(0.7f, 0.9f, 0.3f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        touch()

        for (it in 0..(stageSelectImageTexture.size - 1)) {
            spriteBatch.draw(stageSelectImageTexture[it], previewPixel * (it / 2).toFloat() + stageSelectX, previewPixel - previewPixel * (it % 2).toFloat())
            bitmapFont.draw(spriteBatch, stageSelectFile[it].name().substring(0, stageSelectFile[it].name().length - 5), previewPixel * (it / 2).toFloat() + stageSelectX, previewPixel - previewPixel * (it % 2).toFloat() + Gdx.graphics.height / 25.0f)

            if (isTap && firstTouch != null) {
                if (firstTouch!!.x in (previewPixel * (it / 2).toFloat() + stageSelectX)..(previewPixel * (it / 2).toFloat() + stageSelectX + previewPixel) && firstTouch!!.y in (previewPixel - previewPixel * (it % 2).toFloat())..(previewPixel - previewPixel * (it % 2).toFloat() + previewPixel)) {
                    isTap = false
                    firstTouch = null
                    game.screen = PlayScreen(game, stageSelectFile[it].name())
                }
            }

        }

        if (finishedTexture < stageSelectImage.size) {
            stageSelectImageTexture.add(Texture(stageSelectImage[finishedTexture++]))
            stageSelectImage[finishedTexture - 1].dispose()
        }

        spriteBatch.end()

//        stage.act(Gdx.graphics.deltaTime)
//        stage.draw()
    }

    private fun touch() {
        if (touchCoordinate[0] != null) {
            if (firstTouch == null) {
                firstTouch = Vector2(touchCoordinate[0]!!.x, touchCoordinate[0]!!.y)
                oldStageSelectX = stageSelectX
            }
            if (firstTouch != null) {
                if (checkTap) {
                    if (firstTouch!!.x != touchCoordinate[0]!!.x || firstTouch!!.y != touchCoordinate[0]!!.y) {
                        checkTap = false
                    }
                } else {
                    stageSelectX = Math.min(stageSelectMaxX, Math.max(0, oldStageSelectX + (touchCoordinate[0]!!.x - firstTouch!!.x).toInt()))
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
//        Gdx.app.log("touch", "${isTap}, ${checkTap}, ${firstTouch}")
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

    }

    override fun dispose() {
        spriteBatch.dispose()
        stage.dispose()
    }
}