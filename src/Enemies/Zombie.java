package Enemies;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.Player;
import Utils.Direction;
import Utils.Point;
import java.awt.Color;
import java.util.HashMap;

public class Zombie extends Enemy {
    
    // defining shooting range
    private final int SHOOT_RANGE_HORIZONTAL = 1000;
    private final int VERTICAL_TOLERANCE = 300;
     
    // creates zombie object
    public Zombie(int id, Point location) {
        super(id, location.x, location.y, new SpriteSheet(ImageLoader.load("Zombie.png"), 24, 24), "STAND_RIGHT",6);
    }  
    
    // overrides loadAnimations method to define animation for zombie
    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{

            put("STAND_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(3)
                    .withBounds(3,1,15,20)
                    .build()
            });

            put("STAND_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(3)
                    .withBounds(3,1,15,20)
                    .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .build()
            });
            

        }};
    }

    // define the zombies update logic
    @Override
    public void update(Player player) {
        super.update(player);
        if (shootTimer > 0) {
            shootTimer--;
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        //drawBounds(graphicsHandler, new Color(255, 0, 0, 100));
        super.draw(graphicsHandler);
    }

     

    // define the zombies specific action
    @Override
    protected void performAction(Player player) {

        float playerCenterX = player.getBounds().getX() + (player.getBounds().getWidth() / 2);
        float zombieCenterX = getBounds().getX() + (getBounds().getWidth() / 2);
       //float playerCenterY = player.getBounds().getY() + (player.getBounds().getHeight() / 2);
       // float zombieCenterY = getBounds().getY() + (getBounds().getHeight() / 2);

    
        // distance 
        float distanceX = Math.abs(player.getX() - getX());
        float distanceY = Math.abs(player.getY() - getY());

    // check if player is within the shooting range
        if (distanceX <= SHOOT_RANGE_HORIZONTAL && distanceY <= VERTICAL_TOLERANCE) {
            Direction directionToShoot;

            // determine direction to shoot base on players position
            if (playerCenterX < zombieCenterX) {
                directionToShoot = Direction.LEFT;
            } else {
                directionToShoot = Direction.RIGHT;
            }
            facePlayer(player);
            if (shootTimer == 0) {
                shootProjectile(directionToShoot); // shoots projectile
            }
        }
    }
}
