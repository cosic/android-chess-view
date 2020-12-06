package com.cosic.chessview.sample

import com.cosic.chessview.models.Cell
import com.cosic.chessview.models.Piece
import com.cosic.chessview.models.Side

data class MoveItem(val num: Int,
               val piece: String,
               val fen: String,
               val from: String,
               val to: String,
               val san: String,
               val turn: String,
               var isSelected: Boolean = false) {

    val cell: Cell?
        get() = from(turn, piece)

    companion object {
        fun from(turn: String, piece: String?): Cell? {
            if (turn.toLowerCase() == Side.BLACK.value.toLowerCase()) {
                if (piece == null || piece.toLowerCase() == Piece.PAWN.value.toLowerCase()) {
                    return Cell.PAWN_BLACK
                } else if (piece.toLowerCase() == Piece.ROOK.value.toLowerCase()) {
                    return Cell.ROOK_BLACK
                } else if (piece.toLowerCase() == Piece.KNIGHT.value.toLowerCase()) {
                    return Cell.KNIGHT_BLACK
                } else if (piece.toLowerCase() == Piece.BISHOP.value.toLowerCase()) {
                    return Cell.BISHOP_BLACK
                } else if (piece.toLowerCase() == Piece.QUEEN.value.toLowerCase()) {
                    return Cell.QUEEN_BLACK
                } else if (piece.toLowerCase() == Piece.KING.value.toLowerCase()) {
                    return Cell.KING_BLACK
                }
            } else {
                if (piece == null || piece.toLowerCase() == Piece.PAWN.value.toLowerCase()) {
                    return Cell.PAWN_WHITE
                } else if (piece.toLowerCase() == Piece.ROOK.value.toLowerCase()) {
                    return Cell.ROOK_WHITE
                } else if (piece.toLowerCase() == Piece.KNIGHT.value.toLowerCase()) {
                    return Cell.KNIGHT_WHITE
                } else if (piece.toLowerCase() == Piece.BISHOP.value.toLowerCase()) {
                    return Cell.BISHOP_WHITE
                } else if (piece.toLowerCase() == Piece.QUEEN.value.toLowerCase()) {
                    return Cell.QUEEN_WHITE
                } else if (piece.toLowerCase() == Piece.KING.value.toLowerCase()) {
                    return Cell.KING_WHITE
                }
            }
            return null
        }
    }
}