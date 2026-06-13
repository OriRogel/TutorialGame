package com.example.tutorialgame.components.drop;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.engine.ui.effects.XpEffect;
import com.example.tutorialgame.entities.foregrounds.animated.Coin;
import com.example.tutorialgame.entities.characters.Character;
import com.example.tutorialgame.entities.foregrounds.collectible.CollectibleItem;
import com.example.tutorialgame.entities.foregrounds.statics.Items;
import com.example.tutorialgame.managers.MapManager;
import com.example.tutorialgame.managers.objectpool.ObjectPoolManager;
import com.example.tutorialgame.ui.base.BaseActivity;
import com.example.tutorialgame.R;
import java.util.List;

/**
 * Handles the logic for spawning loot (coins, XP, items) when a character dies.
 */
public class DropComponent {
    private final Character character;
    private final DropEntry dropEntry;

    public DropComponent(Character character) {
        dropEntry = character.getDropEntry();
        this.character = character;
    }

    /**
     * Spawns coins based on the loot table.
     */
    public void dropCoin() {
        if (dropEntry == null) return;
        for (int i = 0; i < dropEntry.getCoinsAmount(); i++) {
            float x = MyApp.getRandom().nextInt(4 * SCALE_MULTIPLIER) + character.getHitBox().left + SCALE_MULTIPLIER;
            float y = MyApp.getRandom().nextInt(4 * SCALE_MULTIPLIER) + character.getHitBox().top + SCALE_MULTIPLIER;

            Coin coin = ObjectPoolManager.acquireCoin(x, y);
            MapManager.getCurrentMap().addCoin(coin);
            SoundManager.getInstance(BaseActivity.getContext()).playSpatialSfx(R.raw.sfx_coin_drop, x);
        }
    }

    /**
     * Drops all specific items defined in the DropEntry if the chance roll succeeds.
     */
    public void dropItems() {
        if (dropEntry == null) return;
        List<Items> itemsToDrop = dropEntry.getItemsToDrop();
        if (itemsToDrop == null) return;
        for (Items item : itemsToDrop) {
            dropItem(item);
        }
    }

    /**
     * Drops a specific item at the character's location.
     * @param item The item type to drop.
     */
    public void dropItem(Items item) {
        if (item == null) return;
        float x = character.getHitBox().centerX();
        float y = character.getHitBox().centerY();
        
        CollectibleItem collectible = ObjectPoolManager.acquireCollectibleItem(x, y, item);
        MapManager.getCurrentMap().addCollectibleItem(collectible);
    }

    /**
     * Grants XP and spawns visual XP effect.
     */
    public void dropXp() {
        if (dropEntry == null) return;
        if(character.getDiedBy() == GameConstants.Faction.PLAYER && character.getFaction() != GameConstants.Faction.ALLY) {
            int xpAmount = dropEntry.getXpAmount();
            if (xpAmount > 0) {
                MyApp.getProgress().updateXp(xpAmount);
                XpEffect xpEffect = ObjectPoolManager.acquireXpEffect(xpAmount, character.getHitBox().left, character.getHitBox().top);
                MapManager.getCurrentMap().addXp(xpEffect);
            }
        }
    }

    /**
     * Drops all loot (coins and items) at once.
     */
    public void dropLoot() {
        dropCoin();
        dropItems();
    }
}
