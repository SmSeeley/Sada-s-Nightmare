package NPCs;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.NPC;
import Level.Player;
import Players.Sada;
import Utils.Point;
import java.util.HashMap;

// This class is for the walrus NPC
public class greenNinja extends NPC {

    public greenNinja(int id, Point location) {
        super(id, location.x, location.y, new SpriteSheet(ImageLoader.load("newGreenNinja.png"), 24, 24), "STAND_LEFT");
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("STAND_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 0))
                            .withScale(3)
                            .withBounds(200, 5, 11, 7)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .build()
            });
            put("STAND_RIGHT", new Frame[] {
                   new FrameBuilder(spriteSheet.getSprite(0, 0))
                           .withScale(3)
                           .withBounds(7, 13, 11, 7)
                           .build()
           });
        }};
    }

    @Override
    public void update(Player player) {
        super.update(player);
        if (player instanceof Sada) {
            chase((Sada) player);
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
    }
    //Method to have NPC chase Sada
    public void chase(Sada sada) {
    float chaseSpeed = 1.5f; 

    float ninjaX = getX();
    float ninjaY = getY();
    float sadaX = sada.getX();
    float sadaY = sada.getY();

    // Calculate distance in each direction
    float dx = sadaX - ninjaX;
    float dy = sadaY - ninjaY;

    // Stop chasing if they’re touching
    if (Math.abs(dx) < 5 && Math.abs(dy) < 5) {
        currentAnimationName = dx < 0 ? "STAND_LEFT" : "STAND_RIGHT";
        return;
    }

    if (Math.abs(dx) > Math.abs(dy)) {
        // move horizontally
        if (dx > 0) {
            moveXHandleCollision(chaseSpeed);
            currentAnimationName = "STAND_RIGHT";
        } else {
            moveXHandleCollision(-chaseSpeed);
            currentAnimationName = "STAND_LEFT";
        }
        } else {
        // move vertically 
        if (dy > 0) {
            moveYHandleCollision(chaseSpeed);
            } else {
            moveYHandleCollision(-chaseSpeed);
            }
        }
    }
}
