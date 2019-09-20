package com.puzzle.dcs

data class StageData(
        val wall: List<Wall>,
        val square: List<Square>,
        val triangle: List<Triangle>,
        val ladder: List<Ladder>,
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