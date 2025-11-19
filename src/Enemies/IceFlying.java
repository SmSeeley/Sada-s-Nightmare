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

    private final int BREATH_ANIM_DISTANCE = 90; //distance before switching animations

    private final int BREATH_RANGE_X = 60;    // switch to breath at ~60px horizontally
    private final int BREATH_RANGE_Y = 40;    // vertical tolerance for breath
    private final int BREATH_COOLDOWN = 800;   // frames between breaths

    // flying chase speed
    private final float FLY_SPEED_X = 0.5f;
    private final float FLY_SPEED_Y = 0.5f;

    private int shootTimer = 0;

    private final float BREATH_WIDTH = 8f;

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

        float absX = Math.abs(dx);
        float absY = Math.abs(dy);

        //chase sada
        if (absX > BREATH_RANGE_X || absY > BREATH_RANGE_Y) {
            // fly horizontally
            if (dx < 0) {
                moveX(-FLY_SPEED_X);
                currentAnimationName = "STAND_LEFT";
            } else if (dx > 0) {
                moveX(FLY_SPEED_X);
                currentAnimationName = "STAND_RIGHT";
            }
            // fly vertically
            if (dy < 0) {
                moveY(-FLY_SPEED_Y);
            } else if (dy > 0) {
                moveY(FLY_SPEED_Y);
            }

            return; // not close enough to attack yet
        }

        //switch animation when close enough
        if (absX <= BREATH_ANIM_DISTANCE && absY <= BREATH_ANIM_DISTANCE) {
            if (dx < 0) {
                currentAnimationName = "ICE_BREATH_LEFT";
            } else {
                currentAnimationName = "ICE_BREATH_RIGHT";
            }
        } else {
            if (dx < 0) {
                currentAnimationName = "STAND_LEFT";
            } else {
                currentAnimationName = "STAND_RIGHT";
            }
        }

        //only damage when breath hits sada
        if (shootTimer == 0 && isBreathTouchingPlayer(player, dx)) {
            player.takeDamage(0.1f);          
            shootTimer = BREATH_COOLDOWN;
        }
    }

    // check if breath is hitting Sada
    private boolean isBreathTouchingPlayer(Player player, float dx) {
        var p = player.getBounds();
        var e = this.getBounds();

        float pLeft   = p.getX();
        float pRight  = p.getX() + p.getWidth();
        float pTop    = p.getY();
        float pBottom = p.getY() + p.getHeight();

        float eLeft   = e.getX();
        float eRight  = e.getX() + e.getWidth();
        float eTop    = e.getY();
        float eBottom = e.getY() + e.getHeight();

        float breathLeft, breathRight;

        if (dx >= 0) {
            // player is on the right – breath extends to the right
            breathRight = eRight;
            breathLeft  = eRight - BREATH_WIDTH;
        } else {
            // player is on the left – breath extends to the left
            breathLeft  = eLeft;
            breathRight = eLeft + BREATH_WIDTH;
        }

        float breathTop    = eTop;
        float breathBottom = eBottom;

        boolean intersect =
                breathRight > pLeft &&
                breathLeft  < pRight &&
                breathBottom > pTop &&
                breathTop   < pBottom;

        return intersect;
    }
}