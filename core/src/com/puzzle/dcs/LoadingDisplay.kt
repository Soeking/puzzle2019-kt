package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import kotlin.concurrent.thread

class LoadingDisplay(private val game: Core, private val fileName: String) : Screen {
    private val spriteBatch = SpriteBatch()
    private lateinit var stage: PlayScreen
    private val playerSprite = Sprite(Texture(Gdx.files.internal("images/player.png")))
    private val gridSize = Gdx.graphics.width / 10.0f

    private var first: Int = 0
    private var f: Boolean = true
    private var finish: Int = 0
    private var ff: Boolean = false

    init {
        playerSprite.setOrigin(0.0f, 0.0f)
        playerSprite.setScale(gridSize / playerSprite.width / 1.5f)
        playerSprite.setOrigin(playerSprite.width / 2.0f, playerSprite.height / 2.0f)
        DrawLoading(game, fileName, this).start()
        MakeStage(game, fileName, this).start()
    }

    class MakeStage(private val game: Core, private val fileName: String, private val dis: LoadingDisplay) : Thread() {
        override fun run() {
            sleep(1000)
            Gdx.app.postRunnable {
                dis.stageInit()
            }
            super.run()
        }
    }

    class DrawLoading(private val game: Core, private val fileName: String, private val dis: LoadingDisplay) : Thread() {
        override fun run() {
            while (dis.first - dis.finish < 1000 || dis.finish == 0) {
                Gdx.app.postRunnable {
                    if (!StageLoaded || dis.first <= 1000) Gdx.gl.glClearColor(Math.min(1.0f, dis.first / 1000.0f), Math.min(0.5f, dis.first / 2000.0f), Math.min(0.5f, dis.first / 2000.0f), 1.0f)
                    else if (!dis.ff) {
                        Gdx.gl.glClearColor(Math.min(1.0f, dis.first / 1000.0f), Math.min(0.5f, dis.first / 2000.0f), Math.min(0.5f, dis.first / 2000.0f), 1.0f)
                        dis.finish = dis.first
                        dis.ff = true
                    } else {
                        Gdx.gl.glClearColor(Math.max(0.0f, 1.0f - (dis.first - dis.finish) / 1000.0f), Math.max(0.0f, 0.5f - (dis.first - dis.finish) / 2000.0f), Math.max(0.0f, 0.5f - (dis.first - dis.finish) / 2000.0f), 1.0f)
                    }
                    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
                }

                Gdx.app.postRunnable {
                    dis.spriteBatch.begin()

                    if (dis.first >= 1000 && dis.finish == 0) {
                        dis.playerSprite.setPosition(Gdx.graphics.width - dis.gridSize - dis.playerSprite.width / 2f, dis.gridSize - dis.playerSprite.height / 2f)//, )
                        dis.playerSprite.rotation = dis.first / 1000.0f * -360
                        dis.playerSprite.draw(dis.spriteBatch)
                    }

                    dis.spriteBatch.end()
                }

                Gdx.app.log("Loading", "${dis.f}, ${dis.first}, ${dis.ff}, ${dis.finish}, ${StageLoaded}")

                if (!dis.f) dis.first += (Gdx.graphics.deltaTime * 1000).toInt()
                else {
                    dis.f = false
                }

                sleep(16)
            }

            Gdx.app.postRunnable {
                game.screen = dis.stage
            }
        }
    }

    override fun render(delta: Float) {
//        Draw()
    }

    private fun stageInit() {
        stage = PlayScreen(game, fileName)
        Gdx.app.log("thread", "stage prepared")
//        while (first - finish < 1000) {
//
//        }
//        game.screen = stage
    }

    private fun draw() {
        if (!StageLoaded || first <= 1000) Gdx.gl.glClearColor(Math.min(1.0f, first / 1000.0f), Math.min(0.5f, first / 2000.0f), Math.min(0.5f, first / 2000.0f), 1.0f)
        else if (!ff) {
            Gdx.gl.glClearColor(Math.min(1.0f, first / 1000.0f), Math.min(0.5f, first / 2000.0f), Math.min(0.5f, first / 2000.0f), 1.0f)
            finish = first
            ff = true
        } else {
            Gdx.gl.glClearColor(Math.max(0.0f, 1.0f - (first - finish) / 1000.0f), Math.max(0.0f, 0.5f - (first - finish) / 2000.0f), Math.max(0.0f, 0.5f - (first - finish) / 2000.0f), 1.0f)
            if (first - finish >= 1000) {
                if (stage != null) game.screen = stage
            }
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        spriteBatch.begin()

        if (first >= 1000 && finish == 0) {
            playerSprite.setPosition(Gdx.graphics.width - gridSize - playerSprite.width / 2f, gridSize - playerSprite.height / 2f)//, )
            playerSprite.rotation = first / 1000.0f * -360
            playerSprite.draw(spriteBatch)
        }

//        if (first >= 2000) StageLoaded = true

        spriteBatch.end()

        Gdx.app.log("Loading", "${f}, ${first}, ${ff}, ${finish}, ${StageLoaded}")

        if (!f) first += (Gdx.graphics.deltaTime * 1000).toInt()
        else {
            f = false
            thread { MakeStage(game, fileName, this).start() }
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

    }

    override fun dispose() {

    }
}