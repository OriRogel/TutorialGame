package com.example.tutorialgame.managers.objectpool;

import android.graphics.Bitmap;
import android.graphics.PointF;
import com.example.tutorialgame.engine.ui.effects.XpEffect;
import com.example.tutorialgame.entities.characters.Character;
import com.example.tutorialgame.entities.characters.GameCharacters;
import com.example.tutorialgame.entities.foregrounds.animated.Coin;
import com.example.tutorialgame.entities.foregrounds.breakable.BrokenParticle;
import com.example.tutorialgame.entities.foregrounds.collectible.CollectibleItem;
import com.example.tutorialgame.entities.foregrounds.statics.Items;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralized manager for Object Pooling.
 * Recycles objects for high-frequency entities to avoid GC pressure.
 */
public class ObjectPoolManager {

    private static final int MAX_POOL_SIZE = 100;

    // Pools for simple entities
    private static final BrokenParticle[] particlePool = new BrokenParticle[MAX_POOL_SIZE];
    private static int particleTop = 0;

    private static final Coin[] coinPool = new Coin[MAX_POOL_SIZE];
    private static int coinTop = 0;

    private static final XpEffect[] xpPool = new XpEffect[MAX_POOL_SIZE];
    private static int xpTop = 0;

    private static final CollectibleItem[] itemPool = new CollectibleItem[MAX_POOL_SIZE];
    private static int itemTop = 0;

    // --- Character Pool (Dynamic for different types) ---
    private static final Map<String, List<Character>> characterPools = new HashMap<>();

    public static Character acquireCharacter(String type, PointF pos) {
        synchronized (characterPools) {
            List<Character> pool = characterPools.get(type);
            if (pool != null && !pool.isEmpty()) {
                Character c = pool.remove(pool.size() - 1);
                c.init(pos); // Re-initialize state
                return c;
            }
        }
        return GameCharacters.createCharacter(type, pos);
    }

    public static void releaseCharacter(Character c) {
        if (c == null) return;
        String type = c.getGameCharType().name();
        synchronized (characterPools) {
            List<Character> pool = characterPools.computeIfAbsent(type, k -> new ArrayList<>());
            if (pool.size() < MAX_POOL_SIZE) {
                pool.add(c);
            }
        }
    }

    // --- Collectible Item Pool ---
    public static CollectibleItem acquireCollectibleItem(float x, float y, Items type) {
        CollectibleItem item;
        synchronized (itemPool) {
            if (itemTop > 0) {
                item = itemPool[--itemTop];
                itemPool[itemTop] = null;
            } else {
                item = new CollectibleItem(x, y, type);
            }
        }
        item.init(x, y, type);
        return item;
    }

    public static void releaseCollectibleItem(CollectibleItem item) {
        if (item == null) return;
        synchronized (itemPool) {
            if (itemTop < MAX_POOL_SIZE) {
                itemPool[itemTop++] = item;
            }
        }
    }

    // --- Other Pools ---
    public static BrokenParticle acquireParticle(float x, float y, Bitmap img) {
        BrokenParticle p;
        synchronized (particlePool) {
            if (particleTop > 0) {
                p = particlePool[--particleTop];
                particlePool[particleTop] = null;
            } else p = new BrokenParticle();
        }
        p.init(x, y, img);
        return p;
    }

    public static void releaseParticle(BrokenParticle p) {
        if (p == null) return;
        synchronized (particlePool) {
            if (particleTop < MAX_POOL_SIZE) {
                particlePool[particleTop++] = p;
            }
        }
    }

    public static Coin acquireCoin(float x, float y) {
        Coin c;
        synchronized (coinPool) {
            if (coinTop > 0) {
                c = coinPool[--coinTop];
                coinPool[coinTop] = null;
            } else c = new Coin(x, y);
        }
        c.init(x, y);
        return c;
    }

    public static void releaseCoin(Coin c) {
        if (c == null) return;
        synchronized (coinPool) {
            if (coinTop < MAX_POOL_SIZE) {
                coinPool[coinTop++] = c;
            }
        }
    }

    public static XpEffect acquireXpEffect(int value, float x, float y) {
        XpEffect xp;
        synchronized (xpPool) {
            if (xpTop > 0) {
                xp = xpPool[--xpTop];
                xpPool[xpTop] = null;
            } else xp = new XpEffect(value, x, y);
        }
        xp.init(value, x, y);
        return xp;
    }

    public static void releaseXpEffect(XpEffect xp) {
        if (xp == null) return;
        synchronized (xpPool) {
            if (xpTop < MAX_POOL_SIZE) {
                xpPool[xpTop++] = xp;
            }
        }
    }

    public static class RespawnTask {
        public double timeLeft;
        public void init(double delay) { this.timeLeft = delay; }
    }

    private static final RespawnTask[] respawnPool = new RespawnTask[MAX_POOL_SIZE];
    private static int respawnTop = 0;

    public static RespawnTask acquireRespawnTask(double delay) {
        RespawnTask task;
        synchronized (respawnPool) {
            if (respawnTop > 0) {
                task = respawnPool[--respawnTop];
                respawnPool[respawnTop] = null;
            } else task = new RespawnTask();
        }
        task.init(delay);
        return task;
    }

    public static void releaseRespawnTask(RespawnTask task) {
        if (task == null) return;
        synchronized (respawnPool) {
            if (respawnTop < MAX_POOL_SIZE) respawnPool[respawnTop++] = task;
        }
    }

    public static void clearAllPools() {
        synchronized (particlePool) { for (int i = 0; i < particleTop; i++) particlePool[i] = null; particleTop = 0; }
        synchronized (coinPool) { for (int i = 0; i < coinTop; i++) coinPool[i] = null; coinTop = 0; }
        synchronized (xpPool) { for (int i = 0; i < xpTop; i++) xpPool[i] = null; xpTop = 0; }
        synchronized (itemPool) { for (int i = 0; i < itemTop; i++) itemPool[i] = null; itemTop = 0; }
        synchronized (respawnPool) { for (int i = 0; i < respawnTop; i++) respawnPool[i] = null; respawnTop = 0; }
        synchronized (characterPools) { characterPools.clear(); }
    }
}
