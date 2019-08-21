package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.utils.Json

class Stage(private val game: Core, private val fileName: String) : Screen {
    private val file = Gdx.files.internal("stages/$fileName")
    private val json = Json()
    lateinit var stageData: StageData

    init {
        Gdx.app.log("Ori", "${file.readString()}")
        stageData = json.fromJson(stageData::class.java, file.readString())
    }

    override fun render(delta: Float) {

    }

    override fun resize(width: Int, height: Int) {

    }

    override fun show() {

    }

    override fun hide() {

    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun dispose() {

    }
}