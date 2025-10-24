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
    private GameObject potionObject;
    public static final int HEAL_AMOUNT = 2; // Heals 1 heart

        private static final java.util.HashSet<String> collectedPotions = new java.util.HashSet<>();

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
        potionObject = new GameObject(getX(), getY(), potionFrame);

        return potionObject;
    }

    private boolean collected = false;
    public static int potionCollected = 0;

    public static boolean isCollectedAt(Utils.Point p) {
        return isCollectedAt(p.x, p.y);
    }

    private String key() {
        return "HealthPotion@" + x + "," + y;
    }

    // Public helper so maps can query without instantiating a Coin
    public static boolean isCollectedAt(float x, float y) {
        return collectedPotions.contains("HealthPotion@" + x + "," + y);
    }

    // Healing and removing the potion
    @Override
    public void update(Player player) {
        if (collected) return;
        if (potionObject == null) return; // nothing to interact with if not rendered
        // Check if the player's bounds intersect the potion's bounds
        if (intersects(player.getBounds())) {

            collected = true;
            collectedPotions.add(key());
            
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