package com.example.tutorialgame.managers;

import static android.content.Context.MODE_PRIVATE;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_HEIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;

import android.content.SharedPreferences;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.ui.base.BaseActivity;

/**
 * Manages the game camera, including following targets, screen shake effects, 
 * zooming, and map boundary clamping.
 */
public class CameraManager {
    private static float offsetX, offsetY;
    private static float lookAtX, lookAtY;

    // Zoom Constants
    private static final float MIN_ZOOM = 1f, MAX_ZOOM = 1.9f;
    private static float finalZoom, tempZoom;

    private static final float MIN_LERP_FACTOR = 0, MAX_LERP_FACTOR = 0.7f;
    private static float lerpFactor;

    private static float mapWidth, mapHeight;

    private static float shakeDuration, shakeMagnitude;
    private static boolean CAM_SHAKE;
    private static float shakeOffsetX, shakeOffsetY;

    private static final SharedPreferences sp;

    static {
        // Load saved zoom on class initialization
        sp = BaseActivity.getContext().getSharedPreferences("settings", MODE_PRIVATE);

        finalZoom = sp.getFloat("zoom", 1.0f);
        tempZoom = finalZoom;
        lerpFactor = sp.getFloat("lerpFactor", 0.1f);
        CAM_SHAKE = sp.getBoolean("cam_shake", true);
    }

    public static void lookAt(float targetX, float targetY, double delta) {
        float lerpStep = (float) (1.0 - Math.pow(lerpFactor, delta * 10));
        lookAtX += (targetX - lookAtX) * lerpStep;
        lookAtY += (targetY - lookAtY) * lerpStep;

        updateShake(delta);
        clampLookAt();

        offsetX = (SCREEN_WIDTH / (2f * tempZoom)) - lookAtX + (shakeOffsetX / tempZoom);
        offsetY = (SCREEN_HEIGHT / (2f * tempZoom)) - lookAtY + (shakeOffsetY / tempZoom);
    }

    private static void updateShake(double delta) {
        if (shakeDuration > 0) {
            shakeDuration -= (float) delta;
            shakeOffsetX = (MyApp.getRandom().nextFloat() - 0.5f) * 2 * shakeMagnitude;
            shakeOffsetY = (MyApp.getRandom().nextFloat() - 0.5f) * 2 * shakeMagnitude;

            if (shakeDuration <= 0) {
                stopShake();
            }
        }
    }

    private static void clampLookAt() {
        if (mapWidth == 0 || mapHeight == 0) return;
        float viewW = SCREEN_WIDTH / tempZoom;
        float viewH = SCREEN_HEIGHT / tempZoom;

        if (mapWidth <= viewW) {
            lookAtX = mapWidth / 2f;
        } else {
            float minX = viewW / 2f;
            float maxX = mapWidth - (viewW / 2f);
            lookAtX = Math.max(minX, Math.min(maxX, lookAtX));
        }

        if (mapHeight <= viewH) {
            lookAtY = mapHeight / 2f;
        } else {
            float minY = viewH / 2f;
            float maxY = mapHeight - (viewH / 2f);
            lookAtY = Math.max(minY, Math.min(maxY, lookAtY));
        }
    }

    public static void setMapSize(float width, float height) {
        mapWidth = width;
        mapHeight = height;
    }

    public static void startShake(float magnitude, float duration) {
        if (!CAM_SHAKE) return;

        shakeMagnitude = magnitude * SCALE_MULTIPLIER;
        shakeDuration = duration;
    }

    public static void stopShake() {
        shakeDuration = 0;
        shakeMagnitude = 0;
        shakeOffsetX = 0;
        shakeOffsetY = 0;
    }

    /**
     * Maps normalized progress (0.0 to 1.0) to zoom range (0.7 to 2.0)
     */
    public static void setZoomFromProgress(float progress) {
        finalZoom = MIN_ZOOM + (progress * (MAX_ZOOM - MIN_ZOOM));
        tempZoom = finalZoom;
        sp.edit().putFloat("zoom", finalZoom).apply();
    }
    public static void setTempZoom(float zoom) {
        tempZoom = zoom;
    }

    public static void setLerpFactorFromProgress(float progress) {
        lerpFactor = MIN_LERP_FACTOR + (progress * (MAX_LERP_FACTOR - MIN_LERP_FACTOR));
        sp.edit().putFloat("lerpFactor", lerpFactor).apply();
    }

    /**
     * Returns the normalized progress (0.0 to 1.0) based on current zoom
     */
    public static float getNormalizedZoom() {
        return (finalZoom - MIN_ZOOM) / (MAX_ZOOM - MIN_ZOOM);
    }
    public static float getNormalizedLerpFactor() {
        return (lerpFactor - MIN_LERP_FACTOR) / (MAX_LERP_FACTOR - MIN_LERP_FACTOR);
    }
    public static void setCamShake(boolean CAM_SHAKE) {
        CameraManager.CAM_SHAKE = CAM_SHAKE;
    }

    public static float getFinalZoom() { return finalZoom; }
    public static float getTempZoom() { return tempZoom; }
    public static float getOffsetX() { return offsetX; }
    public static float getOffsetY() { return offsetY; }
    public static float getLookAtX() { return lookAtX; }
    public static float getLookAtY() { return lookAtY; }
}
