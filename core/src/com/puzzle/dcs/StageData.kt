package com.puzzle.dcs

data class StageData(
        val wall: MutableList<Wall>,
        val square: MutableList<Square>,
        val triangle: MutableList<Triangle>,
        val ladder: MutableList<Ladder>,
        val start: Start,
        val goal: Goal,
        val gravityChange: List<Int>,
        val switch: List<Int>
)

data class Wall(
        val x: Float,
        val y: Float
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
        var gravity: Int
)

data class Start(
        val x: Float,
        val y: Float
)

data class Goal(
        val x: Float,
        val y: Float
)