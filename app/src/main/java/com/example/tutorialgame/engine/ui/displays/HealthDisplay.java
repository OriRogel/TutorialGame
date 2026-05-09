package com.example.tutorialgame.engine.ui.displays;

import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import com.example.tutorialgame.components.HealthComponent;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.engine.ui.CrackIcons;
import com.example.tutorialgame.engine.ui.HealthIcons;
import com.example.tutorialgame.entities.characters.Player;

public class HealthDisplay {
    private final Player player;
    private final float xOffset, yOffset;
    private final Paint flashPaint, crackPaint, crackFlash;
    private static final float SPACING = GameConstants.Sprite.TILE_SIZE - 2 * GameConstants.Sprite.DEFAULT_SIZE;

    public HealthDisplay(Player player, float x, float y) {
        this.player = player;
        this.xOffset = x;
        this.yOffset = y;
        this.flashPaint = new Paint();
        this.flashPaint.setColorFilter(new PorterDuffColorFilter(WHITE, PorterDuff.Mode.SRC_ATOP));

        // אתחול ה-Paint של הסדקים
        this.crackPaint = new Paint();
        this.crackFlash = new Paint();
        this.crackFlash.setColorFilter(new PorterDuffColorFilter(Color.rgb(248, 0, 0), PorterDuff.Mode.SRC_ATOP));
    }

    public void draw(Canvas c) {
        HealthComponent hc = player.getHealthComponent();
        float alpha = hc.getFlashAlpha();
        int flashIdx = hc.getLastFlashHeartIndex();
        int maxHearts = (int) Math.ceil(hc.getMaxHealth() / 100f);
        int currentHealth = hc.getCurrentHealth();

        for (int i = 0; i < maxHearts; i++) {
            float x = xOffset + SPACING * i;

            // כמה חיים נשארו ספציפית בלב הזה (בין 0 ל-100)
            int heartValue = Math.max(0, Math.min(100, currentHealth - (100 * i)));

            // 1. ציור הלב הבסיסי (מלא, 3/4 וכו')
            Bitmap icon = getHeartIcon(heartValue);
            c.drawBitmap(icon, x, yOffset, null);

            // 3. אפקט הפלאש בלב שנפגע
            if (alpha > 0 && i == flashIdx) {
                flashPaint.setAlpha((int) (alpha * 255));
                c.drawBitmap(icon, x, yOffset, flashPaint);
            }

            // 2. חישוב וציור הסדקים (אם הלב לא מלא ולא ריק לגמרי)
            if (heartValue > 0 && heartValue < 100) {
                drawCracksForHeart(c, x, heartValue, alpha);
            }

        }
    }

    /**
     * מחשב איזה סדק להציג ומה תהיה השקיפות שלו
     */
    private void drawCracksForHeart(Canvas c, float x, int heartValue, float alpha) {
        int remainder = heartValue % 25;
        if (remainder == 0) return;

        float damageInQuarter = 25 - remainder; // ערך בין 1 ל-24
        int crackAlpha = (int) ((damageInQuarter / 25f) * 255);

        crackPaint.setAlpha(crackAlpha);

        Bitmap crackBitmap = getCrackIcon(heartValue);
        if (alpha > 0) c.drawBitmap(crackBitmap, x, yOffset, crackFlash);
        else c.drawBitmap(crackBitmap, x, yOffset, crackPaint);
    }

    private Bitmap getHeartIcon(int value) {
        if (value <= 0) return HealthIcons.HEART_EMPTY.getIcon();
        if (value <= 25) return HealthIcons.HEART_1Q.getIcon();
        if (value <= 50) return HealthIcons.HEART_HALF.getIcon();
        if (value <= 75) return HealthIcons.HEART_3Q.getIcon();
        return HealthIcons.HEART_FULL.getIcon();
    }

    private Bitmap getCrackIcon(int value) {
        // אם החיים הם בין 76 ל-99, הסדק מופיע על הרבע הרביעי
        if (value > 75) return CrackIcons.CRACKED_1.getIcon();
        if (value > 50) return CrackIcons.CRACKED_2.getIcon();
        if (value > 25) return CrackIcons.CRACKED_3.getIcon();
        return CrackIcons.CRACKED_4.getIcon();
    }
}