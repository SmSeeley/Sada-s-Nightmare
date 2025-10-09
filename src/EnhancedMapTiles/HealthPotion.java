package EnhancedMapTiles; 

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.MapEntityStatus;
import Level.Player;
import Level.TileType;
import Utils.Point;

public class HealthPotion extends EnhancedMapTile { 
    public static final int HEAL_AMOUNT = 2; // Heals 1 heart

    public HealthPotion(Point location) {
        super(
            location.x, 
            location.y, 
            new SpriteSheet(ImageLoader.load("Health_Potion.png"), 32, 32), 
            TileType.PASSABLE // Player can walk over it
        ); 
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet spriteSheet) {
        // Defines the sprite and the collision box for the potion
        Frame potionFrame = new FrameBuilder(spriteSheet.getSubImage(0, 0))
            .withScale(2) 
            .withBounds(0, 0, 16, 16) 
            .build();
        return new GameObject(getX(), getY(), potionFrame);
    }

    // Healing and removing the potion
    @Override
    public void update(Player player) {
        // Check if the player's bounds intersect the potion's bounds
        if (intersects(player.getBounds())) {
            
            // Only heal if the player is not at max health
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(HEAL_AMOUNT);
                
                // Set the status to REMOVED to make it disappear
                this.mapEntityStatus = MapEntityStatus.REMOVED; 
                System.out.println("Player healed 1 heart. Potion consumed.");
            }
        }
        super.update(player); // Update method
    }
}