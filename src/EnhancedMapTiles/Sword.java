package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Level.ScriptState;
import ScriptActions.TextboxScriptAction;
import Utils.Point;

/**
 * Enhanced map tile for a weapon pickup (Slime Hammer).
 * - Passable prop that renders the hammer sprite.
 * - On collision: collects, equips, shows a textbox.
 * - Textbox uses ScriptAction lifecycle: setup() -> execute() until completed -> cleanup().
 * - Also auto-closes after ~2 seconds (120 frames) as a fallback.
 */
public class Sword extends EnhancedMapTile {
    private Frame swordFrame;
    private GameObject swordObject;
    private boolean collected = false;

    private static final java.util.HashSet<String> collectedSwords = new java.util.HashSet<>();
    private static boolean hasSword = false;
    private static int swordDamage = 2;

    // Textbox handling
    private TextboxScriptAction activeTextbox = null;
    private boolean textboxSetupDone = false;
    private int textboxTimer = 0; // frames (~60 fps)

    public Sword(Point location) {
        super(location.x, location.y,
                new SpriteSheet(ImageLoader.load("slimehammer.png"), 16, 16),
                TileType.PASSABLE);
    }

    private String key() {
        return "Sword@" + x + "," + y;
    }

    public static boolean isCollectedAt(float x, float y) {
        return collectedSwords.contains("Sword@" + x + "," + y);
    }

    public static boolean isCollectedAt(Point p) {
        return isCollectedAt(p.x, p.y);
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet sheet) {
        swordFrame = new FrameBuilder(sheet.getSubImage(0, 0))
                .withScale(3)
                .withBounds(0, 0, 16, 16)
                .build();

        // Draw slightly raised so it sits nicely on floor
        swordObject = new GameObject(x, y - 16, swordFrame);
        return swordObject;
    }

    @Override
    public void update(Player player) {
        // If a textbox is active, drive its lifecycle every frame
        if (activeTextbox != null) {
            if (!textboxSetupDone) {
                // setup() actually pushes text to the map and activates the textbox
                try {
                    activeTextbox.setMap(map);
                    activeTextbox.setup();
                    textboxSetupDone = true;
                    textboxTimer = 0;
                } catch (Exception e) {
                    System.out.println("[Sword] Textbox setup failed: " + e.getMessage());
                    // Abort textbox on failure
                    activeTextbox = null;
                }
            } else {
                // keep advancing the script; it returns COMPLETED when queue empties
                ScriptState state = ScriptState.RUNNING;
                try {
                    state = activeTextbox.execute();
                } catch (Exception ignored) {}

                textboxTimer++;

                // Auto-close after ~120 frames OR when script completes
                if (state == ScriptState.COMPLETED || textboxTimer >= 150) {
                    try {
                        activeTextbox.cleanup(); // this calls map.getTextbox().setIsActive(false)
                    } catch (Exception ignored) {}
                    activeTextbox = null;
                    textboxSetupDone = false;
                    textboxTimer = 0;
                }
            }
        }

        if (collected || swordObject == null) return;

        // Detect pickup
        if (player.getBounds().intersects(swordObject.getBounds())) {
            collected = true;
            collectedSwords.add(key());
            hasSword = true;

            // Hide the sprite
            swordObject.setLocation(-100, -100);

            // Equip on player if a method exists
            try {
                player.setHasSword(true);
            } catch (Exception e) {
                try {
                    java.lang.reflect.Method m = player.getClass().getMethod("equipSword");
                    m.invoke(player);
                } catch (Exception ignored) {}
            }

            // Create the textbox action (it will be setup next frame)
            TextboxScriptAction text = new TextboxScriptAction();
            text.addText("You picked up the Slime Hammer! (+2 Damage) ");
            activeTextbox = text;
            textboxSetupDone = false; // ensure setup() runs
        }
    }

    // Accessors for player/attack logic
    public static boolean hasSword() {
        return hasSword;
    }

    public static int getSwordDamage() {
        return swordDamage;
    }
}
