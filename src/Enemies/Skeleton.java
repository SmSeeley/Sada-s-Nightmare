package Enemies;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import EnhancedMapTiles.DoorKey;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.Player;
import Players.Sada;
import Utils.Direction;
import Utils.Point;
import java.awt.Color;
import java.util.HashMap;

public class Skeleton extends Enemy {
    private final int DETECTION_RADIUS = 600;

    public Skeleton(int id, Point location) {
        super(id, location.x, location.y, new SpriteSheet(ImageLoader.load("skeleton.png"), 24, 24), "STAND_RIGHT", 8);
    }  
    
    // overrides loadAnimations method to define animation
    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{

            put("STAND_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(2)
                    .withBounds(4,2,15,15)
                    .build()
            });

            put("STAND_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(2)
                    .withBounds(5,2,15,15)
                    .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .build()
            });
            

        }};
    }

   @Override
    public void update(Player player) {
        float skeletonCenterX = getBounds().getX() + (getBounds().getWidth() / 2);
        float playerCenterX = player.getBounds().getX() + (player.getBounds().getWidth() / 2);

        float skeletonCenterY = getBounds().getY() + (getBounds().getHeight() / 2);
        float playerCenterY = player.getBounds().getY() + (player.getBounds().getHeight() / 2);

        float distanceX = playerCenterX - skeletonCenterX;
        float distanceY = playerCenterY - skeletonCenterY;
        float distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);
        float detectionRadiusSquared = DETECTION_RADIUS * DETECTION_RADIUS;

        if (distanceSquared <= detectionRadiusSquared) {
            if (playerCenterX < skeletonCenterX) {
                currentAnimationName = "STAND_LEFT";
            } else {
                currentAnimationName = "STAND_RIGHT";
            }
            if (shootTimer == 0) {
                shootProjectileToward(playerCenterX, playerCenterY, skeletonCenterX, skeletonCenterY);
        }
            
        }
        super.update(player);
    }

    private void shootProjectileToward(float playerX, float playerY, float skeletonX, float skeletonY) {
    // Calculate the angle to the player
    float dx = playerX - skeletonX;
    float dy = playerY - skeletonY;
    float magnitude = (float) Math.sqrt(dx * dx + dy * dy);

    // Normalize the direction vector
    float normalizedDx = dx / magnitude;
    float normalizedDy = dy / magnitude;

    // Create and shoot the projectile
    Projectile projectile = new Projectile(
        new Point(skeletonX, skeletonY),
        new SpriteSheet(ImageLoader.load("projectile.png"), 16, 16),
        "PROJECTILE",
        normalizedDx,
        normalizedDy
    );

    projectile.setMap(this.map);
    map.addMapEntity(projectile);

    shootTimer = SHOOT_COOLDOWN; // Reset the shoot timer
    }


    @Override
        public void draw(GraphicsHandler graphicsHandler) {
            //drawBounds(graphicsHandler, new Color(255, 0, 0, 100));
            super.draw(graphicsHandler);
        }
}
