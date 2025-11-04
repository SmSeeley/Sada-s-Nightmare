package Level;

import Enemies.Projectile;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.SpriteSheet;
import Utils.Direction;
import Utils.Point;
import java.awt.Color;
import java.util.HashMap;


// This class for enemies
public class Enemy extends MapEntity {
    private boolean isActive = true;
    protected int id = 0;
    protected boolean isLocked = false;
    //private boolean keyDropped = false;
    protected Player player;

    // damage cooldown
    private final long damageCooldown = 500;
    private long lastDamageTime = 0;

    // health variables
    protected int maxHealth = 5;
    protected int health = maxHealth;
    
    // projectile shooting variables
    protected int shootTimer = 0;
    protected final int SHOOT_COOLDOWN = 240;

    // consstructor that handles max health
    public Enemy(int id, float x, float y, SpriteSheet spriteSheet, String startingAnimation, int maxHealth) {
        super(x, y, spriteSheet, startingAnimation);
        this.id = id;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }
    
    public Enemy(int id, float x, float y, SpriteSheet spriteSheet, String startingAnimation) {
        super(x, y, spriteSheet, startingAnimation);
        this.id = id;
    }

    public Enemy(int id, float x, float y, HashMap<String, Frame[]> animations, String startingAnimation) {
        super(x, y, animations, startingAnimation);
        this.id = id;
    }

    public Enemy(int id, float x, float y, Frame[] frames) {
        super(x, y, frames);
        this.id = id;
    }

    public Enemy(int id, float x, float y, Frame frame) {
        super(x, y, frame);
        this.id = id;
    }

    public Enemy(int id, float x, float y) {
        super(x, y);
        this.id = id;
    }

    public int getId() { return id; }
    

    public void facePlayer(Player player) {
        float centerPoint = getBounds().getX() + (getBounds().getWidth() / 2);
        float playerCenterPoint = player.getBounds().getX() + (player.getBounds().getWidth() / 2);
        if (centerPoint < playerCenterPoint) {
            this.currentAnimationName = "STAND_RIGHT";
        }
        else if (centerPoint >= playerCenterPoint) {
            this.currentAnimationName = "STAND_LEFT";
        }
    }

    public void stand(Direction direction) {
        if (direction == Direction.RIGHT) {
            this.currentAnimationName = "STAND_RIGHT";
        }
        else if (direction == Direction.LEFT) {
            this.currentAnimationName = "STAND_LEFT";
        }
    }

    public void walk(Direction direction, float speed) {
        if (direction == Direction.RIGHT) {
            this.currentAnimationName = "WALK_RIGHT";
        }
        else if (direction == Direction.LEFT) {
            this.currentAnimationName = "WALK_LEFT";
        }
        else {
            if (this.currentAnimationName.contains("RIGHT")) {
                this.currentAnimationName = "WALK_RIGHT";
            }
            else {
                this.currentAnimationName = "WALK_LEFT";
            }
        }
        if (direction == Direction.UP) {
            moveY(-speed);
        }
        else if (direction == Direction.DOWN) {
            moveY(speed);
        }
        else if (direction == Direction.LEFT) {
            moveX(-speed);
        }
        else if (direction == Direction.RIGHT) {
            moveX(speed);
        }
    }

    public void update(Player player) {
        if (!getIsActive()) return; // stop updating if not active
        if (shootTimer > 0) {
            shootTimer--;
        }
        if (!isLocked) {
            this.performAction(player);
        }
        checkPlayerCollision(player); // ✅ ensures Sada can take damage
        super.update();
    }

    public void lock() {
        isLocked = true;
    }

    public void unlock() {
        isLocked = false;
    }


    protected void performAction(Player player){} 

    // method to handle shooting a projectile
    protected void shootProjectile(Direction direction) {

         if (!getIsActive() || isLocked) return;
        
        SpriteSheet projectileSpriteSheet = new SpriteSheet(ImageLoader.load("projectile.png"), 16, 16);

        if(shootTimer == 0) {
            float startX; 

            float startY = this.y + 28; 
            
            if (direction == Direction.LEFT) {
                // When facing left, spawn the projectile 1 pixel to the left of the zombie's LEFT boundary.
                startX = this.getBounds().getX2() -  1; 
            } else { 
                // When facing right, spawn the projectile 1 pixel to the right of the zombie's RIGHT boundary.
                startX = this.getBounds().getX2() + 1;
            }

            // load the projectile sprite sheet HERE
            Projectile projectile = new Projectile(
                new Point(startX, startY), 
                projectileSpriteSheet, 
                "PROJECTILE",
                direction
            );
            
            projectile.setMap(this.map);
            map.addMapEntity(projectile);

            shootTimer = SHOOT_COOLDOWN; // Reset the shoot timer
            
        }
    }
    
    // draw method to handle drawing the enemy health status 
    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        //drawBounds(graphicsHandler, new Color(255, 0, 0, 100));
        if (getIsActive()) {
            super.draw(graphicsHandler);
        }

        // health status drawing logic
        if (health > 0) {
            final int BAR_HEIGHT = 5;
            final int BAR_WIDTH = 54;
            final int Y_OFFSET = 2;

            //  the current entity width from its bounds
            int entityWidth = Math.round(getBounds().getWidth()); 

            float entityScreenX = getCalibratedXLocation(); 
            float entityScreenY = getCalibratedYLocation();

            int healthBarX = Math.round(entityScreenX + (entityWidth / 2) - (BAR_WIDTH / 2));
            int healthBarY = Math.round(entityScreenY - BAR_HEIGHT - Y_OFFSET); 

            float healthPercentage = (float)health / maxHealth;
            int currentHealthWidth = (int)(BAR_WIDTH * healthPercentage);

            graphicsHandler.drawFilledRectangle(
                healthBarX, 
                healthBarY, 
                BAR_WIDTH, 
                BAR_HEIGHT, 
                Color.RED
            );

            graphicsHandler.drawFilledRectangle(
                healthBarX, 
                healthBarY, 
                currentHealthWidth, 
                BAR_HEIGHT, 
                Color.GREEN
            );
        }
    }

    public void takeDamage(int amount) {
       long now = System.currentTimeMillis();
       if (now - lastDamageTime < damageCooldown) {
           return; // Still in cooldown period, ignore damage
        }
        lastDamageTime = now; // Update last damage time
        health -= amount;
        System.out.println("Enemy took damage! Health: " + health);
        if (health <= 0) {
            removeEnemy();
        }
    }


        public void setIsActive(boolean isActive) {
            this.isActive = isActive;
            if (!isActive) {
                // additional safety: lock so no more actions, and clear shoot timer
                lock();
                shootTimer = 0;
            }
        }

        public boolean getIsActive() {
            return isActive;
        }

    

        public void removeEnemy() {
        setIsActive(false);
        lock();
        shootTimer = 0;

    ///incrament keys when enemy dies
    /*if (!keyDropped) {
    public void removeEnemy() {
        System.out.println("[Enemy] Enemy defeated — incremented key count to " + EnhancedMapTiles.DoorKey.keysCollected);
    }*/

        try {
            this.setLocation(-10000, -10000);
        } catch (Exception ignored) {}

        try {
            if (this.map != null) {
                java.lang.reflect.Method m = this.map.getClass().getMethod("removeMapEntity", Level.MapEntity.class);
                if (m != null) {
                    m.invoke(this.map, this);
                }
            }
        } catch (Exception ignored) {}
    }
    // Method to check and apply damage to Sada when colliding
    public void checkPlayerCollision(Player player) {
        if (getIsActive() && this.intersects(player)) {
            long now = System.currentTimeMillis();
            if (now - lastDamageTime >= damageCooldown) {
                lastDamageTime = now;
                player.takeDamage(1); // damage Sada by 1 (adjust if needed)
                System.out.println("Sada took damage from enemy!");
            }
        }
    }
}