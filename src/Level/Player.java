package Level;

import Engine.Key;
import Engine.KeyLocker;
import Engine.Keyboard;
import EnhancedMapTiles.Sword;
import GameObject.GameObject;
import GameObject.Rectangle;
import GameObject.SpriteSheet;
import NPCs.greenNinja;
import Utils.Direction;

public abstract class Player extends GameObject {
    // values that affect player movement
    // these should be set in a subclass
    protected float walkSpeed = 0;
    protected int interactionRange = 1;
    protected Direction currentWalkingXDirection;
    protected Direction currentWalkingYDirection;
    protected Direction currentShootingDirection;
    protected Direction lastWalkingXDirection;
    protected Direction lastWalkingYDirection;

    // values used to handle player movement
    protected float moveAmountX, moveAmountY;
    protected float lastAmountMovedX, lastAmountMovedY;

    // values used to keep track of player's current state
    protected PlayerState playerState;
    protected PlayerState previousPlayerState;
    protected Direction facingDirection;
    protected Direction lastMovementDirection;

    // define keys
    protected KeyLocker keyLocker = new KeyLocker();
    protected KeyLocker shootKeyLocker = new KeyLocker();
    //walking keys and interact
    protected Key MOVE_LEFT_KEY = Key.A;
    protected Key MOVE_RIGHT_KEY = Key.D;
    protected Key MOVE_UP_KEY = Key.W;
    protected Key MOVE_DOWN_KEY = Key.S;
    protected Key INTERACT_KEY = Key.E;
    //shooting keys
    protected Key SHOOT_RIGHT_KEY = Key.RIGHT;
    protected Key SHOOT_LEFT_KEY = Key.LEFT;
    protected Key SHOOT_UP_KEY = Key.UP;
    protected Key SHOOT_DOWN_KEY = Key.DOWN;

    protected boolean isShooting = false;

    protected boolean isLocked = false;

    // Health system variables
    protected int health;
    protected final int MAX_HEALTH = 10;

    //key variables
    protected int keys;
    protected final int MAX_KEYS = 3;
    protected final int DEFAULT_KEYS = 0;

    public Player(SpriteSheet spriteSheet, float x, float y, String startingAnimationName) {
        super(spriteSheet, x, y, startingAnimationName);
        facingDirection = Direction.RIGHT;
        playerState = PlayerState.STANDING;
        previousPlayerState = playerState;
        this.affectedByTriggers = true;
        this.health = MAX_HEALTH;
        this.keys = DEFAULT_KEYS;
    }

    public void update() {
    if (!isLocked) {
        moveAmountX = 0;
        moveAmountY = 0;

        // Update shooting flag
        isShooting = Keyboard.isKeyDown(SHOOT_LEFT_KEY) || Keyboard.isKeyDown(SHOOT_RIGHT_KEY) ||
                     Keyboard.isKeyDown(SHOOT_UP_KEY) || Keyboard.isKeyDown(SHOOT_DOWN_KEY);

        if (!Keyboard.isKeyDown(SHOOT_LEFT_KEY))  shootKeyLocker.unlockKey(SHOOT_LEFT_KEY);
        if (!Keyboard.isKeyDown(SHOOT_RIGHT_KEY)) shootKeyLocker.unlockKey(SHOOT_RIGHT_KEY);
        if (!Keyboard.isKeyDown(SHOOT_UP_KEY))    shootKeyLocker.unlockKey(SHOOT_UP_KEY);
        if (!Keyboard.isKeyDown(SHOOT_DOWN_KEY))  shootKeyLocker.unlockKey(SHOOT_DOWN_KEY);

        switch (playerState) {
            case STANDING:
                playerStanding();
                break;
            case WALKING:
                playerWalking();
                break;
            case SHOOTING:
                playerShooting();
                break;
            
        }
        // Handle shooting direction
        if (isShooting) {
            if (Keyboard.isKeyDown(SHOOT_LEFT_KEY)) {
                facingDirection = Direction.LEFT;
                currentShootingDirection = Direction.LEFT;
            } else if (Keyboard.isKeyDown(SHOOT_RIGHT_KEY)) {
                facingDirection = Direction.RIGHT;
                currentShootingDirection = Direction.RIGHT;
            } else if (Keyboard.isKeyDown(SHOOT_UP_KEY)) {
                facingDirection = Direction.UP;
                currentShootingDirection = Direction.UP;
            } else if (Keyboard.isKeyDown(SHOOT_DOWN_KEY)) {
                facingDirection = Direction.DOWN;
                currentShootingDirection = Direction.DOWN;
            }
        } else {
            currentShootingDirection = Direction.NONE;
        }

        // move player with respect to map collisions based on how much player needs to move this frame
        lastAmountMovedY = super.moveYHandleCollision(moveAmountY);
        lastAmountMovedX = super.moveXHandleCollision(moveAmountX);
    }
    
    handlePlayerAnimation();

    updateLockedKeys();

    // update player's animation
    super.update();

    java.util.List<Level.NPC> npcs = map.getNPCs();
    java.util.List<Level.Enemy> enemies = map.getEnemies();
    //code for enemies (not just greenNinja) to take damage from melee attacks
    int damage = Sword.hasSword() ? Sword.getSwordDamage() : 1;
    for (NPC npc : map.getNPCs()) {
        if (!(npc instanceof greenNinja)) continue;
        greenNinja ninja = (greenNinja) npc;
        if (!this.getBounds().intersects(ninja.getBounds())) continue;
         // LEFT
            if (Keyboard.isKeyDown(SHOOT_LEFT_KEY) && !shootKeyLocker.isKeyLocked(SHOOT_LEFT_KEY)) {
                ninja.takeDamage(damage);
                shootKeyLocker.lockKey(SHOOT_LEFT_KEY);
            }
            // RIGHT
            if (Keyboard.isKeyDown(SHOOT_RIGHT_KEY) && !shootKeyLocker.isKeyLocked(SHOOT_RIGHT_KEY)) {
                ninja.takeDamage(damage);
                shootKeyLocker.lockKey(SHOOT_RIGHT_KEY);
            }
            // UP
            if (Keyboard.isKeyDown(SHOOT_UP_KEY) && !shootKeyLocker.isKeyLocked(SHOOT_UP_KEY)) {
                ninja.takeDamage(damage);
                shootKeyLocker.lockKey(SHOOT_UP_KEY);
            }
            // DOWN
            if (Keyboard.isKeyDown(SHOOT_DOWN_KEY) && !shootKeyLocker.isKeyLocked(SHOOT_DOWN_KEY)) {
                ninja.takeDamage(damage);
                shootKeyLocker.lockKey(SHOOT_DOWN_KEY);
            }
    }

    // iterate enemies list and apply melee damage
        for (Enemy enemy : map.getEnemies()) {
            if (enemy == null) continue;
            if (!this.getBounds().intersects(enemy.getBounds())) continue;

            // LEFT
            if (Keyboard.isKeyDown(SHOOT_LEFT_KEY) && !shootKeyLocker.isKeyLocked(SHOOT_LEFT_KEY)) {
                enemy.takeDamage(damage);
                shootKeyLocker.lockKey(SHOOT_LEFT_KEY);
            }
            // RIGHT
            if (Keyboard.isKeyDown(SHOOT_RIGHT_KEY) && !shootKeyLocker.isKeyLocked(SHOOT_RIGHT_KEY)) {
                enemy.takeDamage(damage);
                shootKeyLocker.lockKey(SHOOT_RIGHT_KEY);
            }
            // UP
            if (Keyboard.isKeyDown(SHOOT_UP_KEY) && !shootKeyLocker.isKeyLocked(SHOOT_UP_KEY)) {
                enemy.takeDamage(damage);
                shootKeyLocker.lockKey(SHOOT_UP_KEY);
            }
            // DOWN
            if (Keyboard.isKeyDown(SHOOT_DOWN_KEY) && !shootKeyLocker.isKeyLocked(SHOOT_DOWN_KEY)) {
                enemy.takeDamage(damage);
                shootKeyLocker.lockKey(SHOOT_DOWN_KEY);
            }
        }
}


    // based on player's current state, call appropriate player state handling method
    protected void handlePlayerState() {

        // If any shoot key is pressed, enter SHOOTING state
        if (Keyboard.isKeyDown(SHOOT_LEFT_KEY) || Keyboard.isKeyDown(SHOOT_RIGHT_KEY) ||
            Keyboard.isKeyDown(SHOOT_UP_KEY) || Keyboard.isKeyDown(SHOOT_DOWN_KEY)) {
            playerState = PlayerState.SHOOTING;
        }
        // If any movement key is pressed, enter WALKING state
        else if (Keyboard.isKeyDown(MOVE_LEFT_KEY) || Keyboard.isKeyDown(MOVE_RIGHT_KEY) ||
            Keyboard.isKeyDown(MOVE_UP_KEY) || Keyboard.isKeyDown(MOVE_DOWN_KEY)) {
            playerState = PlayerState.WALKING;
        }
        // If no keys are pressed, enter STANDING state
        else {
            playerState = PlayerState.STANDING;
        }

        switch (playerState) {
            case STANDING:
                playerStanding();
                break;
            case WALKING:
                playerWalking();
                break;
            case SHOOTING:
                playerShooting();
                break;
        }
    }

    //player SHOOTING state logic
    protected void playerShooting() {
        // if a walk key is pressed, player enters WALKING state
        if (Keyboard.isKeyDown(SHOOT_LEFT_KEY) || Keyboard.isKeyDown(SHOOT_RIGHT_KEY) || Keyboard.isKeyDown(SHOOT_UP_KEY) || Keyboard.isKeyDown(SHOOT_DOWN_KEY)) {
            playerState = PlayerState.SHOOTING;
        }
          // if shoot left key is pressed, shoot left
          if (Keyboard.isKeyDown(SHOOT_LEFT_KEY)) {
            facingDirection = Direction.LEFT;
            currentShootingDirection = Direction.LEFT;
        } else if (Keyboard.isKeyDown(SHOOT_RIGHT_KEY)) {

        // if shoot right key is pressed, shoot right
            facingDirection = Direction.RIGHT;
            currentShootingDirection = Direction.RIGHT;

            
            //if shoot up key is pressed, shoot up

        } else if (Keyboard.isKeyDown(SHOOT_UP_KEY)) {
            facingDirection = Direction.UP;
            currentShootingDirection = Direction.UP;
            
        } //if shoot down key is pressed, shoot down
        else if (Keyboard.isKeyDown(SHOOT_DOWN_KEY)) {
            facingDirection = Direction.DOWN;
            currentShootingDirection = Direction.DOWN;
            
        } else {
            currentShootingDirection = Direction.NONE;
        }

    }

    // player STANDING state logic
    protected void playerStanding() {
        if (!keyLocker.isKeyLocked(INTERACT_KEY) && Keyboard.isKeyDown(INTERACT_KEY)) {
            keyLocker.lockKey(INTERACT_KEY);
            map.entityInteract(this);
        }

        // if a walk key is pressed, player enters WALKING state
        if (Keyboard.isKeyDown(MOVE_LEFT_KEY) || Keyboard.isKeyDown(MOVE_RIGHT_KEY) || Keyboard.isKeyDown(MOVE_UP_KEY) || Keyboard.isKeyDown(MOVE_DOWN_KEY)) {
            playerState = PlayerState.WALKING;
        }
    }

    // player WALKING state logic
    protected void playerWalking() {
        if (!keyLocker.isKeyLocked(INTERACT_KEY) && Keyboard.isKeyDown(INTERACT_KEY)) {
            keyLocker.lockKey(INTERACT_KEY);
            map.entityInteract(this);
        }

        // if walk left key is pressed, move player to the left
        if (Keyboard.isKeyDown(MOVE_LEFT_KEY)) {
            moveAmountX -= walkSpeed;
            facingDirection = Direction.LEFT;
            currentWalkingXDirection = Direction.LEFT;
            lastWalkingXDirection = Direction.LEFT;
        }

        // if walk right key is pressed, move player to the right
        else if (Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
            moveAmountX += walkSpeed;
            facingDirection = Direction.RIGHT;
            currentWalkingXDirection = Direction.RIGHT;
            lastWalkingXDirection = Direction.RIGHT;
        }
        else {
            currentWalkingXDirection = Direction.NONE;
        }

        if (Keyboard.isKeyDown(MOVE_UP_KEY)) {
            moveAmountY -= walkSpeed;
            currentWalkingYDirection = Direction.UP;
            lastWalkingYDirection = Direction.UP;
        }
        else if (Keyboard.isKeyDown(MOVE_DOWN_KEY)) {
            moveAmountY += walkSpeed;
            currentWalkingYDirection = Direction.DOWN;
            lastWalkingYDirection = Direction.DOWN;
        }
        else {
            currentWalkingYDirection = Direction.NONE;
        }

        if ((currentWalkingXDirection == Direction.RIGHT || currentWalkingXDirection == Direction.LEFT) && currentWalkingYDirection == Direction.NONE) {
            lastWalkingYDirection = Direction.NONE;
        }

        if ((currentWalkingYDirection == Direction.UP || currentWalkingYDirection == Direction.DOWN) && currentWalkingXDirection == Direction.NONE) {
            lastWalkingXDirection = Direction.NONE;
        }

        if (Keyboard.isKeyUp(MOVE_LEFT_KEY) && Keyboard.isKeyUp(MOVE_RIGHT_KEY) && Keyboard.isKeyUp(MOVE_UP_KEY) && Keyboard.isKeyUp(MOVE_DOWN_KEY)) {
            playerState = PlayerState.STANDING;
        }
    }

    protected void updateLockedKeys() {
        if (Keyboard.isKeyUp(INTERACT_KEY) && !isLocked) {
            keyLocker.unlockKey(INTERACT_KEY);
        }
    }

    // anything extra the player should do based on interactions can be handled here
    protected void handlePlayerAnimation() {
    if (isShooting) {
        if (facingDirection == Direction.RIGHT) {
            this.currentAnimationName = "SHOOT_RIGHT";
        } else if (facingDirection == Direction.LEFT) {
            this.currentAnimationName = "SHOOT_LEFT";
        } else if (facingDirection == Direction.UP) {
            this.currentAnimationName = "SHOOT_UP";
        } else if (facingDirection == Direction.DOWN) {
            this.currentAnimationName = "SHOOT_DOWN";
        }
    } else if (playerState == PlayerState.WALKING) {
        this.currentAnimationName = facingDirection == Direction.RIGHT ? "WALK_RIGHT" : "WALK_LEFT";
    } else {
        this.currentAnimationName = facingDirection == Direction.RIGHT ? "STAND_RIGHT" : "STAND_LEFT";
    }
}

    @Override
    public void onEndCollisionCheckX(boolean hasCollided, Direction direction, GameObject entityCollidedWith) { }

    @Override
    public void onEndCollisionCheckY(boolean hasCollided, Direction direction, GameObject entityCollidedWith) { }

    public PlayerState getPlayerState() {
        return playerState;
    }

    public void setPlayerState(PlayerState playerState) {
        this.playerState = playerState;
    }

    public Direction getFacingDirection() {
        return facingDirection;
    }

    public void setFacingDirection(Direction facingDirection) {
        this.facingDirection = facingDirection;
    }

    public Rectangle getInteractionRange() {
        return new Rectangle(
                getBounds().getX1() - interactionRange,
                getBounds().getY1() - interactionRange,
                getBounds().getWidth() + (interactionRange * 2),
                getBounds().getHeight() + (interactionRange * 2));
    }

    public Key getInteractKey() { return INTERACT_KEY; }
    public Direction getCurrentWalkingXDirection() { return currentWalkingXDirection; }
    public Direction getCurrentWalkingYDirection() { return currentWalkingYDirection; }
    public Direction getLastWalkingXDirection() { return lastWalkingXDirection; }
    public Direction getLastWalkingYDirection() { return lastWalkingYDirection; }

    
    public void lock() {
        isLocked = true;
        playerState = PlayerState.STANDING;
        this.currentAnimationName = facingDirection == Direction.RIGHT ? "STAND_RIGHT" : "STAND_LEFT";
    }

    public void unlock() {
        isLocked = false;
        playerState = PlayerState.STANDING;
        this.currentAnimationName = facingDirection == Direction.RIGHT ? "STAND_RIGHT" : "STAND_LEFT";
    }

    // used by other files or scripts to force player to stand
    public void stand(Direction direction) {
        playerState = PlayerState.STANDING;
        facingDirection = direction;
        if (direction == Direction.RIGHT) {
            this.currentAnimationName = "STAND_RIGHT";
        }
        else if (direction == Direction.LEFT) {
            this.currentAnimationName = "STAND_LEFT";
        }
    }

    // used by other files or scripts to force player to walk
    public void walk(Direction direction, float speed) {
        playerState = PlayerState.WALKING;
        facingDirection = direction;
        if (direction == Direction.RIGHT) {
            this.currentAnimationName = "WALK_RIGHT";
        }
        else if (direction == Direction.LEFT) {
            this.currentAnimationName = "WALK_LEFT";
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

    // Health Method 
    public int getHealth() {
        return health;
    }

    public int getKeys() {
        return keys;
    }










    
    public int getMaxHealth() {
        return MAX_HEALTH;
    }

    public void heal(int amount) {
        this.health += amount;
        if(this.health > MAX_HEALTH) {
            this.health = MAX_HEALTH;
        }
    }

    public boolean takeDamage(int amount) {
        this.health -= amount;
        if(this.health <= 0) {
            this.health = 0;
            return true;
        }
        return false;
    }

    /* 
    // Uncomment this to have game draw player's bounds to make it easier to visualize
        public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
        drawBounds(graphicsHandler, new Color(255, 0, 0, 100));
    }
    */

    @Override
    public Rectangle getBounds() {
        Rectangle original = super.getBounds();
        int extra = 4; // Increase size by pixels on all sides
        return new Rectangle(
            original.getX1() - extra,
            original.getY1() - extra,
            original.getWidth() + extra * 2,
            original.getHeight() + extra * 2
        );
    }

    public abstract void setHasSword(boolean b);

   
    
}
