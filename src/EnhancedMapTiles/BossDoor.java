package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.AudioPlayer;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.*;
import Level.Map;
import Level.MapEntity;
import Level.Player;
import Level.Textbox;
import Level.TileType;
import Scripts.*;
import ScriptActions.ConditionalScriptAction;
import ScriptActions.ConditionalScriptActionGroup;
import ScriptActions.CustomRequirement;
import ScriptActions.LockPlayerScriptAction;
import ScriptActions.ScriptAction;
import ScriptActions.TextboxScriptAction;
import ScriptActions.UnlockPlayerScriptAction;
import Utils.Point;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * BossDoor: like NormalDoor but shows a Yes/No confirmation using the Textbox options UI.
 * - Interact near door (when closed): Textbox with options ["Yes", "No"].
 * - YES  -> open door (swap frame, make uncollidable, play SFX)
 * - NO   -> close prompt, do nothing
 * - Once open, press E/SPACE near door to queue a map change (teleport).
 */
public class BossDoor extends EnhancedMapTile {

    // --- tuning ---
    private static final int TELEPORT_COOLDOWN_FRAMES = 12;
    private static final float BAND_LEFT_PAD   = 40f;
    private static final float BAND_RIGHT_PAD  = 56f;
    private static final float BAND_TOP_PAD    = 56f;
    private static final float BAND_BOTTOM_PAD = 56f;

    // --- state ---
    private boolean isOpen = false;
    private int teleportCooldown = 0;

    // --- render ---
    private GameObject doorObj;
    private Frame closedFrame;
    private Frame openFrame;

    // --- target map info ---
    private String targetMapName = null;   // map to load when entering
    private int spawnTileX = 0, spawnTileY = 0;
    private int tileW = 48, tileH = 48;    // adjust if your map uses different tile size

    public BossDoor(Point location) {
        super(location.x, location.y,
                new SpriteSheet(ImageLoader.load("Boss_Door.png"), 16, 32),
                TileType.NOT_PASSABLE);

        // Closed door should block movement
        setIsUncollidable(false);

        setInteractScript(new Script() {
            @Override
            public ArrayList<ScriptAction> loadScriptActions() {
                ArrayList<ScriptAction> actions = new ArrayList<>();

                // If already open, don't re-prompt (player can press E/SPACE to enter)
                if (isOpen) return actions;

                actions.add(new LockPlayerScriptAction());

                // Prompt with options (uses the same options panel as your riddle)
                actions.add(new TextboxScriptAction() {{
                    addText("You are about to enter a [BOSS FIGHT]!");
                    addText("Would you still like to enter?", new String[] { "Yes", "No" });
                }});

                // YES branch (index 0)
                actions.add(new ConditionalScriptAction() {{
                    addConditionalScriptActionGroup(new ConditionalScriptActionGroup() {{
                        addRequirement(new CustomRequirement() { @Override
                        public boolean isRequirementMet() {
                            Integer sel = outputManager.getFlagData("TEXTBOX_OPTION_SELECTION");
                            return sel != null && sel == 0;
                        }});
                        addScriptAction(new ScriptAction() {
                            @Override
                            public Level.ScriptState execute() {
                                openDoorVisualsAndCollision();
                                return Level.ScriptState.COMPLETED;
                            }
                        });
                        addScriptAction(new TextboxScriptAction("...Be brave.")); // optional flavor
                    }});

                    // NO branch (index 1)
                    addConditionalScriptActionGroup(new ConditionalScriptActionGroup() {{
                        addRequirement(new CustomRequirement() { @Override
                        public boolean isRequirementMet() {
                            Integer sel = outputManager.getFlagData("TEXTBOX_OPTION_SELECTION");
                            return sel != null && sel == 1;
                        }});
                        addScriptAction(new TextboxScriptAction("Maybe later."));
                    }});
                }});

                actions.add(new UnlockPlayerScriptAction());
                return actions;
            }
        });
    }

    /** Configure which map & where this door should send the player (tile coordinates). */
    public BossDoor toMap(String mapName, int spawnTileX, int spawnTileY) {
        this.targetMapName = mapName;
        this.spawnTileX = spawnTileX;
        this.spawnTileY = spawnTileY;
        return this;
    }

    /** If your map uses a different rendered tile size, set it here. */
    public BossDoor withTileSizePixels(int tileW, int tileH) {
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

        doorObj = new GameObject(x, y - 16, closedFrame); // lift by 16px so base sits on floor
        return doorObj;
    }

    @Override
    public void update(Player player) {
        super.update(player);
        if (player == null) return;

        if (isOpen) {
            if (teleportCooldown > 0) teleportCooldown--;

            boolean confirm = isConfirmDown(); // E or SPACE
            boolean near = nearDoor(player);

            if (confirm && near && teleportCooldown == 0) {
                if (targetMapName != null) {
                    int spawnPx = spawnTileX * tileW + 6;           // gentle offset
                    int spawnPy = spawnTileY * tileH + tileH + 6;   // land just below the door
                    queueMapChange(targetMapName, new Point(spawnPx, spawnPy));
                    teleportCooldown = TELEPORT_COOLDOWN_FRAMES;
                } else {
                    System.out.println("[BossDoor] WARN: no targetMapName set on this door!");
                    teleportCooldown = 8;
                }
            }
        }
    }

    // ===== Door open visuals/collision =====
    private void openDoorVisualsAndCollision() {
        if (isOpen) return;
        isOpen = true;
        setIsUncollidable(true); // allow player to pass

        try {
            boolean swapped = setGameObjectFrame(doorObj, openFrame);
            System.out.println("[BossDoor] Door opened; swapped frame = " + swapped);
        } catch (Throwable th) {
            System.out.println("[BossDoor] WARN: frame swap failed: " + th);
        }

        try {
            AudioPlayer.playSound("Resources/audio/Door_Open.wav", -6.0f);
        } catch (Throwable s) {
            System.out.println("[BossDoor] Could not play Door_Open.wav: " + s);
        }
    }

    // ===== Map/Textbox helpers via reflection (no getMap() dependency) =====

    private Map currentMap() {
        try {
            Class<?> cls = this.getClass();
            while (cls != null) {
                try {
                    Field f = cls.getDeclaredField("map");
                    f.setAccessible(true);
                    Object m = f.get(this);
                    if (m instanceof Map) return (Map) m;
                } catch (NoSuchFieldException ignored) {}
                cls = cls.getSuperclass();
            }
        } catch (Throwable ignored) {}
        return null;
    }

    @SuppressWarnings("unused")
    private Textbox resolveTextbox() {
        Map m = currentMap();
        return (m != null) ? m.getTextbox() : null;
    }

    // ===== Teleport + proximity =====

    private void queueMapChange(String mapName, Point spawnPixels) {
        try {
            Screens.PlayLevelScreen.queueMapChange(mapName, spawnPixels);
            System.out.println("[BossDoor] queued map change to " + mapName +
                    " spawn " + spawnPixels.x + "," + spawnPixels.y);
        } catch (Throwable t) {
            System.out.println("[BossDoor] ERROR: could not queue map change. " + t);
        }
    }

    private boolean isConfirmDown() {
        try {
            Class<?> keyCls = Class.forName("Engine.Key");
            Class<?> kbCls  = Class.forName("Engine.Keyboard");
            Method isDown = kbCls.getMethod("isKeyDown", keyCls);
            Object KEY_E = safeGetKey(keyCls, "E", "e");
            Object KEY_SPACE = safeGetKey(keyCls, "SPACE", null);
            return (KEY_E != null && (boolean) isDown.invoke(null, KEY_E)) ||
                   (KEY_SPACE != null && (boolean) isDown.invoke(null, KEY_SPACE));
        } catch (Throwable ignored) { return true; }
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

    // ===== Reflection utils =====

    private Object safeGetKey(Class<?> keyCls, String primary, String alt) {
        try { return keyCls.getField(primary).get(null); } catch (Throwable ignored) {}
        if (alt != null) {
            try { return keyCls.getField(alt).get(null); } catch (Throwable ignored) {}
        }
        return null;
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
