package EnhancedMapTiles;

import Builders.FrameBuilder;
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

    public Coin(Point location) {
        // Create a passable enhanced map tile using coin.png
        super(location.x, location.y, new SpriteSheet(ImageLoader.load("coin2.png"), 16, 16), TileType.PASSABLE);
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
        if (!collected && player.getBounds().intersects(coinObject.getBounds())) {
            collected = true;
            // Hide the coin by moving it off-screen
            coinObject.setLocation(-100, -100);
            coinsCollected++;
        }
    }
}



