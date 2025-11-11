package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Level.*;
import ScriptActions.*;
import Utils.Point;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Locked door that requires at least 1 key to open.
 * - If keys <= 0: shows "It's locked. You need a key." for 2 seconds.
 * - If keys >= 1: consumes 1 key, opens (becomes passable), and allows teleport.
 */
public class LockedDreamDoor extends EnhancedMapTile {

    private static final int TELEPORT_COOLDOWN_FRAMES = 12;
    private static final float BAND_LEFT_PAD = 40f;
    private static final float BAND_RIGHT_PAD = 56f;
    private static final float BAND_TOP_PAD = 56f;
    private static final float BAND_BOTTOM_PAD = 56f;

    private boolean isOpen = false;
    private int teleportCooldown = 0;

    private GameObject doorObj;
    private Frame closedFrame;
    private Frame openFrame;

    private String targetMapName = null;
    private int spawnTileX = 0, spawnTileY = 0;
    private int tileW = 48, tileH = 48;

    private int arrivalDx = 6;
    private int arrivalDyExtra = 6;

    public LockedDreamDoor(Point location) {
        super(location.x, location.y,
                new SpriteSheet(ImageLoader.load("LockedDreamDoor.png"), 16, 32),
                TileType.NOT_PASSABLE);

        setIsUncollidable(false);

        // Interact script — dynamic key check
        setInteractScript(new Script() {
            @Override
            public ArrayList<ScriptAction> loadScriptActions() {
                ArrayList<ScriptAction> actions = new ArrayList<>();
                actions.add(new LockPlayerScriptAction());

                actions.add(new ScriptAction() {
                    @Override
                    public Level.ScriptState execute() {
                        if (isOpen) return Level.ScriptState.COMPLETED;

                        int keys = DoorKey.keysCollected;
                        System.out.println("[Door] Interact -> keysCollected=" + keys);

                        if (keys <= 0) {
                            System.out.println("[Door] Locked, showing textbox");

                            try {
                                if (getMap() != null && getMap().getTextbox() != null) {
                                    Textbox tb = getMap().getTextbox();
                                    tb.setIsActive(true);
                                    tb.addText("It's locked. You need a key.");

                                    // Hide textbox after 2 seconds
                                    new Thread(() -> {
                                        try {
                                            Thread.sleep(2000);
                                        } catch (InterruptedException ignored) {}
                                        tb.setIsActive(false);
                                    }).start();
                                } else {
                                    System.out.println("[Door] It's locked. You need a key.");
                                }
                            } catch (Throwable t) {
                                System.out.println("[Door] WARN could not show textbox: " + t);
                            }

                            return Level.ScriptState.COMPLETED;
                        }

                        // Has key -> consume and open
                        System.out.println("[Door] Opening. Keys before: " + keys);
                        DoorKey.keysCollected = Math.max(0, keys - 1);
                        System.out.println("[Door] Keys after: " + DoorKey.keysCollected);

                        isOpen = true;
                        setIsUncollidable(true);
                        teleportCooldown = 0;

                        try {
                            setGameObjectFrame(doorObj, openFrame);
                        } catch (Throwable ignored) {}

                        return Level.ScriptState.COMPLETED;
                    }
                });

                actions.add(new UnlockPlayerScriptAction());
                return actions;
            }
        });
    }

    public LockedDreamDoor toMap(String mapName, int spawnTileX, int spawnTileY) {
        this.targetMapName = mapName;
        this.spawnTileX = spawnTileX;
        this.spawnTileY = spawnTileY;
        return this;
    }

    public LockedDreamDoor withTileSizePixels(int tileW, int tileH) {
        this.tileW = tileW;
        this.tileH = tileH;
        return this;
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet sheet) {
        closedFrame = new FrameBuilder(sheet.getSubImage(0, 0))
                .withScale(3)
                .withBounds(0, 16, 16, 16)
                .build();

        SpriteSheet openSheet = new SpriteSheet(ImageLoader.load("DoorOpen.png"), 16, 32);
        openFrame = new FrameBuilder(openSheet.getSubImage(0, 0))
                .withScale(3)
                .withBounds(0, 16, 16, 16)
                .build();

        doorObj = new GameObject(x, y - 16, closedFrame);
        return doorObj;
    }

    @Override
    public void update(Player player) {
        super.update(player);
        if (player == null || !isOpen) return;

        if (teleportCooldown > 0) teleportCooldown--;

        boolean confirm = isConfirmDown();
        boolean near = nearDoor(player);

        if (confirm && near && teleportCooldown == 0) {
            if (targetMapName != null) {
                int spawnPx = spawnTileX * tileW + arrivalDx;
                int spawnPy = spawnTileY * tileH + tileH + arrivalDyExtra;
                Point spawn = new Point(spawnPx, spawnPy);
                queueMapChange(targetMapName, spawn);
                teleportCooldown = TELEPORT_COOLDOWN_FRAMES;
            } else {
                System.out.println("[Door] WARN: no targetMapName set!");
                teleportCooldown = 8;
            }
        }
    }

    private void queueMapChange(String mapName, Point spawnPixels) {
        try {
            Screens.PlayLevelScreen.queueMapChange(mapName, spawnPixels);
        } catch (Throwable t) {
            System.out.println("[Door] ERROR queueing map change: " + t);
        }
    }

    private boolean isConfirmDown() {
        try {
            Class<?> keyCls = Class.forName("Engine.Key");
            Class<?> kbCls = Class.forName("Engine.Keyboard");

            Object KEY_E = null;
            try { KEY_E = keyCls.getField("E").get(null); } catch (Throwable ignored) {}
            if (KEY_E == null) { try { KEY_E = keyCls.getField("e").get(null); } catch (Throwable ignored) {} }

            Object KEY_SPACE = null;
            try { KEY_SPACE = keyCls.getField("SPACE").get(null); } catch (Throwable ignored) {}

            Method isDown = kbCls.getMethod("isKeyDown", keyCls);
            boolean e = (KEY_E != null) && (boolean) isDown.invoke(null, KEY_E);
            boolean sp = (KEY_SPACE != null) && (boolean) isDown.invoke(null, KEY_SPACE);
            return e || sp;
        } catch (Throwable ignored) {
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

        float left = this.x - BAND_LEFT_PAD;
        float right = this.x + BAND_RIGHT_PAD;
        float top = this.y - BAND_TOP_PAD;
        float bottom = this.y + BAND_BOTTOM_PAD;

        return (cx >= left && cx <= right && cy >= top && cy <= bottom);
    }

    private boolean setGameObjectFrame(GameObject obj, Frame newFrame) {
        if (obj == null || newFrame == null) return false;
        Class<?> c = obj.getClass();
        try {
            if (trySetFrameField(obj, c, "currentFrame", newFrame)) return true;
            if (trySetFrameField(obj, c, "frame", newFrame)) return true;
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
