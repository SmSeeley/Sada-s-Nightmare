package Enemies;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import EnhancedMapTiles.DoorKey;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.Map;
import Level.MapTile;
import Level.Player;
import Level.TileType;
import Players.Sada;
import Utils.Point;
import java.util.HashMap;

public class Snowboss extends Enemy {
    private final int DETECTION_RADIUS = 1000;
    public static boolean hasDied = false;
    private Map currentMap;

    public Snowboss(int id, Point location, Map map) {
        super(id, location.x, location.y, new SpriteSheet(ImageLoader.load("snowBoss.png"), 24, 24), "STAND_RIGHT",20);
        this.currentMap = map;
    }  
    
    // overrides loadAnimations method to define animation
    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("STAND_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(3)
                    .withBounds(4,2,15,15)
                    .build()
            });
            put("STAND_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(3)
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

        float snowCenterX = getBounds().getX() + (getBounds().getWidth() / 2);
        float playerCenterX = player.getBounds().getX() + (player.getBounds().getWidth() / 2);

        float snowCenterY = getBounds().getY() + (getBounds().getHeight() / 2);
        float playerCenterY = player.getBounds().getY() + (player.getBounds().getHeight() / 2);

        float distanceX = playerCenterX - snowCenterX;
        float distanceY = playerCenterY - snowCenterY;
        float distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);
        float detectionRadiusSquared = DETECTION_RADIUS * DETECTION_RADIUS;

        if (distanceSquared <= detectionRadiusSquared) {
            if (playerCenterX < snowCenterX) {
                currentAnimationName = "STAND_LEFT";
            } else {
                currentAnimationName = "STAND_RIGHT";
            }
            if (shootTimer == 0) {
                shootProjectileToward(playerCenterX, playerCenterY, snowCenterX, snowCenterY);
        }

        if (isDead() && !hasDied) {
            markAsDead();
            System.out.println("[Snowboss] has died, dropping key!");
        }
        super.update(player);
        }
    }

    private void shootProjectileToward(float playerX, float playerY, float snowX, float snowY) {
        //  angle to the player
        float dx = playerX - snowX;
        float dy = playerY - snowY;
        float magnitude = (float) Math.sqrt(dx * dx + dy * dy);

        // normalize direction vector
        float normalizedDx = dx / magnitude;
        float normalizedDy = dy / magnitude;

        // creates and shoot the projectile
        Projectile projectile = new Projectile(
            new Point(snowX, snowY),
            new SpriteSheet(ImageLoader.load("projectile.png"), 16, 16),
            "PROJECTILE",
            normalizedDx,
            normalizedDy
        );

        projectile.setMap(this.map);
        map.addMapEntity(projectile);

        shootTimer = SHOOT_COOLDOWN; 
    }

    public boolean isDead() {
        return hasDied || !getIsActive() || health <= 0;
    }

    public void markAsDead() {
        hasDied = true;
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
    } 

    @Override
    public void takeDamage(int amount) {
        boolean wasActive = getIsActive();
        super.takeDamage(amount); // applies cooldown, reduces health, and calls removeEnemy() if <= 0
        relocate();

        // if we were active and now we're not, the enemy just died this frame
        if (wasActive && !getIsActive() && !hasDied) {
            hasDied = true;
            try {
                if (currentMap instanceof Maps.Winter_5) {
                    Maps.Winter_5 winter_5 = (Maps.Winter_5) currentMap;
                    Point dropLoc = winter_5.getMapTile(9, 4).getLocation(); 
                    winter_5.addEnhancedMapTile(new DoorKey(dropLoc));
                    System.out.println("[Monster] Dropped key at " + dropLoc.x + ", " + dropLoc.y);
                }
            } catch (Exception e) {
                System.out.println("[Monster] Failed to drop key: " + e.getMessage());
            }
        }
    }

    private void relocate() {
        try {
            // new location is within the map bounds and is a valid tile
            int mapWidth = currentMap.getWidth();
            int mapHeight = currentMap.getHeight();
            Point newLocation = null;

            // Attempt to find valid tile
            for (int attempts = 0; attempts < 10; attempts++) { // Limit attempts to avoid infinite loops
                int newX = (int) (Math.random() * mapWidth);
                int newY = (int) (Math.random() * mapHeight);

                // Get map tile at the random coordinates
                MapTile potentialTile = currentMap.getMapTile(newX, newY);

                // Check if tile is passable
                if (potentialTile.getTileType() != TileType.NOT_PASSABLE) {
                    newLocation = potentialTile.getLocation();
                    break;
                }
            }

            // If valid location was found, update position
            if (newLocation != null) {
                setLocation(newLocation.x, newLocation.y);
                System.out.println("[Snowboss] Relocated to: " + newLocation.x + ", " + newLocation.y);
            } else {
                System.out.println("[Snowboss] Failed to find a valid relocation tile.");
            }
        } catch (Exception e) {
            System.out.println("[Snowboss] Failed to relocate: " + e.getMessage());
        }
    }

    public void chase(Sada sada) {
        float chaseSpeed = 0.5f; 

        float snowX = getX();
        float snowY = getY();
        float sadaX = sada.getX();
        float sadaY = sada.getY();

        // Calculate distance in each direction
        float dx = sadaX - snowX;
        float dy = sadaY - snowY;

        // Stop chasing if they’re touching
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

