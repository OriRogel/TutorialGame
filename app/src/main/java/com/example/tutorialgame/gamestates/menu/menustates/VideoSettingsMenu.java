package com.example.tutorialgame.gamestates.menu.menustates;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_HEIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.core.Game;
import com.example.tutorialgame.engine.renderer.CharacterRenderer;
import com.example.tutorialgame.engine.ui.PlayingUI;
import com.example.tutorialgame.engine.ui.customviews.CustomSeekBar;
import com.example.tutorialgame.engine.ui.customviews.radiogroup.CustomRadioGroup;
import com.example.tutorialgame.engine.ui.customviews.buttons.rects.RectImages;
import com.example.tutorialgame.engine.ui.customviews.buttons.rects.RectButton;
import com.example.tutorialgame.engine.ui.customviews.radiogroup.RadioGroupList;
import com.example.tutorialgame.engine.ui.customviews.switches.CustomSwitch;
import com.example.tutorialgame.engine.ui.customviews.switches.SwitchList;
import com.example.tutorialgame.engine.ui.effects.impcateffects.ImpactEffectType;
import com.example.tutorialgame.gamestates.menu.BaseMenu;
import com.example.tutorialgame.gamestates.menu.MenuManager;
import com.example.tutorialgame.managers.CameraManager;
import com.example.tutorialgame.managers.MapManager;

/**
 * Sub-menu for adjusting video, camera, and debug settings.
 * Allows users to customize the visual experience and performance overlays.
 */
public class VideoSettingsMenu extends BaseMenu implements CustomRadioGroup.OnSelectionChangedListener {
    private final RectButton btnDone;
    private final CustomSeekBar sbZoom, sbCameraSpeed;
    private final CustomRadioGroup radioTapEffects;
    private final CustomSwitch swFPS, swHitbox, swUI, swCamShake;

    public VideoSettingsMenu(Game game, MenuManager menuManager) {
        super(game, menuManager);

        // UI Initialization
        btnDone = new RectButton((int) (SCREEN_WIDTH * 0.2), (int) (SCREEN_HEIGHT * 0.7),
                (int) (SCREEN_WIDTH * 0.6), (int) (SCREEN_HEIGHT * 0.15), RectImages.DONE, false);
        btnDone.setOnClickListener(button -> menuManager.setCurrentMenuState(MenuManager.MenuState.Options));

        radioTapEffects = new CustomRadioGroup(3 * TILE_SIZE, 3.7f * TILE_SIZE, RadioGroupList.TAP_EFFECT);
        radioTapEffects.setOnSelectionChangedListener(this);

        float rightColumnX = radioTapEffects.getHitbox().right + SCALE_MULTIPLIER * 5;
        float topY = radioTapEffects.getHitbox().top;

        swFPS = new CustomSwitch(rightColumnX, topY, SwitchList.FPS);
        swHitbox = new CustomSwitch(rightColumnX, topY + TILE_SIZE, SwitchList.HITBOX);
        swUI = new CustomSwitch(rightColumnX + TILE_SIZE*5.4f, topY, SwitchList.UI);
        swCamShake = new CustomSwitch(rightColumnX + TILE_SIZE*5.4f, topY + TILE_SIZE, SwitchList.CAM_SHAKE);

        swFPS.setOnCheckedChangeListener((customSwitch, isChecked) -> game.setShowFPS(isChecked));
        swHitbox.setOnCheckedChangeListener((customSwitch, isChecked) -> CharacterRenderer.setShowHitbox(isChecked));
        swUI.setOnCheckedChangeListener((customSwitch, isChecked) -> PlayingUI.setShowUI(isChecked));
        swCamShake.setOnCheckedChangeListener((customSwitch, isChecked) -> CameraManager.setCamShake(isChecked));

        sbZoom = new CustomSeekBar(rightColumnX, topY + 2.1f * TILE_SIZE, SCREEN_WIDTH * 0.4f, TILE_SIZE, R.string.zoom_cap);
        sbCameraSpeed = new CustomSeekBar(rightColumnX, topY + 3.3f * TILE_SIZE, SCREEN_WIDTH * 0.4f, TILE_SIZE, R.string.camera_flow_cap);
        
        // Load initial values from CameraManager
        sbZoom.setProgressNormalized(CameraManager.getNormalizedZoom());
        sbCameraSpeed.setProgressNormalized(CameraManager.getNormalizedLerpFactor());
        
        // Setup Listeners
        sbZoom.setOnProgressChangedListener((seekBar, progress) -> {
            CameraManager.setZoomFromProgress(progress);
            // Safe center-on-player for immediate visual feedback
            if (MapManager.getCurrentMap() != null && MapManager.getCurrentMap().getPlayer() != null) {
                RectF r = MapManager.getCurrentMap().getPlayer().getHitBox();
                CameraManager.lookAt(r.centerX(), r.centerY(), 1.0); // Use 1.0 delta for instant snap
            }
        });
        
        sbCameraSpeed.setOnProgressChangedListener((seekBar, progress) -> CameraManager.setLerpFactorFromProgress(progress));
    }

    @Override
    public void onSelectionChanged(CustomRadioGroup group, int selectedIndex) {
        if (group == radioTapEffects) {
            switch (selectedIndex) {
                case 0: game.setTapEffectType(ImpactEffectType.WHITE_CIRCLE); break;
                case 1: game.setTapEffectType(ImpactEffectType.ORANGE_CIRCLE); break;
                case 2: game.setTapEffectType(ImpactEffectType.SPARK); break;
                case 3: game.setTapEffectType(ImpactEffectType.SPARK2); break;
            }
        }
    }

    @Override
    public void update(double delta) { }

    @Override
    public void render(Canvas c) {
        drawBackground(c, R.string.video_settings);
        btnDone.draw(c);
        radioTapEffects.render(c);
        swFPS.draw(c);
        swHitbox.draw(c);
        swUI.draw(c);
        swCamShake.draw(c);
        sbZoom.drawSeekBar(c);
        sbCameraSpeed.drawSeekBar(c);
    }

    @Override
    public void touchEvents(MotionEvent event) {
        btnDone.eventHandler(event);
        radioTapEffects.eventHandler(event);
        swFPS.eventHandler(event);
        swHitbox.eventHandler(event);
        swUI.eventHandler(event);
        swCamShake.eventHandler(event);
        sbZoom.eventHandler(event);
        sbCameraSpeed.eventHandler(event);
    }
}