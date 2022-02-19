package com.cosic.chessview.sample.adapter

import com.cosic.chessview.models.Cell
import com.cosic.chessview.models.Piece
import com.cosic.chessview.models.Side

data class MoveItem(
    val num: Int,
    val piece: String,
    val fen: String,
    val from: String,
    val to: String,
    val san: String,
    val turn: String,
    var isSelected: Boolean = false
) {

    val cell: Cell?
        get() = from(turn, piece)

    companion object {
        fun from(turn: String, piece: String?): Cell? {
            return if (turn.equalsIgnoreCase(Side.BLACK.value)) {
                getBlackCell(piece)
            } else {
                getWhiteCell(piece)
            }
        }

        private fun getBlackCell(piece: String?): Cell? {
            return when {
                piece == null ||
                piece.equalsIgnoreCase(Piece.PAWN.value) -> Cell.PAWN_BLACK
                piece.equalsIgnoreCase(Piece.ROOK.value) -> Cell.ROOK_BLACK
                piece.equalsIgnoreCase(Piece.KNIGHT.value) -> Cell.KNIGHT_BLACK
                piece.equalsIgnoreCase(Piece.BISHOP.value) -> Cell.BISHOP_BLACK
                piece.equalsIgnoreCase(Piece.QUEEN.value) -> Cell.QUEEN_BLACK
                piece.equalsIgnoreCase(Piece.KING.value) -> Cell.KING_BLACK
                else -> null
            }
        }

        private fun getWhiteCell(piece: String?): Cell? {
            return when {
                piece == null ||
                piece.equalsIgnoreCase(Piece.PAWN.value) -> Cell.PAWN_WHITE
                piece.equalsIgnoreCase(Piece.ROOK.value) -> Cell.ROOK_WHITE
                piece.equalsIgnoreCase(Piece.KNIGHT.value) -> Cell.KNIGHT_WHITE
                piece.equalsIgnoreCase(Piece.BISHOP.value) -> Cell.BISHOP_WHITE
                piece.equalsIgnoreCase(Piece.QUEEN.value) -> Cell.QUEEN_WHITE
                piece.equalsIgnoreCase(Piece.KING.value) -> Cell.KING_WHITE
                else -> null
            }
        }

        private fun String.equalsIgnoreCase(value: String): Boolean {
            return this.equals(value, ignoreCase = true)
        }
    }
}