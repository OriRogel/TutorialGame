package com.example.tutorialgame.engine.ui.effects.weathereffects.leaves;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;

import android.graphics.Canvas;

import com.example.tutorialgame.components.AnimationComponent;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.engine.ui.effects.weathereffects.WeatherEffect;

/**
 * Represents a falling leaf effect.
 * Inherits physical properties from WeatherEffect and uses AnimationComponent 
 * for synchronized frame management with randomized "noise".
 */
public class Leaf extends WeatherEffect {
    private final Leaves leafType;
    private final AnimationComponent animator = new AnimationComponent(0, 6);
    
    private float driftIntensity;
    private float driftFrequency;
    private double randomOffset;
    private int cycles;

    public Leaf(Leaves leafType, float startX, float startY) {
        super(startX, startY);
        this.leafType = leafType;
    }

    @Override
    public RenderOrder getRenderOrder() {
        return RenderOrder.ABOVE_ENTITIES;
    }

    @Override
    protected void init(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        
        // Randomize leaf-specific physics
        this.speedX = (random.nextBoolean() ? 1 : -1) * (0.5f + random.nextFloat() * 1.5f);
        this.speedY = 0.8f + random.nextFloat() * 1.2f;
        
        this.driftIntensity = 1.0f + random.nextFloat() * 2.5f;
        this.driftFrequency = 0.002f + random.nextFloat() * 0.003f;
        this.randomOffset = random.nextDouble() * 1000;
        
        // Initialize the animator component with a randomized speed
        int animationSpeed = GameConstants.Animation.SPEED + random.nextInt(8) - 4;
        this.animator.setSpeed(animationSpeed);
        
        this.cycles = random.nextInt(5) + 2;

        alpha = 255;
        fading = false;
        dead = false;
    }

    @Override
    public void update(double delta) {
        if (dead) return;

        // Combine base movement with sine-wave drift
        double time = System.currentTimeMillis() + randomOffset;
        float drift = (float) Math.sin(time * driftFrequency) * driftIntensity;
        
        x += (float) ((speedX + drift) * (delta * 0.625*TILE_SIZE));
        y += (float) (speedY * (delta * 0.625*TILE_SIZE));

        if (!fading) {
            // Use the animation component and detect cycle completion
            if (animator.update()) {
                cycles--;
                if (cycles <= 0) fading = true;
            }
        } else {
            alpha -= (int) (10 * delta * 0.625*TILE_SIZE);
            if (alpha <= 0) {
                alpha = 0;
                dead = true;
            }
        }
    }

    @Override
    public void draw(Canvas c) {
        if (dead) return;
        paint.setAlpha(alpha);
        c.drawBitmap(leafType.getSprite(animator.getAniIndex()), x, y, paint);
    }
}
