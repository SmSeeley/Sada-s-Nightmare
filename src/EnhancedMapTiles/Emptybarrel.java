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

public class Emptybarrel extends EnhancedMapTile {
    private GameObject BloodObject;
    private Frame BloodFrame;

    /** Uses Resources/barrel.png */
    public Emptybarrel(Point location) {
        this(location, "EmptyBarrel.png");
    }

    public Emptybarrel(Point location, String imageFileName) {
        super(
            location.x,
            location.y,
            new SpriteSheet(ImageLoader.load(imageFileName), 16, 16),
            TileType.NOT_PASSABLE                                   // <-- blocks movement
        );
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet sheet) {
        // Build the frame (scaled to 48x48)
        BloodFrame = new FrameBuilder(sheet.getSubImage(0, 0))
                .withScale(3)
                // Collision on the lower part so the player can overlap the top a bit (feels nicer)
                // Adjust if you want the whole 16x16 solid: .withBounds(0, 0, 16, 16)
                .withBounds(2, 8, 12, 8)
                .build();

        BloodObject = new GameObject(x, y, BloodFrame);
        return BloodObject; // drawn on the bottom layer
    }

    @Override
    public void update(Player player) {
        // No behavior; it’s just a blocking prop.
        super.update(player);
    }
}
