package com.example.tutorialgame.entities.foregrounds.breakable;

import android.graphics.Bitmap;
import com.example.tutorialgame.R;
import com.example.tutorialgame.managers.BitmapManager;

public enum BreakableType {
    // ResId, Particles, canRespawn, respawnTimeMs
    WOODEN_BOX(0, 4, 5, 6, BreakParticlesType.WOOD, false, 10000),
    BUSH_1(0, 1, 15, 13, BreakParticlesType.GRASS, true, 9000),
    BUSH_2(15, 1, 16, 13, BreakParticlesType.GRASS, true, 12000),
    BUSH_3(31, 1, 16, 13, BreakParticlesType.GRASS, true, 8060),
    ROCK_1(64, 32, 16, 16, BreakParticlesType.ROCK, true, 15000),
    ROCK_2(80, 32, 16, 16, BreakParticlesType.ROCK, true, 15000),
    TREE_TRUNK(0, 31, 16, 16, BreakParticlesType.WOOD, true, 20000);

    private final Bitmap spr;
    private final BreakParticlesType particles;
    private final boolean canRespawn;
    private final long respawnTime;

    BreakableType(int x, int y, int width, int height, BreakParticlesType particles, boolean canRespawn, long respawnTime) {
        this.spr = BitmapManager.getBitmapRegion(R.drawable.atl_breakables, x, y, width, height, 1, false);
        this.particles = particles;
        this.canRespawn = canRespawn;
        this.respawnTime = respawnTime;
    }

    public Bitmap getSpr() { return spr; }
    public BreakParticlesType getParticles() { return particles; }
    public int getWidth() { return spr.getWidth(); }
    public int getHeight() { return spr.getHeight(); }
    public boolean canRespawn() { return canRespawn; }
    public long getRespawnTime() { return respawnTime; }
}
