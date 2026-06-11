package com.example.tutorialgame.gamestates.playing.playingstates;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.gamestates.State;
import com.example.tutorialgame.managers.CameraManager;
import com.example.tutorialgame.entities.Entity;
import com.example.tutorialgame.entities.characters.Player;
import com.example.tutorialgame.environments.Doorway;
import com.example.tutorialgame.managers.MapManager;
import com.example.tutorialgame.gamestates.GameState;
import com.example.tutorialgame.gamestates.playing.PlayingManager;
import com.example.tutorialgame.engine.core.Game;
import com.example.tutorialgame.engine.audio.MusicManager;
import com.example.tutorialgame.engine.ui.PlayingUI;
import com.example.tutorialgame.managers.WorldEventManager;
import com.example.tutorialgame.engine.ui.effects.weathereffects.WeatherEffect;
import com.example.tutorialgame.engine.renderer.LightRenderer;
import com.example.tutorialgame.engine.ui.effects.MapTransitionEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OverWorld extends GameState {
    private boolean movePlayer;
    private PointF lastTouchDiff;
    final private MapManager mapManager;
    private final Player player;
    final private PlayingUI playingUI;
    private final PlayingManager playingManager;

    private boolean doorwayJustPassed;
    private List<Entity> listOfDrawables;
    private final List<Entity> visibleEntities = new ArrayList<>();

    private final LightRenderer lightRenderer;
    private final MapTransitionEffect transitionEffect;
    private Doorway pendingDoorway;

    @Override
    public void onEnter() {
        playingUI.resetJoystickButton();

        // מעדכנים כניסה יומית עבור הסלוט הספציפי
        MyApp.getProgress().updateLogin();

        // מרעננים את מצב העולם
        WorldEventManager.refreshWorldState();

        // מנגנים מוזיקה
        MusicManager.getInstance(context).play(MapManager.getCurrentMap().getMusicRes());
        CameraManager.stopShake();

        buildEntityList();
    }

    public OverWorld(Game game, PlayingManager playingManager) {
        super(game);
        this.playingManager = playingManager;
        mapManager = new MapManager(this);
        lightRenderer = new LightRenderer();
        transitionEffect = new MapTransitionEffect();

        player = new Player();
        player.setOnDeathCompleteListener(() -> Game.setNextGameState(State.DEATH_SCREEN));

        MapManager.getCurrentMap().setPlayer(player);
        setCameraRelativeToPlayer(0);

        mapManager.initWeatherForCurrentMap();

        playingUI = new PlayingUI(this);
    }

    @Override
    public void update(double delta) {
        transitionEffect.update(delta);

        if (transitionEffect.isFullyClosed() && pendingDoorway != null) {
            mapManager.changeMap(pendingDoorway.getDoorwayConnectedTo());
            pendingDoorway = null;
            transitionEffect.startOpening();
        }

        if (transitionEffect.getCurrentState() == MapTransitionEffect.State.CLOSING) return;

        playingUI.update(delta);
        mapManager.updateWorldEntities(player, delta);

        // חסימת תנועה בזמן מוות
        if (player.isDead()) setPlayerMoveFalse();

        player.setMovementInput(movePlayer, lastTouchDiff);
        player.update(delta, MapManager.getCurrentMap());

        if (player.hasPendingDialogue() && transitionEffect.getCurrentState() == MapTransitionEffect.State.IDLE) {
            playingManager.setCustomDialogState(player, player.consumeInteriorDialogue());
            setPlayerMoveFalse();
        }

        setCameraRelativeToPlayer(delta);
        mapManager.updateViewRect();
        checkForDoorway();

        updateEffects(delta);
        prepareVisibleEntities();
    }

    private void prepareVisibleEntities() {
        visibleEntities.clear();
        if (listOfDrawables == null) return;

        RectF viewRect = mapManager.getViewRect();

        for (int i = 0; i < listOfDrawables.size(); i++) {
            Entity e = listOfDrawables.get(i);
            if (e != null && RectF.intersects(viewRect, e.getHitBox())) {
                visibleEntities.add(e);
            }
        }

        Collections.sort(visibleEntities);
    }

    public void updateEffects(double delta) {
        mapManager.updateWeather(delta);
    }


    @Override
    public void render(Canvas c) {
        if (MapManager.getCurrentMap() == null) return;

        renderWithoutUi(c);
        playingUI.draw(c);
        transitionEffect.draw(c);
    }

    public void renderWithoutUi(Canvas c) {
        float zoom = CameraManager.getTempZoom();
        c.save();
        c.scale(zoom, zoom);
        c.translate(CameraManager.getOffsetX(), CameraManager.getOffsetY());

        mapManager.drawTiles(c);
        mapManager.drawWeather(c, WeatherEffect.RenderOrder.BELOW_ENTITIES);
        drawSortedEntities(c);
        mapManager.drawXpEffects(c);
        mapManager.drawWeather(c, WeatherEffect.RenderOrder.ABOVE_ENTITIES);

        c.restore();

        lightRenderer.render(c, MapManager.getCurrentMap());
    }

    private void setCameraRelativeToPlayer(double delta) {
        CameraManager.lookAt(player.getHitBox().centerX(), player.getHitBox().centerY(), delta);
    }


    public void buildEntityList() {
        listOfDrawables = MapManager.getCurrentMap().getDrawableList();
    }

    public void resetWorld(Runnable onComplete) {
        mapManager.resetWorldForRestart(newMap -> {
            player.reset();
            newMap.setPlayer(player);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    private void checkForDoorway() {
        Doorway doorwayPlayerIsOn = mapManager.isPlayerOnDoorway(player.getHitBox());

        if (doorwayPlayerIsOn != null) {
            if (!doorwayPlayerIsOn.canBeUsed()) return;
            if (!doorwayJustPassed && transitionEffect.getCurrentState() == MapTransitionEffect.State.IDLE) {
                pendingDoorway = doorwayPlayerIsOn;
                transitionEffect.startClosing();
            }
        } else doorwayJustPassed = false;
    }

    public void setDoorwayJustPassed(boolean doorwayJustPassed) {
        this.doorwayJustPassed = doorwayJustPassed;
    }

    private void drawSortedEntities(Canvas c) {
        for (int i = 0; i < visibleEntities.size(); i++) {
            visibleEntities.get(i).draw(c);
        }
    }

    public void setPlayerMoveTrue(PointF movementVector) {
        this.movePlayer = true;
        this.lastTouchDiff = movementVector;
    }

    public void setPlayerMoveFalse() {
        movePlayer = false;
        player.resetAnimation();
    }

    @Override
    public void touchEvents(MotionEvent event) {
        if (!player.isDead())
            playingUI.touchEvents(event);
    }

    public Player getPlayer() {
        return player;
    }
    public PlayingManager getPlayingManager() {
        return playingManager;
    }
}