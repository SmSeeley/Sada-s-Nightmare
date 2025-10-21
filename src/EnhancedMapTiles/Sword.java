// ...existing code...
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

/**
 * Enhanced map tile for a sword pickup.
 * - Passable tile that renders a sword sprite on the bottom layer.
 * - When player intersects the sword it is collected (hidden) and the static flags are set.
 * - Player attack code should read Sword.hasSword() and Sword.getSwordDamage() to apply different damage.
 */
public class Sword extends EnhancedMapTile {
    private Frame swordFrame;
    private GameObject swordObject;
    private boolean collected = false;

    // global state accessible from player/attack logic
    private static boolean hasSword = false;
    private static int swordDamage = 2; 

    public Sword(Point location) {
        super(location.x, location.y, new SpriteSheet(ImageLoader.load("sword4.png"), 16, 16), TileType.PASSABLE);
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet sheet) {
        swordFrame = new FrameBuilder(sheet.getSubImage(0, 0))
                .withScale(3)
                .withBounds(0, 0, 16, 16)
                .build();

        // render the sword slightly above the tile so it is visible (like doors/coins)
        swordObject = new GameObject(x, y - 16, swordFrame);
        return swordObject;
    }

    @Override
    public void update(Player player) {
        if (!collected && player.getBounds().intersects(swordObject.getBounds())) {
            collected = true;
            // hide the sword visually
            swordObject.setLocation(-100, -100);
            hasSword = true;
            System.out.println("Sword collected!");

            // notify the player instance so it can change sprite/animations
            try {
                player.setHasSword(true);
            } catch (Exception e) {
                // fallback: attempt reflective call to common method name
                try {
                    java.lang.reflect.Method m = player.getClass().getMethod("equipSword");
                    m.invoke(player);
                } catch (Exception ex) {
                    System.out.println("Sword pickup: failed to notify player to equip sword.");
                }
            }
        }
    }

    // Accessors for player/attack code
    public static boolean hasSword() {
        return hasSword;
    }

    public static int getSwordDamage() {
        return swordDamage;
    }
}
