package com.cosic.chessview.models

private const val COUNT_OF_CELLS = 8
private val LABEL_HORIZONTAL = arrayOf("A", "B", "C", "D", "E", "F", "G", "H")
private val LABEL_VERTICAL = arrayOf("8", "7", "6", "5", "4", "3", "2", "1")

data class Move(
    val fromX: Int,
    val fromY: Int,
    val toX: Int,
    val toY: Int
) : Action {

    companion object {
        fun from(fromX: Char, fromY: Char, toX: Char, toY: Char): Move {
            var fromXIndex = 0
            var fromYIndex = 0
            var toXIndex = 0
            var toYIndex = 0
            for (i in 0 until COUNT_OF_CELLS) {
                val cX = LABEL_HORIZONTAL[i][0]
                val cY = LABEL_VERTICAL[i][0]
                if (Character.toLowerCase(fromX) == Character.toLowerCase(cX)) {
                    fromXIndex = i
                }
                if (Character.toLowerCase(toX) == Character.toLowerCase(cX)) {
                    toXIndex = i
                }
                if (Character.toLowerCase(fromY) == Character.toLowerCase(cY)) {
                    fromYIndex = i
                }
                if (Character.toLowerCase(toY) == Character.toLowerCase(cY)) {
                    toYIndex = i
                }
            }
            return Move(fromXIndex, fromYIndex, toXIndex, toYIndex)
        }
    }
}