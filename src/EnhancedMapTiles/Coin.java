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



    private static final java.util.HashSet<String> collectedCoins = new java.util.HashSet<>();
    public Coin(Point location) {
        // Create a passable enhanced map tile using coin.png
        super(location.x, location.y, new SpriteSheet(ImageLoader.load("neww_coin.png"), 16, 16), TileType.PASSABLE);
    }

    private String key() {
        return "Coin@" + x + "," + y;
    }
    // Public helper so maps can query without instantiating a Coin
    public static boolean isCollectedAt(float x, float y) {
        return collectedCoins.contains("Coin@" + x + "," + y);
    }

    public static boolean isCollectedAt(Utils.Point p) {
        return isCollectedAt(p.x, p.y);
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
    

    private boolean collected = false;
    public static int coinsCollected = 0;

    @Override
    public void update(Player player) {
        if (collected) return;
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



