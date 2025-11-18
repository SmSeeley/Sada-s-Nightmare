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
import java.util.HashMap;

public class Snake extends Enemy {
    private float chaseSpeed = 1.5f; 

    public Snake(int id, Point location) {
        super(id, location.x, location.y, new SpriteSheet(ImageLoader.load("Snake.png"), 24, 24), "STAND_LEFT", 2);
    }  
    
    @Override
    public void performAction(Player player) {
        if (player instanceof Sada) {
            chase((Sada) player);
        }
    }
        
    public void chase(Sada sada) {
        float SnakeX = getX();
        float SnakeY = getY();
        float sadaX = sada.getX();
        float sadaY = sada.getY();

        // Calculate distance in each direction
        float dx = sadaX - SnakeX;
        float dy = sadaY - SnakeY;

        // Stop chasing if they’re touching
        if (Math.abs(dx) < 5 && Math.abs(dy) < 5) {
            currentAnimationName = dx < 0 ? "STAND_LEFT" : "STAND_RIGHT";
            return;
        }

        if (Math.abs(dx) > Math.abs(dy)) {
            // move horizontally
            if (dx > 0) {
                moveXHandleCollision(chaseSpeed);
                currentAnimationName = "WALK_RIGHT";
            } else {
                moveXHandleCollision(-chaseSpeed);
                currentAnimationName = "WALK_LEFT";
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
    
    // overrides loadAnimations method to define animation
    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{

            put("STAND_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(2)
                    .withBounds(4,2,15,15)
                    .build()
            });

            put("STAND_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(2)
                    .withBounds(5,2,15,15)
                    .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .build()
            });

            
            put("WALK_LEFT", new Frame[] {
                 new FrameBuilder(spriteSheet.getSprite(0, 0), 28)
                        .withScale(2)
                        .withBounds(3, 5, 15, 15)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(0, 1), 28)
                        .withScale(2)
                        .withBounds(3, 5, 15, 15)
                        .build()
            });

            put("WALK_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0), 28)
                        .withScale(2)
                        .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                        .withBounds(3, 5, 15, 15)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(0, 1), 28)
                        .withScale(2)
                        .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                        .withBounds(3, 5, 15, 15)
                        .build()
            });

        }};
        
            
    }

    @Override
        public void draw(GraphicsHandler graphicsHandler) {
            //drawBounds(graphicsHandler, new Color(255, 0, 0, 100));
            super.draw(graphicsHandler);
        }
}
