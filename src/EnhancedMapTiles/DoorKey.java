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

// This class creates an enhanced map tile for key.png that the player can walk through.
// The key will appear below the door in the map layer order.
public class DoorKey extends EnhancedMapTile {
    private Frame keyFrame;
    private GameObject keyObject;

    public DoorKey(Point location) {
        // Create a passable enhanced map tile using key.png
        super(location.x, location.y, new SpriteSheet(ImageLoader.load("NewKey.png"), 16, 16), TileType.PASSABLE);
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet sheet) {
        // Build the key frame from the sprite sheet
        keyFrame = new FrameBuilder(sheet.getSubImage(0, 0))
                .withScale(3)
                .withBounds(0, 0, 16, 16)
                .build();

        // Create the key game object at the specified location
        keyObject = new GameObject(x, y, keyFrame);

        System.out.println("[Key] Created at x=" + x + ", y=" + y);

        // Return the key object to be rendered below the door
        return keyObject;
    }

    private boolean collectedKey = false;
    public static int keysCollected = 0;



    @Override
    public void update(Player player) {
         if (!collectedKey && player.getBounds().intersects(keyObject.getBounds())) {
            collectedKey = true;
            // Hide the key by moving it off-screen
            keyObject.setLocation(-100, -100);
            keysCollected++;
            System.out.println("[Key] Key collected! Total keys = " + keysCollected);
        } 
    }
}
