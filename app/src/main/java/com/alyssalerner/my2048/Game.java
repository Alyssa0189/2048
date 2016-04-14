package com.alyssalerner.my2048;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;


import java.util.ArrayList;

public class Game extends AppCompatActivity {
    GamePanel gamePanel;

    @Override
    protected void onCreate(Bundle inState) {
        super.onCreate(inState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ArrayList<SerializableTile> savedTiles = null;

        if(inState != null && inState.getSerializable("tiles") != null) {
            savedTiles = (ArrayList<SerializableTile>) inState.getSerializable("tiles");
        }

        gamePanel = new GamePanel(this, savedTiles);
        setContentView(gamePanel);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            System.out.println("Tiles: " + gamePanel.getBoard().getTilesOnBoard().size());
        } catch(Exception e) { System.out.println("No board yet"); }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Generate a list of serializable tiles from tiles on the board
        ArrayList<SerializableTile> savedTiles = new ArrayList<SerializableTile>();
        for(Tile tile : gamePanel.getBoard().getTilesOnBoard()) {
            savedTiles.add(new SerializableTile(tile.getValue(), tile.getRow(), tile.getCol()));
        }

        outState.putSerializable("tiles", savedTiles);
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
