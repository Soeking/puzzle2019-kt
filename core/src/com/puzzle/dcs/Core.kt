package com.puzzle.dcs

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import kotlin.math.min

class Core : Game() {
    companion object{
        val gridSize2 = min(Gdx.graphics.width / 20f, Gdx.graphics.height * 4f / 45f)
        val halfGrid2 = gridSize2 / 2.0f
    }

    override fun create() {
        setScreen(Title(this))
    }
}