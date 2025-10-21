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
    protected int id = 0;
    protected boolean isLocked = false;

    // health variables
    protected int maxHealth = 10;
    protected int health = maxHealth;

    // projectile shooting variables
    protected int shootTimer = 0;
    protected final int SHOOT_COOLDOWN = 240;

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

    // update method to handle enemy logic
    public void update(Player player) {
        if (shootTimer > 0) {
            shootTimer--;
        }
        if (!isLocked) {
            this.performAction(player);
        }
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
        super.draw(graphicsHandler);

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
}

