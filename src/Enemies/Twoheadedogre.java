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

public class Twoheadedogre extends Enemy {
    private final int DETECTION_RADIUS = 100;

    private boolean isChasing = false;

    public Twoheadedogre(int id, Point location) {
        super(id, location.x, location.y, new SpriteSheet(ImageLoader.load("ogre2.png"), 24, 24), "STAND_RIGHT", 8);
    }  
    
    // overrides loadAnimations method to define animation for ogre
    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{

            put("STAND_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(4)
                    .withBounds(4,2,15,15)
                    .build()
            });

            put("STAND_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(4)
                    .withBounds(5,2,15,15)
                    .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .build()
            });
        }};
    }

     public void hit(){
        isChasing = true;
     }

   @Override
    public void update(Player player) {
        if(player.getBounds().intersects(getBounds())){
            hit();
        }
        if(isChasing) {
            float chaseSpeed = 0.5f;

            // Get player's position
            float playerX = player.getX();
            float playerY = player.getY();

            // Move horizontally toward the player
            if (playerX < getX()) {
                moveXHandleCollision(-chaseSpeed); // Move left
                currentAnimationName = "STAND_LEFT";
            } else if (playerX > getX()) {
                moveXHandleCollision(chaseSpeed); // Move right
                currentAnimationName = "STAND_RIGHT";
            }

            if (playerY < getY()) {
                moveYHandleCollision(-chaseSpeed); // Move up
            } else if (playerY > getY()) {
                moveYHandleCollision(chaseSpeed); // Move down
            }
        }
        super.update(player);
    }

    @Override
        public void draw(GraphicsHandler graphicsHandler) {
            //drawBounds(graphicsHandler, new Color(255, 0, 0, 100));
            super.draw(graphicsHandler);
        }
}
