package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.TextureData
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
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

    public var stageSelectX: Int
    public val stageSelectMaxX: Int
    public var stageSelectImage: ArrayList<Pixmap> = ArrayList()
    public var stageSelectImageTexture: ArrayList<Texture> = ArrayList()
    public val stageSelectFile: ArrayList<FileHandle> = ArrayList()
    public val json = Gson()
    public val wall: Texture
    public val square: Texture
    public val triangle: Texture
    public val ladder: Texture
    public val player: Texture
    public val goal: Texture
    public val change: Texture
    public val onePixel: Int

    init {
        stage = Stage()
        stageList.add(Pair(ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("UI/kari.png"))))), "kari.json"))
        stageList.add(Pair(ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("UI/new.png"))))), "new.json"))
        stageList.add(Pair(ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("UI/saishin.png"))))), "saishin.json"))

        for (i in stageList.indices) {
            stageList[i].first.setPosition(Gdx.graphics.width / 2f, Gdx.graphics.height / 4f * (3 - i))
            stage.addActor(stageList[i].first)
        }

        //stage preview start
        onePixel = (Gdx.graphics.height / 35.0 * 2.0).toInt()

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

        stageSelectFile.add(Gdx.files.internal("stages/kari.json"))
        stageSelectFile.add(Gdx.files.internal("stages/new.json"))
        stageSelectFile.add(Gdx.files.internal("stages/saishin.json"))

//        for (i in 0..(stageSelectFile.size - 1)) {
//            var pixmap: Pixmap = Pixmap(Gdx.graphics.width / 5 * 2, Gdx.graphics.width / 5 * 2, Pixmap.Format.RGBA8888)
//            stageSelectImage.add(pixmap)
//        }

        stageSelectX = 0
        stageSelectMaxX = Math.max(0, Gdx.graphics.width - Gdx.graphics.height / 5 * 2 * ((stageList.size + 1) / 2))

        var th = DrawButtonThread(this)
        th.start()
//        createPreview()

        //stage preview end

        Gdx.input.inputProcessor = stage
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
                var pixmap: Pixmap = Pixmap(Gdx.graphics.height / 5 * 2, Gdx.graphics.height / 5 * 2, Pixmap.Format.RGBA8888)
//                    pixmap.setColor(0f, 0f, 0f, 0f)
//                    pixmap.fill()
                stageData.wall.forEach {
                    drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), wall)
                }
                stageData.square.forEach {
                    drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), square)
                }
                stageData.triangle.forEach {
                    drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), triangle)
                }
                stageData.ladder.forEach {
                    drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), ladder)
                }
                stageData.start.let {
                    drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), player)
                }
                stageData.goal.let {
                    drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), goal)
                }
                stageData.gravityChange.forEach {
                    drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), change)
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

    private fun drawPixmap(pixmap: Pixmap, x1: Int, y1: Int, texture: Texture) {
        if (x1 >= 6 || y1 >= 6) return
        texture.textureData.prepare()
        var pixmap2: Pixmap = texture.textureData.consumePixmap()
        for (x in 0..(onePixel - 1)) {
            for (y in 0..(onePixel - 1)) {
                try {
                    pixmap.drawPixel(x + x1 * onePixel, Gdx.graphics.height / 5 * 2 - (y1 * onePixel + y), pixmap2.getPixel(texture.width / onePixel * x, texture.height - texture.height / onePixel * y))
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

        for (it in 0..(stageSelectImageTexture.size - 1)) {
            spriteBatch.draw(stageSelectImageTexture[it], Gdx.graphics.height / 5 * 2 * (it / 2).toFloat() + stageSelectX, Gdx.graphics.height / 5 * 2 - Gdx.graphics.height / 5 * 2 * (it % 2).toFloat())
        }

        if(finishedTexture < stageSelectImage.size){
            stageSelectImageTexture.add(Texture(stageSelectImage[finishedTexture++]))
            stageSelectImage[finishedTexture-1].dispose()
        }

        spriteBatch.end()

        stage.act(Gdx.graphics.deltaTime)
        stage.draw()
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