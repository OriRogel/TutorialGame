package com.example.tutorialgame.engine.ui;

import static android.content.Context.MODE_PRIVATE;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_HEIGHT;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.view.MotionEvent;

import com.example.tutorialgame.engine.renderer.TextRenderer;
import com.example.tutorialgame.engine.ui.effects.impcateffects.ImpactEffectType;
import com.example.tutorialgame.engine.ui.effects.impcateffects.TapEffect;

/**
 * Manages UI overlays like FPS counter and tap effects.
 */
public class HUDManager {
    private final TextRenderer fpsRenderer = new TextRenderer(TILE_SIZE / 2f);
    private final TapEffect tapEffect = new TapEffect(null);
    private final PointF lastTouch = new PointF();
    private final SharedPreferences spSettings;
    private boolean showFPS;

    public HUDManager(Context context) {
        this.spSettings = context.getSharedPreferences("settings", MODE_PRIVATE);
        initFPS();
        initTapEffect();
    }

    private void initFPS() {
        showFPS = spSettings.getBoolean("fps", false);
        fpsRenderer.setPosition(SCALE_MULTIPLIER, SCREEN_HEIGHT - fpsRenderer.getTextSize() / 3);
    }

    private void initTapEffect() {
        int index = spSettings.getInt("tapEffect", ImpactEffectType.SPARK.ordinal());
        ImpactEffectType[] types = ImpactEffectType.values();
        if (index >= 0 && index < types.length) {
            tapEffect.setTapType(types[index]);
        } else {
            tapEffect.setTapType(ImpactEffectType.SPARK);
        }
    }

    public void update(int fps) {
        if (showFPS) {
            fpsRenderer.updateColorBasedOnValue(fps, 0, 60);
        }
    }

    public void render(Canvas canvas, int fps) {
        if (showFPS) {
            fpsRenderer.drawText("FPS: " + fps, canvas);
        }
        tapEffect.show(canvas, lastTouch.x, lastTouch.y);
    }

    public void handleInput(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            tapEffect.resetEffect();
            lastTouch.set(event.getX(), event.getY());
        }
    }

    public void setTapEffectType(ImpactEffectType tapType) {
        this.tapEffect.setTapType(tapType);
        spSettings.edit().putInt("tapEffect", tapType.ordinal()).apply();
    }

    public void setShowFPS(boolean showFPS) {
        this.showFPS = showFPS;
    }
}
