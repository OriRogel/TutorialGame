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

import java.util.ArrayList;
import java.util.List;

// --- 1. שנה את הירושה מ-Paint ל-TextPaint ---
public class TextRenderer extends TextPaint {
    private final TextRenderer shadowRenderer;
    private float x, y, xOffset, yOffset;
    private final List<String> cachedPages = new ArrayList<>();
    private final static char[] punctuations = {'.', '!', '?'};

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
     * @param ignoredIsShadow דגל שמסמן שזהו אובייקט צל.
     */
    private TextRenderer(float size, int color, boolean ignoredIsShadow) {
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
     * מפצל טקסט ארוך לרשימה של דפים שמתאימים לרוחב ולגובה נתון.
     * אופטימיזציה: משתמש ברשימה קבועה כדי למנוע הקצאות זיכרון חוזרות.
     */
    public List<String> splitTextIntoPages(String text, int maxWidth, int maxHeight) {
        // 1. ניקוי הרשימה הקיימת במקום הקצאה של חדשה
        cachedPages.clear();

        StaticLayout layout = StaticLayout.Builder.obtain(text, 0, text.length(), this, maxWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.0f)
                .setIncludePad(false)
                .build();

        int lineCount = layout.getLineCount();
        int startLine = 0;

        while (startLine < lineCount) {
            int endLine = startLine;

            // מציאת גבול גובה מקסימלי
            while (endLine < lineCount) {
                float currentHeight = layout.getLineBottom(endLine) - layout.getLineTop(startLine);
                if (currentHeight > maxHeight) break;
                endLine++;
            }

            if (endLine == startLine) endLine++;

            int startOffset = layout.getLineStart(startLine);
            int endOffset = layout.getLineEnd(endLine - 1);

            // לוגיקת חיתוך חכם לפי פיסוק
            String pageCandidate = text.substring(startOffset, endOffset);
            int lastPunctuation = -1;

            for (char p : punctuations) {
                lastPunctuation = Math.max(lastPunctuation, pageCandidate.lastIndexOf(p + " "));
            }

            if (lastPunctuation > pageCandidate.length() * 0.75) {
                endOffset = startOffset + lastPunctuation + 1;
                while (endLine > startLine && layout.getLineStart(endLine - 1) > endOffset) {
                    endLine--;
                }
            }

            // הוספה לרשימה הקבועה
            cachedPages.add(text.substring(startOffset, endOffset).trim());
            startLine = endLine;
        }

        return cachedPages;
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