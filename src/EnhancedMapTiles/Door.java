package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Scripts.*;
import Level.*;
import ScriptActions.*;
import Utils.Point;
import EnhancedMapTiles.key;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;


/*
 * Door that opens via interact script.
 * AFTER opening, pressing E (or SPACE) will teleport you ANY TIME, repeatedly.
 * - No "one-time" limit.
 */
public class Door extends EnhancedMapTile {

    private static final boolean ALWAYS_TELEPORT_ON_KEY = false; 
    private static final int TELEPORT_COOLDOWN_FRAMES = 12;    
    

    private static final float BAND_LEFT_PAD   = 40f;
    private static final float BAND_RIGHT_PAD  = 56f;  
    private static final float BAND_TOP_PAD    = 56f;
    private static final float BAND_BOTTOM_PAD = 56f;


    private boolean isOpen = false;
    private int teleportCooldown = 0;

 
    private GameObject doorObj;
    private Frame closedFrame;
    private Frame openFrame;


    private Point baseDestination;
    private Point afterOpenTarget;

    private int lastTileW = 48;
    private int lastTileH = 48;


    private int arrivalDx = 6;       // px to the right from door tile left
    private int arrivalDyExtra = 6;  // extra px below one full tile

    public Door(Point location) {
        super(location.x, location.y,
              new SpriteSheet(ImageLoader.load("door_close.png"), 16, 32),
              TileType.NOT_PASSABLE);

        // door blocks until opened
        setIsUncollidable(true);

        this.baseDestination = new Point(location.x, location.y);
        this.afterOpenTarget = computeArrivalFromBase(baseDestination);

        setInteractScript(new Script() {
            @Override
            public ArrayList<ScriptAction> loadScriptActions() {
                ArrayList<ScriptAction> actions = new ArrayList<>();
                if (isOpen) return actions;

                actions.add(new LockPlayerScriptAction());

                TextboxScriptAction t = new TextboxScriptAction();
                t.addText("Open sesame!");
                actions.add(t);

                actions.add(new ScriptAction() {
                    @Override
                    public Level.ScriptState execute() {
                        System.out.println("[Door] opening...");
                        isOpen = true;
                        setIsUncollidable(true);
                        teleportCooldown = 0; // ready immediately

                        try {
                            boolean swapped = setGameObjectFrame(doorObj, openFrame);
                            System.out.println("[Door] swapped frame on doorObj = " + swapped);
                        } catch (Throwable th) {
                            System.out.println("[Door] WARN: frame swap failed: " + th);
                        }
                        return Level.ScriptState.COMPLETED;
                    }
                });

                actions.add(new UnlockPlayerScriptAction());
                return actions;
            }
        });
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet sheet) {
        closedFrame = new FrameBuilder(sheet.getSubImage(0, 0))
                .withScale(3)
                .withBounds(0, 16, 16, 16) // collide on lower half
                .build();

        SpriteSheet openSheet = new SpriteSheet(ImageLoader.load("DoorOpen.png"), 16, 32);
        openFrame = new FrameBuilder(openSheet.getSubImage(0, 0))
                .withScale(3)
                .withBounds(0, 16, 16, 16)
                .build();

        // lift 16px so it sits on the floor visually
        doorObj = new GameObject(x, y - 16, closedFrame);
        return doorObj;
    }

    @Override
    public void update(Player player) {
        super.update(player);
        if (player == null || !isOpen) return;

        if (teleportCooldown > 0) teleportCooldown--;

        boolean confirm = isConfirmDown(); // E or SPACE (or true if keys unavailable)
        boolean near = ALWAYS_TELEPORT_ON_KEY || nearDoor(player);

        if (confirm && near && teleportCooldown == 0) {
            Point dest = (afterOpenTarget != null) ? afterOpenTarget : computeArrivalFromBase(baseDestination);
            System.out.println("[Door] Teleport attempt to " + dest.x + "," + dest.y);
            if (movePlayerTo(player, dest)) {
                System.out.println("[Door] Teleport SUCCESS");
                teleportCooldown = TELEPORT_COOLDOWN_FRAMES;
            } else {
                System.out.println("[Door] Teleport FAILED (no matching setter/fields on Player)");
                teleportCooldown = 6; // minimal debounce to avoid spam logs
            }
        }
    }


    public void setDestination(Point doorTileTopLeftPixels) {
        this.baseDestination = doorTileTopLeftPixels;
        this.afterOpenTarget = computeArrivalFromBase(baseDestination);
        System.out.println("[Door] setDestination base(pixels) = " + baseDestination.x + "," + baseDestination.y
                + " -> arrival " + afterOpenTarget.x + "," + afterOpenTarget.y);
    }

    public void setDestinationTiles(int tileX, int tileY) {
        setDestinationTiles(tileX, tileY, lastTileW, lastTileH);
    }

    public void setDestinationTiles(int tileX, int tileY, int tileW, int tileH) {
        this.lastTileW = Math.max(1, tileW);
        this.lastTileH = Math.max(1, tileH);
        int px = tileX * lastTileW;
        int py = tileY * lastTileH;
        this.baseDestination = new Point(px, py);
        this.afterOpenTarget = computeArrivalFromBase(baseDestination);
        System.out.println("[Door] setDestinationTiles tiles(" + tileX + "," + tileY + "), tileSize(" + lastTileW + "x" + lastTileH + ")"
                + " -> base " + px + "," + py + " -> arrival " + afterOpenTarget.x + "," + afterOpenTarget.y);
    }

    public void setArrivalOffset(int dx, int dyExtra) {
        this.arrivalDx = dx;
        this.arrivalDyExtra = dyExtra;
        this.afterOpenTarget = computeArrivalFromBase(baseDestination);
        System.out.println("[Door] setArrivalOffset dx=" + dx + " dyExtra=" + dyExtra
                + " -> arrival " + afterOpenTarget.x + "," + afterOpenTarget.y);
    }


    private Point computeArrivalFromBase(Point base) {
        int ax = (int) base.x + arrivalDx;
        int ay = (int) base.y + lastTileH + arrivalDyExtra; // one tile below + a few px
        if (ax < 0) ax = 0;
        if (ay < 0) ay = 0;
        return new Point(ax, ay);
    }

    // E/SPACE detection via reflection; auto-true if not available
    private boolean isConfirmDown() {
        try {
            Class<?> keyCls = Class.forName("Engine.Key");
            Class<?> kbCls  = Class.forName("Engine.Keyboard");
            Object KEY_E = keyCls.getField("E").get(null);
            Object KEY_SPACE = keyCls.getField("SPACE").get(null);
            Method isDown = kbCls.getMethod("isKeyDown", keyCls);
            boolean e = (boolean) isDown.invoke(null, KEY_E);
            boolean sp = (boolean) isDown.invoke(null, KEY_SPACE);
            return e || sp;
        } catch (Throwable ignored) {
            return true; 
        }
    }

    // Generous proximity check so you don’t need to perfectly overlap the door
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

    private boolean movePlayerTo(Player player, Point p) {
        if (tryInvoke(player, "setLocation", Point.class, p)) return true;
        if (tryInvoke2(player, "setLocation", p.x, p.y))     return true;
        if (tryInvoke2(player, "setPosition", p.x, p.y))     return true;
        if (tryInvoke2(player, "teleportTo",  p.x, p.y))     return true;
        if (tryInvoke1(player, "setX", p.x) & tryInvoke1(player, "setY", p.y)) return true;

        boolean xSet = trySetField(player, "x", p.x);
        boolean ySet = trySetField(player, "y", p.y);
        if (xSet && ySet) return true;

        boolean mx = trySetField(player, "mapX", p.x);
        boolean my = trySetField(player, "mapY", p.y);
        return mx && my;
    }

    private boolean tryInvoke(Player obj, String name, Class<?> pType, Object arg) {
        try { Method m = obj.getClass().getMethod(name, pType); m.invoke(obj, arg); return true; }
        catch (Throwable t) { return false; }
    }
    private boolean tryInvoke1(Player obj, String name, float v) {
        try { Method m = obj.getClass().getMethod(name, float.class); m.invoke(obj, v); return true; }
        catch (Throwable t1) {
            try { Method m = obj.getClass().getMethod(name, int.class); m.invoke(obj, (int) v); return true; }
            catch (Throwable t2) { return false; }
        }
    }
    private boolean tryInvoke2(Player obj, String name, float x, float y) {
        try { Method m = obj.getClass().getMethod(name, float.class, float.class); m.invoke(obj, x, y); return true; }
        catch (Throwable t1) {
            try { Method m = obj.getClass().getMethod(name, int.class, int.class); m.invoke(obj, (int)x, (int)y); return true; }
            catch (Throwable t2) { return false; }
        }
    }
    private boolean trySetField(Object obj, String fieldName, float val) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            if (f.getType() == float.class || f.getType() == Float.class)      f.setFloat(obj, val);
            else if (f.getType() == int.class || f.getType() == Integer.class) f.setInt(obj, (int) val);
            else return false;
            return true;
        } catch (Throwable t) { return false; }
    }
    private float getNumeric(Object obj, String getter, String field) {
        try { Method m = obj.getClass().getMethod(getter); Object r = m.invoke(obj); if (r instanceof Number) return ((Number) r).floatValue(); }
        catch (Throwable ignored) {}
        try { Field f = obj.getClass().getDeclaredField(field); f.setAccessible(true); Object r = f.get(obj); if (r instanceof Number) return ((Number) r).floatValue(); }
        catch (Throwable ignored) {}
        return 0f;
    }

    /** Swap the Frame stored inside a GameObject. */
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
}
