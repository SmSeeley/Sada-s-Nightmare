package Enemies;

import Engine.ImageLoader;
import GameObject.SpriteSheet;
import Utils.Point;

/**
 * VladProjectile
 *
 * Uses VladBall.png and a direction vector (dirX, dirY).
 * Damage: 0.5 hearts (less punishing than before).
 */
public class VladProjectile extends NProjectile {

    public VladProjectile(Point location, float dirX, float dirY, float speed) {
        super(
                location,
                new SpriteSheet(ImageLoader.load("VladBall.png"), 16, 16),
                "PROJECTILE",
                0f,
                0f,
                speed
        );

        // Avoid zero-length direction
        if (dirX == 0 && dirY == 0) {
            dirY = 1;
        }

        float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        float ux = dirX / len;
        float uy = dirY / len;

        // Set velocity
        this.vx = ux * speed;
        this.vy = uy * speed;

        // Less damage: half heart
        this.damage = 0.5;
    }
}
