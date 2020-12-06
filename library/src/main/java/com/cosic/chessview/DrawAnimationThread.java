package com.cosic.chessview;

import com.cosic.chessview.models.Action;
import com.cosic.chessview.models.Cell;
import com.cosic.chessview.models.Move;
import java.util.ArrayList;
import java.util.List;

final class DrawAnimationThread extends Thread {

    private static final long ANIMATION_DELAY_MS = 100;
    private static final long MOVING_DELAY_MS = 300;
    private static final long ANIMATION_FRAME_COUNT_IN_SECOND = 30;
    public static final int ANIMATION_FRAME_COUNT = (int) (ANIMATION_FRAME_COUNT_IN_SECOND * ANIMATION_DELAY_MS / 1000);

    private static final int PRE = 0;
        private static final int MOVING = 1;
        private static final int POST = 2;

        private OnDrawThreadListener mListener;
        private boolean mIsRunning = false;
        private Action[][] mMoves;
        private ChessView mChessView;
        /**
         * Index of iteration per one animation.
         * Each index will be called consistently
         * in MOVING_DELAY millis;
         */
        private int mIndex = 0;
        private int mFrame = 0;
        private int mState = PRE;
        private boolean mShowLastMove = false;

        public DrawAnimationThread(Action[][] moves, ChessView chessView) {
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

                long delay = MOVING_DELAY_MS;
                Action[] moves = mMoves[mIndex];

                List<Move> movesCache = new ArrayList<>();

                for (int i = 0; i < moves.length; i++) {
                    Action action = moves[i];
                    if (action == null || !(action instanceof Move)) continue;
                    Move move = (Move) action;
                    movesCache.add(move);

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

                if (mListener != null && mState == POST && movesCache.size() > 0) {
                    mListener.onMove(movesCache);
                }

                if (mState == MOVING) {
                    if (mFrame <= ANIMATION_FRAME_COUNT) {
                        delay = ANIMATION_DELAY_MS / ANIMATION_FRAME_COUNT;
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
            void onMove(List<Move> moves);

            void onMovingFinished();
        }
    }