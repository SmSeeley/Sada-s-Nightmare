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


// This class creates an enhanced map tile for coin.png that the player can walk through.
// The coin will appear below the door in the map layer order.
public class Coin extends EnhancedMapTile {
   private Frame coinFrame;
   private GameObject coinObject;
   private boolean collected = false;

   private static final java.util.HashSet<String> collectedCoins = new java.util.HashSet<>();
   public static int coinsCollected = 0;
   private final String mapName; // Store which map this coin belongs to


   public Coin(Point location) {
       // Create a passable enhanced map tile using coin.png
       super(location.x, location.y, new SpriteSheet(ImageLoader.load("neww_coin.png"), 16, 16), TileType.PASSABLE);
       this.mapName = ""; // Will be set when added to map
   }
   
   public Coin(Point location, String mapName) {
       // Create a passable enhanced map tile using coin.png
       super(location.x, location.y, new SpriteSheet(ImageLoader.load("neww_coin.png"), 16, 16), TileType.PASSABLE);
       this.mapName = mapName;
   }


   // Add reset methods to clear static data on game restart
   public static void resetAllCoinsTest() {
       collectedCoins.clear();
       coinsCollected = 0;
       System.out.println("All coins reset for new game");
   }


   public static void resetCoinCounter() {
       coinsCollected = 0;
   }


   public static void resetCollectedCoins() {
       collectedCoins.clear();
   }


   private String key() {
       return mapName + "@Coin@" + x + "," + y;
   }
   // Public helper so maps can query without instantiating a Coin
   public static boolean isCollectedAt(String mapName, float x, float y) {
       return collectedCoins.contains(mapName + "@Coin@" + x + "," + y);
   }


   public static boolean isCollectedAt(String mapName, Utils.Point p) {
       return isCollectedAt(mapName, p.x, p.y);
   }


   @Override
   protected GameObject loadBottomLayer(SpriteSheet sheet) {
      
       // Build the coin frame from the sprite sheet
       coinFrame = new FrameBuilder(sheet.getSubImage(0, 0))
               .withScale(3)
               .withBounds(0, 0, 16, 16)
               .build();
       // Create the coin game object at the specified location
       coinObject = new GameObject(x, y, coinFrame);


       // Return the coin object to be rendered below the door
       return coinObject;
   }
  


   @Override
   public void update(Player player) {
       // Check if this specific coin was already collected
       if (collected || collectedCoins.contains(key())) {
           collected = true;
           if (coinObject != null) {
               coinObject.setLocation(-100, -100);
           }
       return;
       }
       if (coinObject == null) return; // nothing to interact with if not rendered


       if (player.getBounds().intersects(coinObject.getBounds())) {
           collected = true;
           collectedCoins.add(key());
           // Hide the coin by moving it off-screen (and increment global counter)
           coinObject.setLocation(-100, -100);
           coinsCollected++;


            // ✅ Play special pickup sound
           try {
               AudioPlayer.playSound("Resources/audio/Coin_Pickup.wav", -10.0f); // volume around 70%
           } catch (Exception e) {
               System.out.println("Failed to play sound: " + e.getMessage());
       }
       }
      
   }
}
