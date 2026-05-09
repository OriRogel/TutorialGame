package com.example.tutorialgame.engine.ui.effects;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_HEIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.entities.characters.Player;
import com.example.tutorialgame.managers.CameraManager;
import com.example.tutorialgame.managers.MapManager;
import com.example.tutorialgame.ui.base.BaseActivity;

public class MapTransitionEffect {
    public enum State { IDLE, CLOSING, OPENING }

    private State currentState = State.IDLE;
    private float circleRadius;
    private final float maxRadius;
    private final Path path = new Path();
    private static final float SPEED = 15.625f * TILE_SIZE; // פיקסלים לשנייה
    private static final PointF targetPos = new PointF();

    public MapTransitionEffect() {
        this.maxRadius = (float) Math.hypot(SCREEN_WIDTH, SCREEN_HEIGHT) / 1.5f;
        this.circleRadius = maxRadius;
    }

    public void startClosing() {
        currentState = State.CLOSING;
        SoundManager.getInstance(BaseActivity.getContext()).playRndPitchSfx(R.raw.sfx_iris_close);
    }

    public void startOpening() {
        currentState = State.OPENING;
        SoundManager.getInstance(BaseActivity.getContext()).playRndPitchSfx(R.raw.sfx_iris_open);
    }

    public void update(double delta) {
        if (currentState == State.CLOSING) {
            circleRadius -= (float) (SPEED * delta);
            if (circleRadius <= 0) {
                circleRadius = 0;
                // כאן המנוע ידע להחליף מפה
            }
        } else if (currentState == State.OPENING) {
            circleRadius += (float) (SPEED * delta);
            if (circleRadius >= maxRadius) {
                circleRadius = maxRadius;
                currentState = State.IDLE;
            }
        }
    }

    public void draw(Canvas c) {
        if (currentState == State.IDLE && circleRadius >= maxRadius) return;

        updateTargetPos();
        path.reset();
        path.addCircle(targetPos.x, targetPos.y, circleRadius, Path.Direction.CW);

        c.save(); // חותכים את העיגול מתוך הקנבס כך שהציור הבא יצבע רק את מה שמחוץ לעיגול
        c.clipOutPath(path);
        c.drawColor(Color.BLACK);
        c.restore();
    }

    private void updateTargetPos() {
        Player p = MapManager.getCurrentMap().getPlayer();
        targetPos.set(
                p.getHitBox().centerX() * CameraManager.getTempZoom() + CameraManager.getOffsetX() * CameraManager.getTempZoom(),
                p.getHitBox().centerY() * CameraManager.getTempZoom() + CameraManager.getOffsetY() * CameraManager.getTempZoom()
        );
    }

    public boolean isFullyClosed() { return currentState == State.CLOSING && circleRadius <= 0; }
    public State getCurrentState() { return currentState; }
}