package EnhancedMapTiles;


import Builders.FrameBuilder;
import Engine.AudioPlayer;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.Point;


public class HealthPotion extends EnhancedMapTile {
   private GameObject potionObject;
   private Frame potionFrame;
   public static final int HEAL_AMOUNT = 10; // Heals 1 heart
   private boolean collected = false;


   public HealthPotion(Point location) {
       super(location.x,
           location.y,
           new SpriteSheet(ImageLoader.load("Health_Potion.png"), 32, 32),
           TileType.PASSABLE // Player can walk over it
       );
   }


   @Override
   protected GameObject loadBottomLayer(SpriteSheet spriteSheet) {
       // Defines the sprite and the collision box for the potion
       potionFrame = new FrameBuilder(spriteSheet.getSubImage(0, 0))
           .withScale(1.5f)
           .withBounds(0, 0, 32, 32)
           .build();
       potionObject = new GameObject(x, y, potionFrame);
       
       System.out.println("[HealthPotion] Created at x=" + x + ", y=" + y);
       return potionObject;
   }


   // Healing and removing the potion
   @Override
   public void update(Player player) {
       if (collected) {
           if (potionObject != null) {
               potionObject.setLocation(-100, -100);
           }
           return;
       }
       if (potionObject == null) return; // nothing to interact with if not rendered
       
       // Check if the player's bounds intersect the potion's bounds
       if (intersects(player.getBounds())) {
           collected = true;
          
           // Only heal if the player is not at max health
           if (player.getHealth() < player.getMaxHealth()) {
               player.heal(HEAL_AMOUNT);
               System.out.println("Player healed 1 heart. Potion consumed.");
               AudioPlayer.playSound("Resources/audio/Healing.wav", -3.0f);
           }
           
           // Hide the potion by moving it off-screen
           potionObject.setLocation(-100, -100);
       }
   }
}
