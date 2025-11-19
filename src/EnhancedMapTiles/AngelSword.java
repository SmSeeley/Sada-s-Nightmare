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
 * Enhanced map tile for a weapon pickup (Angel Sword).
 * - Passable prop that renders the sword sprite.
 * - On collision: collects, equips, shows a textbox, and plays a sound.
 * - Can be purchased from shopkeeper or found as pickup.
 */
public class AngelSword extends EnhancedMapTile {
    private Frame swordFrame;
    private GameObject swordObject;
    private boolean collected = false;

    private static boolean hasAngelSword = false;
    private static int angelSwordDamage = 10; // High damage weapon

    // Textbox handling
    private TextboxScriptAction activeTextbox = null;
    private boolean textboxSetupDone = false;
    private int textboxTimer = 0;

    public AngelSword(Point location) {
        super(location.x, location.y,
                new SpriteSheet(ImageLoader.load("sada-angelSword.png"), 16, 16),
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
                    System.out.println("[AngelSword] Textbox setup failed: " + e.getMessage());
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
            hasAngelSword = true;

            swordObject.setLocation(-100, -100);

            // Equip Angel Sword on player
            try {
                player.setHasWeapon("angelsword");
            } catch (Exception e) {
                System.out.println("[AngelSword] Failed to equip: " + e.getMessage());
            }

            // Play special pickup sound
            try {
                AudioPlayer.playSound("Resources/audio/Key_Item.wav", -3.0f);
            } catch (Exception e) {
                System.out.println("[AngelSword] Failed to play special_item sound: " + e.getMessage());
            }

            // Create textbox
            TextboxScriptAction text = new TextboxScriptAction();
            text.addText("You picked up the Angel Sword! A divine blade!");
            activeTextbox = text;
            textboxSetupDone = false;
        }
    }

    // Static methods for shop integration
    public static boolean hasAngelSword() {
        return hasAngelSword;
    }

    public static void setAngelSword(boolean has) {
        hasAngelSword = has;
    }

    public static int getAngelSwordDamage() {
        return angelSwordDamage;
    }

    // Method for shopkeeper to give Angel Sword to player without pickup animation
    public static void giveToPlayer(Player player) {
        hasAngelSword = true;
        try {
            player.setHasWeapon("angelsword");
        } catch (Exception e) {
            System.out.println("[AngelSword] Failed to equip from shop: " + e.getMessage());
        }
    }
}
