package com.example.tutorialgame.entities.foregrounds.animated;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import com.example.tutorialgame.components.AnimationComponent;
import com.example.tutorialgame.entities.Entity;
import com.example.tutorialgame.entities.characters.Player;

/**
 * A generic entity for animated objects like torches, fountains, or moving decorations.
 */
public class AnimatedObject extends Entity {

    protected final AnimationComponent animation;
    protected final Bitmap[] sprites;

    public AnimatedObject(PointF pos, AnimatedType type) {
        super(pos, type.getWidth(), type.getHeight(), type.isCollision());
        this.sprites = type.getSprites();
        this.animation = new AnimationComponent(type.getSpeed(), sprites.length);
        collider = isCollider();
    }

    public void update(double delta, Player player) {
        animation.update();
    }

    @Override
    public void draw(Canvas c) {
        if (sprites == null || sprites.length == 0) return;
        c.drawBitmap(sprites[animation.getAniIndex()], hitBox.left, hitBox.top, null);
    }
}
