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
 * Enhanced map tile for a weapon pickup (Watermelon Weapon).
 * - Passable prop that renders the watermelon sprite.
 * - On collision: collects, equips, shows a textbox, and plays a sound.
 * - Can be purchased from shopkeeper or found as pickup.
 */
public class Watermelon extends EnhancedMapTile {
    private Frame watermelonFrame;
    private GameObject watermelonObject;
    private boolean collected = false;

    private static boolean hasWatermelon = false;
    private static int watermelonDamage = 1; // Low damage weapon

    // Textbox handling
    private TextboxScriptAction activeTextbox = null;
    private boolean textboxSetupDone = false;
    private int textboxTimer = 0;

    public Watermelon(Point location) {
        super(location.x, location.y,
                new SpriteSheet(ImageLoader.load("sada-watermelon.png"), 16, 16),
                TileType.PASSABLE);
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet sheet) {
        watermelonFrame = new FrameBuilder(sheet.getSubImage(0, 0))
                .withScale(3)
                .withBounds(0, 0, 16, 16)
                .build();

        watermelonObject = new GameObject(x, y - 16, watermelonFrame);
        return watermelonObject;
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
                    System.out.println("[Watermelon] Textbox setup failed: " + e.getMessage());
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

        if (collected || watermelonObject == null) return;

        // Detect pickup
        if (player.getBounds().intersects(watermelonObject.getBounds())) {
            collected = true;
            hasWatermelon = true;

            watermelonObject.setLocation(-100, -100);

            // Equip on player
            try {
                player.setHasWeapon("watermelon");
            } catch (Exception e) {
                System.out.println("[Watermelon] Failed to equip: " + e.getMessage());
            }

            // Play special pickup sound
            try {
                AudioPlayer.playSound("Resources/audio/Key_Item.wav", -3.0f);
            } catch (Exception e) {
                System.out.println("[Watermelon] Failed to play special_item sound: " + e.getMessage());
            }

            // Create textbox
            TextboxScriptAction text = new TextboxScriptAction();
            text.addText("You picked up the Watermelon! A refreshing weapon!");
            activeTextbox = text;
            textboxSetupDone = false;
        }
    }

    // Static methods for shop integration
    public static boolean hasWatermelon() {
        return hasWatermelon;
    }

    public static void setWatermelon(boolean has) {
        hasWatermelon = has;
    }

    public static int getWatermelonDamage() {
        return watermelonDamage;
    }

    // Method for shopkeeper to give Watermelon to player without pickup animation
    public static void giveToPlayer(Player player) {
        hasWatermelon = true;
        try {
            player.setHasWeapon("watermelon");
        } catch (Exception e) {
            System.out.println("[Watermelon] Failed to equip from shop: " + e.getMessage());
        }
    }
}
