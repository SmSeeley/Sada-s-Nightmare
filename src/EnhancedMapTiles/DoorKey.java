package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.AudioPlayer;
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

public class DoorKey extends EnhancedMapTile {
    private Frame keyFrame;
    private GameObject keyObject;

    private boolean collectedKey = false;
    public static int keysCollected = 0;

    // --- textbox lifecycle (same pattern as Sword)
    private TextboxScriptAction activeTextbox = null;
    private boolean textboxSetupDone = false;
    private int textboxTimer = 0;              // frames
    private static final int TOAST_FRAMES = 120; // ~2 seconds @ 60fps

    public DoorKey(Point location) {
        super(location.x, location.y,
              new SpriteSheet(ImageLoader.load("NewKey.png"), 16, 16),
              TileType.PASSABLE);
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet sheet) {
        keyFrame = new FrameBuilder(sheet.getSubImage(0, 0))
                .withScale(3)
                .withBounds(0, 0, 16, 16)
                .build();

        keyObject = new GameObject(x, y, keyFrame);
        System.out.println("[Key] Created at x=" + x + ", y=" + y);
        return keyObject;
    }

    @Override
    public void update(Player player) {
        // drive textbox if active
        if (activeTextbox != null) {
            if (!textboxSetupDone) {
                try {
                    activeTextbox.setMap(map);
                    activeTextbox.setup();
                    textboxSetupDone = true;
                    textboxTimer = 0;
                } catch (Exception e) {
                    System.out.println("[Key] Textbox setup failed: " + e.getMessage());
                    activeTextbox = null;
                }
            } else {
                ScriptState state = ScriptState.RUNNING;
                try { state = activeTextbox.execute(); } catch (Exception ignored) {}
                textboxTimer++;
                if (state == ScriptState.COMPLETED || textboxTimer >= TOAST_FRAMES) {
                    try { activeTextbox.cleanup(); } catch (Exception ignored) {}
                    activeTextbox = null;
                    textboxSetupDone = false;
                    textboxTimer = 0;
                }
            }
        }

        if (collectedKey || keyObject == null) return;

        // pickup detection
        if (player.getBounds().intersects(keyObject.getBounds())) {
            collectedKey = true;

            // hide sprite and count
            keyObject.setLocation(-100, -100);
            keysCollected++;
            System.out.println("[Key] Key collected! Total keys = " + keysCollected);

            // ✅ play pickup SFX (same style as Sword)
            try {
                AudioPlayer.playSound("Resources/audio/keyPickup.wav", -3.0f);
            } catch (Exception e) {
                System.out.println("[Key] Failed to play keyPickup.wav: " + e.getMessage());
            }

            // ✅ show textbox for ~2s
            TextboxScriptAction text = new TextboxScriptAction();
            text.addText("You Got a Key!");
            activeTextbox = text;
            textboxSetupDone = false;
        }
    }
}
