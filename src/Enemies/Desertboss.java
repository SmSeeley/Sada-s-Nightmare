package Enemies;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import EnhancedMapTiles.DoorKey;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.Map;
import Level.Player;
import Utils.Point;
import java.util.HashMap;

public class Desertboss extends Enemy {

    private final int DETECTION_RADIUS = 100;

    public static boolean hasDied = false;

    private Map currentMap;

    public Desertboss(int id, Point location, Map map) {
        super(id, location.x, location.y, new SpriteSheet(ImageLoader.load("desertBoss.png"), 24, 24), "STAND_RIGHT",16);
        this.currentMap = map;
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

        float desertCenterX = getBounds().getX() + (getBounds().getWidth() / 2);
        float playerCenterX = player.getBounds().getX() + (player.getBounds().getWidth() / 2);

        float desertCenterY = getBounds().getY() + (getBounds().getHeight() / 2);
        float playerCenterY = player.getBounds().getY() + (player.getBounds().getHeight() / 2);

        float distanceX = playerCenterX - desertCenterX;
        float distanceY = playerCenterY - desertCenterY;
        float distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);
        float detectionRadiusSquared = DETECTION_RADIUS * DETECTION_RADIUS;

        if (distanceSquared <= detectionRadiusSquared) {
            if (playerCenterX < desertCenterX) {
                currentAnimationName = "STAND_LEFT";
            } else {
                currentAnimationName = "STAND_RIGHT";
            }
        }
        //if (!keyDropped && health <= 0) {
            //dropKey();
        //}
        if (isDead() && !hasDied) {
            markAsDead();
            System.out.println("[Desertmonster] has died, dropping key!");
        }
        super.update(player);

        
    }
    /*private void dropKey() {
        keyDropped = true;
        Point dropLoc = new Point(getBounds().getX(), getBounds().getY());
        DoorKey key = new DoorKey(dropLoc);

        // Register the key into the current map’s enhanced tiles list
        if (map != null) {
            map.addEnhancedMapTile(key);
            System.out.println("[Ogre] Dropped key at " + dropLoc.x + ", " + dropLoc.y);
        } else {
            System.out.println("[Ogre] Map was null, couldn’t add key!");
        }
    }*/
    public boolean isDead() {
    return hasDied || !getIsActive() || health <= 0;
    }

    public void markAsDead() {
        hasDied = true;
    }

    //public boolean hasDroppedKey() {
        //return keyDropped;
    //}

    //public DoorKey createKey() {
        //Point dropLoc = new Point(getBounds().getX(), getBounds().getY());
        //keyDropped = true;  // mark as dropped
        //return new DoorKey(dropLoc);
    //}
    @Override
        public void draw(GraphicsHandler graphicsHandler) {
            super.draw(graphicsHandler);
        }

    @Override
    public void takeDamage(int amount) {
        boolean wasActive = getIsActive();
        super.takeDamage(amount); // applies cooldown, reduces health, and calls removeEnemy() if <= 0

        // if we were active and now we're not, the enemy just died this frame
        if (wasActive && !getIsActive() && !hasDied) {
            hasDied = true;
            try {
                if (currentMap instanceof Maps.Desert_5) {
                    Maps.Desert_5 desert_5 = (Maps.Desert_5) currentMap;
                    Point dropLoc = desert_5.getMapTile(9, 4).getLocation(); 
                    desert_5.addEnhancedMapTile(new DoorKey(dropLoc));
                    System.out.println("[Monster] Dropped key at " + dropLoc.x + ", " + dropLoc.y);
                }
            } catch (Exception e) {
                System.out.println("[Monster] Failed to drop key: " + e.getMessage());
            }
        }
    }
}
