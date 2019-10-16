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
    private val stageList = mutableListOf<Pair<ImageButton, String>>()

    init {
        stage = Stage()
        stageList.add(Pair(ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("UI/kari.png"))))), "kari.json"))
        stageList.add(Pair(ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("UI/new.png"))))), "new.json"))
        stageList.add(Pair(ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("UI/saishin.png"))))), "saishin.json"))
        for (i in stageList.indices) {
            stageList[i].first.setPosition(Gdx.graphics.width / 2f, Gdx.graphics.height / 4f * (3 - i))
            stage.addActor(stageList[i].first)
        }
        Gdx.input.inputProcessor = stage
    }

    override fun render(delta: Float) {
        stageList.forEach {
            if (it.first.isPressed) game.screen = PlayScreen(game, it.second)
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
        spriteBatch.dispose()
        stage.dispose()
    }
}