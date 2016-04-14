package com.alyssalerner.my2048;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Alyssa on 2016-02-19.
 */
public class Board {
    public final int N_TILES = 11;          // Number of tiles starting with 2, 4, 8, 16, etc.
    public final int BORDER_SIZE = 8;       // Number of pixels the border is in the image file
    public final int ROWS = 4;              // Number of rows and columns
    public final int TILE_IMG_LENGTH = 100; // The height of each tile image in file
    public final int WIN_TILE = 2048;

    public enum Direction {RIGHT, LEFT, UP, DOWN, UNKNOWN};

    // tilesOnBoard and board contain the same Tile objects
    private ArrayList<Tile> tilesOnBoard = new ArrayList<Tile>();
    private Tile[][] board = new Tile[ROWS][ROWS];

    private double scaleFactor;     // Amount to scale the board image and tiles by
    private Rect boardSpace;        // The rectangle that the board should take up on the screen
    private Bitmap boardImage;      // Image of board background
    private Bitmap[] tileImages;    // Images of number-tiles in increasing order
    private int tilesX;             // Top-left pixel coordinate of the tiles
    private int tilesY;
    private boolean playerCanMove;  // False whenever tiles are still sliding
    private Random rand = new Random();


    public Board(Bitmap board, Bitmap tiles, Rect b, double s, ArrayList<SerializableTile> savedTiles) {
        scaleFactor = s;
        boardImage = board;
        boardSpace = b;
        playerCanMove = true;

        // Fill tile images
        tileImages = new Bitmap[N_TILES];

        for(int i = 0; i < N_TILES; i++) {
            try {
                tileImages[i] = Bitmap.createBitmap(tiles, 0, TILE_IMG_LENGTH*i, TILE_IMG_LENGTH, TILE_IMG_LENGTH);
            } catch(Exception e){ e.printStackTrace(); }
        }

        // Find out where to start drawing tiles
        tilesX = (int)(boardSpace.left + (BORDER_SIZE * scaleFactor));
        tilesY = (int)(boardSpace.top + (BORDER_SIZE * scaleFactor));

        Tile.size = (int)(TILE_IMG_LENGTH * scaleFactor);   // Real pixel size of tile

        // Create 2 random tiles if this is a new game
        if(savedTiles == null) {
            createNewTile();
            createNewTile();
        }
        // Restore old tiles if game was only paused
        else {
            for(SerializableTile tile : savedTiles) {
                createTileAt(tile.getValue(), tile.getRow(), tile.getCol());
            }
        }
    }

    public void update() {
        Tile mergeTwin;

        boolean makeNewTile = false;    // True if a new tile should be made once board is unlocked
        if(!playerCanMove) makeNewTile = true;
        playerCanMove = true;

        for(Tile t : tilesOnBoard) {
            // Test whether at least one tile is still sliding
            if(t.getXPos() != t.getGoalXPos() || t.getYPos() != t.getGoalYPos())
                playerCanMove = false;

            t.update();

            // If both tiles to be merged have reached their final positions
            if(t.needsToBeReplaced() && !t.getSliding() && !t.getMergeTwin().getSliding()) {
                mergeTwin = t.getMergeTwin();

                // Remove both the tile and its twin
                tilesOnBoard.remove(t);
                tilesOnBoard.remove(mergeTwin);

                // Create a new tile
                int newValue = t.getValue()*2;
                Tile newTile = new Tile(t.getValue()*2, t.getRow(), t.getCol(), tileImages[getTileIndex(newValue)], t.getXPos(), t.getYPos());
                board[t.getRow()][t.getCol()] = newTile;
                tilesOnBoard.add(newTile);
            }
        }

        if(makeNewTile && playerCanMove) {
            createNewTile();
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(boardImage, null, boardSpace, null);

        // Draw any tiles that exist on the board
        for(Tile t : tilesOnBoard) {
            t.draw(canvas);
        }
    }

    // Called when user flings right
    public void slideRight() {
        simpleSlideRight();
        mergeRight();
        simpleSlideRight();
        for(Tile t : tilesOnBoard) {
            t.performSlide();
        }
    }

    // Called when user flings left
    public void slideLeft() {
        simpleSlideLeft();
        mergeLeft();
        simpleSlideLeft();
        for(Tile t : tilesOnBoard) {
            t.performSlide();
        }
    }

    // Called when user flings up
    public void slideUp() {
        simpleSlideUp();
        mergeUp();
        simpleSlideUp();
        for(Tile t : tilesOnBoard) {
            t.performSlide();
        }
    }

    // Called when user flings down
    public void slideDown() {
        simpleSlideDown();
        mergeDown();
        simpleSlideDown();
        for(Tile t : tilesOnBoard) {
            t.performSlide();
        }
    }

    // Reset board to starting position
    public void reset() {
        removeTiles();
        createNewTile();
        createNewTile();
    }

    // Determine direction given x and y velocity (of a fling)
    public Direction getDirection(float velX, float velY) {

        if (velX > 0 && velX > Math.abs(velY))
            return Direction.RIGHT;

        else if (velX < 0 && Math.abs(velX) > Math.abs(velY))
            return Direction.LEFT;

        else if (velY < 0 && Math.abs(velY) > Math.abs(velX))
            return Direction.UP;

        else if (velY > 0 && velY > Math.abs(velX))
            return Direction.DOWN;

        else
            return Direction.UNKNOWN;
    }

    // Determine if there's room for tiles to move in the given direction
    public boolean canMoveInDirection(Direction direction) {
        if(direction == Direction.RIGHT || direction == Direction.LEFT) {
            int tilesInRow, start;
            for(int i = 0; i < ROWS; i++) {

                tilesInRow = 0;
                // Determine number of tiles in this row
                for(int j = 0; j < ROWS; j++)
                    if(board[i][j] != null)
                        tilesInRow++;

                start = (direction == Direction.RIGHT)? ROWS-tilesInRow : 0;
                // Can't move if tiles are already as far as possible in appropriate direction
                for(int j = start; j < start+tilesInRow; j++) {
                    if(board[i][j] == null )
                        return true;

                    // Can also move if two identical tiles in a row
                    if(j < start+tilesInRow-1 && board[i][j+1] != null && board[i][j].getValue() == board[i][j+1].getValue())
                        return true;
                }
            }
        }
        else if(direction == Direction.DOWN || direction == Direction.UP) {
            int tilesInCol, start;
            for(int j = 0; j < ROWS; j++) {

                tilesInCol = 0;
                // Determine number of tiles in this col
                for(int i = 0; i < ROWS; i++)
                    if(board[i][j] != null)
                        tilesInCol++;

                start = (direction == Direction.DOWN)? ROWS-tilesInCol : 0;
                // Can't move if tiles are already as far as possible in appropriate direction
                for(int i = start; i < start+tilesInCol; i++) {
                    if(board[i][j] == null)
                        return true;

                    // Can also move if two identical tiles in a col
                    if(i < start+tilesInCol-1 && board[i+1][j] != null && board[i][j].getValue() == board[i+1][j].getValue())
                        return true;
                }
            }
        }
        return false;
    }

    // Find goal positions for all tiles if sliding right, but not performing any merges (treating all tiles as walls)
    private void simpleSlideRight() {

        Tile[] tilesInCurRow;
        int tilesInCurRowIndex;

        // Do one row at a time
        for(int i = 0; i < ROWS; i++) {
            tilesInCurRow = new Tile[ROWS];
            tilesInCurRowIndex = ROWS-1;

            // Fill tilesInCurRow with all the tiles in row i, but moved as far right as they can go
            for(int j = ROWS-1; j >= 0; j--) {
                if(board[i][j] != null) {
                    tilesInCurRow[tilesInCurRowIndex] = board[i][j];
                    tilesInCurRowIndex--;
                }
            }
            copyRowToBoard(tilesInCurRow, i);
        }
    }

    // Find goal positions for all tiles if sliding left, but not performing any merges (treating all tiles as walls)
    private void simpleSlideLeft() {

        Tile[] tilesInCurRow;
        int tilesInCurRowIndex;

        // Do one row at a time
        for(int i = 0; i < ROWS; i++) {
            tilesInCurRow = new Tile[ROWS];
            tilesInCurRowIndex = 0;

            // Fill tilesInCurRow with all the tiles in row i, but moved as far left as they can go
            for (int j = 0; j < ROWS; j++) {
                if (board[i][j] != null) {
                    tilesInCurRow[tilesInCurRowIndex] = board[i][j];
                    tilesInCurRowIndex++;
                }
            }
            copyRowToBoard(tilesInCurRow, i);
        }
    }


    // Find goal positions for all tiles if sliding up, but not performing any merges (treating all tiles as walls)
    private void simpleSlideUp() {
        Tile[] tilesInCurCol;
        int tilesInCurColIndex;

        // Do one column at a time
        for(int j = 0; j < ROWS; j++) {
            tilesInCurCol = new Tile[ROWS];
            tilesInCurColIndex = 0;

            // Fill tilesInCurCol with all the tiles in col j, but moved as far up as they can go
            for(int i = 0; i < ROWS; i++) {
                if(board[i][j] != null) {
                    tilesInCurCol[tilesInCurColIndex] = board[i][j];
                    tilesInCurColIndex++;
                }
            }
            copyColToBoard(tilesInCurCol, j);
        }
    }


    // Find goal positions for all tiles if sliding down, but not performing any merges (treating all tiles as walls)
    private void simpleSlideDown() {

        Tile[] tilesInCurCol;
        int tilesInCurColIndex;

        // Do one col at a time
        for(int j = 0; j < ROWS; j++) {
            tilesInCurCol = new Tile[ROWS];
            tilesInCurColIndex = ROWS-1;

            // Fill tilesInCurRow with all the tiles in col i, but moved as far down as they can go
            for(int i = ROWS-1; i >= 0; i--) {
                if(board[i][j] != null) {
                    tilesInCurCol[tilesInCurColIndex] = board[i][j];
                    tilesInCurColIndex--;
                }
            }
            copyColToBoard(tilesInCurCol, j);
        }
    }

    // Whenever two tiles appear in a row, merge them to the right
    private void mergeRight() {
        Tile curTile, nextTile;
        for(int i = 0; i < ROWS; i++) {
            for(int j = ROWS-1; j > 0; j--) {
                curTile = board[i][j];
                nextTile = board[i][j - 1];
                if(curTile != null && nextTile != null) {
                    if (curTile.getValue() == nextTile.getValue()) {
                        mergeInto(nextTile, curTile);
                    }
                }
            }
        }
    }

    // Whenever two tiles appear in a row, merge them to the left
    private void mergeLeft() {
        Tile curTile, nextTile;
        for(int i = 0; i < ROWS; i++) {
            for(int j = 0; j < ROWS-1; j++) {
                curTile = board[i][j];
                nextTile = board[i][j + 1];
                if(curTile != null && nextTile != null) {
                    if(curTile.getValue() == nextTile.getValue()) {
                        mergeInto(nextTile, curTile);
                    }
                }
            }
        }
    }

    // Whenever two tiles appear in a column, merge them upward
    private void mergeUp() {
        Tile curTile, nextTile;
        for(int j = 0; j < ROWS; j++) {
            for(int i = 0; i < ROWS-1; i++) {
                curTile = board[i][j];
                nextTile = board[i+1][j];
                if(curTile != null && nextTile != null) {
                    if(curTile.getValue() == nextTile.getValue()) {
                        mergeInto(nextTile, curTile);
                    }
                }
            }
        }
    }

    // Whenever two tiles appear in a column, merge them downward
    private void mergeDown() {
        Tile curTile, nextTile;
        for(int j = 0; j < ROWS; j++) {
            for(int i = ROWS-1; i > 0; i--) {
                curTile = board[i][j];
                nextTile = board[i-1][j];
                if(curTile != null && nextTile != null) {
                    if(curTile.getValue() == nextTile.getValue()) {
                        mergeInto(nextTile, curTile);
                    }
                }
            }
        }
    }

    // Merge from one tile to another,
    private void mergeInto(Tile from, Tile to) {
        board[from.getRow()][from.getCol()] = null;    // Remove one of the tiles from the board (it is still in the list of tiles)

        from.setGoalPos(to.getXPos(), to.getYPos());
        from.setRowCol(to.getRow(), to.getCol());
        from.setMergeAfterSlide(true);
        to.setMergeAfterSlide(true);
        from.setMergeTwin(to);
        to.setMergeTwin(from);
    }

    // Copy the given row to the board at the given row index, and set relevant parameters for that tile
    private void copyRowToBoard(Tile[] row, int rowIndex) {
        for(int j = 0; j < ROWS; j++) {
            board[rowIndex][j] = row[j];
            if(board[rowIndex][j] != null) {
                board[rowIndex][j].setRowCol(rowIndex, j);
                board[rowIndex][j].setGoalPos(getTilePosX(j), board[rowIndex][j].getYPos());

                // If another tile is to be merged with this one, reset its variables as well
                Tile mergeTwin = board[rowIndex][j].getMergeTwin();
                if(mergeTwin != null) {
                    mergeTwin.setRowCol(rowIndex, j);
                    mergeTwin.setGoalPos(getTilePosX(j), board[rowIndex][j].getYPos());
                }
            }
        }
    }

    // Copy the given col to the board at the given col index
    private void copyColToBoard(Tile[] col, int colIndex) {
        for(int i = 0; i < ROWS; i++) {
            board[i][colIndex] = col[i];
            if(board[i][colIndex] != null) {
                board[i][colIndex].setRowCol(i, colIndex);
                board[i][colIndex].setGoalPos(board[i][colIndex].getXPos(), getTilePosY(i));

                // If another tile is to be merged with this one, reset its variables as well
                Tile mergeTwin = board[i][colIndex].getMergeTwin();
                if(mergeTwin != null) {
                    mergeTwin.setRowCol(i, colIndex);
                    mergeTwin.setGoalPos(board[i][colIndex].getXPos(), getTilePosY(i));
                }
            }
        }
    }

    // Determine if the game has been won (if a 2048 tile exists)
    public boolean gameWon() {
        for(Tile t : tilesOnBoard) {
            if(t.getValue() == WIN_TILE) {
                return true;
            }
        }
        return false;
    }

    // Determine if the game has been lost (all spaces are full)
    public boolean gameLost() {
        // Cannot have lost if there are some empty tiles
        if(tilesOnBoard.size() <  ROWS*ROWS) {
            return false;
        }

        // Check if there are any two consecutive tiles
        for(int i = 0; i < ROWS; i++) {
            for(int j = 0; j < ROWS; j++) {
                if(j < ROWS-1 && board[i][j].getValue() == board[i][j+1].getValue())
                    return false;
                if(i < ROWS-1 && board[i][j].getValue() == board[i+1][j].getValue())
                    return false;
            }
        }
        return true;
    }

    // Don't allow player to move
    public void lock() {
        playerCanMove = false;
    }

    // Return true if player can move (no sliding in progress)
    public boolean boardUnlocked() {
        return playerCanMove;
    }

    // Randomly place a 2 or 4 tile on the board
    // REQ: Board isn't full (if full, infinite loop will occur)
    public void createNewTile() {
        // Decide whether to place 2 or 4 tile
        int tileValue = (rand.nextInt() % 2 == 0)? 2 : 4;

        // Find an empty space to put the new tile
        int i = rand.nextInt(ROWS) % ROWS;
        int j = rand.nextInt(ROWS) % ROWS;

        try {
            while (tileAt(i, j)) {
                i = rand.nextInt(ROWS) % ROWS;
                j = rand.nextInt(ROWS) % ROWS;
            }
        } catch(ArrayIndexOutOfBoundsException e) { e.printStackTrace(); }

        createTileAt(tileValue, i, j);
    }

    public void createTileAt(int value, int row, int col) {
        // Determine tile parameters
        int tileIndex = getTileIndex(value);
        int tileXPos = tilesX + (Tile.size * col);
        int tileYPos = tilesY + (Tile.size * row);

        // Create the tile and add it to both tile lists
        Tile tile = new Tile(value, row, col, tileImages[tileIndex], tileXPos, tileYPos);
        tilesOnBoard.add(tile);
        board[row][col] = tile;
    }

    public ArrayList<Tile> getTilesOnBoard() {
        return tilesOnBoard;
    }

    // Determines whether a tile exists as the given row and column
    private boolean tileAt(int row, int col) {
        return (board[row][col] != null);
    }

    // Get tile index from value.
    // Eg: Input of 16 returns 3, 32 returns 4, etc.
    private int getTileIndex(int tileValue) {
        return (int)(Math.log(tileValue) / Math.log(2.0)) - 1;
    }

    // Return the tile x pixel position given the column
    private int getTilePosX(int col) {
        int tileSize = (int)(TILE_IMG_LENGTH * scaleFactor);
        return tilesX + (tileSize * col);
    }

    // Return the tile y pixel position given the row
    private int getTilePosY(int row) {
        int tileSize = (int)(TILE_IMG_LENGTH * scaleFactor);
        return tilesY + (tileSize * row);
    }

    // Remove all tiles on board
    private void removeTiles() {
        tilesOnBoard.clear();
        board = new Tile[ROWS][ROWS];
    }

    // For debugging
    private void printBoard() {
        int nTiles = 0;
        for(int i = 0; i < ROWS; i++) {
            for(int j = 0; j < ROWS; j++) {
                if(board[i][j] != null) {
                    System.out.print("[" + board[i][j].getValue() + "]");
                    nTiles++;
                }
                else
                    System.out.print("[ ]");
            }
            System.out.println();
        }

        if(nTiles != tilesOnBoard.size())
            System.out.println("Note: Likely error found. Counted " + nTiles + " tiles on board, but tilesOnBoard contains "+tilesOnBoard.size() + " tiles.");
    }
}
