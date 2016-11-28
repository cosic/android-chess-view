package com.ltst.chessview.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ltst.chessview.ChessView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ChessView mChessView;
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ImageView mFirstStepButton;
    private ImageView mPreviousStepButton;
    private ImageView mNextStepButton;
    private ImageView mLastStepButton;
    private TextView mLabelCell;
    private ImageView mLabelFigure;

    private MovesAdapter mMovesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mChessView = (ChessView) findViewById(R.id.chess_view);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mFirstStepButton = (ImageView) findViewById(R.id.board_navigation_first_step);
        mPreviousStepButton = (ImageView) findViewById(R.id.board_navigation_previous_step);
        mLastStepButton = (ImageView) findViewById(R.id.board_navigation_last_step);
        mNextStepButton = (ImageView) findViewById(R.id.board_navigation_next_step);
        mLabelCell = (TextView) findViewById(R.id.board_navigation_label_cell);
        mLabelFigure = (ImageView) findViewById(R.id.board_navigation_label_figure);

        setSupportActionBar(mToolbar);

        mMovesAdapter = new MovesAdapter();
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(lm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mMovesAdapter);

        mMovesAdapter.setOnSelectedItemChange(new MovesAdapter.OnSelectedItemChange() {
            @Override
            public void onSelectedItemChange(int position) {
                MovesAdapter.Move item = mMovesAdapter.getItem(position);
                mChessView.applyFen(item.getFen(), from(item));
            }
        });

        mChessView.setShowLastMove(true);

        mNextStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = mMovesAdapter.getSelectedPosition() + 1;
                if (position >= mMovesAdapter.getItemCount()) return;
                mMovesAdapter.setSelection(position);
                mRecyclerView.scrollToPosition(position);
            }
        });

        mPreviousStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = mMovesAdapter.getSelectedPosition() - 1;
                if (position < 0) return;
                mMovesAdapter.setSelection(position);
                mRecyclerView.scrollToPosition(position);
            }
        });

        mFirstStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMovesAdapter.setSelection(0);
                mRecyclerView.scrollToPosition(0);
            }
        });

        mLastStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = mMovesAdapter.getItemCount() - 1;
                mMovesAdapter.setSelection(position);
                mRecyclerView.scrollToPosition(position);
            }
        });

        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                mMovesAdapter.setSelection(position);
            }
        });

        List<MovesAdapter.Move> list = new ArrayList<>();
        list.add(new MovesAdapter.Move(ChessView.Cell.BISHOP_WHITE, "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR", "e2", "e4", "e4", "w", true));
        list.add(new MovesAdapter.Move(ChessView.Cell.BISHOP_WHITE, "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR", "e7", "e5", "e5", "b", false));
        list.add(new MovesAdapter.Move(ChessView.Cell.BISHOP_WHITE, "rnbqkbnr/pppp1ppp/8/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R", "g1", "f3", "Nf3", "w", false));
        list.add(new MovesAdapter.Move(ChessView.Cell.BISHOP_WHITE, "r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R", "b8", "c6", "Nc6", "b", false));
        list.add(new MovesAdapter.Move(ChessView.Cell.BISHOP_WHITE, "r1bqkbnr/pppp1ppp/2n5/1B2p3/4P3/5N2/PPPP1PPP/RNBQK2R", "f1", "b5", "Bb5", "w", false));
        list.add(new MovesAdapter.Move(ChessView.Cell.BISHOP_WHITE, "r1bqkbnr/1ppp1ppp/p1n5/1B2p3/4P3/5N2/PPPP1PPP/RNBQK2R", "a7", "a6", "a6", "b", false));
        list.add(new MovesAdapter.Move(ChessView.Cell.BISHOP_WHITE, "r1bqkbnr/1ppp1ppp/p1n5/4p3/B3P3/5N2/PPPP1PPP/RNBQK2R", "b5", "a4", "Ba4", "w", false));
        list.add(new MovesAdapter.Move(ChessView.Cell.BISHOP_WHITE, "r1bqkb1r/1ppp1ppp/p1n2n2/4p3/B3P3/5N2/PPPP1PPP/RNBQK2R", "g8", "f6", "Nf6", "b", false));
        list.add(new MovesAdapter.Move(ChessView.Cell.BISHOP_WHITE, "r1bqkb1r/1ppp1ppp/p1n2n2/4p3/B3P3/5N2/PPPP1PPP/RNBQ1RK1", "e1", "g1", "O-O", "w", false));
        mMovesAdapter.addAll(list);
        mMovesAdapter.setSelection(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reset:
                mChessView.reset();
                mMovesAdapter.setSelection(0);
                mRecyclerView.scrollToPosition(0);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private ChessView.Move from(MovesAdapter.Move item) {
        return ChessView.Move.from(
                item.getFrom().charAt(0),
                item.getFrom().charAt(1),
                item.getTo().charAt(0),
                item.getTo().charAt(1));
    }

}
