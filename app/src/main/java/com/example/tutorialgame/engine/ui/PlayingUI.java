package com.example.tutorialgame.engine.ui;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_HEIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.engine.core.Game;
import com.example.tutorialgame.engine.ui.circleframes.CircleFrames;
import com.example.tutorialgame.engine.ui.customviews.buttons.GameButton;
import com.example.tutorialgame.engine.ui.customviews.buttons.circles.CircleButton;
import com.example.tutorialgame.engine.ui.customviews.buttons.circles.CircleImages;
import com.example.tutorialgame.engine.ui.displays.CoinDisplay;
import com.example.tutorialgame.engine.ui.displays.HealthDisplay;
import com.example.tutorialgame.engine.ui.displays.StaminaDisplay;
import com.example.tutorialgame.engine.ui.effects.LevelUpEffect;
import com.example.tutorialgame.engine.ui.joystick.Joystick;
import com.example.tutorialgame.entities.characters.Player;
import com.example.tutorialgame.gamestates.State;
import com.example.tutorialgame.gamestates.playing.playingstates.OverWorld;
import com.example.tutorialgame.managers.QuestManager;
import com.example.tutorialgame.ui.base.BaseActivity;

public class PlayingUI implements GameButton.OnClickListener {
    private static boolean SHOW_UI;
    private final Joystick joystick;
    private final CircleButton btnJump, btnAttack, btnSpeak, btnMenu;
    private final Player player;

    private final OverWorld overWorld;
    private final RectF facesetHitbox;
    private final Bitmap currentFrame = CircleFrames.valueOf(MyApp.getCosmetic().getCurrentFrame()).getCircleFrame();

    // faceset tracking
    private int facesetPointerId = -1;
    private boolean facesetPressed;
    private boolean startFace;
    private float facesetBaseRotation = 0f;
    private static final float FRAME_OFFSET = 0.83f*SCALE_MULTIPLIER;
    private static final float CENTER_FRAME_OFFSET = FRAME_OFFSET + CircleFrames.BACKGROUND.getCircleFrame().getHeight() / 2f;


    // rotation animation
    private boolean rotating;
    private float rotationAngle = 0f;
    private float rotationFrom = 0f;
    private float rotationTo = 0f;
    private float rotationElapsed = 0f;
    private final CoinDisplay coinDisplay;
    private final StaminaDisplay staminaDisplay;
    private final HealthDisplay healthDisplay;
    private final LevelUpEffect levelUpEffect;

    static {
        SharedPreferences sp = BaseActivity.getContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        SHOW_UI = sp.getBoolean("ui", true);
    }

    public PlayingUI(OverWorld overWorld) {
        this.overWorld = overWorld;
        this.player = overWorld.getPlayer();

        // אתחול הג'ויסטיק החדש במקום הנקודות הידניות
        this.joystick = new Joystick(SCREEN_WIDTH / 6.4f, SCREEN_HEIGHT / 1.35f, 25 * SCALE_MULTIPLIER);

        btnMenu = new CircleButton(new PointF(SCREEN_WIDTH - TILE_SIZE, TILE_SIZE), CircleImages.PAUSE, true);
        btnJump = new CircleButton(new PointF((SCREEN_WIDTH - SCREEN_WIDTH / 5f) + TILE_SIZE * 2, SCREEN_HEIGHT / 1.35f - TILE_SIZE * 1.5f), CircleImages.JUMP, true);
        btnAttack = new CircleButton(new PointF(SCREEN_WIDTH - SCREEN_WIDTH / 5f, SCREEN_HEIGHT / 1.2f), CircleImages.ATTACK, true);
        btnSpeak = new CircleButton(new PointF((SCREEN_WIDTH - SCREEN_WIDTH / 5f) + TILE_SIZE * 2.5f, SCREEN_HEIGHT / 1.25f), CircleImages.SPEAK, true);

        int healthIconX = 45 * SCALE_MULTIPLIER;
        int healthIconY = SCALE_MULTIPLIER;

        Bitmap faceExample = PlayerFaceset.IDLE.getFace();
        float facesetX = 6.5f + (currentFrame.getWidth() - faceExample.getWidth()) / 2f;
        float facesetY = -5 + (currentFrame.getHeight() - faceExample.getHeight()) / 2f;
        facesetHitbox = new RectF(facesetX, facesetY, facesetX + faceExample.getWidth(), facesetY + faceExample.getHeight());

        coinDisplay = new CoinDisplay();
        healthDisplay = new HealthDisplay(player, healthIconX, healthIconY);
        staminaDisplay = new StaminaDisplay(player, healthIconX, healthIconY + 1.1f * TILE_SIZE);
        levelUpEffect = new LevelUpEffect();

        setListeners();
    }

    private void setListeners() {
        btnMenu.setOnClickListener(this);
        btnAttack.setOnClickListener(this);
        btnSpeak.setOnClickListener(this);
        btnJump.setOnClickListener(this);
    }

    public void update(double delta) {
        // אם השחקן מת, נועלים שליטה ומאפסים ג'ויסטיק
        if (player.isDead()) {
            if (joystick.isPushed()) joystick.reset();
            overWorld.setPlayerMoveFalse();
        } else {
            // עדכון תנועת השחקן לפי מצב הג'ויסטיק
            if (joystick.isPushed()) overWorld.setPlayerMoveTrue(joystick.getMovementVector());
            else overWorld.setPlayerMoveFalse();
        }

        updateButtons();

        // אנימציית סיבוב הפריימים
        if (rotating) {
            rotationElapsed += (float) delta;
            float t = rotationElapsed / 0.1f;
            if (t >= 1f) {
                t = 1f;
                rotating = false;
            }
            float ease = 1f - (1f - t) * (1f - t);
            rotationAngle = rotationFrom + (rotationTo - rotationFrom) * ease;

            if (!rotating) {
                rotationAngle = ((rotationAngle % 360f) + 360f) % 360f;
                rotationFrom = rotationAngle;
                rotationTo = rotationAngle;
                rotationElapsed = 0f;
            }
        }

        staminaDisplay.update();
        coinDisplay.update(delta);
        levelUpEffect.update(delta);

        QuestManager.update();
    }

    private void updateButtons() {
        if (player.getStaminaComponent().isStaminaLocked()) {
            btnJump.setEnabled(false);
            btnAttack.setEnabled(false);
            return;
        }

        if (!player.getStaminaComponent().hasEnough(10)) {
            btnJump.setEnabled(false);
        } else if (!player.getStaminaComponent().hasEnough(5)) {
            btnAttack.setEnabled(false);
            btnJump.setEnabled(false);
        }
        else {
            btnJump.setEnabled(true);
            btnAttack.setEnabled(true);
        }

        if (player.getWeapon() == null) btnAttack.setEnabled(false);
    }
    private void startRotateTo(float target) {
        rotationFrom = rotationAngle;
        rotationTo = target;
        rotationElapsed = 0f;
        rotating = true;
    }

    public void draw(Canvas c) {
        if(!SHOW_UI) return;
        if (overWorld.getGame().getCurrentGameState() != State.PLAYING) return;
        // ציור הג'ויסטיק החדש
        joystick.draw(c);

        // Buttons
        btnJump.draw(c);
        btnAttack.draw(c);
        if (player.getCurrentSpeaker() != null) btnSpeak.draw(c);
        btnMenu.draw(c);

        drawFaceset(c);
        coinDisplay.draw(c);
        healthDisplay.draw(c);
        staminaDisplay.draw(c);
        levelUpEffect.draw(c);
        QuestManager.draw(c);
    }

    private void drawFaceset(Canvas c) {
        drawCircleFrame(c);

        // Ensure facesetHitbox is pre-calculated (don't create Rects inside draw)
        c.drawBitmap(getActiveFace(), facesetHitbox.left, facesetHitbox.top, null);
    }

    private Bitmap getActiveFace() {
        if (player.isDamaged()) return PlayerFaceset.HURT.getFace();
        if (player.getStaminaComponent().isStaminaLocked()) return PlayerFaceset.EXHAUSTED.getFace();
        return PlayerFaceset.IDLE.getFace();
    }

    private void drawCircleFrame(Canvas c) {
        // 1. Draw Background
        c.drawBitmap(CircleFrames.BACKGROUND.getCircleFrame(), FRAME_OFFSET, FRAME_OFFSET, null);

        boolean needsRotation = rotating || rotationAngle != 0f;

        if (needsRotation) {
            c.save();
            c.rotate(rotationAngle, CENTER_FRAME_OFFSET, CENTER_FRAME_OFFSET);
        }

        c.drawBitmap(currentFrame, FRAME_OFFSET, FRAME_OFFSET, null);

        if (needsRotation) {
            c.restore();
        }
    }

    public void touchEvents(MotionEvent event) {
        // 1. העברת אירוע לג'ויסטיק
        joystick.eventHandler(event);

        // 2. העברת אירוע לכפתורים (תיקון הבעיה שהם לא הגיבו)
        btnMenu.eventHandler(event);
        btnJump.eventHandler(event);
        btnAttack.eventHandler(event);
        if (player.getCurrentSpeaker() != null) btnSpeak.eventHandler(event);

        // 3. לוגיקת ה-Faceset (שדרוג המערכת)
        handleFacesetTouch(event);
    }

    private void handleFacesetTouch(MotionEvent event) {
        final int action = event.getActionMasked();
        final int actionIndex = event.getActionIndex();
        final int pointerId = event.getPointerId(actionIndex);

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            if (facesetHitbox.contains(event.getX(actionIndex), event.getY(actionIndex))) {
                facesetPressed = true;
                facesetPointerId = pointerId;
                facesetBaseRotation = rotationAngle;
                startRotateTo(facesetBaseRotation + 90f);
                startFace = true;
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (facesetPressed) {
                int idx = -1;
                for (int i = 0; i < event.getPointerCount(); i++) {
                    if (event.getPointerId(i) == facesetPointerId) {
                        idx = i;
                        break;
                    }
                }
                if (idx >= 0) {
                    if (!facesetHitbox.contains(event.getX(idx), event.getY(idx))) {
                        startFace = false;
                        startRotateTo(facesetBaseRotation);
                    }
                }
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            if (facesetPressed && pointerId == facesetPointerId) {
                if (facesetHitbox.contains(event.getX(actionIndex), event.getY(actionIndex)) && startFace) {
                    Game.setNextGameState(State.UPGRADE_STATE);
                    facesetBaseRotation = facesetBaseRotation + 90f;
                } else {
                    startRotateTo(facesetBaseRotation);
                }
                facesetPressed = false;
                facesetPointerId = -1;
                startFace = false;
            }
        }
    }

    public void resetJoystickButton() {
        joystick.reset();
        overWorld.setPlayerMoveFalse();
    }

    @Override
    public void onClick(GameButton button) {
        if (button == btnMenu) Game.setNextGameState(State.MENU);
        if (button == btnAttack && !player.isAttacking()) player.setAttacking(true);
        if (button == btnSpeak) {
            overWorld.getPlayingManager().setDialogState(player.getCurrentSpeaker());
            player.resetAnimation();
        }
        if (button == btnJump) player.setJumping(true);
    }

    public static void setShowUI(boolean flag) {
        SHOW_UI = flag;
    }
}