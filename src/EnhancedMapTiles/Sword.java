package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.AudioPlayer;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.Player;
import Level.ScriptState;
import Level.TileType;
import ScriptActions.TextboxScriptAction;
import Utils.Point;

/**
 * Enhanced map tile for a weapon pickup (Slime Hammer).
 * - Passable prop that renders the hammer sprite.
 * - On collision: collects, equips, shows a textbox, and plays a sound.
 */
public class Sword extends EnhancedMapTile {
    private Frame swordFrame;
    private GameObject swordObject;
    private boolean collected = false;

    private static boolean hasSword = false;
    private static int swordDamage = 2;

    // Textbox handling
    private TextboxScriptAction activeTextbox = null;
    private boolean textboxSetupDone = false;
    private int textboxTimer = 0;

    public Sword(Point location) {
        super(location.x, location.y,
                new SpriteSheet(ImageLoader.load("slimehammer.png"), 16, 16),
                TileType.PASSABLE);
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet sheet) {
        swordFrame = new FrameBuilder(sheet.getSubImage(0, 0))
                .withScale(3)
                .withBounds(0, 0, 16, 16)
                .build();

        swordObject = new GameObject(x, y - 16, swordFrame);
        return swordObject;
    }

    @Override
    public void update(Player player) {
        // If a textbox is active, drive its lifecycle every frame
        if (activeTextbox != null) {
            if (!textboxSetupDone) {
                try {
                    activeTextbox.setMap(map);
                    activeTextbox.setup();
                    textboxSetupDone = true;
                    textboxTimer = 0;
                } catch (Exception e) {
                    System.out.println("[Sword] Textbox setup failed: " + e.getMessage());
                    activeTextbox = null;
                }
            } else {
                ScriptState state = ScriptState.RUNNING;
                try {
                    state = activeTextbox.execute();
                } catch (Exception ignored) {}

                textboxTimer++;
                if (state == ScriptState.COMPLETED || textboxTimer >= 120) {
                    try {
                        activeTextbox.cleanup();
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
            hasSword = true;

            swordObject.setLocation(-100, -100);

            // Equip on player
            try {
                player.setHasSword(true);
            } catch (Exception e) {
                try {
                    java.lang.reflect.Method m = player.getClass().getMethod("equipSword");
                    m.invoke(player);
                } catch (Exception ignored) {}
            }

            // ✅ Play special pickup sound
            try {
                AudioPlayer.playSound("Resources/audio/Key_Item.wav", -3.0f); // volume around 70%
            } catch (Exception e) {
                System.out.println("[Sword] Failed to play special_item sound: " + e.getMessage());
            }

            // Create textbox
            TextboxScriptAction text = new TextboxScriptAction();
            text.addText("You picked up the Slime Hammer!");
            activeTextbox = text;
            textboxSetupDone = false;
        }
    }

    public static boolean hasSword() {
        return hasSword;
    }

    public static int getSwordDamage() {
        return swordDamage;
    }
}
