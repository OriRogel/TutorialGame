package com.example.tutorialgame.entities.characters.nonenemies.allies;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;

import android.graphics.PointF;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.R;
import com.example.tutorialgame.components.drop.DropEntry;
import com.example.tutorialgame.entities.Weapons;
import com.example.tutorialgame.entities.characters.GameCharacters;
import com.example.tutorialgame.managers.MapManager;

import java.util.Objects;

public class BestFriend extends Ally {
    public BestFriend(PointF pos) {
        super(pos, GameCharacters.BEST_FRIEND, 350, 250, 2.6f);
//        updatePosAccordingToStory();
    }

    public void updatePosAccordingToStory() {
        // השתמש בזה רק ב-Constructor כדי למקם אותו כשפותחים את המשחק מחדש
        if (MyApp.getWorldStateDoc().getCheckPoint("event_player_talkedToFriend2")) {
            // מיקום ליד השער
            teleportTo(spawnPos.x - 1.3f * TILE_SIZE, spawnPos.y + SCALE_MULTIPLIER * 2);
        } else if (MyApp.getWorldStateDoc().getCheckPoint("event_player_receivedWeapon")) {
            // הוא אמור להיות בחנות
            MapManager.getMapByName("village.tmx").removeCharacter(getGameCharType().getName());
            MapManager.getMapByName("weapon_store.tmx").addCharacter(this);
            teleportTo(TILE_SIZE , TILE_SIZE);
            Objects.requireNonNull(MapManager.getMapByName("weapon_store.tmx")).getDoorwayByName("weapon_store_to_village").setDoorwayActive(false);
        } else if (MyApp.getWorldStateDoc().getCheckPoint("event_player_talkedToFriend")) {
            teleportTo(spawnPos.x - 1.3f * TILE_SIZE, spawnPos.y + SCALE_MULTIPLIER * 2);
        }
    }

    //abstracted implements from character
    @Override
    public Weapons getWeapon() {
        return Weapons.CLUB;
    }
    @Override
    public int getAttackDamage() {
        return 35;
    }
    @Override
    protected int getHealth() {
        return 200;
    }
    @Override
    protected int getStamina() {
        return 200;
    }
    @Override
    protected int getStaminaCoolDown() {
        return 3000;
    }
    @Override
    public int getImpactSfx() {
        return R.raw.sfx_impact_player;
    }
    @Override
    public DropEntry getDropEntry() {
        return new DropEntry(0, 0, 0, 0, 0);
    }
    @Override
    public float getTimeToApexSec() {
        return 0.35f;
    }
    @Override
    public float getDesiredTiles() {
        return 1.5f;
    }
}
