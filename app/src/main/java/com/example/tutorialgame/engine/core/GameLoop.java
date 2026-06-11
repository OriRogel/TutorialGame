package com.example.tutorialgame.engine.core;

import android.util.Log;

public class GameLoop implements Runnable {
    private static final String TAG = "GameLoop";
    private final Game game;
    private Thread gameThread;
    private volatile boolean active;
    private int FPS, errorCount;
    private static final int TARGET_FPS = 60; // קצב הפריימים הרצוי
    private static final double OPTIMAL_TIME = 1_000_000_000.0 / TARGET_FPS; // הזמן האופטימלי לפריים בננו-שניות

    public GameLoop(Game game) {
        this.game = game;
    }

    @Override
    public void run() {
        long lastFPScheck = System.currentTimeMillis();
        int fpsCounter = 0;
        long lastLoopTime = System.nanoTime();

        while (active) {
            long now = System.nanoTime();
            long updateLength = now - lastLoopTime;
            lastLoopTime = now;

            double delta = updateLength / 1_000_000_000.0;

            // --- שיפור 1: הגנה מפני Delta גדולה מדי (למשל אחרי Sleep של המכשיר) ---
            if (delta > 0.1) delta = 0.1;

            try {
                // --- שיפור 2: הגנה קריטית - אם ה-Update קורס, הלולאה לא תמות ---
                game.processNextState();
                game.update(delta);
                game.render();
                errorCount = 0;
            } catch (Exception e) {
                errorCount++;
                Log.e(TAG, "CRITICAL ERROR in Game Loop: " + e.getMessage(), e);
                if (errorCount > 5) {
                    Log.e(TAG, "Too many errors, stopping game loop!");
                    active = false; // עוצרים את הלולאה באופן יזום
                }
            }

            fpsCounter++;
            if (System.currentTimeMillis() - lastFPScheck >= 1000) {
                FPS = fpsCounter;
                fpsCounter = 0;
                lastFPScheck += 1000;
            }

            // הגבלת קצב הפריימים
            try {
                long currentTime = System.nanoTime();
                // חישוב כמה זמן נשאר לנו עד לסיום ה-OPTIMAL_TIME
                long workTime = currentTime - now;
                long sleepTimeMs = (long) ((OPTIMAL_TIME - workTime) / 1_000_000);

                if (sleepTimeMs > 0) {
                    Thread.sleep(sleepTimeMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void startGameLoop() {
        if (active) return;
        active = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void stopGameLoop() {
        active = false;
        if (gameThread != null) {
            // "הער" את ה-thread אם הוא ישן (ב-sleep) כדי שיסיים את ריצתו מהר.
            // זה לא מבטיח עצירה מיידית, אלא מאותת ללולאה להפסיק בריצה הבאה.
            gameThread.interrupt();
        }
        // אנחנו לא קוראים ל-join() כאן בכוונה. קריאה ל-join מה-MainMenu Thread
        // היא מסוכנת ויכולה לגרום לאפליקציה לקפוא (ANR).
    }

    public int getFPS() {
        return FPS;
    }
}
