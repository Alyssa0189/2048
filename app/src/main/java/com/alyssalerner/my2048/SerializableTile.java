package com.alyssalerner.my2048;

import java.io.Serializable;

/**
 * Created by Alyssa on 2016-02-29.
 */
public class SerializableTile implements Serializable {
    private static final long serialVersionUID = 0L;

    private int value;
    private int row;
    private int col;

    public SerializableTile(int value, int row, int col) {
        this.value = value;
        this.row = row;
        this.col = col;
    }

    public int getValue() {
        return value;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
