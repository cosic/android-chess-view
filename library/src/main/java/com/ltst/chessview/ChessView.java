package com.ltst.chessview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

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

    private static final long ANIMATION_DELAY = 100; // ms
    private static final long MOVING_DELAY = 300; // ms
    private static final long ANIMATION_FRAME_COUNT_IN_SECOND = 30;
    private static final int ANIMATION_FRAME_COUNT = (int) (ANIMATION_FRAME_COUNT_IN_SECOND * ANIMATION_DELAY / 1000);

    private static String[] LABEL_HORIZONTAL = new String[]{"A", "B", "C", "D", "E", "F", "G", "H"};
    private static String[] LABEL_VERTICAL = new String[]{"8", "7", "6", "5", "4", "3", "2", "1"};

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

    boolean mShowLastMove = false;

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
     * @param fen  - Forsyth - Edwards Notation and looks like: "rnbqkbnr/pp1ppppp/8/2p5/2P5/8/PP1PPPPP/RNBQKBNR"
     *             More information: @see <a href="Wiki">https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation</a>
     */
    public void applyFen(String fen, Move lastMove) {

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
            mData[lastMove.fromX][lastMove.fromY].setSelected(true);
            mData[lastMove.toX][lastMove.toY].setSelected(true);
        }

        invalidate();
    }

    public Move[][] getMoves(String fenFrom, String fenTo) {

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
                    if (aChar == Cell.PAWN_BLACK.piece) {
                        data[j][i] = new ChessData(Cell.PAWN_BLACK, false);
                    } else if (aChar == Cell.PAWN_WHITE.piece) {
                        data[j][i] = new ChessData(Cell.PAWN_WHITE, false);
                    } else if (aChar == Cell.ROOK_BLACK.piece) {
                        data[j][i] = new ChessData(Cell.ROOK_BLACK, false);
                    } else if (aChar == Cell.ROOK_WHITE.piece) {
                        data[j][i] = new ChessData(Cell.ROOK_WHITE, false);
                    } else if (aChar == Cell.KNIGHT_BLACK.piece) {
                        data[j][i] = new ChessData(Cell.KNIGHT_BLACK, false);
                    } else if (aChar == Cell.KNIGHT_WHITE.piece) {
                        data[j][i] = new ChessData(Cell.KNIGHT_WHITE, false);
                    } else if (aChar == Cell.BISHOP_BLACK.piece) {
                        data[j][i] = new ChessData(Cell.BISHOP_BLACK, false);
                    } else if (aChar == Cell.BISHOP_WHITE.piece) {
                        data[j][i] = new ChessData(Cell.BISHOP_WHITE, false);
                    } else if (aChar == Cell.QUEEN_BLACK.piece) {
                        data[j][i] = new ChessData(Cell.QUEEN_BLACK, false);
                    } else if (aChar == Cell.QUEEN_WHITE.piece) {
                        data[j][i] = new ChessData(Cell.QUEEN_WHITE, false);
                    } else if (aChar == Cell.KING_BLACK.piece) {
                        data[j][i] = new ChessData(Cell.KING_BLACK, false);
                    } else if (aChar == Cell.KING_WHITE.piece) {
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
                s += (data[j][i] == null ? "o" : data[j][i].getCell().piece) + "|";
            }
            Log.d(TAG, s);
        }
    }

    /**
     * Apply moving for current figures state;
     *
     * @param moves     - moving array;
     * @param showSteps - true start animation, false - calculate and set last state of figures immediately;
     */
    public void applyMoving(Move[][] moves, boolean showSteps) {

        stopAnimation();

        if (showSteps) {
            mDrawAnimationThread = new DrawAnimationThread(moves, this);
            mDrawAnimationThread.setRunning(true);
            mDrawAnimationThread.setShowLastMove(mShowLastMove);
            mDrawAnimationThread.setListener(new DrawAnimationThread.OnDrawThreadListener() {
                @Override
                public void onMove(Move[] move) {
                    if (mOnMoveListener != null) {
                        mOnMoveListener.onMove(move);
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
            for (Move[] moves2 : moves) {
                for (Move move : moves2) {

//                for (int i = 0; i < 3; i++) {
//                    ChessData chessDataFrom = mData[move.getFromX()][move.getFromY()];
//                    ChessData chessDataTo = mData[move.getToX()][move.getToY()];
//                    switch (i) {
//                        case 0:
//                            chessDataFrom.setSelected(true);
//                            chessDataTo.setSelected(true);
//                            break;
//                        case 1:
//                            mData[move.getToX()][move.getToY()] = chessDataFrom;
//                            mData[move.getFromX()][move.getFromY()] = chessDataTo;
//                            break;
//                        case 2:
//                            chessDataFrom.setSelected(false);
//                            chessDataTo.setSelected(false);
//                            break;
//                        default:
//                            break;
//                    }
//                }
                    mData[move.getToX()][move.getToY()] = mData[move.getFromX()][move.getFromY()];
                    mData[move.getFromX()][move.getFromY()] = new ChessData(Cell.EMPTY, false);
                }
            }

            if (mShowLastMove) {
                boolean flag = false;
                for (int i = moves.length - 1; i >= 0; i--) {
                    for (int j = moves[i].length - 1; j >= 0; j--) {
                        if (moves[i][j] != null) {
                            mData[moves[i][j].fromX][moves[i][j].fromY].setSelected(true);
                            mData[moves[i][j].toX][moves[i][j].toY].setSelected(true);
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
        int size = Math.min(w, h);
        mCellSize = (size - 2 * mBorderWidth) / COUNT_OF_CELLS;
        mFullRect = new Rect(0, 0, size, size);
        mInnerRect = new Rect(mBorderWidth, mBorderWidth, size - mBorderWidth, size - mBorderWidth);
        for (int i = 0; i < COUNT_OF_CELLS; i++) {
            for (int j = 0; j < COUNT_OF_CELLS; j++) {
                mCells[i][j] = new Rect(
                        mBorderWidth + i * mCellSize,
                        mBorderWidth + j * mCellSize,
                        mBorderWidth + (i + 1) * mCellSize,
                        mBorderWidth + (j + 1) * mCellSize);
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
                    mBorderWidth / 2 - mLabelBoundsRect.width() / 2 - 2,
                    mBorderWidth + mCellSize * i + mCellSize / 2 + mLabelBoundsRect.height() / 2,
                    mLabelPaint
            );
            canvas.drawText(String.valueOf(LABEL_HORIZONTAL[i]),
                    mBorderWidth + mCellSize * i + mCellSize / 2 - labelWidth / 2,
                    height - (mBorderWidth - labelHeight) / 2 - 2,
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

                drawFigure(canvas, rectFrom, rectTo, frame, cell.drawable);
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

    public static final class ChessData {

        /**
         * Chess mPiece;
         */
        private Cell mCell;

        /**
         * true - the cell of chess mPiece is selected;
         * false - the cell of chess mPiece has default background;
         */
        private boolean mIsSelected;


        /**
         * X index of target position for animation;
         */
        private int mIndexToX;

        /**
         * Y index of target position for animation;
         */
        private int mIndexToY;

        /**
         * Current frame number used for moving animation;
         */
        private int mFrame;

        public ChessData(Cell cell, boolean isSelected) {
            this.mCell = cell;
            this.mIsSelected = isSelected;
        }

        public static ChessData create(Cell cell) {
            return new ChessData(cell, false);
        }

        public Cell getCell() {
            return mCell;
        }

        public boolean isSelected() {
            return mIsSelected;
        }

        public void setCell(Cell cell) {
            mCell = cell;
        }

        public void setSelected(boolean selected) {
            mIsSelected = selected;
        }

        public int getIndexToX() {
            return mIndexToX;
        }

        public void setIndexToX(int indexToX) {
            this.mIndexToX = indexToX;
        }

        public int getIndexToY() {
            return mIndexToY;
        }

        public void setIndexToY(int indexToY) {
            this.mIndexToY = indexToY;
        }

        public int getFrame() {
            return mFrame;
        }

        public void setFrame(int frame) {
            mFrame = frame;
        }

        @Override
        public String toString() {
            return "ChessData{" +
                    "mFigureFrom=" + mCell +
                    ", mIsSelected=" + mIsSelected +
                    ", mIndexToX=" + mIndexToX +
                    ", mIndexToY=" + mIndexToY +
                    ", mFrame=" + mFrame +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChessData chessData = (ChessData) o;

            if (mIsSelected != chessData.mIsSelected) return false;
            if (mIndexToX != chessData.mIndexToX) return false;
            if (mIndexToY != chessData.mIndexToY) return false;
            if (mFrame != chessData.mFrame) return false;
            return mCell == chessData.mCell;
        }

        @Override
        public int hashCode() {
            int result = mCell != null ? mCell.hashCode() : 0;
            result = 31 * result + (mIsSelected ? 1 : 0);
            result = 31 * result + mIndexToX;
            result = 31 * result + mIndexToY;
            result = 31 * result + mFrame;
            return result;
        }

        protected ChessData clone() {
            ChessData o = new ChessData(mCell, mIsSelected);
            o.setFrame(mFrame);
            o.setIndexToX(mIndexToX);
            o.setIndexToY(mIndexToY);
            return o;
        }
    }

    public static final class Move {

        private int fromX;
        private int fromY;
        private int toX;
        private int toY;

        public Move(int fromX, int fromY, int toX, int toY) {
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }

        public static Move from(char fromX, char fromY, char toX, char toY) {

            int fromXIndex = 0;
            int fromYIndex = 0;
            int toXIndex = 0;
            int toYIndex = 0;

            for (int i = 0; i < COUNT_OF_CELLS; i++) {

                char cX = LABEL_HORIZONTAL[i].charAt(0);
                char cY = LABEL_VERTICAL[i].charAt(0);

                if (Character.toLowerCase(fromX) == Character.toLowerCase(cX)) {
                    fromXIndex = i;
                }
                if (Character.toLowerCase(toX) == Character.toLowerCase(cX)) {
                    toXIndex = i;
                }
                if (Character.toLowerCase(fromY) == Character.toLowerCase(cY)) {
                    fromYIndex = i;
                }
                if (Character.toLowerCase(toY) == Character.toLowerCase(cY)) {
                    toYIndex = i;
                }
            }
            return new Move(fromXIndex, fromYIndex, toXIndex, toYIndex);
        }

        public int getFromX() {
            return fromX;
        }

        public int getFromY() {
            return fromY;
        }

        public int getToX() {
            return toX;
        }

        public int getToY() {
            return toY;
        }

        @Override
        public String toString() {
            return "Move{" +
                    "fromX=" + fromX +
                    ", fromY=" + fromY +
                    ", toX=" + toX +
                    ", toY=" + toY +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Move move = (Move) o;

            if (fromX != move.fromX) return false;
            if (fromY != move.fromY) return false;
            if (toX != move.toX) return false;
            return toY == move.toY;

        }

        @Override
        public int hashCode() {
            int result = fromX;
            result = 31 * result + fromY;
            result = 31 * result + toX;
            result = 31 * result + toY;
            return result;
        }
    }

    public enum Side {

        BLACK("b"),
        WHITE("w");

        public final String value;

        Side(String value) {
            this.value = value;
        }
    }

    public enum Piece {

        PAWN("P"),
        ROOK("R"),
        KNIGHT("N"),
        BISHOP("B"),
        QUEEN("Q"),
        KING("K");

        public final String value;

        Piece(String value) {
            this.value = value;
        }
    }

    public enum Cell {

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

        public final int value;
        public final @DrawableRes int drawable;
        public final Side side;
        public final char piece;

        Cell(int value, int drawable, Side side, char piece) {
            this.value = value;
            this.drawable = drawable;
            this.side = side;
            this.piece = piece;
        }
    }

    private static final class DrawAnimationThread extends Thread {

        private static final int PRE = 0;
        private static final int MOVING = 1;
        private static final int POST = 2;

        private OnDrawThreadListener mListener;
        private boolean mIsRunning = false;
        private Move[][] mMoves;
        private ChessView mChessView;
        private int mIndex = 0;
        private int mFrame = 0;
        private int mState = PRE;
        private boolean mShowLastMove = false;

        public DrawAnimationThread(Move[][] moves, ChessView chessView) {
            this.mMoves = moves;
            this.mChessView = chessView;
        }

        public void setListener(OnDrawThreadListener listener) {
            this.mListener = listener;
        }

        public void setRunning(boolean running) {
            this.mIsRunning = running;
        }

        public void setShowLastMove(boolean showLastMove) {
            this.mShowLastMove = showLastMove;
        }

        public void reset() {
            mMoves = null;
            mListener = null;
            mChessView = null;
        }

        @Override
        public void run() {

            final ChessData[][] mData = mChessView.getData();
            while (mIsRunning && mIndex < mMoves.length) {

                long delay = MOVING_DELAY;
                Move[] moves = mMoves[mIndex];

                for (int i = 0; i < moves.length; i++) {
                    Move move = moves[i];

                    if (move == null) continue;

                    boolean needSelect = i == 0;
                    int fromX = move.getFromX();
                    int fromY = move.getFromY();
                    ChessData chessDataFrom = mData[fromX][fromY];
                    ChessData chessDataTo = mData[move.getToX()][move.getToY()];
                    switch (mState) {
                        case PRE:
                            chessDataFrom.setSelected(needSelect);
                            chessDataTo.setSelected(needSelect);
                            break;
                        case MOVING:
                            if (mFrame <= ANIMATION_FRAME_COUNT) {
                                chessDataFrom.setFrame(mFrame);
                                chessDataFrom.setIndexToX(move.getToX());
                                chessDataFrom.setIndexToY(move.getToY());
                            } else {
                                mData[move.getToX()][move.getToY()] = chessDataFrom;
                                ChessData emptyCellData = new ChessData(Cell.EMPTY, needSelect);
                                mData[fromX][fromY] = emptyCellData;
                                chessDataFrom.setFrame(0);
                            }
                            break;
                        case POST:
                            chessDataFrom.setSelected(i == 0 && mShowLastMove);
                            chessDataTo.setSelected(i == 0 && mShowLastMove);
                            break;
                        default:
                            break;
                    }
                }

                mChessView.postInvalidate();

                if (mListener != null && mState == POST) {
                    mListener.onMove(moves);
                }

                if (mState == MOVING) {
                    if (mFrame <= ANIMATION_FRAME_COUNT) {
                        delay = ANIMATION_DELAY / ANIMATION_FRAME_COUNT;
                        mFrame++;
                    } else {
                        mFrame = 0;
                        mState++;
                    }
                } else if (mState != POST) {
                    mState++;
                } else {
                    mState = PRE;
                    mIndex++;
                }

                if (!mIsRunning || mIndex >= mMoves.length) {
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    if (mListener != null) {
                        mListener.onMovingFinished();
                    }
                    continue;
                }

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            setRunning(false);
        }

        public interface OnDrawThreadListener {
            void onMove(Move[] move);

            void onMovingFinished();
        }
    }

    public interface OnMoveListener {
        void onMove(Move[] move);

        void onMovingFinished();
    }
}
