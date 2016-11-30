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

    public static final int NO_POSITION = -1;

    private OnSelectedItemChange mOnSelectedItemChange;

    private List<MoveItem> mItems = new ArrayList<>();

    public void setOnSelectedItemChange(@Nullable OnSelectedItemChange onSelectedItemChange) {
        this.mOnSelectedItemChange = onSelectedItemChange;
    }

    public int getSelectedPosition() {
        List<MoveItem> items = getItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isSelected()) {
                return i;
            }
        }
        return NO_POSITION;
    }

    public void setSelection(int position) {

        List<MoveItem> items = getItems();

        if (items.size() == 0) return;

        int selectedPosition = getSelectedPosition();

        if (selectedPosition != position) {
            items.get(selectedPosition).setSelected(false);
            notifyItemChanged(selectedPosition);

            items.get(position).setSelected(true);
            notifyItemChanged(position);
        }

        if (mOnSelectedItemChange != null) {
            mOnSelectedItemChange.onSelectedItemChange(position, selectedPosition);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.move_item_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        MoveItem item = getItem(position);
        viewHolder.mStep.setVisibility(position % 2 == 0 ? View.VISIBLE : View.GONE);
        viewHolder.mStep.setText(String.valueOf(position / 2 + 1));
        viewHolder.mMove.setText(item.getSan());
        viewHolder.mRoot.setBackground(item.isSelected()
                ? ContextCompat.getDrawable(viewHolder.mRoot.getContext(), R.drawable.sp_rect_accent) : null);
        if (item.getSan().contains("O-O")) {
            viewHolder.mFigure.setVisibility(View.GONE);
        } else {
            ChessView.Cell cell = item.getCell();
            if (cell != null) {
                viewHolder.mFigure.setImageResource(cell.drawable);
            }
            viewHolder.mFigure.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public MoveItem getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mItems.get(position).getNum();
    }

    private List<MoveItem> getItems() {
        return mItems;
    }

    public void addAll(List<MoveItem> list) {
        mItems.addAll(list);
        notifyDataSetChanged();
    }

    public interface OnSelectedItemChange {
        void onSelectedItemChange(int newPosition, int oldPosition);
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
