package com.example.tutorialgame.gamestates;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_HEIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;

import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.R;
import com.example.tutorialgame.cloud.document.StatsDoc;
import com.example.tutorialgame.components.AnimationComponent;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.engine.core.Game;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.engine.renderer.TextRenderer;
import com.example.tutorialgame.engine.ui.HealthIcons;
import com.example.tutorialgame.engine.ui.circleframes.CircleFrames;
import com.example.tutorialgame.engine.ui.customviews.buttons.circles.CircleButton;
import com.example.tutorialgame.engine.ui.customviews.buttons.circles.CircleImages;
import com.example.tutorialgame.engine.ui.customviews.buttons.rects.RectButton;
import com.example.tutorialgame.engine.ui.customviews.buttons.rects.RectImages;
import com.example.tutorialgame.engine.ui.customviews.upgrade.CustomUpgrade;
import com.example.tutorialgame.engine.ui.customviews.upgrade.Upgrades;
import com.example.tutorialgame.engine.ui.displays.XpDisplay;
import com.example.tutorialgame.entities.characters.GameCharacters;
import com.example.tutorialgame.managers.BitmapManager;
import com.example.tutorialgame.managers.MapManager;

public class UpgradeState extends GameState implements CustomUpgrade.OnUpgradeListener {

    private static final int
            LINE_ANIM_SPEED = 7,
            LINE_FRAME_COUNT = 5,
            LINE_FRAME_W = 230,
            LINE_FRAME_H = 13;

    // --- מערך סטטי עבור הבלר ---
    private static final int BLUR_STEPS = 30;
    private static final BlurMaskFilter[] BLUR_FILTERS = new BlurMaskFilter[BLUR_STEPS];
    private static final int BLUR_UPDATE_MS = 100; // מהירות החלפת הערכים

    static {
        for (int i = 0; i < BLUR_STEPS; i++) {
            // יצירת ערכי בלר מדורגים (צמיחה חלקה)
            float radius = (float) (2 + Math.pow(i, 1.5));
            BLUR_FILTERS[i] = new BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL);
        }
    }

    private int blurIndex, blurDir = 1;
    private long blurElapsed = 0;
    private final Paint blurPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF screenRect = new RectF(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

    private static final int STAMINA_MAX_PER_ICON = 50;
    private static final int FADE_WIDTH = TILE_SIZE; 
    private static final float STAGGER_FACTOR = 0.2f; 

    private AnimationComponent lineAnimation;
    private Bitmap[] lineSprites;
    private float lineX, lineY;

    private final RectF modelHitbox = new RectF();
    private Bitmap modelBmp;
    private float firstTouch;
    private boolean isModelTouch;

    private float heartX, staminaX;
    private RectButton btnReturn;
    private CircleButton btnApplyChanges;

    private CustomUpgrade[] upgrades;
    private CustomUpgrade healthUpgrade, staminaUpgrade;
    private float startX, spacingX, spacingY;

    private Bitmap pointsBoxBmp;
    private int pointsLeft, prevPointsLeft;
    private TextRenderer pointsRenderer;

    private float scrollX, maxScrollX, lastTouchX;
    private boolean isDragging;
    private float dragThreshold;
    private final Rect upgradeClipArea = new Rect();

    private XpDisplay xpDisplay;
    private final Paint fadePaint = new Paint(), previewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Bitmap staminaProgress, staminaEmpty;
    private final float staminaXAdjustment = SCALE_MULTIPLIER * 2;

    private final Rect tempSrcRect = new Rect();
    private final RectF tempDstRect = new RectF(), layerRect = new RectF();

    public UpgradeState(Game game) {
        super(game);
        staminaProgress = BitmapManager.getBitmap(R.drawable.lightning_progress);
        staminaEmpty = BitmapManager.getBitmap(R.drawable.lightning_empty);
        init();
    }

    private void init() {
        dragThreshold = ViewConfiguration.get(context).getScaledTouchSlop();
        previewPaint.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#AA00FFFF"), PorterDuff.Mode.SRC_ATOP));
        fadePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        // אתחול צבע הבלר - כחול עמוק שקוף למחצה
        blurPaint.setColor(Color.parseColor("#44001122"));

        createLine();
        createUI();
    }

    private void createLine() {
        lineSprites = BitmapManager.getSpritesheet(R.drawable.particle_sprakling_line, LINE_FRAME_W, LINE_FRAME_H, LINE_FRAME_COUNT, 1, false, false);
        lineAnimation = new AnimationComponent(LINE_ANIM_SPEED, LINE_FRAME_COUNT);
        lineX = (SCREEN_WIDTH - LINE_FRAME_W * SCALE_MULTIPLIER) / 2f;
        lineY = SCREEN_HEIGHT * 0.7f;
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private void createUI() {
        startX = lineX + (LINE_FRAME_W * SCALE_MULTIPLIER) - CircleFrames.BACKGROUND.getCircleFrame().getWidth() / 2f;
        spacingX = 9 * TILE_SIZE;
        spacingY = 2.5f * TILE_SIZE;

        Upgrades[] availableTypes = { Upgrades.HEALTH, Upgrades.STRENGTH, Upgrades.ATTACK_SPEED, Upgrades.STAMINA, Upgrades.CRIT_CHANCE };
        upgrades = new CustomUpgrade[availableTypes.length];

        for (int i = 0; i < availableTypes.length; i++) {
            float x = startX + (i / 3) * spacingX; 
            float y = (lineY - TILE_SIZE) - (i % 3) * spacingY;
            upgrades[i] = new CustomUpgrade(availableTypes[i], new PointF(x, y));
            upgrades[i].setOnUpgradeListener(this);

            if (availableTypes[i] == Upgrades.HEALTH) healthUpgrade = upgrades[i];
            if (availableTypes[i] == Upgrades.STAMINA) staminaUpgrade = upgrades[i];
        }

        if (upgrades.length > 0) {
            float lastRight = upgrades[upgrades.length - 1].getBounds().right;
            maxScrollX = Math.max(0, lastRight + 2 * TILE_SIZE - SCREEN_WIDTH);
        }

        upgradeClipArea.set((int)(lineX + 5.5f * TILE_SIZE), 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        pointsBoxBmp = BitmapManager.getBitmap(R.drawable.dialogbox, 1, false);
        pointsRenderer = new TextRenderer(7 * SCALE_MULTIPLIER, R.color.black);
        pointsRenderer.setPosition(2 * SCALE_MULTIPLIER, 11.5f * SCALE_MULTIPLIER);
        xpDisplay = new XpDisplay(pointsRenderer.getX(), pointsRenderer.getY() + TILE_SIZE);

        btnApplyChanges = new CircleButton(new PointF(SCREEN_WIDTH * 0.6f, lineY + 2 * TILE_SIZE), CircleImages.APPLY, false);
        btnReturn = new RectButton(btnApplyChanges.getHitbox().left - SCREEN_WIDTH * 0.25f,
                btnApplyChanges.getHitbox().top - SCALE_MULTIPLIER, SCREEN_WIDTH * 0.2f,
                btnApplyChanges.getHitbox().height(), RectImages.RETURN, false);

        Shader fadeShader = new LinearGradient(
                upgradeClipArea.left, 0,
                upgradeClipArea.left + FADE_WIDTH, 0,
                new int[]{Color.TRANSPARENT, Color.BLACK, Color.BLACK}, 
                new float[]{0f, 1f, 1f},
                Shader.TileMode.CLAMP);

        fadePaint.setShader(fadeShader);

        blurPaint.setColor(Color.BLACK);
        blurPaint.setAlpha(100);
    }

    @Override
    public void onEnter() {
        super.onEnter();
        pointsLeft = MyApp.getProgress().getUpgradePoints();
        prevPointsLeft = pointsLeft;
        scrollX = 0;
        blurIndex = 0;
        blurElapsed = System.currentTimeMillis();
        updateModel();
        for (CustomUpgrade u : upgrades) u.updateCircleFrame();
    }

    @Override
    public void onExit() {
        super.onExit();
        updateModelDirection(0);
        for (CustomUpgrade u : upgrades) {
            u.resetCountToApply();
            u.setBalloonVisible(false);
        }
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    @Override
    public void update(double delta) {
        lineAnimation.update();
        updateBlurEffect();

        for (int i = 0; i < upgrades.length; i++) {
            int row = i % 3;
            float rowStagger = row * (-scrollX * STAGGER_FACTOR);
            float baseX = startX + (i / 3) * spacingX;
            float baseY = (lineY - TILE_SIZE) - row * spacingY;

            upgrades[i].setCenter(baseX + rowStagger, baseY);
            upgrades[i].update(pointsLeft, delta);
        }

        btnApplyChanges.setEnabled(prevPointsLeft > pointsLeft);
    }

    private void updateBlurEffect() {
        if (System.currentTimeMillis() - blurElapsed > BLUR_UPDATE_MS) {
            // אנימציית Ping-pong (הילוך אחורי בהגעה לקצה)
            blurIndex += blurDir;
            if (blurIndex >= BLUR_STEPS - 1 || blurIndex <= 0) {
                blurDir *= -1;
            }

            blurPaint.setMaskFilter(BLUR_FILTERS[blurIndex]);

            // שינוי אלפא עדין סביב 100 (נע בין 90 ל-110)
            int alpha = 90 + (int) (20 * ((float) blurIndex / BLUR_STEPS));
            blurPaint.setAlpha(alpha);

            blurElapsed = System.currentTimeMillis();
        }
    }

    @Override
    public void render(Canvas c) {
        c.drawRect(screenRect, blurPaint); // שכבת הבלר הדינמית

        c.drawBitmap(lineSprites[lineAnimation.getAniIndex()], lineX, lineY, null);
        drawModel(c);
        drawHearts(c);
        drawStamina(c);
        btnReturn.draw(c);
        btnApplyChanges.draw(c);

        layerRect.set(upgradeClipArea);
        int saveCount = c.saveLayer(layerRect, null);

        c.save();
        c.translate(scrollX, 0);
        for (CustomUpgrade u : upgrades) u.draw(c);
        for (CustomUpgrade u : upgrades) u.drawBalloon(c);
        c.restore();

        c.drawRect(layerRect, fadePaint);
        c.restoreToCount(saveCount);

        c.drawBitmap(pointsBoxBmp, -pointsBoxBmp.getWidth() * 0.2f, -5 * SCALE_MULTIPLIER, null);
        pointsRenderer.drawText(context.getString(R.string.points_left) + pointsLeft, c);
        xpDisplay.draw(c);
    }

    @Override
    public void touchEvents(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) handleBalloonDismissal(event);
        if (btnReturn.eventHandler(event)) { switcher.changeState(State.PLAYING); return; }
        if (btnApplyChanges.eventHandler(event)) { applyChanges(); return; }

        handleScrolling(event);
        float x = event.getX(), y = event.getY();
        if (upgradeClipArea.contains((int)x, (int)y) && !isDragging) {
            event.setLocation(x - scrollX, y);
            for (CustomUpgrade u : upgrades) u.eventHandler(event);
            event.setLocation(x, y);
        }
        handleModelRotation(event);
    }

    private void handleScrolling(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: lastTouchX = event.getX(); isDragging = false; break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - lastTouchX;
                if (Math.abs(dx) > dragThreshold || isDragging) {
                    isDragging = true;
                    scrollX += dx;
                    scrollX = Math.max(-maxScrollX, Math.min(0, scrollX));
                    lastTouchX = event.getX();
                    for (CustomUpgrade u : upgrades) u.setBalloonVisible(false);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: isDragging = false; break;
        }
    }

    private void handleBalloonDismissal(MotionEvent event) {
        CustomUpgrade clicked = null;
        if (upgradeClipArea.contains((int)event.getX(), (int)event.getY())) {
            float tx = event.getX() - scrollX;
            for (CustomUpgrade u : upgrades) {
                if (u.getBounds().contains(tx, event.getY())) { clicked = u; break; }
            }
        }
        for (CustomUpgrade u : upgrades) {
            if (u == clicked && !u.isBalloonVisible()) {
                u.setBalloonVisible(true);
                SoundManager.getInstance(context).playSfx(R.raw.sfx_bloop);
            } else u.setBalloonVisible(false);
        }
    }

    private void handleModelRotation(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: firstTouch = event.getX(); isModelTouch = modelHitbox.contains(event.getX(), event.getY()); break;
            case MotionEvent.ACTION_MOVE:
                if (isModelTouch) {
                    if (event.getX() > firstTouch + 20) updateModelDirection(1);
                    else if (event.getX() < firstTouch - 20) updateModelDirection(-1);
                }
                break;
            case MotionEvent.ACTION_UP: updateModelDirection(0); break;
        }
    }

    private void updateModelDirection(int dir) {
        int face = GameConstants.Face_Dir.DOWN;
        if (dir == 1) face = GameConstants.Face_Dir.RIGHT;
        else if (dir == -1) face = GameConstants.Face_Dir.LEFT;
        modelBmp = GameCharacters.PLAYER.getSprite(0, face);
    }

    private void drawHearts(Canvas c) {
        int currentMax = MyApp.getPlayerStats().getMaxHealth();
        int previewAdd = (healthUpgrade != null) ? healthUpgrade.getCountToApply() * healthUpgrade.getUpgradeValue() : 0;
        int total = currentMax + previewAdd, icons = (int) Math.ceil(total / 100f);
        for (int i = 0; i < icons; i++) {
            int curVal = Math.max(0, Math.min(100, currentMax - 100 * i));
            int totVal = Math.max(0, Math.min(100, total - 100 * i));
            float y = (modelHitbox.bottom - HealthIcons.HEART_FULL.getIcon().getHeight() * 1.5f + SCALE_MULTIPLIER) - 1.3f * TILE_SIZE * i;
            c.save(); c.scale(1.5f, 1.5f, heartX, y);
            c.drawBitmap(getHeartIcon(curVal), heartX, y, null);
            if (totVal > curVal) c.drawBitmap(getHeartIcon(totVal), heartX, y, previewPaint);
            c.restore();
        }
    }

    private void drawStamina(Canvas c) {
        int currentMax = MyApp.getPlayerStats().getMaxStamina();
        int previewAdd = (staminaUpgrade != null) ? staminaUpgrade.getCountToApply() * staminaUpgrade.getUpgradeValue() : 0;
        int total = currentMax + previewAdd, icons = (int) Math.ceil((double) total / STAMINA_MAX_PER_ICON);
        for (int i = 0; i < icons; i++) {
            int curVal = Math.max(0, Math.min(STAMINA_MAX_PER_ICON, currentMax - (STAMINA_MAX_PER_ICON * i)));
            int totVal = Math.max(0, Math.min(STAMINA_MAX_PER_ICON, total - (STAMINA_MAX_PER_ICON * i)));
            float y = (modelHitbox.bottom - staminaEmpty.getHeight() * 1.5f + SCALE_MULTIPLIER) - 1.3f * TILE_SIZE * i;
            c.save(); c.scale(1.5f, 1.5f, staminaX, y);
            c.drawBitmap(staminaEmpty, staminaX, y, null);
            if (curVal > 0) drawClippedStamina(c, staminaProgress, staminaX, y, curVal, null);
            if (totVal > curVal) drawClippedStamina(c, staminaProgress, staminaX, y, totVal, previewPaint);
            c.restore();
        }
    }

    private void drawClippedStamina(Canvas c, Bitmap bmp, float x, float y, int val, Paint p) {
        float pct = (float) val / STAMINA_MAX_PER_ICON;
        int cutW = Math.round(bmp.getWidth() * pct);
        tempSrcRect.set(0, 0, cutW, bmp.getHeight());
        tempDstRect.set(x + staminaXAdjustment, y, x + staminaXAdjustment + cutW, y + bmp.getHeight());
        c.drawBitmap(bmp, tempSrcRect, tempDstRect, p);
    }

    private Bitmap getHeartIcon(int val) {
        if (val >= 100) return HealthIcons.HEART_FULL.getIcon();
        if (val >= 75) return HealthIcons.HEART_3Q.getIcon();
        if (val >= 50) return HealthIcons.HEART_HALF.getIcon();
        if (val >= 25) return HealthIcons.HEART_1Q.getIcon();
        return HealthIcons.HEART_EMPTY.getIcon();
    }

    private void updateModel() {
        float scale = 2 + (MyApp.getProgress().getUpgradesDone() / 20f);
        updateModelDirection(0);
        staminaX = 5 * SCALE_MULTIPLIER; heartX = staminaX + 1.3f * TILE_SIZE;
        float centerX = (heartX + 1.5f * TILE_SIZE + upgradeClipArea.left) / 2f;
        float bottomY = lineY - 2 * SCALE_MULTIPLIER;
        float w = modelBmp.getWidth() * scale, h = modelBmp.getHeight() * scale;
        modelHitbox.set(centerX - w / 2f, bottomY - h, centerX + w / 2f, bottomY);
    }

    @Override
    public boolean onUpgradeChanged(CustomUpgrade upgrade, int costChange) {
        if (pointsLeft < costChange) return false;
        this.pointsLeft -= costChange; return true;
    }


    private void applyChanges() {
        StatsDoc stats = MyApp.getPlayerStats();
        boolean changed = false;

        // הפחתת הנקודות בבסיס הנתונים רק פעם אחת לפי הערך הסופי
        int totalSpent = prevPointsLeft - pointsLeft;
        if (totalSpent > 0) {
            MyApp.getProgress().decreaseUpgradePoints(totalSpent);
        }

        for (CustomUpgrade u : upgrades) {
            if (u.getCountToApply() > 0) {
                // עדכון הסטטוס ב-StatsDoc ללא הפחתת נקודות כפולה
                stats.updateStat(u.getStatField(), u.getCountToApply() * u.getUpgradeValue(), 0, u.getCountToApply());
                u.resetCountToApply(); 
                changed = true;
            }
        }

        if (changed) {
            MapManager.getCurrentMap().getPlayer().refreshStats();
            SoundManager.getInstance(context).playSfx(R.raw.sfx_success4);
            prevPointsLeft = pointsLeft;
        }
    }

    private void drawModel(Canvas c) { c.drawBitmap(modelBmp, null, modelHitbox, null); }
}
