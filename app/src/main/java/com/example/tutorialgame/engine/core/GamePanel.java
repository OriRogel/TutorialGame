package com.example.tutorialgame.engine.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.annotation.NonNull;


import com.example.tutorialgame.cloud.UserRepository;
import com.example.tutorialgame.engine.audio.SoundManager;

@SuppressLint("ViewConstructor")
public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    private final Game game;

    public GamePanel(Context context, UserRepository userRepository, SoundManager soundManager) {
        super(context);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        game = new Game(holder, context, userRepository, soundManager);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return game.touchEvent(event);

    }

    public Game getGame() {
        return game;
    }

    /**
     * מפסיק את לולאת המשחק (pause)
     */
    public void pauseGame() {
        game.stopGameLoop();
    }

    /**
     * מפעיל (resume) את לולאת המשחק אם היא לא רצה
     */
    public void resumeGame() {
        game.startGameLoop();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        resumeGame();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        pauseGame();
    }
}
