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

public class Skull extends EnhancedMapTile {
    private GameObject barrelObject;
    private Frame barrelFrame;

    /** Uses Resources/barrel.png */
    public Skull(Point location) {
        this(location, "Skull.png");
    }

    public Skull(Point location, String imageFileName) {
        super(
            location.x,
            location.y,
            new SpriteSheet(ImageLoader.load(imageFileName), 16, 16),
            TileType.PASSABLE                                 // <-- blocks movement
        );
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet sheet) {
        // Build the frame (scaled to 48x48)
        barrelFrame = new FrameBuilder(sheet.getSubImage(0, 0))
                .withScale(3)
                // Collision on the lower part so the player can overlap the top a bit (feels nicer)
                // Adjust if you want the whole 16x16 solid: .withBounds(0, 0, 16, 16)
                .withBounds(2, 8, 12, 8)
                .build();

        barrelObject = new GameObject(x, y, barrelFrame);
        return barrelObject; // drawn on the bottom layer
    }

    @Override
    public void update(Player player) {
        // No behavior; it’s just a blocking prop.
        super.update(player);
    }
}
