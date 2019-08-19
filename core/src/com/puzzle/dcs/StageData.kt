package com.puzzle.dcs

data class StageData(
    val wall: List<Int>,
    val square: List<Int>,
    val triangle: List<Int>,
    val ladder: List<Int>,
    val start: Int,
    val goal: Int,
    val grav_ch: List<Int>,
    val switch: List<Int>
)