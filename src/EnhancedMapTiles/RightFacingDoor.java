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

/*
 * This file creates an enhanced map object "Door" that allows users to open the door and enter, teleporting them to a set location.
 */

public class RightFacingDoor extends EnhancedMapTile {
    private boolean isOpen = false;

    // The object the engine will draw
    private GameObject doorObj;

    // Frames
    private Frame closedFrame;
    private Frame openFrame;

    // Teleport destination
    private Point afterOpenTarget;
    private boolean hasTeleported = false;

    public RightFacingDoor(Point location) {
        // load image and assign image accurate width and height, update variables to change it.
        super(location.x, location.y,
              new SpriteSheet(ImageLoader.load("door_close_right.png"), 32, 16),
              TileType.NOT_PASSABLE);
        setIsUncollidable(false); // The door will remained blocked until it is opened, change to true to make the door passable.

        // Default: one tile inside (above) the doorway, use this so that the door image appears above the ground and doesnt "sink" into the floor
        this.afterOpenTarget = new Point(location.x + 700, location.y);

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
                        //print in command line to track program
                        System.out.println("[Door] opening...");
                        isOpen = true;

                        // Make walk-through once the door is opened. 
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
                .withBounds(0, 0, 32, 16) // collide on lower half
                .build();

        // OPEN
        SpriteSheet openSheet = new SpriteSheet(ImageLoader.load("DoorOpen_Side.png"), 32, 16);
        openFrame = new FrameBuilder(openSheet.getSubImage(0, 0))
                .withScale(3)
                .withBounds(0, 0, 32, 16)
                .build();

        // Lift sprite 16px so it sits on ground
        doorObj = new GameObject(x - 16, y, closedFrame);
        return doorObj; // engine caches & draws this


        //assign for bottom frame of the door object
    }

    @Override
    public void update(Player player) {
        super.update(player);

        // After opening, teleport once when player steps into the doorway, takes a second for user to teleport once entering blank doorway.
        if (isOpen && !hasTeleported && player != null) {
            if (insidePortal(player)) {
                if (movePlayerTo(player, afterOpenTarget)) {
                    hasTeleported = true;
                }
            }
        }
    }

    // helper methods for teleporting the player to set location

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
        // Door rectangle: x..x+16, (y-16)..(y+16) because sprite is lifted by 16, must account for this. 
        float left = this.x;
        float right = this.x + 32;
        float top = this.y;
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

    /** Swap the Frame stored inside a GameObject using reflection. */
    private boolean setGameObjectFrame(GameObject obj, Frame newFrame) {
        if (obj == null || newFrame == null) return false;
        Class<?> c = obj.getClass();
        try {
            // currentFrame, and frame
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
    // Allows another class to assign where this door should teleport the player
    public void setDestination(Point p) {
        this.afterOpenTarget = p;
        this.hasTeleported = false; // reset so the teleport works again if reused
    }
}
