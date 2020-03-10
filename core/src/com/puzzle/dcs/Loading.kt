package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Thread.sleep
import kotlin.math.*

class Loading(private val game: Core, private val fileName: String) : Screen {
    private val spriteBatch = SpriteBatch()
    private lateinit var stage: PlayScreen
    private val playerSprite: Sprite = Sprite(Texture(Gdx.files.internal("images/player.png")))
    private val gridSize: Float = Gdx.graphics.width / 10.0f

    private var loadFinish: Boolean = false
    private var loadstarttime: Long = System.currentTimeMillis()
    private var loadendtime: Long = 0

    init {

        playerSprite.setOrigin(0.0f, 0.0f)
        playerSprite.setScale(gridSize / playerSprite.width / 1.5f)
        playerSprite.setOrigin(playerSprite.width / 2.0f, playerSprite.height / 2.0f)
    }

    var first: Boolean = true;

    override fun render(delta: Float) {
        if (first) {
            GlobalScope.launch {
                GlobalScope.launch {
                    while (!loadFinish || System.currentTimeMillis() - loadendtime < 1000) {
                        val first = System.currentTimeMillis() - loadstarttime
                        val end = System.currentTimeMillis() - loadendtime
                        GlobalScope.launch(Dispatchers.Unconfined) {
                            Gdx.app.postRunnable {
                                if (first < 1000) {
                                    Gdx.gl.glClearColor(min(1.0f, first / 1000.0f), min(0.5f, first / 2000.0f), min(0.5f, first / 2000.0f), 1.0f)
                                    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
                                } else if (end < 1000) {
                                    Gdx.gl.glClearColor(max(0.0f, 1.0f - end / 1000.0f), max(0.0f, 0.5f - end / 2000.0f), max(0.0f, 0.5f - end / 2000.0f), 1.0f)
                                    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
                                } else {
                                    Gdx.gl.glClearColor(1.0f, 0.5f, 0.5f, 1.0f)
                                    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
                                    spriteBatch.begin()
                                    playerSprite.setPosition(Gdx.graphics.width - gridSize - playerSprite.width / 2f, gridSize - playerSprite.height / 2f)//, )
                                    playerSprite.rotation = first / 1000.0f * -360
                                    playerSprite.draw(spriteBatch)
                                    spriteBatch.end()
                                }
                            }
//                        Gdx.app.log("loading", "first: ${first}, end: ${end}, currentTime: ${System.currentTimeMillis()}")
                        }.join()
                        sleep(16);
                    }
                    game.screen = stage
                    spriteBatch.dispose()
                }

                GlobalScope.launch(Dispatchers.Unconfined) {
                    delay(2500L)
                    Gdx.app.postRunnable {
                        stage = PlayScreen(game, fileName)
                        loadendtime = System.currentTimeMillis()
                        loadFinish = true
                    }
                }.join()
            }
            first = false
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
        spriteBatch.dispose()
    }

    override fun dispose() {

    }
}