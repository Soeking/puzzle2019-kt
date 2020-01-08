package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Thread.sleep

class Loading(private val game: Core, private val fileName: String) : Screen {
    private val spriteBatch = SpriteBatch()
    private lateinit var stage: PlayScreen
    private val playerSprite: Sprite
    private val gridSize: Float

    private var loadFinish: Boolean = false
    private var loadstarttime: Long = System.currentTimeMillis()
    private var loadendtime: Long = 0

    init {
        playerSprite = Sprite(Texture(Gdx.files.internal("images/player.png")))

        gridSize = Gdx.graphics.width / 10.0f

        playerSprite.setOrigin(0.0f, 0.0f)
        playerSprite.setScale(gridSize / playerSprite.width / 1.5f)
        playerSprite.setOrigin(playerSprite.width / 2.0f, playerSprite.height / 2.0f)
    }

    override fun render(delta: Float) {
        if (!loadFinish) {
            GlobalScope.launch {
                while (!loadFinish || System.currentTimeMillis() - loadendtime < 1000) {
                    var first = System.currentTimeMillis() - loadstarttime
                    var end = System.currentTimeMillis() - loadendtime
                    GlobalScope.launch(Dispatchers.Unconfined) {
                        if (first < 1000) {
                            Gdx.gl.glClearColor(Math.min(1.0f, first / 1000.0f), Math.min(0.5f, first / 2000.0f), Math.min(0.5f, first / 2000.0f), 1.0f)
                        } else if (end < 1000) {
                            Gdx.gl.glClearColor(Math.max(0.0f, 1.0f - end / 1000.0f), Math.max(0.0f, 0.5f - end / 2000.0f), Math.max(0.0f, 0.5f - end / 2000.0f), 1.0f)
                        } else {
                            Gdx.gl.glClearColor(1.0f, 0.5f, 0.5f, 1.0f)
                            spriteBatch.begin()
                            playerSprite.setPosition(Gdx.graphics.width - gridSize - playerSprite.width / 2f, gridSize - playerSprite.height / 2f)//, )
                            playerSprite.rotation = first / 1000.0f * -360
                            playerSprite.draw(spriteBatch)
                            spriteBatch.end()
                        }
                    }.join()
                    sleep(16);
                }
                game.screen = stage
            }
            stage = PlayScreen(game, fileName)
            loadendtime = System.currentTimeMillis()
            loadFinish = true
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