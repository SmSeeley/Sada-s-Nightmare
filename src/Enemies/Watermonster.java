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
import java.util.HashMap;

public class Watermonster extends Enemy {    

    private final int SHOOT_RANGE_HORIZONTAL = 1000;
    private final int VERTICAL_TOLERANCE = 300;

    public Watermonster(int id, Point location) {
        super(id, location.x, location.y, new SpriteSheet(ImageLoader.load("watermonster.png"), 24, 24), "STAND_RIGHT",12);
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

    // define the water monster specific action
    @Override
    protected void performAction(Player player) {

        float playerCenterX = player.getBounds().getX() + (player.getBounds().getWidth() / 2);
        float waterMonsterCenterX = getBounds().getX() + (getBounds().getWidth() / 2);
        //float playerCenterY = player.getBounds().getY() + (player.getBounds().getHeight() / 2);
        //float waterMonsterCenterY = getBounds().getY() + (getBounds().getHeight() / 2);

        // distance 
        float distanceX = Math.abs(player.getX() - getX());
        float distanceY = Math.abs(player.getY() - getY());

    // check if player is within the shooting range
        if (distanceX <= SHOOT_RANGE_HORIZONTAL && distanceY <= VERTICAL_TOLERANCE) {
            Direction directionToShoot;

            // determine direction to shoot base on players position
            if (playerCenterX < waterMonsterCenterX) {
                directionToShoot = Direction.DOWN;
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
