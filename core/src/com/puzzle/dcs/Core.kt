package com.puzzle.dcs

import com.badlogic.gdx.Game

class Core : Game() {
    override fun create() {
        setScreen(Title(this))
    }
}