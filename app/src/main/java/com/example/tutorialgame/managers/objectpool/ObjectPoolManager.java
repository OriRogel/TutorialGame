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
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized manager for Object Pooling.
 * Recycles objects for high-frequency entities to avoid GC pressure.
 */
public class ObjectPoolManager {

    private static final int MAX_POOL_SIZE = 100;

    // Pools using the generic ObjectPool class
    private static final ObjectPool<BrokenParticle> particlePool = new ObjectPool<>(MAX_POOL_SIZE, BrokenParticle::new);
    private static final ObjectPool<Coin> coinPool = new ObjectPool<>(MAX_POOL_SIZE, () -> new Coin(0, 0));
    private static final ObjectPool<XpEffect> xpPool = new ObjectPool<>(MAX_POOL_SIZE, () -> new XpEffect(0, 0, 0));
    private static final ObjectPool<CollectibleItem> itemPool = new ObjectPool<>(MAX_POOL_SIZE, () -> new CollectibleItem(0, 0, Items.EMPTY_POT));
    private static final ObjectPool<RespawnTask> respawnPool = new ObjectPool<>(MAX_POOL_SIZE, RespawnTask::new);

    // --- Character Pool (Dynamic for different types) ---
    private static final Map<String, ObjectPool<Character>> characterPools = new HashMap<>();

    public static Character acquireCharacter(String type, PointF pos) {
        ObjectPool<Character> pool;
        synchronized (characterPools) {
            pool = characterPools.computeIfAbsent(type, k ->
                new ObjectPool<>(MAX_POOL_SIZE, () -> GameCharacters.createCharacter(k, new PointF(0, 0)))
            );
        }
        Character c = pool.acquire();
        c.init(pos); // Re-initialize state
        return c;
    }

    public static void releaseCharacter(Character c) {
        if (c == null) return;
        String type = c.getGameCharType().name();
        ObjectPool<Character> pool;
        synchronized (characterPools) {
            pool = characterPools.get(type);
        }
        if (pool != null) {
            pool.release(c);
        }
    }

    // --- Collectible Item Pool ---
    public static CollectibleItem acquireCollectibleItem(float x, float y, Items type) {
        CollectibleItem item = itemPool.acquire();
        item.init(x, y, type);
        return item;
    }

    public static void releaseCollectibleItem(CollectibleItem item) {
        itemPool.release(item);
    }

    // --- Other Pools ---
    public static BrokenParticle acquireParticle(float x, float y, Bitmap img) {
        BrokenParticle p = particlePool.acquire();
        p.init(x, y, img);
        return p;
    }

    public static void releaseParticle(BrokenParticle p) {
        particlePool.release(p);
    }

    public static Coin acquireCoin(float x, float y) {
        Coin c = coinPool.acquire();
        c.init(x, y);
        return c;
    }

    public static void releaseCoin(Coin c) {
        coinPool.release(c);
    }

    public static XpEffect acquireXpEffect(int value, float x, float y) {
        XpEffect xp = xpPool.acquire();
        xp.init(value, x, y);
        return xp;
    }

    public static void releaseXpEffect(XpEffect xp) {
        xpPool.release(xp);
    }

    public static class RespawnTask {
        public double timeLeft;
        public void init(double delay) { this.timeLeft = delay; }
    }

    public static RespawnTask acquireRespawnTask(double delay) {
        RespawnTask task = respawnPool.acquire();
        task.init(delay);
        return task;
    }

    public static void releaseRespawnTask(RespawnTask task) {
        respawnPool.release(task);
    }

    public static void clearAllPools() {
        particlePool.clear();
        coinPool.clear();
        xpPool.clear();
        itemPool.clear();
        respawnPool.clear();
        synchronized (characterPools) {
            for (ObjectPool<Character> pool : characterPools.values()) {
                pool.clear();
            }
            characterPools.clear();
        }
    }
}