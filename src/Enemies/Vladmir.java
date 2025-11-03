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
import EnhancedMapTiles.DoorKey;
import java.util.HashMap;

public class Vladmir extends Enemy {

    private int health = 10;

    private final int DETECTION_RADIUS = 100;

    public boolean keyDropped = false;

    public Vladmir(int id, Point location) {
        super(id, location.x, location.y, new SpriteSheet(ImageLoader.load("Vladmir.png"), 24, 24), "STAND_RIGHT");
    }  
    
    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("STAND_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                        .withScale(3)
                        .withBounds(6, 12, 12, 7)
                        .build()
            });

            put("STAND_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                        .withScale(3)
                        .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                        .withBounds(6, 12, 12, 7)
                        .build()
            });

            put("WALK_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(1, 0), 14)
                        .withScale(3)
                        .withBounds(6, 12, 12, 7)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(1, 1), 14)
                        .withScale(3)
                        .withBounds(6, 12, 12, 7)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(1, 2), 14)
                        .withScale(3)
                        .withBounds(6, 12, 12, 7)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(1, 3), 14)
                        .withScale(3)
                        .withBounds(6, 12, 12, 7)
                        .build()
            });

            put("WALK_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(1, 0), 14)
                        .withScale(3)
                        .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                        .withBounds(6, 12, 12, 7)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(1, 1), 14)
                        .withScale(3)
                        .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                        .withBounds(6, 12, 12, 7)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(1, 2), 14)
                        .withScale(3)
                        .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                        .withBounds(6, 12, 12, 7)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(1, 3), 14)
                        .withScale(3)
                        .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                        .withBounds(6, 12, 12, 7)
                        .build()
            });

            // Treat SHOOT_* as the swing/attack animations
            put("SHOOT_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 1), 14)
                        .withScale(3)
                        .withBounds(6, 12, 12, 7)
                        .build()
            });

            put("SHOOT_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 1), 14)
                        .withScale(3)
                        .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                        .withBounds(6, 12, 12, 7)
                        .build()
            });

            put("SHOOT_DOWN", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 2), 14)
                        .withScale(3)
                        .withBounds(6, 12, 12, 7)
                        .build()
            });

            put("SHOOT_UP", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 3), 14)
                        .withScale(3)
                        .withBounds(6, 12, 12, 7)
                        .build()
            });
        }};
    }

   @Override
    public void update(Player player) {

        float vladmirCenterX = getBounds().getX() + (getBounds().getWidth() / 2);
        float playerCenterX = player.getBounds().getX() + (player.getBounds().getWidth() / 2);

        float vladmirCenterY = getBounds().getY() + (getBounds().getHeight() / 2);
        float playerCenterY = player.getBounds().getY() + (player.getBounds().getHeight() / 2);

        float distanceX = playerCenterX - vladmirCenterX;
        float distanceY = playerCenterY - vladmirCenterY;
        float distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);
        float detectionRadiusSquared = DETECTION_RADIUS * DETECTION_RADIUS;

        if (distanceSquared <= detectionRadiusSquared) {
            if (playerCenterX < vladmirCenterX) {
                currentAnimationName = "STAND_LEFT";
            } else {
                currentAnimationName = "STAND_RIGHT";
            }
        }
        if (player instanceof Sada) {
            chase((Sada) player);
        }
        super.update(player);
    }
    
    public boolean isDead() {
    return health <= 0;
    }

    @Override
        public void draw(GraphicsHandler graphicsHandler) {
            super.draw(graphicsHandler);
        }
    

    //Have Vladmir chase Sada
    public void chase(Sada sada) {
    float chaseSpeed = 1.5f; 

    float vladX = getX();
    float vladY = getY();
    float sadaX = sada.getX();
    float sadaY = sada.getY();

    // Calculate distance in each direction
    float dx = sadaX - vladX;
    float dy = sadaY - vladY;

    // Stop chasing if they’re touching
    if (Math.abs(dx) < 5 && Math.abs(dy) < 5) {
        currentAnimationName = dx < 0 ? "SHOOT_DOWN" : "SHOOT_UP";
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

