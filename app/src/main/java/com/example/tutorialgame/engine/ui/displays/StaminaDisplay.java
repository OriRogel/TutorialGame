package com.example.tutorialgame.engine.ui.displays;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;

import com.example.tutorialgame.R;
import com.example.tutorialgame.components.StaminaComponent;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.entities.characters.Player;
import com.example.tutorialgame.managers.BitmapManager;

/**
 * A HUD component that visualizes the player's stamina levels using a row of icons.
 * Highly optimized for rendering performance using pre-calculated cached values.
 */
public class StaminaDisplay {
    private final Player player;

    // Static assets
    private static final Bitmap progressBmp, emptyIconBmp;
    private static final int ICON_WIDTH, ICON_HEIGHT;
    private static final float SPACING;
    private static final int MAX_PER_ICON = 50;
    private final float progressXAdjustment = SCALE_MULTIPLIER * 2;

    static {
        progressBmp = BitmapManager.getBitmap(R.drawable.lightning_progress);
        emptyIconBmp = BitmapManager.getBitmap(R.drawable.lightning_empty);
        ICON_WIDTH = progressBmp.getWidth();
        ICON_HEIGHT = progressBmp.getHeight();
        SPACING = GameConstants.Sprite.TILE_SIZE - 2 * GameConstants.Sprite.DEFAULT_SIZE;
    }

    private final Paint flashPaint;
    private final float xOffset, yOffset;

    // --- State Caching (מטמון למניעת חישובים מיותרים) ---
    private int lastMaxStamina = -1;
    private int currentStaminaCache = -1;
    private int prevStaminaCache = -1;
    private float alphaCache = -1f;
    private int iconsCount = 0;

    // --- Pre-allocated arrays (ללא יצירת אובייקטים בזמן ריצה) ---
    private float[] xPositions;
    private Rect[] progressSrcRects;
    private Rect[] progressDstRects;
    private Rect[] flashSrcRects;
    private Rect[] flashDstRects;
    private boolean[] drawProgress;
    private boolean[] drawFlash;

    public StaminaDisplay(Player player, float x, float y) {
        this.player = player;
        this.xOffset = x;
        this.yOffset = y;

        flashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        flashPaint.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP));
    }

    public void update() {
        StaminaComponent sc = player.getStaminaComponent();
        int maxStam = sc.getMaxStamina();
        int curStam = sc.getCurrentStamina();
        int prevStam = sc.getPrevStamina();
        float alpha = sc.getFlashAlpha();

        boolean structureChanged = false;

        // 1. בדיקה אם כמות הסטמינה המקסימלית השתנתה (דורש בנייה מחדש של המערכים)
        if (maxStam != lastMaxStamina) {
            lastMaxStamina = maxStam;
            iconsCount = (int) Math.ceil((double) lastMaxStamina / MAX_PER_ICON);
            initArrays(iconsCount);
            structureChanged = true;
        }

        // 2. חישוב מתמטיקה *רק* אם הסטמינה או האנימציה (alpha) השתנו מאז הפריים האחרון
        if (structureChanged || curStam != currentStaminaCache || prevStam != prevStaminaCache || alpha != alphaCache) {
            currentStaminaCache = curStam;
            prevStaminaCache = prevStam;
            alphaCache = alpha;

            // עדכון ה-Alpha של הצייר קורה פעם אחת בלבד בעדכון, ולא בתוך לולאת הציור
            flashPaint.setAlpha((int) (alpha * 255));

            recalculateRects(curStam, prevStam, alpha);
        }
    }

    /**
     * מאתחל את המערכים מראש. מונע זימוני 'new' בזמן המשחק כדי למנוע Garbage Collection
     */
    private void initArrays(int size) {
        xPositions = new float[size];
        progressSrcRects = new Rect[size];
        progressDstRects = new Rect[size];
        flashSrcRects = new Rect[size];
        flashDstRects = new Rect[size];
        drawProgress = new boolean[size];
        drawFlash = new boolean[size];

        for (int i = 0; i < size; i++) {
            xPositions[i] = xOffset + SPACING * i;
            progressSrcRects[i] = new Rect();
            progressDstRects[i] = new Rect();
            flashSrcRects[i] = new Rect();
            flashDstRects[i] = new Rect();
        }
    }

    /**
     * מחשב את כל נקודות החיתוך ושומר אותם מראש באובייקטים הקיימים
     */
    private void recalculateRects(int currentStamina, int prevStamina, float alpha) {
        for (int i = 0; i < iconsCount; i++) {
            drawProgress[i] = false;
            drawFlash[i] = false;

            float xPos = xPositions[i];
            int staminaInThisIcon = Math.max(0, Math.min(MAX_PER_ICON, currentStamina - (MAX_PER_ICON * i)));

            // חישוב רקע הסטמינה הזמינה
            if (staminaInThisIcon > 0) {
                drawProgress[i] = true;
                float pct = (float) staminaInThisIcon / MAX_PER_ICON;
                int cutW = Math.round(ICON_WIDTH * pct);

                progressSrcRects[i].set(0, 0, cutW, ICON_HEIGHT);
                int drawX = (int) (xPos + progressXAdjustment);
                progressDstRects[i].set(drawX, (int) yOffset, drawX + cutW, (int) (yOffset + ICON_HEIGHT));
            }

            // חישוב אנימציית הבהוב (Flash) לסטמינה שנוצלה הרגע
            if (alpha > 0 && prevStamina > currentStamina) {
                int prevInThisIcon = Math.max(0, Math.min(MAX_PER_ICON, prevStamina - (MAX_PER_ICON * i)));

                if (prevInThisIcon > staminaInThisIcon) {
                    drawFlash[i] = true;
                    float currentPct = (float) staminaInThisIcon / MAX_PER_ICON;
                    float flashPct = ((float) prevInThisIcon / MAX_PER_ICON) - currentPct;

                    int currentW = Math.round(ICON_WIDTH * currentPct);
                    int flashW = Math.round(ICON_WIDTH * flashPct * alpha);

                    flashSrcRects[i].set(currentW, 0, currentW + flashW, ICON_HEIGHT);
                    int flashX = (int) (xPos + progressXAdjustment + currentW);
                    flashDstRects[i].set(flashX, (int) yOffset, flashX + flashW, (int) (yOffset + ICON_HEIGHT));
                }
            }
        }
    }

    public void draw(Canvas c) {
        if (iconsCount == 0) return;

        // פעולת ציור "טיפשה" ומהירה - ללא לוגיקה או הקצאות זיכרון בכלל!
        for (int i = 0; i < iconsCount; i++) {
            // ציור הרקע האפור (הריק)
            c.drawBitmap(emptyIconBmp, xPositions[i], yOffset, null);

            // ציור המילוי רק אם נדרש
            if (drawProgress[i]) {
                c.drawBitmap(progressBmp, progressSrcRects[i], progressDstRects[i], null);
            }

            // ציור אפקט הפלאש רק אם נדרש
            if (drawFlash[i]) {
                c.drawBitmap(progressBmp, flashSrcRects[i], flashDstRects[i], flashPaint);
            }
        }
    }
}