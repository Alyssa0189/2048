package com.alyssalerner.my2048;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;


/**
 * Created by Alyssa on 2016-02-20.
 */
public class Tile {
    public final int TILE_IMG_LENGTH = 100; // The height of each tile image in file
    public final int TILE_SPEED = 40;    // Amount tile should move per frame
    public static int size;   // Pixel width and height of tile

    private int value;  // The tile's value (2, 4, 8, ...)
    private int row;    // The tile's row
    private int col;    // The tile's column
    private Bitmap image;   // Image of the tile
    private int xPos;   // Pixel x-position of tile
    private int yPos;   // Pixel y-position of tile

    private int goalXPos;   // Position the tile will eventually slide to
    private int goalYPos;
    private boolean hasBeenMerged;  // True if this tile has already been merged with another on the current slide
    // For the purpose of knowing not to merge it again

    private boolean mergeAfterSlide;   /* True if this tile will merge with another to create a new tile after the current slide
                                          Note: Once set to true, it is expected that this tile will be removed from the board,
                                          so it will never be set to false again */
    private boolean sliding;    // True when sliding is in process
    private Tile mergeTwin;     // Tile that this tile will merge with

    public Tile(int value, int row, int col, Bitmap image, int xPos, int yPos) {
        this.value = value;
        this.row = row;
        this.col = col;
        this.image = image;

        this.xPos = xPos;
        this.yPos = yPos;
        this.goalXPos = xPos;
        this.goalYPos = yPos;

        mergeAfterSlide = false;
        sliding = false;
        hasBeenMerged = false;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getValue() {
        return value;
    }

    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }

    public int getGoalXPos() {
        return goalXPos;
    }

    public int getGoalYPos() {
        return goalYPos;
    }

    public int getSize() {
        return size;
    }

    public boolean hasBeenMerged() {
        return hasBeenMerged;
    }

    public void setHasBeenMerged(boolean hasBeenMerged) {
        this.hasBeenMerged = hasBeenMerged;
    }

    public boolean getSliding() {
        return sliding;
    }

    public void setMergeAfterSlide(boolean mergeAfterSlide) {
        this.mergeAfterSlide = mergeAfterSlide;
    }

    // Return true if this tile should be destroyed and replaced with higher tile after slide is complete
    public boolean needsToBeReplaced() {
        return mergeAfterSlide;
    }

    // Sets the tile that this will be merged with
    public void setMergeTwin(Tile mergeTwin) {
        this.mergeTwin = mergeTwin;
    }

    // Get the tile that this tile merges with
    public Tile getMergeTwin() {
        return mergeTwin;
    }

    public void setRowCol(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void setGoalPos(int goalXPos, int goalYPos) {
        this.goalXPos = goalXPos;
        this.goalYPos = goalYPos;
    }

    public void update() {
        // If tile is close enough to the goal position, place it at the goal position
        if ((goalXPos != xPos && (Math.abs(goalXPos - xPos)) < TILE_SPEED) ||
                (goalYPos != yPos && (Math.abs(goalYPos - yPos)) < TILE_SPEED)) {
            sliding = false;
            xPos = goalXPos;
            yPos = goalYPos;
        }


        // Perform a slide increment
        else if (goalXPos > xPos) xPos += TILE_SPEED;
        else if (goalXPos < xPos) xPos -= TILE_SPEED;
        else if (goalYPos > yPos) yPos += TILE_SPEED;
        else if (goalYPos < yPos) yPos -= TILE_SPEED;

            // If sliding motion complete, set sliding to false
        else if (sliding) {
            sliding = false;
        }
    }

    public void draw(Canvas canvas) {

        Rect tileSpace = new Rect(xPos, yPos, xPos + size, yPos + size);    // Where tile will be placed

        try {
            canvas.drawBitmap(image, null, tileSpace, null);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    // Begin the slide animation on this tile to the goal row and column
    // Should be called after final positions are known
    public void performSlide() {
        if (goalXPos != xPos || goalYPos != yPos) {
            sliding = true;
        }
    }
}