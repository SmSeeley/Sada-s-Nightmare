package Enemies;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import GameObject.Frame;
import GameObject.SpriteSheet;
import Level.MapEntity;
import Level.MapEntityStatus;
import Utils.Direction;
import Utils.Point;
import java.awt.Color;
import java.util.HashMap;

// A projectile shot by an enemy
public class Projectile extends MapEntity {

    private float speed = 2f; // speed of projectile
    private Direction direction; // direction its moving 
    private float directionX; // x-component 
    private float directionY; // y-component
    private boolean usesNormalizedDirection; // determine movement type
    private int damage = 1; 

    private  final float MAX_DISTANCE = 600f;
    private float distanceTraveled = 0;

    public Projectile(Point location, SpriteSheet spriteSheet, String startingAnimation, Direction direction) {
        super(location.x, location.y, spriteSheet, "PROJECTILE");
        this.direction = direction;
        this.usesNormalizedDirection = false; // cardinal movment
        this.animations = loadAnimations(spriteSheet);
        if (animations != null && animations.containsKey(startingAnimation)) {
            currentAnimationName = startingAnimation;
        } else {
            System.out.println("Animation is broken");
        }
        setMap(this.map);
    }

    public Projectile(Point location, SpriteSheet spriteSheet, String startingAnimation, float directionX, float directionY) {
        super(location.x, location.y, spriteSheet, "PROJECTILE");
        this.directionX = directionX;
        this.directionY = directionY;
        this.usesNormalizedDirection = true; // Indicates arbitrary movement
        this.animations = loadAnimations(spriteSheet);
        if (animations != null && animations.containsKey(startingAnimation)) {
            currentAnimationName = startingAnimation;
        } else {
            System.out.println("Animation is broken");
        }
        setMap(this.map);
    }
    
    public void setDamage(int damage) {
        this.damage = damage;
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("PROJECTILE", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 0)) 
                            .withScale(2)
                            .withBounds(0,10,13,13)
                            .build()
            });
        }};
    }

    @Override
    public void update() {
       float moveX = 0; 
       float moveY = 0;
       
       if(usesNormalizedDirection) {
            moveX = directionX * speed;
            moveY = directionY * speed;
       } else {
            switch (direction) {
                case RIGHT:
                    moveX = speed;
                    break;
                case LEFT:
                    moveX = -speed;
                    break;
                case UP:
                    moveY = -speed;
                    break;
                case DOWN:
                    moveY = speed;
                    break;
                default:
                    break;
            }
       }

       float actualMoveX = moveXHandleCollision(moveX);
       float actualMoveY = moveYHandleCollision(moveY);
       
        distanceTraveled += Math.sqrt(actualMoveX * actualMoveX + actualMoveY * actualMoveY);
       
        // removes projectile
        boolean collidedWithSolid = Math.abs(actualMoveX) < Math.abs(moveX) || Math.abs(actualMoveY) < Math.abs(moveY);       
        // removal logic
        if (collidedWithSolid) {
            //projectile hit a wall/solid tile remove it 
            this.mapEntityStatus = MapEntityStatus.REMOVED;
        }
        super.update();
    }

    public int getDamage(){
        return damage;
    }

    // draws projectile
    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        //drawBounds(graphicsHandler, new Color(255, 0, 0, 100));
        super.draw(graphicsHandler);
    }
}