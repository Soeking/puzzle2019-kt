package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.audio.Sound
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
import com.badlogic.gdx.utils.GdxRuntimeException
import com.google.gson.Gson
import java.lang.Exception
import kotlin.math.max
import kotlin.math.min

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
    private val wallPixmap: Pixmap
    private val squarePixmap: Pixmap
    private val trianglePixmap: Pixmap
    private val ladderPixmap: Pixmap
    private val playerPixmap: Pixmap
    private val goalPixmap: Pixmap
    private val changePixmap: Pixmap
    private val onePixel: Int
    private val previewPixel: Int
    private val fontGenerator: FreeTypeFontGenerator
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
    private val soundDisposed: Sound
    private val soundDisposed2: Sound

    private val cachePath: String

    init {
        cachePath = Gdx.files.absolute("${Gdx.files.local("").file().parent}/cache/").path()

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

        fontGenerator2 = FreeTypeFontGenerator(Gdx.files.internal("fonts/meiryo.ttc"))
        val param2 = FreeTypeFontGenerator.FreeTypeFontParameter()
        param2.size = Gdx.graphics.height / 20
        param2.color = Color.RED
        param2.incremental = true
        bitmapFont2 = fontGenerator2.generateFont(param2)

        //stage preview start
        onePixel = (Gdx.graphics.height / 5.0 / previewWidthAndHeight * 2.0).toInt()
        previewPixel = (Gdx.graphics.height / 5.0 * 2).toInt()

        wall = Texture(Gdx.files.internal("images/puzzle cube.png"))
        square = Texture(Gdx.files.internal("images/puzzle cubepattern.png"))
        triangle = Texture(Gdx.files.internal("images/puzzle cubepatternT.png"))
        ladder = Texture(Gdx.files.internal("images/ladder (2).png"))
        player = Texture(Gdx.files.internal("images/player.png"))
        goal = Texture(Gdx.files.internal("images/warphole.png"))
        change = Texture(Gdx.files.internal("images/change.png"))
        wall.textureData.prepare()
        square.textureData.prepare()
        triangle.textureData.prepare()
        ladder.textureData.prepare()
        player.textureData.prepare()
        goal.textureData.prepare()
        change.textureData.prepare()
        wallPixmap = wall.textureData.consumePixmap();
        squarePixmap = square.textureData.consumePixmap()
        trianglePixmap = triangle.textureData.consumePixmap()
        ladderPixmap = ladder.textureData.consumePixmap()
        playerPixmap = player.textureData.consumePixmap()
        goalPixmap = goal.textureData.consumePixmap()
        changePixmap = change.textureData.consumePixmap()
        wall.textureData.disposePixmap()
        square.textureData.disposePixmap()
        triangle.textureData.disposePixmap()
        ladder.textureData.disposePixmap()
        player.textureData.disposePixmap()
        goal.textureData.disposePixmap()
        change.textureData.disposePixmap()
        wall.dispose()
        square.dispose()
        triangle.dispose()
        ladder.dispose()
        player.dispose()
        goal.dispose()
        change.dispose()

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
        stageSelectMaxX = min(0, Gdx.graphics.width - Gdx.graphics.height / 5 * 2 * ((stageSelectFile.size + 1) / 2))

        firstTouch = null
        isTap = false
        checkTap = true

        val th = createPreviewThread(this)
        th.start()
//        createPreview()

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
        //create frame end

        //prepare sound

        sound = Gdx.audio.newSound(Gdx.files.internal("sounds/cymbal.mp3"))
        soundDisposed = Gdx.audio.newSound(Gdx.files.internal("sounds/09_delicious_y_kapo_2_y_44_c4.wav"))
        soundDisposed2 = Gdx.audio.newSound(Gdx.files.internal("sounds/VO_YS_OVTAK2.rwav.wav"))

        //prepare sound finish

        val mu = InputMultiplexer()
        mu.addProcessor(Touch())
//        mu.addProcessor(stage)
        Gdx.input.inputProcessor = mu
    }


    class createPreviewThread(private val stageselect: StageSelect) : Thread() {
        override fun run() {
            stageselect.createPreview()

//            stageselect.wallPixmap.dispose()
//            stageselect.squarePixmap.dispose()
//            stageselect.trianglePixmap.dispose()
//            stageselect.ladderPixmap.dispose()
//            stageselect.playerPixmap.dispose()
//            stageselect.goalPixmap.dispose()
//            stageselect.changePixmap.dispose()

            Gdx.app.log("thread", "createPreviewThread is dead")

            super.run()
        }
    }

    fun createPreview() {
        stageSelectFile.forEach() {
            if (it.exists()) {
                try {
                    val pixmap = PixmapIO.readCIM(Gdx.files.absolute("${cachePath}/${it.name().substring(0, it.name().length - 5)}"))
                    stageSelectImage.add(pixmap)
                } catch (e: Exception) {
                    val stageData: StageData = json.fromJson(it.readString(), StageData::class.java)
                    val pixmap = Pixmap(previewPixel, previewPixel, Pixmap.Format.RGBA8888)
//                    pixmap.setColor(0f, 0f, 0f, 0f)
//                    pixmap.fill()
                    stageData.wall.forEach {
                        drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), wallPixmap, 0)
                    }
                    stageData.square.forEach {
                        drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), squarePixmap, 0)
                    }
                    stageData.triangle.forEach {
                        drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), trianglePixmap, it.rotate)
                    }
                    stageData.ladder.forEach {
                        drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), ladderPixmap, it.rotate)
                    }
                    stageData.start.let {
                        drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), playerPixmap, it.gravity + 1)
                    }
                    stageData.goal.let {
                        drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), goalPixmap, it.gravity + 1)
                    }
                    stageData.gravityChange.forEach {
                        drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), squarePixmap, it.setGravity + 1)
                        drawPixmap(pixmap, it.x.toInt(), it.y.toInt(), changePixmap, it.setGravity + 1)
                    }
                    stageSelectImage.add(pixmap)
//                stageSelectImageTexture.add(Texture(pixmap))
//                stageSelectImageTexture[stageSelectImageTexture.size-1].draw(pixmap, 0, 0)
//                pixmap.dispose()
                }
            } else {
                dispose()
                game.screen = StageSelect(game)
            }
        }
    }

    private fun drawPixmap(pixmap: Pixmap, x1: Int, y1: Int, pixmap2: Pixmap, rotation: Int) {
        if (x1 >= previewWidthAndHeight - 1 || y1 >= previewWidthAndHeight - 1) return
        for (x in 0..onePixel) {
            for (y in 0..onePixel) {
                try {
                    when (rotation % 4) {
                        0 -> pixmap.drawPixel(x + x1 * onePixel, previewPixel - (y + y1 * onePixel), pixmap2.getPixel((pixmap2.width.toFloat() / onePixel * x).toInt(), pixmap2.height - (pixmap2.height.toFloat() / onePixel * y).toInt()))
                        3 -> pixmap.drawPixel(y + x1 * onePixel, previewPixel - ((onePixel - x) + y1 * onePixel), pixmap2.getPixel((pixmap2.width.toFloat() / onePixel * x).toInt(), pixmap2.height - (pixmap2.height.toFloat() / onePixel * y).toInt()))
                        1 -> pixmap.drawPixel((onePixel - y) + x1 * onePixel, previewPixel - (x + y1 * onePixel), pixmap2.getPixel((pixmap2.width.toFloat() / onePixel * x).toInt(), pixmap2.height - (pixmap2.height.toFloat() / onePixel * y).toInt()))
                        2 -> pixmap.drawPixel((onePixel - x) + x1 * onePixel, previewPixel - ((onePixel - y) + y1 * onePixel), pixmap2.getPixel((pixmap2.width.toFloat() / onePixel * x).toInt(), pixmap2.height - (pixmap2.height.toFloat() / onePixel * y).toInt()))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private var finishedTexture: Int = 0

    private var loadingTime: Int = 0
    private var loadingString: String = "loading"

    override fun render(delta: Float) {
        stageList.forEach {
            if (it.first.isPressed) game.screen = PlayScreen(game, it.second)
        }

        if (disposed) {
            disposed = (soundDisposed.play() == -1L)
        }
        if (disposed2) {
            disposed2 = (soundDisposed2.play() == -1L)
        }

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
                    StageLoaded = false
//                    game.screen = PlayScreen(game, stageSelectFile[it].name())
//                    game.screen = LoadingDisplay(game, stageSelectFile[it].name())
//                    game.screen = StageSelect(game)
                    game.screen = Loading(game, stageSelectFile[it].name())
                }
            }
        }
        for (it in (stageSelectImageTexture.size)..(stageSelectFile.size - 1)) {
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

            loadingTime += (Gdx.graphics.deltaTime * 1000.0f / (stageSelectFile.size - stageSelectImageTexture.size)).toInt()
            loadingTime %= 1000
            when (loadingTime) {
                in 0..250 -> loadingString = "loading"
                in 250..500 -> loadingString = "loading."
                in 500..750 -> loadingString = "loading.."
                in 750..1000 -> loadingString = "loading..."
            }
            bitmapFont2.draw(spriteBatch, loadingString, previewPixel * (it / 2).toFloat() + stageSelectX + Gdx.graphics.height / 15.0f, previewPixel - previewPixel * (it % 2).toFloat() + Gdx.graphics.height / 20.0f + Gdx.graphics.height / 10.0f)

            if (isTap && firstTouch != null) {
                if (firstTouch!!.x in (previewPixel * (it / 2).toFloat() + stageSelectX)..(previewPixel * (it / 2).toFloat() + stageSelectX + previewPixel) && firstTouch!!.y in (previewPixel - previewPixel * (it % 2).toFloat())..(previewPixel - previewPixel * (it % 2).toFloat() + previewPixel)) {
                    isTap = false
                    firstTouch = null
                    StageLoaded = false
//                    game.screen = PlayScreen(game, stageSelectFile[it].name())
//                    game.screen = LoadingDisplay(game, stageSelectFile[it].name())
//                    game.screen = StageSelect(game)
                    game.screen = Loading(game, stageSelectFile[it].name())
                }
            }
        }

        if (finishedTexture < stageSelectImage.size) {
            stageSelectImageTexture.add(Texture(stageSelectImage[finishedTexture++]))
//            Gdx.app.log("file", "${cachePath}/${stageSelectFile[finishedTexture - 1].name().substring(0, stageSelectFile[finishedTexture - 1].name().length - 5)}")
            try {
                PixmapIO.writeCIM(Gdx.files.absolute("${cachePath}/${stageSelectFile[finishedTexture - 1].name().substring(0, stageSelectFile[finishedTexture - 1].name().length - 5)}"), stageSelectImage[finishedTexture - 1])
            } catch (e: GdxRuntimeException) {
                Gdx.files.absolute("${cachePath}/").mkdirs()
                PixmapIO.writeCIM(Gdx.files.absolute("${cachePath}/${stageSelectFile[finishedTexture - 1].name().substring(0, stageSelectFile[finishedTexture - 1].name().length - 5)}"), stageSelectImage[finishedTexture - 1])
            }
            stageSelectImage[finishedTexture - 1].dispose()
        }

        spriteBatch.end()

//        stage.act(Gdx.graphics.deltaTime)
//        stage.draw()

        if (game.screen != this) {
            remove()
        }
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
//        stage.dispose()
//        spriteBatch.dispose()
//        sound.dispose()
    }

    override fun dispose() {
//        sound.dispose()
//        spriteBatch.dispose()
//        stage.dispose()
    }

    private fun remove() {
        wallPixmap.dispose()
        squarePixmap.dispose()
        trianglePixmap.dispose()
        ladderPixmap.dispose()
        playerPixmap.dispose()
        goalPixmap.dispose()
        changePixmap.dispose()
        fontGenerator.dispose()
        fontGenerator2.dispose()
        bitmapFont.dispose()
        bitmapFont2.dispose()
        stageSelectImage.clear()
        stageSelectImageTexture.forEach {
            it.textureData.disposePixmap()
            it.dispose()
        }
        stageSelectImageTexture.clear()
        stageSelectFile.clear()
        spriteBatch.dispose()
        sound.dispose()
        soundDisposed.dispose()
        soundDisposed2.dispose()

        alreadyRemoved = true
    }

    private var alreadyRemoved: Boolean = false;

    protected fun finalize() {
        Gdx.app.log("finalize", "StageSelect is disposed")
        if (!alreadyRemoved) {
            wallPixmap.dispose()
            squarePixmap.dispose()
            trianglePixmap.dispose()
            ladderPixmap.dispose()
            playerPixmap.dispose()
            goalPixmap.dispose()
            changePixmap.dispose()
            fontGenerator.dispose()
            fontGenerator2.dispose()
            bitmapFont.dispose()
            bitmapFont2.dispose()
            stageSelectImage.clear()
            stageSelectImageTexture.forEach {
                it.textureData.disposePixmap()
                it.dispose()
            }
            stageSelectImageTexture.clear()
            stageSelectFile.clear()
            spriteBatch.dispose()
            sound.dispose()
            soundDisposed.dispose()
            soundDisposed2.dispose()
            disposed2 = true
        }
    }
}