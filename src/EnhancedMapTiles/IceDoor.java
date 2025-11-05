package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.AudioPlayer;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.MapEntity;
import Level.Player;
import Level.*;
import Scripts.*;
import ScriptActions.LockPlayerScriptAction;
import ScriptActions.ScriptAction;
import ScriptActions.UnlockPlayerScriptAction;
import Utils.Point;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Door that opens via interact script.
 * AFTER opening, pressing E (or SPACE) near the door will swap to another map and spawn the player there.
 * Repeatable (cooldown prevents double-triggering).
 */
public class IceDoor extends EnhancedMapTile {

    // --- behavior tuning ---
    private static final int TELEPORT_COOLDOWN_FRAMES = 12;   // debounce key presses
    private static final float BAND_LEFT_PAD   = 40f;         // proximity band around door
    private static final float BAND_RIGHT_PAD  = 56f;
    private static final float BAND_TOP_PAD    = 56f;
    private static final float BAND_BOTTOM_PAD = 56f;

    // state
    private boolean isOpen = false;
    private int teleportCooldown = 0;

    // render
    private GameObject doorObj;
    private Frame closedFrame;
    private Frame openFrame;

    // target map info
    private String targetMapName = null;          // e.g., "SecondRoom" or "FirstRoom"
    private int spawnTileX = 0, spawnTileY = 0;   // where to drop the player on target map (tile coords)
    private int tileW = 48, tileH = 48;           // rendered tile size

    public IceDoor(Point location) {
        super(location.x, location.y,
                new SpriteSheet(ImageLoader.load("Ice_Door.png"), 16, 32),
                TileType.NOT_PASSABLE);

        // block until opened
        setIsUncollidable(true);

        // interact script to open the door (one-time) and swap frame
        setInteractScript(new Script() {
            @Override
            public ArrayList<ScriptAction> loadScriptActions() {
                ArrayList<ScriptAction> actions = new ArrayList<>();
                if (isOpen) return actions;

                actions.add(new LockPlayerScriptAction());

                actions.add(new ScriptAction() {
                    @Override
                    public Level.ScriptState execute() {
                        System.out.println("[Door] opening...");
                        isOpen = true;
                        setIsUncollidable(true);      // once opened, no longer blocks
                        teleportCooldown = 0;

                        // swap art to open frame
                        try {
                            boolean swapped = setGameObjectFrame(doorObj, openFrame);
                            System.out.println("[Door] swapped frame on doorObj = " + swapped);
                        } catch (Throwable th) {
                            System.out.println("[Door] WARN: frame swap failed: " + th);
                        }

                        // ✅ play door open SFX (non-looping)
                        try {
                            AudioPlayer.playSound("Resources/audio/Door_Open.wav", -6.0f);
                        } catch (Throwable s) {
                            System.out.println("[Door] Could not play Door_Open.wav: " + s);
                        }

                        return Level.ScriptState.COMPLETED;
                    }
                });

                actions.add(new UnlockPlayerScriptAction());
                return actions;
            }
        });
    }

    /** Configure which map & where this door should send the player (tile coordinates). */
    public IceDoor toMap(String mapName, int spawnTileX, int spawnTileY) {
        this.targetMapName = mapName;
        this.spawnTileX = spawnTileX;
        this.spawnTileY = spawnTileY;
        return this;
    }

    /** If your map uses a different rendered tile size, set it here. */
    public IceDoor withTileSizePixels(int tileW, int tileH) {
        this.tileW = tileW;
        this.tileH = tileH;
        return this;
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet sheet) {
        closedFrame = new FrameBuilder(sheet.getSubImage(0, 0))
                .withScale(3)
                .withBounds(0, 16, 16, 16) // collide lower half
                .build();

        SpriteSheet openSheet = new SpriteSheet(ImageLoader.load("DoorOpen.png"), 16, 32);
        openFrame = new FrameBuilder(openSheet.getSubImage(0, 0))
                .withScale(3)
                .withBounds(0, 16, 16, 16)
                .build();

        // lift by 16px so the base sits on the floor visually
        doorObj = new GameObject(x, y - 16, closedFrame);
        return doorObj;
    }

    @Override
    public void update(Player player) {
        super.update(player);
        if (player == null || !isOpen) return;

        if (teleportCooldown > 0) teleportCooldown--;

        boolean confirm = isConfirmDown(); // E or SPACE
        boolean near = nearDoor(player);

        if (confirm && near && teleportCooldown == 0) {
            if (targetMapName != null) {
                int spawnPx = spawnTileX * tileW + 6;           // small offset so feet land comfortably
                int spawnPy = spawnTileY * tileH + tileH + 6;   // one tile below + a bump
                Point spawn = new Point(spawnPx, spawnPy);

                queueMapChange(targetMapName, spawn);
                teleportCooldown = TELEPORT_COOLDOWN_FRAMES;
            } else {
                System.out.println("[Door] WARN: no targetMapName set on this door!");
                teleportCooldown = 8;
            }
        }
    }

    // ---- glue: call PlayLevelScreen queue method directly ----
    private void queueMapChange(String mapName, Point spawnPixels) {
        try {
            Screens.PlayLevelScreen.queueMapChange(mapName, spawnPixels);
            System.out.println("[Door] queued map change to " + mapName +
                    " spawn " + spawnPixels.x + "," + spawnPixels.y);
        } catch (Throwable t) {
            System.out.println("[Door] ERROR: could not queue map change. " + t);
        }
    }

    // -------- helpers (input, proximity, reflection utilities) --------

    private boolean isConfirmDown() {
        // Try Engine.Key.E / Engine.Key.SPACE in a flexible way
        try {
            Class<?> keyCls = Class.forName("Engine.Key");
            Class<?> kbCls  = Class.forName("Engine.Keyboard");
            Object KEY_E = null;
            try { KEY_E = keyCls.getField("E").get(null); } catch (Throwable ignored) {}
            if (KEY_E == null) {
                try { KEY_E = keyCls.getField("e").get(null); } catch (Throwable ignored) {}
            }
            Object KEY_SPACE = null;
            try { KEY_SPACE = keyCls.getField("SPACE").get(null); } catch (Throwable ignored) {}
            Method isDown = kbCls.getMethod("isKeyDown", keyCls);

            boolean e = (KEY_E != null) && (boolean) isDown.invoke(null, KEY_E);
            boolean sp = (KEY_SPACE != null) && (boolean) isDown.invoke(null, KEY_SPACE);
            return e || sp;
        } catch (Throwable ignored) {
            // If reflection fails, default to true so door still works in dev
            return true;
        }
    }

    private boolean nearDoor(Player player) {
        try {
            Method m = player.getClass().getMethod("intersects", GameObject.class);
            if ((boolean) m.invoke(player, doorObj)) return true;
        } catch (Throwable ignored) {}
        try {
            Method m = player.getClass().getMethod("intersects", MapEntity.class);
            if ((boolean) m.invoke(player, this)) return true;
        } catch (Throwable ignored) {}

        float px = getNumeric(player, "getX", "x");
        float py = getNumeric(player, "getY", "y");
        float pw = getNumeric(player, "getWidth", "width");
        float ph = getNumeric(player, "getHeight", "height");
        float cx = px + (pw > 0 ? pw / 2f : 8f);
        float cy = py + (ph > 0 ? ph / 2f : 8f);

        float left   = this.x - BAND_LEFT_PAD;
        float right  = this.x + BAND_RIGHT_PAD;
        float top    = this.y - BAND_TOP_PAD;
        float bottom = this.y + BAND_BOTTOM_PAD;

        return (cx >= left && cx <= right && cy >= top && cy <= bottom);
    }

    private boolean setGameObjectFrame(GameObject obj, Frame newFrame) {
        if (obj == null || newFrame == null) return false;
        Class<?> c = obj.getClass();
        try {
            if (trySetFrameField(obj, c, "currentFrame", newFrame)) return true;
            if (trySetFrameField(obj, c, "frame",        newFrame)) return true;
            while (c != null) {
                for (Field f : c.getDeclaredFields()) {
                    if (Frame.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        f.set(obj, newFrame);
                        return true;
                    }
                }
                c = c.getSuperclass();
            }
        } catch (Throwable ignored) {}
        return false;
    }
    private boolean trySetFrameField(GameObject obj, Class<?> cls, String fieldName, Frame value) {
        try { Field f = cls.getDeclaredField(fieldName); f.setAccessible(true); f.set(obj, value); return true; }
        catch (Throwable ignored) { return false; }
    }

    private float getNumeric(Object obj, String getter, String field) {
        try { Method m = obj.getClass().getMethod(getter); Object r = m.invoke(obj); if (r instanceof Number) return ((Number) r).floatValue(); }
        catch (Throwable ignored) {}
        try { Field f = obj.getClass().getDeclaredField(field); f.setAccessible(true); Object r = f.get(obj); if (r instanceof Number) return ((Number) r).floatValue(); }
        catch (Throwable ignored) {}
        return 0f;
    }
}
