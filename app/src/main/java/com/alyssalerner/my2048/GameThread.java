package com.alyssalerner.my2048;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Created by Alyssa on 2016-02-19.
 */
public class GameThread extends Thread {
    private int FPS = 32;
    private SurfaceHolder surfaceHolder;
    private GamePanel gamePanel;
    private boolean running;
    private static Canvas canvas;
    long targetTime = (int)(1000.0 / FPS);   // Time per loop

    public GameThread(SurfaceHolder sh, GamePanel gp) {
        super();
        this.surfaceHolder = sh;
        this.gamePanel = gp;
    }

    @Override
    public void run() {

        long startTime = System.nanoTime();
        while(running) {
            startTime = System.nanoTime();
            canvas = null;

            // Try locking canvas for pixel editing
            try {
                canvas = this.surfaceHolder.lockCanvas();

                // Each game loop, update and draw the game once.
                synchronized (surfaceHolder) {
                    this.gamePanel.draw(canvas);
                    this.gamePanel.update();
                }
            } catch (Exception e) {
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Decide how long to wait
            long timeItTookToDraw = (System.nanoTime() - startTime) / 1000000;
            long waitTime = targetTime - timeItTookToDraw;

            // Wait
            try {
                this.sleep(waitTime);
            } catch (Exception e) {
            }
        }
    }

    public void setRunning(boolean isRunning) {
        running = isRunning;
    }
}
