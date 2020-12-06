package com.cosic.chessview.models

import androidx.annotation.DrawableRes
import com.cosic.chessview.R

enum class Cell(
    val value: Int,
    @field:DrawableRes val drawable: Int,
    val side: Side?,
    val piece: Char
) {
    EMPTY(1, 0, null, 'o'),
    PAWN_WHITE(2, R.drawable.ic_figure_pawn_white, Side.WHITE, 'P'),
    PAWN_BLACK(3, R.drawable.ic_figure_pawn_black, Side.BLACK, 'p'),
    ROOK_WHITE(4, R.drawable.ic_figure_rook_white, Side.WHITE, 'R'),
    ROOK_BLACK(5, R.drawable.ic_figure_rook_black, Side.BLACK, 'r'),
    KNIGHT_WHITE(6, R.drawable.ic_figure_knight_white, Side.WHITE, 'N'),
    KNIGHT_BLACK(7, R.drawable.ic_figure_knight_black, Side.BLACK, 'n'),
    BISHOP_WHITE(8, R.drawable.ic_figure_bishop_white, Side.WHITE, 'B'),
    BISHOP_BLACK(9, R.drawable.ic_figure_bishop_black, Side.BLACK, 'b'),
    QUEEN_WHITE(10, R.drawable.ic_figure_queen_white, Side.WHITE, 'Q'),
    QUEEN_BLACK(11, R.drawable.ic_figure_queen_black, Side.BLACK, 'q'),
    KING_WHITE(12, R.drawable.ic_figure_king_white, Side.WHITE, 'K'),
    KING_BLACK(13, R.drawable.ic_figure_king_black, Side.BLACK, 'k');
}