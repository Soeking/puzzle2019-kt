package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

class StageSelect(private val game: Core) : Screen {
    private val stage: Stage
    private val spriteBatch = SpriteBatch()
    private val kari: ImageButton
    private val neww: ImageButton
    private val stages = mutableListOf<ImageButton>()

    init {
        stage = Stage()
        kari = ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("UI/kari.png")))))
        neww = ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("UI/new.png")))))
        kari.setPosition(Gdx.graphics.width / 2f, Gdx.graphics.height / 4f * 3)
        neww.setPosition(Gdx.graphics.width / 2f, Gdx.graphics.height / 4f * 1)
        stages.add(kari)
        stages.add(neww)
        stages.forEach {
            stage.addActor(it)
        }
        Gdx.input.inputProcessor = stage
    }

    override fun render(delta: Float) {
        repeat(stages.size) {
            if (stages[it].isPressed) {
                Gdx.app.log("push", "$it")
                game.screen = when (it) {
                    0 -> PlayScreen(game, "kari.json")
                    1 -> PlayScreen(game, "new.json")
                    else -> Title(game)
                }
            }
        }

        spriteBatch.begin()
        Gdx.gl.glClearColor(0.7f, 0.9f, 0.3f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(Gdx.graphics.deltaTime)
        stage.draw()
        spriteBatch.end()
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