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

public class IceMan extends Enemy {

    // distance before switching to a stab animation
    private final int STAB_ANIM_DISTANCE = 60;

    private final int ATTACK_RANGE_X = 35;
    private final int ATTACK_RANGE_Y = 35;
    private final int ATTACK_COOLDOWN = 45;

    // width of just the sword
    private final float SWORD_WIDTH = 11f;

    // chase speed
    private final float CHASE_SPEED = 0.8f;

    private int shootTimer = 0;

    public IceMan(int id, Point location) {
        super(id, location.x, location.y, new SpriteSheet(ImageLoader.load("Iceman.png"), 24, 34), "STAND_RIGHT",10);
    }  
    
    // overrides loadAnimations method to define animation
    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{

            put("STAND_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(2)
                    .withBounds(3, 2, 19, 24)
                    .build()
            });

            put("STAND_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(2)
                    .withBounds(4, 2, 19, 24)
                    .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .build()
            });

            put("STAB_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 1))
                    .withScale(2)
                    .withBounds(4, 2, 30, 24)
                    .build()
            });

            put("STAB_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 1))
                    .withScale(2)
                    .withBounds(4, 2, 30, 24)
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
        super.draw(graphicsHandler);
    }

     
    @Override
    protected void performAction(Player player) {
        float playerCenterX = player.getBounds().getX()
                + (player.getBounds().getWidth() / 2);
        float playerCenterY = player.getBounds().getY()
                + (player.getBounds().getHeight() / 2);

        float iceCenterX = getBounds().getX()
                + (getBounds().getWidth() / 2);
        float iceCenterY = getBounds().getY()
                + (getBounds().getHeight() / 2);

        float dx = playerCenterX - iceCenterX;
        float dy = playerCenterY - iceCenterY;

        float absX = Math.abs(dx);
        float absY = Math.abs(dy);

        // chase sada
        if (absX > ATTACK_RANGE_X || absY > ATTACK_RANGE_Y) {
            // horizontal movement
            if (dx < 0) {
                moveX(-CHASE_SPEED);
            } else if (dx > 0) {
                moveX(CHASE_SPEED);
            }
            // vertical movement 
            if (dy < 0) {
                moveY(-CHASE_SPEED);
            } else if (dy > 0) {
                moveY(CHASE_SPEED);
            }
        }

        // switch to stab animation when sada is 60 pixels away
        if (absX <= STAB_ANIM_DISTANCE && absY <= STAB_ANIM_DISTANCE) {
            if (dx < 0) {
                currentAnimationName = "STAB_LEFT";
            } else {
                currentAnimationName = "STAB_RIGHT";
            }
        } else {
            if (dx < 0) {
                currentAnimationName = "STAND_LEFT";
            } else {
                currentAnimationName = "STAND_RIGHT";
            }
        }

        //only damage when sword is hitting sada
        if (shootTimer == 0 && isSwordTouchingPlayer(player, dx)) {
            player.takeDamage(.5);
            shootTimer = ATTACK_COOLDOWN;
        }
    }

    // check if sword is hitting Sada
    private boolean isSwordTouchingPlayer(Player player, float dx) {
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
        float swordLeft, swordRight;

        if (dx >= 0) {
            // player is on the right
            swordRight = eRight;
            swordLeft  = eRight - SWORD_WIDTH;
        } else {
            // player is on the left
            swordLeft  = eLeft;
            swordRight = eLeft + SWORD_WIDTH;
        }

        float swordTop    = eTop;
        float swordBottom = eBottom;

        boolean intersect =
                swordRight > pLeft &&
                swordLeft  < pRight &&
                swordBottom > pTop &&
                swordTop   < pBottom;

        return intersect;
    }
}