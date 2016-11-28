package com.ltst.chessview.sample;

import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ltst.chessview.ChessView;

import java.util.ArrayList;
import java.util.List;

public class MovesAdapter extends RecyclerView.Adapter<MovesAdapter.ViewHolder> {

    private OnSelectedItemChange mOnSelectedItemChange;

    private List<Move> mItems = new ArrayList<>();



    public void setOnSelectedItemChange(@Nullable OnSelectedItemChange onSelectedItemChange) {
        this.mOnSelectedItemChange = onSelectedItemChange;
    }

    public int getSelectedPosition() {
        List<Move> items = getItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isSelected()) {
                return i;
            }
        }
        return 0;
    }

    public void setSelection(int position) {

        List<Move> items = getItems();

        if (items.size() == 0) return;

        int selectedPosition = getSelectedPosition();

        if (selectedPosition != position) {
            items.get(selectedPosition).setSelected(false);
            notifyItemChanged(selectedPosition);

            items.get(position).setSelected(true);
            notifyItemChanged(position);
        }

        if (mOnSelectedItemChange != null) {
            mOnSelectedItemChange.onSelectedItemChange(position);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.board_move_item_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Move item = getItem(position);
        viewHolder.mStep.setVisibility(position % 2 == 0 ? View.VISIBLE : View.GONE);
        viewHolder.mStep.setText(String.valueOf(position / 2 + 1));
        viewHolder.mMove.setText(item.getSan());
        viewHolder.mRoot.setBackground(item.isSelected()
                ? ContextCompat.getDrawable(viewHolder.mRoot.getContext(), R.drawable.sp_rect_accent) : null);
        if (item.getSan().contains("O-O")) {
            viewHolder.mFigure.setVisibility(View.GONE);
        } else {
            viewHolder.mFigure.setImageResource(item.getCell().drawable);
            viewHolder.mFigure.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public Move getItem(int position) {
        return mItems.get(position);
    }

    private List<Move> getItems() {
        return mItems;
    }

    public void addAll(List<Move> list) {
        mItems.addAll(list);
        notifyDataSetChanged();
    }

    public final static class Move {

        private ChessView.Cell cell;
        private String fen;
        private String from;
        private String to;
        private String san;
        private String turn;
        private boolean isSelected;

        public Move(ChessView.Cell cell, String fen, String from, String to, String san, String turn, boolean isSelected) {
            this.cell = cell;
            this.fen = fen;
            this.from = from;
            this.to = to;
            this.san = san;
            this.turn = turn;
            this.isSelected = isSelected;
        }

        public ChessView.Cell getCell() {
            return cell;
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
    }

    public interface OnSelectedItemChange {
        void onSelectedItemChange(int position);
    }

    public static ChessView.Cell from(String turn, String piece) {
        if (turn.toLowerCase().equals(ChessView.Side.BLACK.value.toLowerCase())) {
            if (piece == null) {
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
            if (piece == null) {
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

    public static final class ViewHolder extends RecyclerView.ViewHolder {

        public final ViewGroup mRoot;
        public final TextView mStep;
        public final ImageView mFigure;
        public final TextView mMove;

        public ViewHolder(View itemView) {
            super(itemView);
            mRoot = (ViewGroup) itemView.findViewById(R.id.board_move_item_root);
            mStep = (TextView) itemView.findViewById(R.id.board_move_item_step);
            mFigure = (ImageView) itemView.findViewById(R.id.board_move_item_figure);
            mMove = (TextView) itemView.findViewById(R.id.board_move_item_cell);
        }
    }
}
