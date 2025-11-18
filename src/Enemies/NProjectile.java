package Enemies;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.SpriteSheet;
import Level.MapEntity;
import Level.MapEntityStatus;
import Utils.Point;

import java.awt.Color;
import java.util.HashMap;

/**
 * NProjectile – Custom projectile used only by Vladmir.
 *
 * - Moves along normalized vector (dirX, dirY)
 * - Uses VladBall.png
 * - Has speed + damage parameters
 * - Despawns on collision or max distance
 */
public class NProjectile extends MapEntity {

    // Store our own spriteSheet because MapEntity might not expose one
    private SpriteSheet mySpriteSheet;

    private float dirX;
    private float dirY;
    private float speed;
    private double damage;

    private static final float MAX_DISTANCE = 800f;
    private float distanceTraveled = 0f;

    public NProjectile(Point location, float dirX, float dirY, float speed, double damage) {
        super(
            location.x,
            location.y,
            new SpriteSheet(ImageLoader.load("VladBall.png"), 16, 16),
            "PROJECTILE"
        );

        // Save sprite sheet reference (fixes your error)
        this.mySpriteSheet = new SpriteSheet(ImageLoader.load("VladBall.png"), 16, 16);

        // Normalize direction
        float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (len == 0) {
            this.dirX = 1;
            this.dirY = 0;
        } else {
            this.dirX = dirX / len;
            this.dirY = dirY / len;
        }

        this.speed = speed;
        this.damage = damage;

        this.animations = loadAnimations(mySpriteSheet);
        this.currentAnimationName = "PROJECTILE";
    }

    public double getDamage() {
        return damage;
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        HashMap<String, Frame[]> map = new HashMap<>();

        map.put("PROJECTILE", new Frame[] {
            new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(2.5f)
                    .withBounds(0, 0, 16, 16)
                    .build()
        });

        return map;
    }

    @Override
    public void update() {
        if (mapEntityStatus == MapEntityStatus.REMOVED) {
            return;
        }

        float stepX = dirX * speed;
        float stepY = dirY * speed;

        float movedX = moveXHandleCollision(stepX);
        float movedY = moveYHandleCollision(stepY);

        float frameDist = (float) Math.sqrt(movedX * movedX + movedY * movedY);
        distanceTraveled += frameDist;

        // Detect wall hit
        boolean collided =
                Math.abs(movedX) < Math.abs(stepX) - 0.001f ||
                Math.abs(movedY) < Math.abs(stepY) - 0.001f;

        if (collided || distanceTraveled >= MAX_DISTANCE) {
            mapEntityStatus = MapEntityStatus.REMOVED;
        }

        super.update();
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        // Debug hitbox:
        // drawBounds(graphicsHandler, new Color(255, 0, 0, 100));

        super.draw(graphicsHandler);
    }
}
