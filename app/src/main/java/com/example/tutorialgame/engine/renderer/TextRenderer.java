package com.example.tutorialgame.engine.renderer;

import android.graphics.Canvas;
import android.graphics.Color;
// --- ייבוא חדש ונכון ---
import android.text.TextPaint;

import android.text.Layout;
import android.text.StaticLayout;
import androidx.core.content.res.ResourcesCompat;
import com.example.tutorialgame.R;
import com.example.tutorialgame.ui.base.BaseActivity;

// --- 1. שנה את הירושה מ-Paint ל-TextPaint ---
public class TextRenderer extends TextPaint {
    private final TextRenderer shadowRenderer;
    private float x, y, xOffset, yOffset;

    /**
     * בנאי ציבורי ראשי.
     */
    public TextRenderer(float size, int color) {
        super();
        this.setTextSize(size);

        try { this.setColor(BaseActivity.getContext().getColor(color)); }
        catch (Exception e) { this.setColor(color); }

        this.setTypeface(ResourcesCompat.getFont(BaseActivity.getContext(), R.font.pixel_font));

        // יוצר את הצל באמצעות הבנאי הפרטי
        this.shadowRenderer = new TextRenderer(size, Color.BLACK, true);
        this.yOffset = this.getTextSize() / 10f;
        // עדכן מיד את המיקום של הצל
        updateShadowPosition();
    }

    /**
     * בנאי ציבורי משני.
     */
    public TextRenderer(float size) {
        super();
        this.setTextSize(size);
        this.setTypeface(ResourcesCompat.getFont(BaseActivity.getContext(), R.font.pixel_font));

        // יוצר את הצל באמצעות הבנאי הפרטי
        this.shadowRenderer = new TextRenderer(size, Color.BLACK, true);
        this.yOffset = this.getTextSize() / 10f;
        updateShadowPosition();
    }

    /**
     * בנאי פרטי, שנועד ליצירת אובייקט "צל" בלבד.
     * הוא לא יוצר צל משל עצמו, ובכך עוצר את הרקורסיה.
     * @param isShadow דגל שמסמן שזהו אובייקט צל.
     */
    private TextRenderer(float size, int color, boolean isShadow) {
        super();
        this.setTextSize(size);
        this.setColor(color);
        this.setTypeface(ResourcesCompat.getFont(BaseActivity.getContext(), R.font.pixel_font));
        // חשוב: לא מאתחלים כאן shadowRenderer נוסף!
        this.shadowRenderer = null;
    }

    public void drawText(String text, Canvas c) {
        c.drawText(text, x, y, this);
    }

    public void drawWithShadow(String text, Canvas c) {
        shadowRenderer.drawText(text, c);
        drawText(text, c);
    }

    /**
     * מצייר טקסט עם גלישת שורות אוטומטית בתוך רוחב נתון.
     * @param c הקנבס לציור.
     * @param text הטקסט המלא לציור.
     * @param x קואורדינטת ה-X ההתחלתית.
     * @param y קואורדינטת ה-Y ההתחלתית של השורה הראשונה.
     * @param maxWidth הרוחב המקסימלי של תיבת הטקסט.
     */
    public void drawWrappedText(Canvas c, String text, float x, float y, int maxWidth) {
        StaticLayout staticLayout = StaticLayout.Builder.obtain(text, 0, text.length(), this, maxWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.0f)
                .setIncludePad(false)
                .build();

        c.save();
        c.translate(x, y);
        staticLayout.draw(c);
        c.restore();
    }

    private void updateShadowPosition() {
        if (shadowRenderer != null) {
            shadowRenderer.setPosition(this.x + this.xOffset, this.y + this.yOffset);
        }
    }

    public void setShadowColor(int color) {
        shadowRenderer.setColor(color);
    }

    public void setShadowOffset(float dx, float dy) {
        xOffset = dx;
        yOffset = dy;
        updateShadowPosition();
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        updateShadowPosition();
    }
    public int getShadowColor() {
        return shadowRenderer.getColor();
    }

    // ... שאר ה-getters וה-setters ...
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public float getX() { return x; }
    public float getY() { return y; }

    public void updateColorBasedOnValue(int value, int minValue, int maxValue) {
        float normalizedValue = (float) (value - minValue) / (maxValue - minValue);

        // Clamp the normalized value to ensure it's within 0 and 1
        normalizedValue = Math.max(0f, Math.min(1f, normalizedValue));

        int redComponent = (int) (255 * (1 - normalizedValue)); // Decreases as value increases
        int greenComponent = (int) (255 * normalizedValue);    // Increases as value increases

        // Ensure components are within 0-255 range
        redComponent = Math.max(0, Math.min(255, redComponent));
        greenComponent = Math.max(0, Math.min(255, greenComponent));

        int interpolatedColor = Color.rgb(redComponent, greenComponent, 0); // Blue is 0
        this.setColor(interpolatedColor);
    }
}