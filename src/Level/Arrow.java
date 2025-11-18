package Level;

import java.util.HashMap;
import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import GameObject.Frame;
import GameObject.SpriteSheet;
import Engine.ImageLoader;
import Level.MapEntity;
import Utils.Direction;


public class Arrow extends MapEntity {
    private Direction direction;
    private float speed = 2.8f;
    private boolean shouldRemove = false;
    private int damage;
    
    public Arrow(float x, float y, String playerDirection) {
        super(x, y, new SpriteSheet(ImageLoader.load("arrow.png"), 16, 16), "DEFAULT");
        
        // Set arrow direction based on player's facing direction
        switch (playerDirection.toLowerCase()) {
            case "stand_up":
            case "walk_up":
            case "shoot_up":
                this.direction = Direction.UP;
                break;
            case "stand_down":
            case "walk_down":
            case "shoot_down":
                this.direction = Direction.DOWN;
                break;
            case "stand_left":
            case "walk_left":
            case "shoot_left":
                this.direction = Direction.LEFT;
                break;
            case "stand_right":
            case "walk_right":
            case "shoot_right":
            default:
                this.direction = Direction.RIGHT;
                break;
        }
        
        // Get damage from ArchersBow
        this.damage = EnhancedMapTiles.ArchersBow.getBowDamage();
    }
    
    @Override
    public void update() {
        // Move arrow in the direction it was shot
        switch (direction) {
            case UP:
                moveY(-speed);
                break;
            case DOWN:
                moveY(speed);
                break;
            case LEFT:
                moveX(-speed);
                break;
            case RIGHT:
                moveX(speed);
                break;
        }
        
        // Check if arrow is off-screen (remove it)
        if (getX() < -50 || getX() > 850 || getY() < -50 || getY() > 650) {
            shouldRemove = true;
        }
        
        super.update();
    }
    
    public boolean shouldRemove() {
        return shouldRemove;
    }
    
    public void markForRemoval() {
        shouldRemove = true;
    }
    
    public int getDamage() {
        return damage;
    }
    
    protected SpriteSheet loadSpriteSheet() {
        return new SpriteSheet(ImageLoader.load("arrow.png"), 16, 16);
    }
    
    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("DEFAULT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(2)
                    .withBounds(2, 2, 12, 12)
                    .build()
            });
        }};
    }
}
