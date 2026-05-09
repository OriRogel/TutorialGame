package com.example.tutorialgame.engine.ui.customviews.upgrade;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.view.MotionEvent;
import androidx.core.content.res.ResourcesCompat;
import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.R;
import com.example.tutorialgame.cloud.document.StatsDoc;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.engine.renderer.TextRenderer;
import com.example.tutorialgame.engine.ui.circleframes.CircleFrames;
import com.example.tutorialgame.engine.ui.customviews.buttons.BaseButton;
import com.example.tutorialgame.engine.ui.customviews.buttons.circles.CircleButton;
import com.example.tutorialgame.engine.ui.customviews.buttons.circles.CircleImages;
import com.example.tutorialgame.managers.BitmapManager;
import com.example.tutorialgame.ui.base.BaseActivity;

public class CustomUpgrade implements BaseButton.OnClickListener {

    public interface OnUpgradeListener {
        boolean onUpgradeChanged(CustomUpgrade upgrade, int costChange);
    }

    private OnUpgradeListener listener;
    private final Upgrades upgradeImg;
    private CircleFrames circleFrame;
    private CircleButton btnUpgrade, btnDowngrade;
    private final PointF upgradeCenter;
    private final RectF bounds;
    private float containerX, containerY;
    private int countToApply, currentLevel, price;
    private float levelX, levelY, nameX, nameY;
    
    private boolean isBalloonVisible;

    // Static resources shared across all instances
    private final static Bitmap container, balloonDrawable;
    private final static Paint levelPaint, statPaint, cutPaint;
    private final static TextRenderer balloonTitleRenderer, balloonDescRenderer, balloonPriceRenderer;

    private float balloonX, balloonY;
    private static final float balloonWidth, balloonHeight;
    private float titleX, titleY, descX, descY, priceX, priceY, currentScale;
    private StaticLayout descriptionLayout;

    static {
        container = BitmapManager.getBitmap(R.drawable.dialogbox);

        balloonDrawable = BitmapManager.getBitmap(R.drawable.dialogbox_dark);
        balloonWidth = balloonDrawable.getWidth();
        balloonHeight = balloonDrawable.getHeight();


        levelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        levelPaint.setColor(BaseActivity.getContext().getColor(R.color.magnolia_white));
        levelPaint.setTypeface(ResourcesCompat.getFont(BaseActivity.getContext(), R.font.pixel_font));
        levelPaint.setTextSize(4.5f * SCALE_MULTIPLIER);

        statPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        statPaint.setTypeface(ResourcesCompat.getFont(BaseActivity.getContext(), R.font.pixel_font));
        statPaint.setFakeBoldText(true);
        statPaint.setTextSize(SCALE_MULTIPLIER * 9);

        cutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cutPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        balloonTitleRenderer = new TextRenderer(5 * SCALE_MULTIPLIER, R.color.magnolia_white);
        balloonDescRenderer = new TextRenderer(3.5f * SCALE_MULTIPLIER, R.color.magnolia_white);
        balloonPriceRenderer = new TextRenderer(4.5f * SCALE_MULTIPLIER, Color.YELLOW);
    }

    public CustomUpgrade(Upgrades upgradeImg, PointF upgradeCenter) {
        this.upgradeImg = upgradeImg;
        this.circleFrame = CircleFrames.valueOf(MyApp.getCosmetic().getCurrentFrame());
        this.upgradeCenter = upgradeCenter;
        this.bounds = new RectF();

        // 1. קריאה לאתחול הלוגי פעם אחת בלבד ביצירת האובייקט
        initLogic();
        updateLayout();
    }

    private void initLogic() {
        currentLevel = calCurrentLevel();
        setPrice();

        // בניית ה-StaticLayout פעם אחת במקום בכל פריים (משפר ביצועים משמעותית!)
        descriptionLayout = StaticLayout.Builder.obtain(upgradeImg.getDescription(), 0, upgradeImg.getDescription().length(),
                        balloonDescRenderer, (int)(balloonWidth - 12 * SCALE_MULTIPLIER))
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.0f)
                .setIncludePad(false)
                .build();
    }

    private void updateLayout() {
        this.bounds.set(getBounds());
        this.containerX = bounds.left - 0.9f * container.getWidth();
        this.containerY = bounds.top + container.getHeight() / 2.2f;

        float btnX = upgradeCenter.x + circleFrame.getCircleFrame().getWidth() / 2f + 8 * SCALE_MULTIPLIER;
        if (btnUpgrade == null) {
            btnUpgrade = new CircleButton(new PointF(btnX, getBtnUpY()), CircleImages.UPGRADE, false);
            btnUpgrade.setOnClickListener(this);
        } else {
            btnUpgrade.getHitbox().offsetTo(btnX - btnUpgrade.getHitbox().width()/2f, getBtnUpY() - btnUpgrade.getHitbox().height()/2f);
        }

        if (btnDowngrade == null) {
            btnDowngrade = new CircleButton(new PointF(btnX, getBtnDownY()), CircleImages.DOWNGRADE, false);
            btnDowngrade.setOnClickListener(this);
        } else {
            btnDowngrade.getHitbox().offsetTo(btnX - btnDowngrade.getHitbox().width()/2f, getBtnDownY() - btnDowngrade.getHitbox().height()/2f);
        }

        updatePositions(); // קריאה לעדכון מיקומים בלבד
    }

    // שונתה מ-setNumbers ל-updatePositions: מטפלת רק בקואורדינטות של הציור
    private void updatePositions() {
        levelX = containerX + 5.5f * SCALE_MULTIPLIER;
        levelY = containerY + 6 * SCALE_MULTIPLIER;
        nameX = containerX + 8 * SCALE_MULTIPLIER;
        nameY = containerY + 17.5f * SCALE_MULTIPLIER;

        balloonX = bounds.centerX() - balloonWidth / 2f;
        balloonY = bounds.top + balloonHeight - 5 * SCALE_MULTIPLIER;

        float padding = 9 * SCALE_MULTIPLIER;
        titleX = balloonX + padding;
        titleY = balloonY + 14 * SCALE_MULTIPLIER;
        descX = titleX;
        descY = titleY + 3 * SCALE_MULTIPLIER;
        priceX = titleX;
        priceY = balloonY + balloonHeight - 8 * SCALE_MULTIPLIER;
    }

    public void setOnUpgradeListener(OnUpgradeListener listener) {
        this.listener = listener;
    }

    private int calCurrentLevel() {
        int level = (int) ((upgradeImg.getCurrentStatValue() - upgradeImg.getDefaultValue()) / upgradeImg.getUpgradeValue());
        if (StatsDoc.F_ATTACK_SPEED.equals(getStatField())) level = -level;
        return level + 1;
    }

    private float getBtnDownY() { return upgradeCenter.y + CircleImages.DOWNGRADE.getHeight() / 2f + 0.5f * SCALE_MULTIPLIER; }
    private float getBtnUpY() { return upgradeCenter.y - CircleImages.UPGRADE.getHeight() / 2f - 0.5f * SCALE_MULTIPLIER; }

    public void draw(Canvas c) {
        drawContainer(c);
        drawButtons(c);
        drawUpgradeIcon(c);
    }

    public void drawBalloon(Canvas c) {
        if (!isBalloonVisible) return;

        c.save();
        c.scale(currentScale, currentScale,
                balloonX + balloonWidth / 2f, balloonY + balloonHeight / 2f);

        c.drawBitmap(balloonDrawable, balloonX, balloonY, null);

        balloonTitleRenderer.setPosition(titleX, titleY);
        balloonTitleRenderer.drawText(upgradeImg.getText(), c);

        c.save();
        c.translate(descX, descY);
        descriptionLayout.draw(c);
        c.restore();

        String priceText = BaseActivity.getContext().getString(R.string.points_to_upgrade) + ": " + price;
        balloonPriceRenderer.setPosition(priceX, priceY);
        balloonPriceRenderer.drawText(priceText, c);

        c.restore();
    }

    private void drawContainer(Canvas c) {
        c.drawBitmap(container, containerX, containerY, null);
        levelPaint.setColor(countToApply > 0 ? Color.GREEN : BaseActivity.getContext().getColor(R.color.magnolia_white));
        c.drawText("Lv. " + currentLevel, levelX, levelY, levelPaint);
        c.drawText(upgradeImg.getText(), nameX, nameY, statPaint);
    }

    private void drawButtons(Canvas c) {
        btnUpgrade.draw(c);
        btnDowngrade.draw(c);
    }

    private void drawUpgradeIcon(Canvas c) {
        int saveId = c.saveLayer(bounds, null);
        c.drawBitmap(CircleFrames.BACKGROUND.getCircleFrame(), bounds.left, bounds.top, null);
        c.drawBitmap(upgradeImg.getUpgradeImg(),
                upgradeCenter.x - upgradeImg.getUpgradeImg().getWidth() / 2f,
                upgradeCenter.y - upgradeImg.getUpgradeImg().getHeight() / 2f,
                cutPaint);
        c.restoreToCount(saveId);
        c.drawBitmap(circleFrame.getCircleFrame(), bounds.left, bounds.top, null);
    }

    public void eventHandler(MotionEvent event) {
        btnUpgrade.eventHandler(event);
        btnDowngrade.eventHandler(event);
    }

    @Override
    public void onClick(BaseButton button) {
        if (button == btnUpgrade) {
            if (listener != null && listener.onUpgradeChanged(this, price)) {
                countToApply++;
                currentLevel++;
                setPrice();
                SoundManager.getInstance(MyApp.getAppContext()).playSfx(R.raw.sfx_bloop);
            }
        } else if (button == btnDowngrade && countToApply > 0) {
            // כשאנחנו מורידים, אנחנו מקבלים חזרה את המחיר של הרמה הקודמת
            int refundPrice = (currentLevel - 1) * 2;
            if (listener != null) {
                listener.onUpgradeChanged(this, -refundPrice);
                countToApply--;
                currentLevel--;
                setPrice();
                SoundManager.getInstance(MyApp.getAppContext()).playSfx(R.raw.sfx_bloop);
            }
        }
    }

    public void update(int upgradesLeft, double delta) {
        if (currentScale < 1.0f) {
            currentScale = Math.min(1.0f, currentScale + (float) (delta * 8.0));
        }

        btnUpgrade.setEnabled(upgradesLeft >= price);
        btnDowngrade.setEnabled(countToApply > 0);
    }

    private void setPrice() { price = currentLevel * 2; }

    public RectF getBounds() {
        float left = upgradeCenter.x - circleFrame.getCircleFrame().getWidth() / 2f;
        float top = upgradeCenter.y - circleFrame.getCircleFrame().getHeight() / 2f;
        return new RectF(left, top, left + circleFrame.getCircleFrame().getWidth(), top + circleFrame.getCircleFrame().getHeight());
    }

    public void updateCircleFrame() { 
        circleFrame = CircleFrames.valueOf(MyApp.getCosmetic().getCurrentFrame()); 
        updateLayout();
    }

    public void setCenter(float x, float y) {
        this.upgradeCenter.set(x, y);
        updateLayout();
    }

    public PointF getCenter() {
        return upgradeCenter;
    }

    public int getCountToApply() { return countToApply; }
    public String getStatField() { return upgradeImg.getStatField(); }
    public int getUpgradeValue() { return upgradeImg.getUpgradeValue(); }

    public void resetCountToApply() {
        this.countToApply = 0;
        currentLevel = calCurrentLevel();
        setPrice();
    }

    public void setBalloonVisible(boolean visible) {
        this.isBalloonVisible = visible;
        if (!visible) currentScale = 0;
    }
    
    public boolean isBalloonVisible() {
        return isBalloonVisible;
    }
}
