package com.cosic.chessview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.DrawableRes;
import static com.cosic.chessview.DrawAnimationThread.ANIMATION_FRAME_COUNT;
import com.cosic.chessview.models.Action;
import com.cosic.chessview.models.Cell;
import com.cosic.chessview.models.Move;
import com.cosic.chessview.utils.DimenTools;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Preview for watching a chess game by steps.
 */
public class ChessView extends View {

    private static final String TAG = "ChessView";

    private static final int COUNT_OF_CELLS = 8;
    private static final String DEFAULT_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
    private static final ChessData[][] DEFAULT_START_DATA = convertFenToData(DEFAULT_FEN);

    private static String[] LABEL_HORIZONTAL = new String[]{"A", "B", "C", "D", "E", "F", "G", "H"};
    private static String[] LABEL_VERTICAL = new String[]{"8", "7", "6", "5", "4", "3", "2", "1"};

    /**
     * The ratio of the figure size to the cell;
     */
    private static final float FIGURE_SIZE_COEFF = 0.8f;

    private static final int BACKGROUND_COLOR = Color.parseColor("#5c646f");
    private static final int CELL_LIGHT_COLOR = Color.parseColor("#f4f4f4");
    private static final int CELL_DARK_COLOR = Color.parseColor("#b0b9c3");
    private static final int CELL_SELECTED_LIGHT_COLOR = Color.parseColor("#a8ddfa");
    private static final int CELL_SELECTED_DARK_COLOR = Color.parseColor("#77cdff");

    private static final int LABEL_COLOR = Color.parseColor("#949aa1");
    private static final int LABEL_SIZE_SP = 14;

    private static final float BORDER_WIDTH_DP = 16.f;

    private Paint mPiecePaint;
    private Paint mLabelPaint;
    private Paint mBackgroundPaint;
    private Paint mCellLightPaint;
    private Paint mCellDarkPaint;
    private Paint mCellSelectedLightPaint;
    private Paint mCellSelectedDarkPaint;

    private Rect mFullRect;

    private Rect mInnerRect;

    private Picture mPictureBackground;

    private HashMap<Integer, Bitmap> mBitmapCache = new HashMap<>();

    /**
     * Size of small cells;
     */
    private int mCellSize;

    /**
     * Rects array for all cells;
     */
    private Rect[][] mCells = new Rect[COUNT_OF_CELLS][COUNT_OF_CELLS];

    /**
     * Array with data that presents all chess figures;
     */
    private ChessData[][] mData = getDefaultFigureData();

    /**
     * Width of desk boarder where labels are drawn;
     */
    private int mBorderWidth;

    private float mLabelSize;

    private Rect mLabelBoundsRect;

    private DrawAnimationThread mDrawAnimationThread;

    private OnMoveListener mOnMoveListener;

    /**
     * true - leave selection from last move after moving animation;
     */
    private boolean mShowLastMove = false;

    private int mBackgroundColor = BACKGROUND_COLOR;
    private int mLabelColor = LABEL_COLOR;
    private int mCellLightColor = CELL_LIGHT_COLOR;
    private int mCellDarkColor = CELL_DARK_COLOR;
    private int mCellSelectedLightColor = CELL_SELECTED_LIGHT_COLOR;
    private int mCellSelectedDarkColor = CELL_SELECTED_DARK_COLOR;

    public ChessView(Context context) {
        this(context, null);
    }

    public ChessView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChessView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {

//        mLabelSize = DimenTools.pxFromSp(getContext(), LABEL_SIZE_SP);
//
//        mBorderWidth = (int) DimenTools.pxFromDp(getContext(), BORDER_WIDTH_DP);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.ChessView, defStyleAttr, defStyleAttr);

        try {

            mLabelSize = a.getDimensionPixelSize(R.styleable.ChessView_chessView_labelSize,
                    (int) DimenTools.pxFromSp(getContext(), LABEL_SIZE_SP));
            mBorderWidth = a.getDimensionPixelSize(R.styleable.ChessView_chessView_boarderWidth,
                    (int) DimenTools.pxFromDp(getContext(), BORDER_WIDTH_DP));
            mBackgroundColor = a.getColor(R.styleable.ChessView_chessView_boardColor, BACKGROUND_COLOR);
            mLabelColor = a.getColor(R.styleable.ChessView_chessView_labelColor, LABEL_COLOR);
            mCellLightColor = a.getColor(R.styleable.ChessView_chessView_cellWhiteColor, CELL_LIGHT_COLOR);
            mCellDarkColor = a.getColor(R.styleable.ChessView_chessView_cellBlackColor, CELL_DARK_COLOR);
            mCellSelectedLightColor = a.getColor(R.styleable.ChessView_chessView_cellSelectedWhiteColor, CELL_SELECTED_LIGHT_COLOR);
            mCellSelectedDarkColor = a.getColor(R.styleable.ChessView_chessView_cellSelectedBlackColor, CELL_SELECTED_DARK_COLOR);

        } finally {
            a.recycle();
        }

        initPaints();
    }

    public void initPaints() {
        mPiecePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPiecePaint.setStyle(Paint.Style.FILL);
        mPiecePaint.setColor(Color.BLACK);

        mLabelBoundsRect = new Rect();
        mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLabelPaint.setStyle(Paint.Style.FILL);
        mLabelPaint.setTextSize(mLabelSize);
        mLabelPaint.getTextBounds(LABEL_VERTICAL[0], 0, 1, mLabelBoundsRect);
        mLabelPaint.setColor(mLabelColor);

        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(mBackgroundColor);

        mCellLightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCellLightPaint.setStyle(Paint.Style.FILL);
        mCellLightPaint.setColor(mCellLightColor);

        mCellDarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCellDarkPaint.setStyle(Paint.Style.FILL);
        mCellDarkPaint.setColor(mCellDarkColor);

        mCellSelectedLightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCellSelectedLightPaint.setStyle(Paint.Style.FILL);
        mCellSelectedLightPaint.setColor(mCellSelectedLightColor);

        mCellSelectedDarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCellSelectedDarkPaint.setStyle(Paint.Style.FILL);
        mCellSelectedDarkPaint.setColor(mCellSelectedDarkColor);
    }

    public static ChessData[][] getDefaultFigureData() {
        ChessData[][] clone = new ChessData[COUNT_OF_CELLS][COUNT_OF_CELLS];
        for (int i = 0; i < DEFAULT_START_DATA.length; i++) {
            for (int j = 0; j < DEFAULT_START_DATA.length; j++) {
                clone[i][j] = DEFAULT_START_DATA[i][j].clone();
            }

//            ChessData[] copy = DEFAULT_START_DATA[i];
//            clone[i] = new ChessData[COUNT_OF_CELLS];
//            System.arraycopy(copy, 0, clone[i], 0, copy.length);

//            clone[i] = DEFAULT_START_DATA[i].clone();
        }
        return clone;
    }

    public void setOnMoveListener(OnMoveListener onMoveListener) {
        this.mOnMoveListener = onMoveListener;
    }

    /**
     * @return current pieces configuration on chess view;
     */
    public ChessData[][] getData() {
        return mData;
    }

    /**
     * Set started/default state pieces on chess view;
     */
    public void reset() {
        stopAnimation();
        mData = getDefaultFigureData();
        postInvalidate();
    }

    public void setData(ChessData[][] data) {
        // TODO add checks of data;
        stopAnimation();
        this.mData = data;
        invalidate();
    }

    public void setShowLastMove(boolean showLastMove) {
        this.mShowLastMove = showLastMove;
    }

    /**
     * Stop animation thread;
     */
    private void stopAnimation() {
        if (mDrawAnimationThread != null) {
            mDrawAnimationThread.setRunning(false);

            boolean retry = true;
            while (retry) {
                try {
                    mDrawAnimationThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mDrawAnimationThread.reset();
            mDrawAnimationThread = null;
        }
    }

    /**
     * @param fen  - The Forsyth - Edwards Notation and looks like: "rnbqkbnr/pp1ppppp/8/2p5/2P5/8/PP1PPPPP/RNBQKBNR"
     * More information: @see <a href="Wiki">https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation</a>
     */
    public void applyFen(String fen, Move lastMove, boolean showAnimation) {

        if (fen == null) {
            return;
        }

        ChessData[][] data = convertFenToData(fen);
        if (data == null) {
            return;
        }

        if (Arrays.equals(mData, data)) {
            return;
        }

        mData = data;

        if (lastMove != null) {
            mData[lastMove.getFromX()][lastMove.getFromY()].setSelected(true);
            mData[lastMove.getToX()][lastMove.getToY()].setSelected(true);
        }

        invalidate();
    }

    /**
     * First array index - index of iteration;
     * Second array index - piece index;
     */
    public Action[][] getMoves(String fenFrom, String fenTo) {

        ChessData[][] dataFrom = convertFenToData(fenFrom);
        ChessData[][] dataTo = convertFenToData(fenTo);

        printSquareArray(dataTo);

        for (int i = 0; i < COUNT_OF_CELLS; i++) {
            for (int j = 0; j < COUNT_OF_CELLS; j++) {
                if (dataFrom[j][i].getCell().equals(dataTo[j][i].getCell())) {
                    dataFrom[j][i] = null;
                    dataTo[j][i] = null;
                }
            }
        }

        // TODO I believe there is a better way to do that;
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < COUNT_OF_CELLS; i++) {
            for (int j = 0; j < COUNT_OF_CELLS; j++) {
                if (dataFrom[j][i] != null && dataFrom[j][i].getCell() != Cell.EMPTY) {
                    for (int k = 0; k < COUNT_OF_CELLS; k++) {
                        for (int q = 0; q < COUNT_OF_CELLS; q++) {
                            if (dataTo[q][k] != null && dataTo[q][k].getCell() != Cell.EMPTY) {
                                if (dataFrom[j][i].getCell().equals(dataTo[q][k].getCell())) {
                                    moves.add(new Move(j, i, q, k));
                                }
                            }
                        }
                    }
                }
            }
        }

        Move[][] m = new Move[1][moves.size()];
        m[0] = moves.toArray(new Move[moves.size()]);
        return m;
    }

    private static ChessData[][] convertFenToData(String fen) {

        String[] rows = fen.split("/");
        if (rows.length != COUNT_OF_CELLS) {
            return null;
        }

        ChessData[][] data = new ChessData[COUNT_OF_CELLS][COUNT_OF_CELLS];

        for (int i = 0; i < COUNT_OF_CELLS; i++) {

            char[] chars = rows[i].toCharArray();
            int j = 0;
            int q = 0;
            while (q < chars.length) {
                char aChar = chars[q++];

                if (Character.isDigit(aChar)) {
                    int integer = Character.getNumericValue(aChar);
                    for (int k = j; k < j + integer; k++) {
                        data[k][i] = new ChessData(Cell.EMPTY, false);
                    }
                    j += integer;
                } else {
                    if (aChar == Cell.PAWN_BLACK.getPiece()) {
                        data[j][i] = new ChessData(Cell.PAWN_BLACK, false);
                    } else if (aChar == Cell.PAWN_WHITE.getPiece()) {
                        data[j][i] = new ChessData(Cell.PAWN_WHITE, false);
                    } else if (aChar == Cell.ROOK_BLACK.getPiece()) {
                        data[j][i] = new ChessData(Cell.ROOK_BLACK, false);
                    } else if (aChar == Cell.ROOK_WHITE.getPiece()) {
                        data[j][i] = new ChessData(Cell.ROOK_WHITE, false);
                    } else if (aChar == Cell.KNIGHT_BLACK.getPiece()) {
                        data[j][i] = new ChessData(Cell.KNIGHT_BLACK, false);
                    } else if (aChar == Cell.KNIGHT_WHITE.getPiece()) {
                        data[j][i] = new ChessData(Cell.KNIGHT_WHITE, false);
                    } else if (aChar == Cell.BISHOP_BLACK.getPiece()) {
                        data[j][i] = new ChessData(Cell.BISHOP_BLACK, false);
                    } else if (aChar == Cell.BISHOP_WHITE.getPiece()) {
                        data[j][i] = new ChessData(Cell.BISHOP_WHITE, false);
                    } else if (aChar == Cell.QUEEN_BLACK.getPiece()) {
                        data[j][i] = new ChessData(Cell.QUEEN_BLACK, false);
                    } else if (aChar == Cell.QUEEN_WHITE.getPiece()) {
                        data[j][i] = new ChessData(Cell.QUEEN_WHITE, false);
                    } else if (aChar == Cell.KING_BLACK.getPiece()) {
                        data[j][i] = new ChessData(Cell.KING_BLACK, false);
                    } else if (aChar == Cell.KING_WHITE.getPiece()) {
                        data[j][i] = new ChessData(Cell.KING_WHITE, false);
                    } else {
                        data[j][i] = new ChessData(Cell.EMPTY, false);
                    }
                    j++;
                }
            }
        }
        return data;
    }

    public void printCurrentChessConfiguration() {
        printSquareArray(mData);
    }

    public void printSquareArray(ChessData[][] data) {
        for (int i = 0; i < COUNT_OF_CELLS; i++) {
            String s = "|"; // TODO use StringBuffer;
            for (int j = 0; j < COUNT_OF_CELLS; j++) {
                s += (data[j][i] == null ? "o" : data[j][i].getCell().getPiece()) + "|";
            }
            Log.d(TAG, s);
        }
    }

    /**
     * Apply moving for current figures state;
     *
     * @param moves     - moving array;
     * @param showAnimation - true start animation, false - calculate and set last state of figures immediately;
     */
    public void applyMoving(Action[][] moves, boolean showAnimation) {

        stopAnimation();

        if (showAnimation) {
            mDrawAnimationThread = new DrawAnimationThread(moves, this);
            mDrawAnimationThread.setRunning(true);
            mDrawAnimationThread.setShowLastMove(mShowLastMove);
            mDrawAnimationThread.setListener(new DrawAnimationThread.OnDrawThreadListener() {
                @Override
                public void onMove(List<Move> moves) {
                    if (mOnMoveListener != null) {
                        mOnMoveListener.onMove(moves);
                    }
                }

                @Override
                public void onMovingFinished() {
                    if (mOnMoveListener != null) {
                        mOnMoveListener.onMovingFinished();
                    }
                }
            });
            mDrawAnimationThread.start();
        } else {
            for (Action[] actionArray : moves) {
                for (Action action : actionArray) {

//                for (int i = 0; i < 3; i++) {
//                    ChessData chessDataFrom = mData[action.getFromX()][action.getFromY()];
//                    ChessData chessDataTo = mData[action.getToX()][action.getToY()];
//                    switch (i) {
//                        case 0:
//                            chessDataFrom.setSelected(true);
//                            chessDataTo.setSelected(true);
//                            break;
//                        case 1:
//                            mData[action.getToX()][action.getToY()] = chessDataFrom;
//                            mData[action.getFromX()][action.getFromY()] = chessDataTo;
//                            break;
//                        case 2:
//                            chessDataFrom.setSelected(false);
//                            chessDataTo.setSelected(false);
//                            break;
//                        default:
//                            break;
//                    }
//                }
                    if (action == null || !(action instanceof Move)) continue;
                    Move move = (Move) action;
                    mData[move.getToX()][move.getToY()] = mData[move.getFromX()][move.getFromY()];
                    mData[move.getFromX()][move.getFromY()] = new ChessData(Cell.EMPTY, false);
                }
            }

            if (mShowLastMove) {
                boolean flag = false;
                for (int i = moves.length - 1; i >= 0; i--) {
                    for (int j = moves[i].length - 1; j >= 0; j--) {
                        if (moves[i][j] != null && moves[i][j] instanceof Move) {
                            Move move = (Move) moves[i][j];
                            mData[move.getFromX()][move.getFromY()].setSelected(true);
                            mData[move.getToX()][move.getToY()].setSelected(true);
                            flag = true;
                            break;
                        }
                    }
                    if (flag) {
                        break;
                    }
                }
            }

            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int min = Math.min(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(min, min);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int size = Math.min(w - getPaddingLeft() - getPaddingRight(), h - getPaddingTop() - getPaddingBottom());
        // TODO keep in mind the padding size;
        mCellSize = (size - 2 * mBorderWidth) / COUNT_OF_CELLS;
        mFullRect = new Rect(getPaddingLeft(), getPaddingTop(), getPaddingLeft() + size, getPaddingTop() + size);
        mInnerRect = new Rect(mBorderWidth + getPaddingLeft(), mBorderWidth + getPaddingTop(), size - mBorderWidth + getPaddingLeft(), size - mBorderWidth + getPaddingTop());
        for (int i = 0; i < COUNT_OF_CELLS; i++) {
            for (int j = 0; j < COUNT_OF_CELLS; j++) {
                mCells[i][j] = new Rect(
                        getPaddingLeft() + mBorderWidth + i * mCellSize,
                        getPaddingTop() + mBorderWidth + j * mCellSize,
                        getPaddingLeft() + mBorderWidth + (i + 1) * mCellSize,
                        getPaddingTop() + mBorderWidth + (j + 1) * mCellSize);
            }
        }
        mBitmapCache.clear();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mFullRect == null) return;

        int width = getWidth();
        int height = getHeight();

//        canvas.drawPicture(mPictureBackground);

        canvas.drawRect(mFullRect, mBackgroundPaint);

        // Draw labels;
        int labelHeight = mLabelBoundsRect.height();
        int labelWidth = mLabelBoundsRect.width();
        for (int i = 0; i < COUNT_OF_CELLS; i++) {
            canvas.drawText(LABEL_VERTICAL[i],
                    mBorderWidth / 2 - mLabelBoundsRect.width() / 2 + getPaddingLeft(),
                    mBorderWidth + mCellSize * i + mCellSize / 2 + mLabelBoundsRect.height() / 2 + getPaddingTop(),
                    mLabelPaint
            );
            canvas.drawText(String.valueOf(LABEL_HORIZONTAL[i]),
                    mBorderWidth + mCellSize * i + mCellSize / 2 - labelWidth / 2 + getPaddingLeft(),
                    mBorderWidth + mCellSize * COUNT_OF_CELLS + labelHeight + (mBorderWidth - labelHeight) / 2 + 2 + getPaddingTop(),
                    mLabelPaint
            );
        }

//        canvas.clipRect(mInnerRect);

        // Draw cells
        for (int i = 0; i < COUNT_OF_CELLS; i++) {
            for (int j = 0; j < COUNT_OF_CELLS; j++) {
                Rect rectFrom = mCells[i][j];
                if (mData[i][j].isSelected()) {
                    canvas.drawRect(rectFrom, ((i + j) % 2 == 0) ? mCellSelectedDarkPaint : mCellSelectedLightPaint);
                } else {
                    canvas.drawRect(rectFrom, ((i + j) % 2 == 0) ? mCellLightPaint : mCellDarkPaint);
                }
            }
        }

        // Draw cells figures;
        for (int i = 0; i < COUNT_OF_CELLS; i++) {
            for (int j = 0; j < COUNT_OF_CELLS; j++) {

                ChessData chessData = mData[i][j];
                Rect rectFrom = mCells[i][j];
                Rect rectTo = null;
                int frame = chessData.getFrame();
                if (frame > 0) {
                    rectTo = mCells[chessData.getIndexToX()][chessData.getIndexToY()];
                }
                Cell cell = chessData.getCell();

                drawFigure(canvas, rectFrom, rectTo, frame, cell.getDrawable());
            }
        }

//        canvas.restore();
    }

    private void drawFigure(Canvas canvas, Rect rectFrom, Rect rectTo, int frame, @DrawableRes int drawableRes) {

        if (drawableRes == 0) return;

        Bitmap bitmap;

        if (mBitmapCache.containsKey(drawableRes)) {
            bitmap = mBitmapCache.get(drawableRes);
        } else {

            bitmap = BitmapFactory.decodeResource(getContext().getResources(), drawableRes);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            int targetHeight = (int) (rectFrom.height() * FIGURE_SIZE_COEFF);
            boolean flag = false;
            if (height > targetHeight) {
                width = width * targetHeight / height;
                height = targetHeight;
                flag = true;
            }

            int targetWidth = (int) (rectFrom.width() * FIGURE_SIZE_COEFF);
            if (width > targetWidth) {
                height = height * targetWidth / width;
                width = targetWidth;
                flag = true;
            }

            if (flag) {
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
            }
            mBitmapCache.put(drawableRes, bitmap);
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int fromX = rectFrom.centerX() - width / 2;
        int fromY = rectFrom.centerY() - height / 2;
        if (rectTo == null || frame == 0) {
            canvas.drawBitmap(bitmap, fromX, fromY, mPiecePaint);
        } else {
            int toX = rectTo.centerX() - width / 2;
            int toY = rectTo.centerY() - height / 2;
            int left = fromX - frame * (fromX - toX) / ANIMATION_FRAME_COUNT;
            int top = fromY - frame * (fromY - toY) / ANIMATION_FRAME_COUNT;
            canvas.drawBitmap(bitmap, left, top, mPiecePaint);
        }
    }
}
