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
import Players.Sada;
import Utils.Direction;
import Utils.Point;
import java.awt.Color;
import java.util.HashMap;

public class Pirate extends Enemy {
    private final int DETECTION_RADIUS = 1000;

    public Pirate(int id, Point location) {
        super(id, location.x, location.y, new SpriteSheet(ImageLoader.load("pirate.png"), 24, 24), "STAND_RIGHT", 10);
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
        if (player instanceof Sada) {
            chase((Sada) player);
        }

        float pirateCenterX = getBounds().getX() + (getBounds().getWidth() / 2);
        float playerCenterX = player.getBounds().getX() + (player.getBounds().getWidth() / 2);

        float pirateCenterY = getBounds().getY() + (getBounds().getHeight() / 2);
        float playerCenterY = player.getBounds().getY() + (player.getBounds().getHeight() / 2);

        float distanceX = playerCenterX - pirateCenterX;
        float distanceY = playerCenterY - pirateCenterY;
        float distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);
        float detectionRadiusSquared = DETECTION_RADIUS * DETECTION_RADIUS;

        if (distanceSquared <= detectionRadiusSquared) {
            if (playerCenterX < pirateCenterX) {
                currentAnimationName = "STAND_LEFT";
            } else {
                currentAnimationName = "STAND_RIGHT";
            }
            if (shootTimer == 0) {
                shootProjectileToward(playerCenterX, playerCenterY, pirateCenterX, pirateCenterY);
        }
            
        }
        super.update(player);
    }

    private void shootProjectileToward(float playerX, float playerY, float pirateX, float pirateY) {
        // angle to the player
        float dx = playerX - pirateX;
        float dy = playerY - pirateY;
        float magnitude = (float) Math.sqrt(dx * dx + dy * dy);

        // normalize direction vector
        float normalizedDx = dx / magnitude;
        float normalizedDy = dy / magnitude;

        // creates and shoot projectile
        Projectile projectile = new Projectile(
            new Point(pirateX, pirateY),
            new SpriteSheet(ImageLoader.load("projectile.png"), 16, 16),
            "PROJECTILE",
            normalizedDx,
            normalizedDy
        );

        projectile.setMap(this.map);
        map.addMapEntity(projectile);

        shootTimer = SHOOT_COOLDOWN; // reset shoot timer
    }


    @Override
        public void draw(GraphicsHandler graphicsHandler) {
            //drawBounds(graphicsHandler, new Color(255, 0, 0, 100));
            super.draw(graphicsHandler);
        }

    
    public void chase(Sada sada) {
        float chaseSpeed = 0.5f; 

        float pirateX = getX();
        float pirateY = getY();
        float sadaX = sada.getX();
        float sadaY = sada.getY();

        float dx = sadaX - pirateX;
        float dy = sadaY - pirateY;

        if (Math.abs(dx) < 5 && Math.abs(dy) < 5) {
            currentAnimationName = dx < 0 ? "STAND_LEFT" : "STAND_RIGHT";
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
