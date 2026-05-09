package com.example.tutorialgame.entities.foregrounds.breakable;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.entities.Entity;
import com.example.tutorialgame.managers.objectpool.ObjectPoolManager;
import com.example.tutorialgame.ui.base.BaseActivity;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an environmental object that can be smashed into pieces.
 * Managed by a State Machine and optimized with Object Pooling.
 */
public class BreakableEntity extends Entity {
    
    private enum State { INTACT, PIECES_FLYING, WAITING_TO_RESPAWN, RESPAWNING }
    private State currentState = State.INTACT;

    private final BreakableType type;
    private final List<BrokenParticle> activeParticles = new ArrayList<>();
    private float respawnProgress = 0f;
    private static final float RESPAWN_ANIM_DURATION = 0.5f; 
    private float respawnTimer;
    private final Paint paint = new Paint();
    private final RectF destRect = new RectF();

    public BreakableEntity(PointF pos, BreakableType type) {
        super(pos, type.getWidth(), type.getHeight(), true);
        this.type = type;
    }


    public boolean handleHit() {
        if (currentState != State.INTACT) return false;

        if (type.getParticles().getLevelRequired() > MyApp.getProgress().getLevel()) {
            SoundManager.getInstance(BaseActivity.getContext()).playSfx(R.raw.sfx_impact_player);
            return true; // דורש ריקוייל
        }

        currentState = State.PIECES_FLYING;
        this.collider = false;

        // Visual Feedback: Acquire particles from the centralized pool
        int pieces = 4 + MyApp.RND.nextInt(4);
        for (int i = 0; i < pieces; i++) {
            activeParticles.add(ObjectPoolManager.acquireParticle(
                    hitBox.centerX(),
                    hitBox.centerY(),
                    type.getParticles().getRandomParticle())
            );
        }

        SoundManager.getInstance(BaseActivity.getContext()).playRndPitchSfx(type.getParticles().getSfxId());
        this.respawnTimer = (type.getRespawnTime() + MyApp.RND.nextInt(10000)) / 1000f;
        return false;
    }

    public void update(double delta) {
        switch (currentState) {
            case INTACT:
                break;
            case PIECES_FLYING:
                updateParticles(delta);
                if (activeParticles.isEmpty()) {
                    if (type.canRespawn()) {
                        currentState = State.WAITING_TO_RESPAWN;
                    } else active = false; 
                }
                break;
            case WAITING_TO_RESPAWN:
                respawnTimer -= (float) delta;
                if (respawnTimer <= 0) {
                    currentState = State.RESPAWNING;
                    SoundManager.getInstance(BaseActivity.getContext()).playSpatialSfx(R.raw.sfx_pop, hitBox.centerX());
                }
                break;
            case RESPAWNING:
                respawnProgress += (float) (delta / RESPAWN_ANIM_DURATION);
                if (respawnProgress >= 1f) {
                    respawnProgress = 0;
                    currentState = State.INTACT;
                    collider = true;
                }
        }
    }

    private void updateParticles(double delta) {
        for (int i = activeParticles.size() - 1; i >= 0; i--) {
            BrokenParticle p = activeParticles.get(i);
            p.update(delta);
            if (!p.isActive()) {
                ObjectPoolManager.releaseParticle(p); // Recycle the object
                activeParticles.remove(i);
            }
        }
    }

    @Override
    public void draw(Canvas c) {
        if (currentState == State.INTACT) {
            c.drawBitmap(type.getSpr(), hitBox.left, hitBox.top, null);
        } else if (currentState == State.RESPAWNING) {
            drawRespawnAnimation(c);
        } else if (currentState == State.PIECES_FLYING) {
            for (BrokenParticle p : activeParticles) p.draw(c);
        }
    }

    private void drawRespawnAnimation(Canvas c) {
        float t = respawnProgress;
        float invT = 1f - t;
        float scaleY = (float) (1.0 + Math.sin(t * Math.PI * 2) * 0.3f * invT);
        float scaleX = 1.0f / scaleY;

        float width = hitBox.width() * scaleX;
        float height = hitBox.height() * scaleY;

        float left = hitBox.centerX() - width / 2f;
        float bottom = hitBox.bottom;
        float top = bottom - height;

        destRect.set(left, top, left + width, bottom);
        paint.setAlpha((int) (t * 255));

        c.drawBitmap(type.getSpr(), null, destRect, paint);
    }
}
