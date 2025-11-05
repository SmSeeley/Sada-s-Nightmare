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

    private float speed = 1.5f; // speed of projectile
    private Direction direction; // direction its moving 
    private int damage = 1; 

    private  final float MAX_DISTANCE = 600f;
    private float distanceTraveled = 0;

    public Projectile(Point location, SpriteSheet spriteSheet, String startingAnimation, Direction direction) {
        super(location.x, location.y, spriteSheet, "PROJECTILE");
        this.direction = direction;
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
       float moveAmount = 0; 
        
       
        switch (direction) {
            case RIGHT:
                moveAmount = moveXHandleCollision(speed);
                break;
            case LEFT:
                moveAmount = moveXHandleCollision(-speed);
                break;
            case UP:
                moveAmount = moveYHandleCollision(-speed);
                break;
            case DOWN:
                moveAmount = moveYHandleCollision(speed);
                break;
            default:
                break;
        }

        distanceTraveled += Math.abs(moveAmount);

        // removes projectile
        boolean collidedWithSolid = Math.abs(moveAmount) < (speed - 0.001f);
        
        // removal logic
        if (collidedWithSolid) {
            //projectile hit a wall/solid tile
            this.mapEntityStatus = MapEntityStatus.REMOVED;
        } else if (distanceTraveled >= MAX_DISTANCE) {
            // projectile ran out of range
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