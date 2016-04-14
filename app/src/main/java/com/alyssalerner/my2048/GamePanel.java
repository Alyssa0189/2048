package com.alyssalerner.my2048;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

/**
 * Created by Alyssa on 2016-02-18.
 */
public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    public final float BOARD_RATIO = 0.90F;  // percentage of the screen width/height that the board takes up
    public final int N_TILES = 11;          // Number of tiles starting with 2, 4, 8, 16, etc.
    public final float BOARD_IMG_SIZE = 416;   // width and height of board image in the file
    public final int TILE_IMG_LENGTH = 100; // The height of each tile image in file

    private GestureDetectorCompat gestureDetector;
    private Board board;
    private float scaleFactor;
    private boolean resetNextTouch = false; // True if player has lost, and next touch should result in a board reset
    GameThread thread;
    Context context;
    private ArrayList<SerializableTile> savedTiles; // Keeps previous tile state for when screen is rotated, etc.

    public GamePanel(Context context, ArrayList<SerializableTile> savedTiles) {
        super(context);
        this.context = context;
        getHolder().addCallback(this);
        setFocusable(true);
        gestureDetector = new GestureDetectorCompat(context, new FlingListener());
        this.savedTiles = savedTiles;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        int minScreenLength = (getWidth() > getHeight())? getHeight() : getWidth();

        // Create board
        int boardWidth = (int)(minScreenLength * BOARD_RATIO);
        scaleFactor = (float)boardWidth / BOARD_IMG_SIZE;
        int boardX = (int)((getWidth() / 2.0) - (boardWidth / 2.0));
        int boardY = (int)((getHeight() / 2.0) - (boardWidth / 2.0));

        // For unknown reason, BitmapFactory returns a tile image that is 3x larger than it really is, so scale the result to compensate
        Bitmap tileBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tiles);
        Bitmap scaledTileBitmap = Bitmap.createScaledBitmap(tileBitmap, TILE_IMG_LENGTH, TILE_IMG_LENGTH * N_TILES, false);

        if(board == null) {
            board = new Board(
                    BitmapFactory.decodeResource(getResources(), R.drawable.board),
                    scaledTileBitmap,
                    new Rect(boardX, boardY, boardX + boardWidth, boardY + boardWidth),
                    scaleFactor,
                    savedTiles
            );
        }

        thread = new GameThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        int counter = 0;    // Ensure don't enter an infinite loop

        // Try to destroy the thread until successful (can take a few attempts)
        while(retry && counter < 1000) {
            counter++;
            try {
                thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;  // So garbage collector can pick up object
            } catch(InterruptedException e) { e.printStackTrace();}
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(resetNextTouch) {
            resetNextTouch = false;
            board.reset();
        }
        else gestureDetector.onTouchEvent(event);
        return true;
    }

    public void update() {
        board.update();
        if(board.gameLost()) {
            resetNextTouch = true;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if(canvas != null) {

            final int savedState = canvas.save();
            board.draw(canvas);
            canvas.restoreToCount(savedState);
        }
    }

    public Board getBoard() {
        return board;
    }

    class FlingListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Board.Direction direction = board.getDirection(velocityX, velocityY);

            if(board.boardUnlocked() && board.canMoveInDirection(direction)) {
                board.lock();

                switch(direction) {
                    case RIGHT: board.slideRight(); break;
                    case LEFT:  board.slideLeft();  break;
                    case UP:    board.slideUp();    break;
                    case DOWN:  board.slideDown();  break;
                    default:    break;
                }
            }

            return true;
        }
    }
}
