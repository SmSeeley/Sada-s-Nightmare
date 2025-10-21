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

    private static final java.util.HashSet<String> collectedSwords = new java.util.HashSet<>();
    // global state accessible from player/attack logic
    private static boolean hasSword = false;
    private static int swordDamage = 2; 

    public Sword(Point location) {
        super(location.x, location.y, new SpriteSheet(ImageLoader.load("sword4.png"), 16, 16), TileType.PASSABLE);
    }

    private String key() {
        return "Sword@" + x + "," + y;
    }

     public static boolean isCollectedAt(float x, float y) {
        return collectedSwords.contains("Sword@" + x + "," + y);
    }

    public static boolean isCollectedAt(Utils.Point p) {
        return isCollectedAt(p.x, p.y);
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
        if (collected) return;
        if (swordObject == null) return;

        if (player.getBounds().intersects(swordObject.getBounds())) {
            collected = true;
            collectedSwords.add(key());
            swordObject.setLocation(-100, -100);
            hasSword = true;
            // notify player immediately
            try {
                player.setHasSword(true);
            } catch (Exception e) {
                try {
                    java.lang.reflect.Method m = player.getClass().getMethod("equipSword");
                    m.invoke(player);
                } catch (Exception ex) {
                    // ignore
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
