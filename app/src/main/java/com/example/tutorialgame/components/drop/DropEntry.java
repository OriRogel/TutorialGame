package com.example.tutorialgame.components.drop;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.entities.foregrounds.statics.Items;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Defines the loot table for an entity.
 * Supports coins, XP, and specific collectible items with a variable drop chance.
 */
public class DropEntry {
    private final int minCoins, maxCoins;
    private final int minXp, maxXp;
    private final double chance;
    private final List<Items> items;

    /**
     * Comprehensive constructor for all drop types.
     * Use varargs at the end for any number of specific Items.
     */
    public DropEntry(int minCoins, int maxCoins, int minXp, int maxXp, double chance, Items... items) {
        this.minCoins = minCoins;
        this.maxCoins = maxCoins;
        this.minXp = minXp;
        this.maxXp = maxXp;
        this.chance = chance;
        this.items = (items != null && items.length > 0) ? Arrays.asList(items) : Collections.emptyList();
    }

    /**
     * Simplified constructor for dropping only specific items.
     */
    public DropEntry(double chance, Items... items) {
        this(0, 0, 0, 0, chance, items);
    }

    public int getCoinsAmount() {
        // Return minCoins guaranteed, and try for a bonus if chance succeeds
        if (maxCoins > minCoins && MyApp.getRandom().nextDouble() <= chance)
            return minCoins + MyApp.getRandom().nextInt(maxCoins - minCoins + 1);
        return minCoins;
    }

    public int getXpAmount() {
        // Return minXp guaranteed, and try for a bonus if chance succeeds
        if (maxXp > minXp && MyApp.getRandom().nextDouble() <= chance)
            return minXp + MyApp.getRandom().nextInt(maxXp - minXp + 1);
        return minXp;
    }

    /**
     * @return The pre-defined list of items if the chance roll is successful.
     * Uses Collections.emptyList() to avoid new object allocations.
     */
    public List<Items> getItemsToDrop() {
        if (!items.isEmpty() && MyApp.getRandom().nextDouble() <= chance/2) {
            return items;
        }
        return null;
    }
}
