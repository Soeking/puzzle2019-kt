package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

class GameMain(private val game: Core) : Screen {
    private var stage: Stage
    private val batch: SpriteBatch
    private val font: BitmapFont
    private val button: ImageButton

    init {
        stage = Stage()
        batch = SpriteBatch()
        font = BitmapFont()
        font.color = Color.BLACK
        font.data.setScale(10f)
        button = ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("UI/play.png")))))
        button.setPosition(Gdx.graphics.width / 2f, Gdx.graphics.height / 2f)
        button.setScale(10f)
        stage.addActor(button)
        Gdx.input.inputProcessor = stage
    }

    override fun render(delta: Float) {
        if (button.isPressed) {
            game.screen = StageSelect(game)
        }

        batch.begin()
        Gdx.gl.glClearColor(0.9f, 0.1f, 0.5f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        font.draw(batch, "GAME", Gdx.graphics.width / 2f, Gdx.graphics.height / 2f)
        stage.act(Gdx.graphics.deltaTime)
        stage.draw()
        batch.end()
    }

    override fun show() {

    }

    override fun hide() {

    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun resize(width: Int, height: Int) {

    }

    override fun dispose() {
        batch.dispose()
        stage.dispose()
    }
}
