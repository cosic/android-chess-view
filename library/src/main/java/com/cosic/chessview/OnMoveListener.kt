package com.cosic.chessview

import com.cosic.chessview.models.Move

interface OnMoveListener {
    fun onMove(moves: List<Move?>?)
    fun onMovingFinished()
}