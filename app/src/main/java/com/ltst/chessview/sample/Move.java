package com.ltst.chessview.sample;

import com.ltst.chessview.ChessView;

public final class Move {

    private int num;
    private String piece;
    private String fen;
    private String from;
    private String to;
    private String san;
    private String turn;
    private boolean isSelected;

    public Move(int num,
                String piece,
                String fen,
                String from,
                String to,
                String san,
                String turn,
                boolean isSelected) {

        this.num = num;
        this.piece = piece;
        this.fen = fen;
        this.from = from;
        this.to = to;
        this.san = san;
        this.turn = turn;
        this.isSelected = isSelected;
    }

    public int getNum() {
        return num;
    }

    public String getPiece() {
        return piece;
    }

    public String getTurn() {
        return turn;
    }

    public String getFen() {
        return fen;
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public String getSan() {
        return san;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public ChessView.Cell getCell() {
        return from(getTurn(), getPiece());
    }

    public static ChessView.Cell from(String turn, String piece) {
        if (turn.toLowerCase().equals(ChessView.Side.BLACK.value.toLowerCase())) {
            if (piece == null || piece.toLowerCase().equals(ChessView.Piece.PAWN.value.toLowerCase())) {
                return ChessView.Cell.PAWN_BLACK;
            } else if (piece.toLowerCase().equals(ChessView.Piece.ROOK.value.toLowerCase())) {
                return ChessView.Cell.ROOK_BLACK;
            } else if (piece.toLowerCase().equals(ChessView.Piece.KNIGHT.value.toLowerCase())) {
                return ChessView.Cell.KNIGHT_BLACK;
            } else if (piece.toLowerCase().equals(ChessView.Piece.BISHOP.value.toLowerCase())) {
                return ChessView.Cell.BISHOP_BLACK;
            } else if (piece.toLowerCase().equals(ChessView.Piece.QUEEN.value.toLowerCase())) {
                return ChessView.Cell.QUEEN_BLACK;
            } else if (piece.toLowerCase().equals(ChessView.Piece.KING.value.toLowerCase())) {
                return ChessView.Cell.KING_BLACK;
            }
        } else {
            if (piece == null || piece.toLowerCase().equals(ChessView.Piece.PAWN.value.toLowerCase())) {
                return ChessView.Cell.PAWN_WHITE;
            } else if (piece.toLowerCase().equals(ChessView.Piece.ROOK.value.toLowerCase())) {
                return ChessView.Cell.ROOK_WHITE;
            } else if (piece.toLowerCase().equals(ChessView.Piece.KNIGHT.value.toLowerCase())) {
                return ChessView.Cell.KNIGHT_WHITE;
            } else if (piece.toLowerCase().equals(ChessView.Piece.BISHOP.value.toLowerCase())) {
                return ChessView.Cell.BISHOP_WHITE;
            } else if (piece.toLowerCase().equals(ChessView.Piece.QUEEN.value.toLowerCase())) {
                return ChessView.Cell.QUEEN_WHITE;
            } else if (piece.toLowerCase().equals(ChessView.Piece.KING.value.toLowerCase())) {
                return ChessView.Cell.KING_WHITE;
            }
        }
        return null;
    }

}