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

public class IceFlying extends Enemy {

    private final int SHOOT_RANGE_HORIZONTAL = 1000;
    private final int VERTICAL_TOLERANCE = 300;

    // how close Sada must be to actually get hit by the breath
    private final int BREATH_RANGE = 60;      // play with this value
    private final int BREATH_COOLDOWN = 60;   // frames between breaths

    private final float FLY_SPEED_X = 0.5f;
    private final float FLY_SPEED_Y = 0.5f;

    public IceFlying(int id, Point location) {
        super(id, location.x, location.y, new SpriteSheet(ImageLoader.load("iceflying.png"), 24, 24), "STAND_RIGHT",10);
    }  
    
    // overrides loadAnimations method to define animation
    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{

            put("STAND_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(3)
                    .withBounds(6,3,21,13)
                    .build()
            });

            put("STAND_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(3)
                    .withBounds(6,3,21,13)
                    .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .build()
            });

            put("ICE_BREATH_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 1))
                    .withScale(3)
                    .withBounds(6,3,27,13)
                    .build()
            });

            put("ICE_BREATH_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 1))
                    .withScale(3)
                    .withBounds(6,3,27,13)
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

     
    @Override
    protected void performAction(Player player) {

        float playerCenterX = player.getBounds().getX() + (player.getBounds().getWidth() / 2);
        float playerCenterY = player.getBounds().getY() + (player.getBounds().getHeight() / 2);

        float iceCenterX = getBounds().getX() + (getBounds().getWidth() / 2);
        float iceCenterY = getBounds().getY() + (getBounds().getHeight() / 2);

        float dx = playerCenterX - iceCenterX;
        float dy = playerCenterY - iceCenterY;

        float distanceX = Math.abs(dx);
        float distanceY = Math.abs(dy);

        //check if sada is in range
        if (distanceX <= SHOOT_RANGE_HORIZONTAL && distanceY <= VERTICAL_TOLERANCE) {

            // if not yet close enough for breath chase sada
            if (distanceX > BREATH_RANGE || distanceY > 20) {

                // horizontal movement
                if (dx < -1) {
                    moveX(-FLY_SPEED_X);
                    currentAnimationName = "STAND_LEFT";   // looks like flying left
                } else if (dx > 1) {
                    moveX(FLY_SPEED_X);
                    currentAnimationName = "STAND_RIGHT";  // looks like flying right
                }

                // vertical movement (hover up/down to follow)
                if (dy < -1) {
                    moveY(-FLY_SPEED_Y);
                } else if (dy > 1) {
                    moveY(FLY_SPEED_Y);
                }

            } else {
                if (dx < 0) {
                    currentAnimationName = "ICE_BREATH_LEFT";
                } else {
                    currentAnimationName = "ICE_BREATH_RIGHT";
                }

                if (shootTimer == 0) {
                    // damage sada when close
                    if (player.getBounds().intersects(this.getBounds())) {
                        player.takeDamage(.5);  
                    }
                    shootTimer = BREATH_COOLDOWN;
                }
            }

        } else {
            //do nothing
        }
    }
}

