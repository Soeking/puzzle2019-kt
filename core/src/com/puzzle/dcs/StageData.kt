package com.puzzle.dcs

data class StageData(
    val wall: MutableList<Wall>,
    val square: MutableList<Square>,
    val triangle: MutableList<Triangle>,
    val ladder: MutableList<Ladder>,
    val gravityChange: MutableList<GravityChange>,
    val start: Start,
    val goal: Goal
)

data class Wall(
    var x: Float,
    var y: Float
)

data class Square(
    var x: Float,
    var y: Float,
    val gravityID: Int,
    var gravity: Int
)

data class Triangle(
    var x: Float,
    var y: Float,
    val gravityID: Int,
    var gravity: Int,
    val rotate: Int
)

data class Ladder(
    var x: Float,
    var y: Float,
    val gravityID: Int,
    var gravity: Int,
    val rotate: Int
)

data class GravityChange(
        var x: Float,
        var y: Float,
        val rotate: Int,
        val setGravity: Int
)

data class Start(
    var x: Float,
    var y: Float,
    var gravity: Int
)

data class Goal(
    var x: Float,
    var y: Float,
    val gravity: Int
)

