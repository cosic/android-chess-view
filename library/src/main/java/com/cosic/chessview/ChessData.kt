package com.cosic.chessview

import com.cosic.chessview.models.Cell

class ChessData(
    /**
     * Chess mPiece;
     */
    var cell: Cell,
    /**
     * true - the cell of chess mPiece is selected;
     * false - the cell of chess mPiece has default background;
     */
    var isSelected: Boolean) {

    /**
     * X index of target position for animation;
     */
    var indexToX = 0

    /**
     * Y index of target position for animation;
     */
    var indexToY = 0

    /**
     * Current frame number used for moving animation;
     */
    var frame = 0
    var fade = 1f

    fun clone(): ChessData {
        val o = ChessData(cell, isSelected)
        o.frame = frame
        o.indexToX = indexToX
        o.indexToY = indexToY
        o.fade = fade
        return o
    }

    companion object {
        fun create(cell: Cell): ChessData {
            return ChessData(cell, false)
        }
    }
}