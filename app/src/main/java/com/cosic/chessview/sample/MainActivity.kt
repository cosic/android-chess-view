package com.cosic.chessview.sample

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cosic.chessview.models.Action
import com.cosic.chessview.ChessView
import com.cosic.chessview.models.Move
import com.cosic.chessview.sample.adapter.ItemClickSupport
import com.cosic.chessview.sample.adapter.MoveItem
import com.cosic.chessview.sample.adapter.MovesAdapter

private const val ALPHA_DISABLE = 0.3f
private const val ALPHA_ENABLE = 1.0f

class MainActivity : AppCompatActivity() {

    /**
     * Data which would be gotten from server;
     */
    private val mockedMoves by lazy {
        val list = mutableListOf<MoveItem>()
        list.add(MoveItem(1, "P", "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR", "e2", "e4", "e4", "w", true))
        list.add(MoveItem(2, "P", "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR", "e7", "e5", "e5", "b"))
        list.add(MoveItem(3, "N", "rnbqkbnr/pppp1ppp/8/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R", "g1", "f3", "Nf3", "w"))
        list.add(MoveItem(4, "N", "r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R", "b8", "c6", "Nc6", "b"))
        list.add(MoveItem(5, "B", "r1bqkbnr/pppp1ppp/2n5/1B2p3/4P3/5N2/PPPP1PPP/RNBQK2R", "f1", "b5", "Bb5", "w"))
        list.add(MoveItem(6, "N", "r1bqkb1r/pppp1ppp/2n2n2/1B2p3/4P3/5N2/PPPP1PPP/RNBQK2R", "g8", "f6", "Nf6", "b"))
        list.add(MoveItem(7, "P", "r1bqkb1r/pppp1ppp/2n2n2/1B2p3/4P3/5N2/PPPP1PPP/RNBQ1RK1", "e1", "g1", "O-O", "w"))
        list.add(MoveItem(8, "N", "r1bqkb1r/pppp1ppp/2n5/1B2p3/4n3/5N2/PPPP1PPP/RNBQ1RK1", "f6", "e4", "Nxe4", "b"))
        list.add(MoveItem(9, "R", "r1bqkb1r/pppp1ppp/2n5/1B2p3/4n3/5N2/PPPP1PPP/RNBQR1K1", "f1", "e1", "Re1", "w"))
        list.add(MoveItem(10, "N", "r1bqkb1r/pppp1ppp/2nn4/1B2p3/8/5N2/PPPP1PPP/RNBQR1K1", "e4", "d6", "Nd6", "b"))
        list.add(MoveItem(11, "N", "r1bqkb1r/pppp1ppp/2nn4/1B2N3/8/8/PPPP1PPP/RNBQR1K1", "f3", "e5", "Nxe5", "w"))
        return@lazy list
    }

    private val chessView by bindView<ChessView>(R.id.chess_view)
    private val toolbar by bindView<Toolbar>(R.id.toolbar)
    private val recyclerView by bindView<RecyclerView>(R.id.recycler_view)
    private val firstStepButton by bindView<ImageView>(R.id.board_navigation_first_step)
    private val previousStepButton by bindView<ImageView>(R.id.board_navigation_previous_step)
    private val nextStepButton by bindView<ImageView>(R.id.board_navigation_next_step)
    private val lastStepButton by bindView<ImageView>(R.id.board_navigation_last_step)
    private val labelCell by bindView<TextView>(R.id.board_navigation_label_cell)
    private val labelFigure by bindView<ImageView>(R.id.board_navigation_label_figure)

    private lateinit var mMovesAdapter: MovesAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setSupportActionBar(toolbar)
        
        mMovesAdapter = MovesAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = mMovesAdapter

        mMovesAdapter.onSelectedItemChange = { newPosition, previousPosition ->
            val item = mMovesAdapter.getItem(newPosition)
            if (newPosition == previousPosition + 1) {
                val moves = Array(1) { arrayOfNulls<Action>(1) }
                val previousItem = mMovesAdapter.getItem(previousPosition)
                moves[0][0] = from(item)
                chessView.applyFen(previousItem.fen, null, false)
                chessView.applyMoving(moves, true)
            } else {
                chessView.applyFen(item.fen, from(item), false)
            }
            labelCell.text = item.san
            labelFigure.setImageResource(item.cell!!.drawable)
        }
        chessView.setShowLastMove(true)

        nextStepButton.setOnClickListener {
            val position = mMovesAdapter.selectedPosition + 1
            if (position >= mMovesAdapter.itemCount) {
                return@setOnClickListener
            }
            mMovesAdapter.setSelection(position)
            recyclerView.scrollToPosition(position)
            reloadButtonsState()
        }
        previousStepButton.setOnClickListener {
            val position = mMovesAdapter.selectedPosition - 1
            if (position < 0) {
                return@setOnClickListener
            }
            mMovesAdapter.setSelection(position)
            recyclerView.scrollToPosition(position)
            reloadButtonsState()
        }
        firstStepButton.setOnClickListener {
            mMovesAdapter.setSelection(0)
            recyclerView.scrollToPosition(0)
            reloadButtonsState()
        }
        lastStepButton.setOnClickListener {
            val position = mMovesAdapter.itemCount - 1
            mMovesAdapter.setSelection(position)
            recyclerView.scrollToPosition(position)
            reloadButtonsState()
        }

        ItemClickSupport.addTo(recyclerView).mOnItemClickListener = { _, position, _ ->
            mMovesAdapter.setSelection(position)
        }

        mMovesAdapter.addAll(mockedMoves)
        mMovesAdapter.setSelection(0)
        reloadButtonsState()
    }

    private fun reloadButtonsState() {

        val selectedPosition = mMovesAdapter.selectedPosition
        val itemCount = mMovesAdapter.itemCount

        val isFirstPosition = selectedPosition == 0
        previousStepButton.applyEnable(!isFirstPosition)
        firstStepButton.applyEnable(!isFirstPosition)

        val isLastPosition = selectedPosition == itemCount - 1
        nextStepButton.applyEnable(!isLastPosition)
        lastStepButton.applyEnable(!isLastPosition)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> {
                chessView.reset()
                mMovesAdapter.setSelection(0)
                recyclerView.scrollToPosition(0)
                reloadButtonsState()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun from(item: MoveItem): Move {
        return Move.from(item.from[0], item.from[1], item.to[0], item.to[1])
    }
}

fun ImageView.applyEnable(enable: Boolean) {
    this.isClickable = enable
    this.alpha = if (enable) {
        ALPHA_ENABLE
    } else {
        ALPHA_DISABLE
    }
}