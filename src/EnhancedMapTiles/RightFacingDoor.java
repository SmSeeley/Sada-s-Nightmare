package EnhancedMapTiles;

import Engine.ImageLoader;
import GameObject.Frame;
import Builders.FrameBuilder;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Level.Player;
import Scripts.*;
import Utils.Point;

public class RightFacingDoor extends Door {
    public RightFacingDoor(Point location) {
        super(location);
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet ignoredSheet) {
        // CLOSED frame (right-facing)
        SpriteSheet closedSheet = new SpriteSheet(ImageLoader.load("door_close_right.png"), 32, 16);
        Frame closedFrame = new FrameBuilder(closedSheet.getSubImage(0, 0))
                .withScale(3)
                .withBounds(0, 16, 16, 16)
                .build();

        // OPEN frame (right-facing)
        SpriteSheet openSheet = new SpriteSheet(ImageLoader.load("DoorOpen_Side.png"), 32, 16);
        Frame openFrame = new FrameBuilder(openSheet.getSubImage(0, 0))
                .withScale(3)
                .withBounds(0, 16, 16, 16)
                .build();

        // Store open frame in parent class so the door can swap it later
        try {
            java.lang.reflect.Field openFrameField = Door.class.getDeclaredField("openFrame");
            openFrameField.setAccessible(true);
            openFrameField.set(this, openFrame);
        } catch (Exception e) {
            e.printStackTrace();
        }

        GameObject doorObj = new GameObject(x - 16, y, closedFrame);

        // Store doorObj in parent class so the engine draws it
        try {
            java.lang.reflect.Field doorObjField = Door.class.getDeclaredField("doorObj");
            doorObjField.setAccessible(true);
            doorObjField.set(this, doorObj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return doorObj;
    }
}