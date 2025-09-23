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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class Door extends EnhancedMapTile {
    private boolean isOpen = false;

    // The object the engine caches & draws
    private GameObject doorObj;

    // Frames
    private Frame closedFrame;
    private Frame openFrame;

    // Teleport destination & one-shot flag
    private Point afterOpenTarget;
    private boolean hasTeleported = false;

    public Door(Point location) {
        // Start CLOSED and blocking
        super(location.x, location.y,
              new SpriteSheet(ImageLoader.load("door_close.png"), 16, 32),
              TileType.NOT_PASSABLE);
        setIsUncollidable(false); // block until opened

        // Default: one tile inside (above) the doorway
        this.afterOpenTarget = new Point(location.x, location.y - 700);

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

                        // Make walk-through
                        setIsUncollidable(true);

                        // Swap the frame on the SAME bottom-layer object the engine draws
                        boolean swapped = setGameObjectFrame(doorObj, openFrame);
                        System.out.println("[Door] swapped frame on doorObj = " + swapped);

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
        // CLOSED
        closedFrame = new FrameBuilder(sheet.getSubImage(0, 0))
                .withScale(3)
                .withBounds(0, 16, 16, 16) // collide on lower half
                .build();

        // OPEN
        SpriteSheet openSheet = new SpriteSheet(ImageLoader.load("DoorOpen.png"), 16, 32);
        openFrame = new FrameBuilder(openSheet.getSubImage(0, 0))
                .withScale(3)
                .withBounds(0, 16, 16, 16)
                .build();

        // Lift sprite 16px so it sits on ground
        doorObj = new GameObject(x, y - 16, closedFrame);
        return doorObj; // engine caches & draws this
    }

    @Override
    public void update(Player player) {
        super.update(player);

        // After opening, teleport once when player steps into the doorway
        if (isOpen && !hasTeleported && player != null) {
            if (insidePortal(player)) {
                if (movePlayerTo(player, afterOpenTarget)) {
                    hasTeleported = true;
                }
            }
        }
    }

    // ---------------- helpers ----------------

    /** Robust "inside doorway" test: try multiple overlap strategies. */
    private boolean insidePortal(Player player) {
        // (1) If Player has intersects(GameObject), try with the doorObj
        try {
            Method m = player.getClass().getMethod("intersects", GameObject.class);
            boolean b = (boolean) m.invoke(player, doorObj);
            if (b) return true;
        } catch (Throwable ignored) {}

        // (2) If Player has intersects(MapEntity), try with "this"
        try {
            Method m = player.getClass().getMethod("intersects", MapEntity.class);
            boolean b = (boolean) m.invoke(player, this);
            if (b) return true;
        } catch (Throwable ignored) {}

        // (3) Coordinate check: is player's center inside the door's 16x32 area?
        float px = getNumeric(player, "getX", "x");
        float py = getNumeric(player, "getY", "y");
        // Door rectangle: x..x+16, (y-16)..(y+16) because sprite is lifted by 16
        float left = this.x;
        float right = this.x + 16;
        float top = this.y - 16;
        float bottom = this.y + 16;

        return (px >= left && px <= right && py >= top && py <= bottom);
    }

    /** Move the player to pixel point p. Tries many common signatures, then fields. */
    private boolean movePlayerTo(Player player, Point p) {
        // Methods first
        if (tryInvoke(player, "setLocation", Point.class, p)) return true;
        if (tryInvoke2(player, "setLocation", p.x, p.y))     return true;
        if (tryInvoke2(player, "setPosition", p.x, p.y))     return true;
        if (tryInvoke2(player, "teleportTo",  p.x, p.y))     return true;
        if (tryInvoke1(player, "setX", p.x) & tryInvoke1(player, "setY", p.y)) return true;

        // Fallback: fields named x/y
        boolean xSet = trySetField(player, "x", p.x);
        boolean ySet = trySetField(player, "y", p.y);
        return xSet && ySet;
    }

    private boolean tryInvoke(Player obj, String name, Class<?> pType, Object arg) {
        try {
            Method m = obj.getClass().getMethod(name, pType);
            m.invoke(obj, arg);
            return true;
        } catch (Throwable t) { return false; }
    }

    private boolean tryInvoke1(Player obj, String name, float v) {
        try {
            Method m = obj.getClass().getMethod(name, float.class);
            m.invoke(obj, v);
            return true;
        } catch (Throwable t1) {
            try {
                Method m = obj.getClass().getMethod(name, int.class);
                m.invoke(obj, (int) v);
                return true;
            } catch (Throwable t2) { return false; }
        }
    }

    private boolean tryInvoke2(Player obj, String name, float x, float y) {
        // float,float
        try {
            Method m = obj.getClass().getMethod(name, float.class, float.class);
            m.invoke(obj, x, y);
            return true;
        } catch (Throwable t1) {
            // int,int
            try {
                Method m = obj.getClass().getMethod(name, int.class, int.class);
                m.invoke(obj, (int) x, (int) y);
                return true;
            } catch (Throwable t2) { return false; }
        }
    }

    private boolean trySetField(Object obj, String fieldName, float val) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            if (f.getType() == float.class || f.getType() == Float.class) {
                f.setFloat(obj, val);
            } else if (f.getType() == int.class || f.getType() == Integer.class) {
                f.setInt(obj, (int) val);
            } else {
                return false;
            }
            return true;
        } catch (Throwable t) { return false; }
    }

    private float getNumeric(Object obj, String getter, String field) {
        try {
            Method m = obj.getClass().getMethod(getter);
            Object r = m.invoke(obj);
            if (r instanceof Number) return ((Number) r).floatValue();
        } catch (Throwable ignored) {}
        try {
            Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            Object r = f.get(obj);
            if (r instanceof Number) return ((Number) r).floatValue();
        } catch (Throwable ignored) {}
        return 0f;
    }

    /** Swap the Frame stored inside a GameObject via reflection. */
    private boolean setGameObjectFrame(GameObject obj, Frame newFrame) {
        if (obj == null || newFrame == null) return false;
        Class<?> c = obj.getClass();
        try {
            // common names
            if (trySetFrameField(obj, c, "currentFrame", newFrame)) return true;
            if (trySetFrameField(obj, c, "frame",        newFrame)) return true;

            // any Frame field up the hierarchy
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
        try {
            Field f = cls.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(obj, value);
            return true;
        } catch (Throwable ignored) { return false; }
    }
}
