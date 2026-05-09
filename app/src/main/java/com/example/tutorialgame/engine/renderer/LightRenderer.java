package com.example.tutorialgame.engine.renderer;

import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_HEIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

import com.example.tutorialgame.engine.ui.effects.lighting.LightSource;
import com.example.tutorialgame.entities.characters.Player;
import com.example.tutorialgame.environments.GameMap;
import com.example.tutorialgame.managers.CameraManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LightRenderer {
    // רזולוציה גבוהה יותר מבעבר (4), כי שיטת ה-Multiply סופר יעילה
    private static final float SCALE_DOWN = 4f;
    private static final int QUANTIZE_STEP = 12;

    // מפת תאורה אחת בלבד! אין יותר חושך ואור בנפרד
    private Bitmap lightmapBitmap;
    private Canvas lightmapCanvas;

    private final LinkedHashMap<Integer, Bitmap> whiteMaskCache = new LinkedHashMap<Integer, Bitmap>(32, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, Bitmap> eldest) {
            return size() > 32;
        }
    };

    private final RectF dstRect = new RectF();
    private final Rect screenRect = new Rect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

    // מברשת לציור מקורות אור זוהרים על גבי המפה
    private final Paint addLightPaint = new Paint();
    // מברשת שמכפילה את מפת התאורה עם מסך המשחק האמיתי
    private final Paint screenMultiplyPaint = new Paint();
    // המברשת שתוסיף את ה"זוהר" הצבעוני החסר
    private final Paint directGlowPaint = new Paint();
    private final LightSource[] visibleLights = new LightSource[50];
    private int visibleCount = 0;

    public LightRenderer() {
        int lowResW = Math.max(16, (int) (SCREEN_WIDTH / SCALE_DOWN));
        int lowResH = Math.max(16, (int) (SCREEN_HEIGHT / SCALE_DOWN));

        lightmapBitmap = Bitmap.createBitmap(lowResW, lowResH, Bitmap.Config.ARGB_8888);
        lightmapCanvas = new Canvas(lightmapBitmap);

        // מברשת האור מוסיפה צבע. חושך + צבע = זוהר
        addLightPaint.setAntiAlias(false);
        addLightPaint.setFilterBitmap(false);
        addLightPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));

        directGlowPaint.setAntiAlias(false);
        directGlowPaint.setFilterBitmap(false);
        directGlowPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
        directGlowPaint.setAlpha(130); // אתה יכול לשחק עם זה: 150 לזוהר חזק, 80 לזוהר עדין

        // הפעולה היחידה שתורנדר למסך - Multiply.
        // לבן מראה משחק רגיל, צבעוני צובע אותו, שחור מסתיר אותו.
        screenMultiplyPaint.setAntiAlias(false);
        screenMultiplyPaint.setFilterBitmap(false);
        screenMultiplyPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    public void render(Canvas targetCanvas, GameMap map) {
        if (map == null || map.getAmbientDarkness() <= 0) return;

        final float zoom = CameraManager.getTempZoom();
        final float renderZoom = zoom / SCALE_DOWN;
        final float offX = (CameraManager.getOffsetX() * zoom) / SCALE_DOWN;
        final float offY = (CameraManager.getOffsetY() * zoom) / SCALE_DOWN;

        // 1. צביעת הרקע של מפת התאורה (החושך המקומי)
        // במקום אלפא, אנחנו ממירים את ה"חושך" לצבע אפור/כחלחל כהה.
        int ambientLevel = Math.max(0, 255 - map.getAmbientDarkness());
        // ניתן לזה גוון טיפ-טיפה כחול שירגיש כמו לילה
        int ambientColor = Color.rgb(ambientLevel, ambientLevel, Math.min(255, ambientLevel + 15));
        lightmapCanvas.drawColor(ambientColor, PorterDuff.Mode.SRC);

        // 2. איסוף מהיר של אורות (Culling) - חוסך כמות אדירה של חישובים
        collectVisibleLights(map.getLightSources(), offX, offY, renderZoom);

        // 3. הוספת האורות המקומיים
        for (int i = 0; i < visibleCount; i++) {
            drawLightToMap(visibleLights[i], offX, offY, renderZoom);
        }

        // 4. הוספת האור הלבן של השחקן
        if (map.getPlayer() != null) {
            drawPlayerLightToMap(map.getPlayer(), offX, offY, renderZoom);
        }

        // 5. פעולת ציור *אחת יחידה* על כל המסך בשיטת Multiply!
        targetCanvas.drawBitmap(lightmapBitmap, null, screenRect, screenMultiplyPaint);

        // 6. *** התוספת החדשה: הילה צבעונית ישירה! ***
        // מחשבים את המיקומים האמיתיים של המסך (ללא החלוקה ב-SCALE_DOWN)
        final float realOffX = CameraManager.getOffsetX() * zoom;
        final float realOffY = CameraManager.getOffsetY() * zoom;

        for (int i = 0; i < visibleCount; i++) {
            LightSource ls = visibleLights[i];

            // אנחנו מוסיפים זוהר רק לאורות צבעוניים (לא לאור הלבן של השחקן)
            if (!ls.isPureWhite()) {
                drawGlowDirectlyToScreen(targetCanvas, ls, zoom, realOffX, realOffY);
            }
        }
    }

    private void drawGlowDirectlyToScreen(Canvas targetCanvas, LightSource light, float zoom, float offX, float offY) {
        float cx = (light.getPos().x * zoom) + offX;
        float cy = (light.getPos().y * zoom) + offY;
        float r = light.getRadius() * zoom;

        // אנחנו משתמשים באותה מסכה קטנה שכבר קיימת בזיכרון! ללא יצירת אובייקטים חדשים
        int sizeForCache = quantize((int) ((light.getRadius() * 2f) / SCALE_DOWN));
        Bitmap mask = getWhiteMask(sizeForCache);
        if (mask == null) return;

        dstRect.set(cx - r, cy - r, cx + r, cy + r);

        // מורחים את הפילטר הצבעוני בשיטת הוספה (ADD) ישר על הלבנים של הרצפה
        directGlowPaint.setColorFilter(light.getOuterFilter());
        targetCanvas.drawBitmap(mask, null, dstRect, directGlowPaint);
        directGlowPaint.setColorFilter(null); // ניקוי הפילטר
    }

    private void collectVisibleLights(List<LightSource> lights, float offX, float offY, float renderZoom) {
        visibleCount = 0;
        if (lights == null) return;

        float lowResW = SCREEN_WIDTH / SCALE_DOWN;
        float lowResH = SCREEN_HEIGHT / SCALE_DOWN;

        for (int i = 0; i < lights.size(); i++) {
            LightSource ls = lights.get(i);
            float cx = (ls.getPos().x * renderZoom) + offX;
            float cy = (ls.getPos().y * renderZoom) + offY;
            float r = ls.getRadius() * renderZoom;

            if (cx + r > 0 && cx - r < lowResW && cy + r > 0 && cy - r < lowResH) {
                if (visibleCount < visibleLights.length) {
                    visibleLights[visibleCount++] = ls;
                }
            }
        }
    }

    private void drawLightToMap(LightSource light, float offX, float offY, float renderZoom) {
        float cx = (light.getPos().x * renderZoom) + offX;
        float cy = (light.getPos().y * renderZoom) + offY;
        float r = light.getRadius() * renderZoom;

        int size = quantize((int) (r * 2f));
        Bitmap mask = getWhiteMask(size);
        if (mask == null) return;

        dstRect.set(cx - r, cy - r, cx + r, cy + r);

        if (light.isPureWhite()) {
            // אור לבן (כמו של השחקן) פשוט מוסיף צבע לבן למפה
            addLightPaint.setColorFilter(null);
            lightmapCanvas.drawBitmap(mask, null, dstRect, addLightPaint);
        } else {
            // אור צבעוני מוחל על המסכה ומוסף למפה
            addLightPaint.setColorFilter(light.getOuterFilter());
            lightmapCanvas.drawBitmap(mask, null, dstRect, addLightPaint);

            float innerR = r * 0.6f;
            int innerSize = quantize((int) (innerR * 2f));
            Bitmap innerMask = getWhiteMask(innerSize);

            if (innerMask != null) {
                dstRect.set(cx - innerR, cy - innerR, cx + innerR, cy + innerR);
                addLightPaint.setColorFilter(light.getInnerFilter());
                lightmapCanvas.drawBitmap(innerMask, null, dstRect, addLightPaint);
            }
        }
        addLightPaint.setColorFilter(null);
    }

    private void drawPlayerLightToMap(Player p, float offX, float offY, float renderZoom) {
        float radius = 160 * renderZoom;
        float px = (p.getProjectedHitBox().centerX() * renderZoom) + offX;
        float py = (p.getProjectedHitBox().centerY() * renderZoom) + offY;

        dstRect.set(px - radius, py - radius, px + radius, py + radius);
        int size = quantize((int) (radius * 2f));
        Bitmap mask = getWhiteMask(size);

        if (mask != null) {
            // אור לבן טהור. אנחנו שמים פילטר null כדי לבטל את המסנן השחור
            // שהוגדר בטעות ב-LightSource, כך שהאור יהיה באמת לבן מאיר.
            addLightPaint.setColorFilter(null);
            lightmapCanvas.drawBitmap(mask, null, dstRect, addLightPaint);
        }
    }

    private Bitmap getWhiteMask(int sizePx) {
        if (sizePx <= 0) return null;
        Integer key = sizePx;
        Bitmap bmp = whiteMaskCache.get(key);
        if (bmp != null) return bmp;

        Bitmap mask = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(mask);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        RadialGradient g = new RadialGradient(
                sizePx / 2f, sizePx / 2f, sizePx / 2f,
                new int[]{Color.WHITE, Color.argb(128, 255, 255, 255), Color.TRANSPARENT},
                new float[]{0f, 0.45f, 1f},
                Shader.TileMode.CLAMP
        );
        p.setShader(g);
        c.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, p);

        whiteMaskCache.put(key, mask);
        return mask;
    }

    private int quantize(int diameterPx) {
        if (diameterPx <= 0) return QUANTIZE_STEP;
        return Math.max(QUANTIZE_STEP, ((diameterPx + QUANTIZE_STEP - 1) / QUANTIZE_STEP) * QUANTIZE_STEP);
    }
}