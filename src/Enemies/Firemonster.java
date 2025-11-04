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
import Utils.Point;
import java.awt.Color;
import java.util.HashMap;

public class Firemonster extends Enemy {
    private final int DETECTION_RADIUS = 100;

    public boolean keyDropped = false;

    public Firemonster(int id, Point location) {
        super(id, location.x, location.y, new SpriteSheet(ImageLoader.load("firemonster.png"), 24, 24), "STAND_RIGHT", 16);
    }  
    
    // overrides loadAnimations method to define animation
    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{

            put("STAND_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(3)
                    .withBounds(4,2,15,15)
                    .build()
            });

            put("STAND_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(3)
                    .withBounds(5,2,15,15)
                    .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .build()
            });
            

        }};
    }

   @Override
    public void update(Player player) {
        float firemonsterCenterX = getBounds().getX() + (getBounds().getWidth() / 2);
        float playerCenterX = player.getBounds().getX() + (player.getBounds().getWidth() / 2);

        float firemonsterCenterY = getBounds().getY() + (getBounds().getHeight() / 2);
        float playerCenterY = player.getBounds().getY() + (player.getBounds().getHeight() / 2);

        float distanceX = playerCenterX - firemonsterCenterX;
        float distanceY = playerCenterY - firemonsterCenterY;
        float distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);
        float detectionRadiusSquared = DETECTION_RADIUS * DETECTION_RADIUS;

        if (distanceSquared <= detectionRadiusSquared) {
            if (playerCenterX < firemonsterCenterX) {
                currentAnimationName = "STAND_LEFT";
            } else {
                currentAnimationName = "STAND_RIGHT";
            }
        }
        //if (!keyDropped && health <= 0) {
            //dropKey();
        //}
        super.update(player);
    }
    private void dropKey() {
        keyDropped = true;
        Point dropLoc = new Point(getBounds().getX(), getBounds().getY());
        DoorKey key = new DoorKey(dropLoc);

        // Register the key into the current map’s enhanced tiles list
        if (map != null) {
            map.addEnhancedMapTile(key);
            System.out.println("[firemonster] Dropped key at " + dropLoc.x + ", " + dropLoc.y);
        } else {
            System.out.println("[firemonster] Map was null, couldn’t add key!");
        }
    }
    public boolean isDead() {
    return health <= 0;
    }

    public boolean hasDroppedKey() {
        return keyDropped;
    }

    public DoorKey createKey() {
        Point dropLoc = new Point(getBounds().getX(), getBounds().getY());
        keyDropped = true;  // mark as dropped
        return new DoorKey(dropLoc);
    }
    @Override
        public void draw(GraphicsHandler graphicsHandler) {
            //drawBounds(graphicsHandler, new Color(255, 0, 0, 100));
            super.draw(graphicsHandler);
        }
}
