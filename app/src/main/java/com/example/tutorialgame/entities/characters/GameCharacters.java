package com.example.tutorialgame.entities.characters;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.DEFAULT_SIZE;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.Log;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.R;
import com.example.tutorialgame.entities.characters.enemies.EyeMonster;
import com.example.tutorialgame.entities.characters.enemies.Skeleton;
import com.example.tutorialgame.entities.characters.nonenemies.allies.BestFriend;
import com.example.tutorialgame.entities.characters.nonenemies.allies.Blacksmith;
import com.example.tutorialgame.entities.characters.nonenemies.allies.Master;
import com.example.tutorialgame.entities.characters.nonenemies.neutral.BlackKnight;
import com.example.tutorialgame.entities.characters.nonenemies.neutral.Father;
import com.example.tutorialgame.entities.characters.nonenemies.neutral.WhiteKnight;
import com.example.tutorialgame.entities.characters.nonenemies.neutral.fighter.FighterRed;
import com.example.tutorialgame.entities.characters.nonenemies.neutral.fighter.FighterWhite;
import com.example.tutorialgame.managers.BitmapManager;
import com.example.tutorialgame.ui.base.BaseActivity;

import java.util.Objects;

/**
 * Central configuration enum for all character types in the game.
 * Defines sprites, voice assets, physics properties (mass), and UI components.
 */
public enum GameCharacters {
    PLAYER(R.drawable.spr_player, -1, R.raw.sfx_voice_player, 1.2f, -1, DEFAULT_SIZE, DEFAULT_SIZE, 7, 0, 0, 1.0f, Player.class),
    BEST_FRIEND(R.drawable.spr_bestfriend, -1, R.raw.sfx_voice_bestfriend, 1.5f, R.string.best_friend, DEFAULT_SIZE, DEFAULT_SIZE, 7, 1, 0, 1.1f, BestFriend.class),
    BLACK_KNIGHT(R.drawable.spr_blackknight, -1, R.raw.sfx_voice_black_knight, 1.5f, R.string.black_knight, DEFAULT_SIZE, DEFAULT_SIZE, 7, 2, 0, 1.7f, BlackKnight.class),
    WHITE_KNIGHT(R.drawable.spr_whiteknight, -1, R.raw.sfx_voice_white_knight, 1.5f, R.string.white_knight, DEFAULT_SIZE, DEFAULT_SIZE, 7, 3, 0, 1.2f, WhiteKnight.class),
    SKELETON(R.drawable.spr_skeleton, -1, -1, 4.5f, R.string.skeleton, DEFAULT_SIZE, DEFAULT_SIZE, 7, -1, -1, 0.6f, Skeleton.class),
    BLACKSMITH(R.drawable.spr_blacksmith, -1, R.raw.sfx_voice_blacksmith, 1.5f, R.string.blacksmith, DEFAULT_SIZE, DEFAULT_SIZE, 7, 5, 0, 1.5f, Blacksmith.class),
    FATHER(R.drawable.spr_father, -1, R.raw.sfx_voice_blacksmith, 1.5f, R.string.father, 17, 18, 7, 6, 0, 1.0f, Father.class),
    MASTER(R.drawable.spr_master, -1, R.raw.sfx_voice_blacksmith, 1.5f, R.string.master, DEFAULT_SIZE, DEFAULT_SIZE, 7, 7, 0, 0.9f, Master.class),
    FIGHTER_RED(R.drawable.spr_fighter_red, -1, R.raw.sfx_voice_blacksmith, 1.5f, R.string.fighter_red, DEFAULT_SIZE, DEFAULT_SIZE, 7, 8, 0, 1.1f, FighterRed.class),
    FIGHTER_WHITE(R.drawable.spr_fighter_white, -1, R.raw.sfx_voice_blacksmith, 1.5f, R.string.fighter_white, DEFAULT_SIZE, DEFAULT_SIZE, 7, 9, 0, 1.1f, FighterWhite.class),
    EYE_MONSTER(R.drawable.spr_eye, R.drawable.spr_eye_rage, -1, 6, R.string.eye, DEFAULT_SIZE, DEFAULT_SIZE, 4, -1, -1, 0.5f, EyeMonster.class);

    private final Class<? extends Character> characterClass;
    private final Bitmap[][] normalSprites, rageSprites;
    private final Bitmap faceSet;
    private float viewDistance;
    private final int nameId, voiceRes, width, height;
    private final float mass;

    GameCharacters(int resID, int rageResID, int voiceRes, float viewDistance, int charName, int width, int height, int spriteRows, int faceCol, int faceRow, float mass, Class<? extends Character> characterClass) {
        this.characterClass = characterClass;
        this.normalSprites = BitmapManager.getSpritesheet2D(resID, width, height, spriteRows, 4, 1, false);

        if (rageResID != -1)
            this.rageSprites = BitmapManager.getSpritesheet2D(rageResID, width, height, spriteRows, 4, 1, false);
        else this.rageSprites = null;
        if (faceCol != -1 && faceRow != -1)
            this.faceSet = BitmapManager.getBitmapRegion(R.drawable.atl_faceset, 38 * faceCol, 38 * faceRow, 38, 38, 1, false);
        else this.faceSet = null;

        this.viewDistance = viewDistance;
        this.nameId = charName;
        this.voiceRes = voiceRes;
        this.width = width;
        this.height = height;
        this.mass = mass;
    }

    public static Character createCharacter(String type, PointF pos) {
        try {
            // 1. מוצאים את ה-Enum לפי המחרוזת (למשל "SKELETON")
            GameCharacters charEnum = GameCharacters.valueOf(type);

            // 2. שולפים את המחלקה שלו (Skeleton.class)
            Class<? extends Character> clazz = charEnum.characterClass;

            if (clazz == null) return null;

            // 3. יוצרים מופע חדש אוטומטית בעזרת הבנאי שמקבל PointF
            return clazz.getConstructor(PointF.class).newInstance(pos);

        } catch (Exception e) {
            Log.e("GameCharacters", Objects.requireNonNull(e.getMessage()));
            return null;
        }
    }

    public int getVoiceRes() { return voiceRes; }
    public int getHeight() { return height; }
    public int getWidth() { return width; }
    public Bitmap getSprite(int yPos, int xPos) { return normalSprites[yPos][xPos]; }
    public Bitmap getRageSprites(int yPos, int xPos) { return rageSprites[yPos][xPos]; }
    public boolean hasRage() { return rageSprites != null; }
    public Bitmap getFaceSet() { return faceSet; }
    public float getViewDistance() { return viewDistance * TILE_SIZE; }
    public float getMass() { return mass; }
    public void setViewDistance(float viewDistance) { this.viewDistance = viewDistance; }

    public String getName() {
        if (nameId == -1) return MyApp.getProfile().getNickname();
        return BaseActivity.getContext().getString(nameId);
    }
}
