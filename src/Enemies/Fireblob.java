package Enemies;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.Player;
import Players.Sada;
import Utils.Point;
import java.awt.Color;
import java.util.HashMap;

public class Fireblob extends Enemy {
    private final int DETECTION_RADIUS = 100;

    public boolean keyDropped = false;

    public Fireblob(int id, Point location) {
        super(id, location.x, location.y, new SpriteSheet(ImageLoader.load("fireblob.png"), 24, 24), "STAND_RIGHT", 2);
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
        if (player instanceof Sada) {
            chase((Sada) player);
        }

        float fireblobCenterX = getBounds().getX() + (getBounds().getWidth() / 2);
        float playerCenterX = player.getBounds().getX() + (player.getBounds().getWidth() / 2);

        float fireblobCenterY = getBounds().getY() + (getBounds().getHeight() / 2);
        float playerCenterY = player.getBounds().getY() + (player.getBounds().getHeight() / 2);

        float distanceX = playerCenterX - fireblobCenterX;
        float distanceY = playerCenterY - fireblobCenterY;
        float distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);
        float detectionRadiusSquared = DETECTION_RADIUS * DETECTION_RADIUS;

        if (distanceSquared <= detectionRadiusSquared) {
            if (playerCenterX < fireblobCenterX) {
                currentAnimationName = "STAND_LEFT";
            } else {
                currentAnimationName = "STAND_RIGHT";
            }
        }
        super.update(player);
    }
    
    public boolean isDead() {
        return health <= 0;
    }

    
    @Override
        public void draw(GraphicsHandler graphicsHandler) {
            //drawBounds(graphicsHandler, new Color(255, 0, 0, 100));
            super.draw(graphicsHandler);
        }

    public void chase(Sada sada) {
        float chaseSpeed = 1.5f; 

        float blobX = getX();
        float blobY = getY();
        float sadaX = sada.getX();
        float sadaY = sada.getY();

        // Calculate distance in each direction
        float dx = sadaX - blobX;
        float dy = sadaY - blobY;

        // Stop chasing if they’re touching
        if (Math.abs(dx) < 5 && Math.abs(dy) < 5) {
            currentAnimationName = dx < 0 ? "STAND_LEFT" : "STAND_RIGHT";
            return;
        }

        if (Math.abs(dx) > Math.abs(dy)) {
            // move horizontally
            if (dx > 0) {
                moveXHandleCollision(chaseSpeed);
                currentAnimationName = "STAND_RIGHT";
            } else {
                moveXHandleCollision(-chaseSpeed);
                currentAnimationName = "STAND_LEFT";
            }
            } else {
            // move vertically 
            if (dy > 0) {
                moveYHandleCollision(chaseSpeed);
                } else {
                moveYHandleCollision(-chaseSpeed);
                }
            }
        }
}
