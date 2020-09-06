package com.puzzle.dcs

import com.badlogic.gdx.Game

class Core : Game() {
    companion object{
        var gridSize2 = 1f
        var halfGrid2 = 1f
    }

    override fun create() {
        setScreen(Title(this))
    }
}