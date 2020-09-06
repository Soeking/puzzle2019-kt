package com.puzzle.dcs.type

data class StageData(
        val idMax: Int,
        val wall: MutableList<Wall>,
        val square: MutableList<Square>,
        val triangle: MutableList<Triangle>,
        val ladder: MutableList<Ladder>,
        val gravityChange: MutableList<GravityChange>,
        val start: Start,
        val goal: Goal
)
