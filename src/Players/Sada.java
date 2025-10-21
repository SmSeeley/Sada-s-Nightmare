package Players;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Player;
import java.util.HashMap;
import java.lang.reflect.Field;
import EnhancedMapTiles.Sword;

// This is the class for the Cat player character
// basically just sets some values for physics and then defines animations
public class Sada extends Player {

    private boolean hasSword = false;

    public Sada(float x, float y) {
        super(new SpriteSheet(ImageLoader.load("Sada.png"), 24, 24), x, y, "STAND_RIGHT");
        walkSpeed = 2.3f;
    }

    public void update() {
        super.update();
    }

    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
    }

    @Override
    public void setHasSword(boolean v) {
        if (!v || hasSword) return;
        hasSword = true;

        try {
            // load new sheet (adjust filename and cell size if needed)
            SpriteSheet newSheet = new SpriteSheet(ImageLoader.load("Sada-slimehammer.png"), 24, 24);

            // set spriteSheet field on superclass chain
            Class<?> cls = this.getClass();
            while (cls != null) {
                try {
                    Field spriteSheetField = cls.getDeclaredField("spriteSheet");
                    spriteSheetField.setAccessible(true);
                    spriteSheetField.set(this, newSheet);
                    break;
                } catch (NoSuchFieldException e) {
                    cls = cls.getSuperclass();
                }
            }

             // rebuild animations using the new sheet and set animations field
            @SuppressWarnings("unchecked")
            HashMap<String, Frame[]> newAnims = this.loadAnimations(newSheet);

            cls = this.getClass();
            while (cls != null) {
                try {
                    Field animationsField = cls.getDeclaredField("animations");
                    animationsField.setAccessible(true);
                    animationsField.set(this, newAnims);
                    break;
                } catch (NoSuchFieldException e) {
                    cls = cls.getSuperclass();
                }
            }
            // reset current animation name to a safe default
            try {
                cls = this.getClass();
                while (cls != null) {
                    try {
                        Field currentAnimField = cls.getDeclaredField("currentAnimationName");
                        currentAnimField.setAccessible(true);
                        currentAnimField.set(this, "STAND_RIGHT");
                        break;
                    } catch (NoSuchFieldException e) {
                        cls = cls.getSuperclass();
                    }
                }
            } catch (Exception ignored) {}

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to equip sword sprite — ensure Sada-slimehammer.png exists and matches sprite layout.");
        }
    }


    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("STAND_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 0))
                            .withScale(3)
                            .withBounds(6, 12, 12, 7)
                            .build()
            });

            put("STAND_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 0))
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(6, 12, 12, 7)
                            .build()
            });

            put("WALK_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(1, 0), 14)
                            .withScale(3)
                            .withBounds(6, 12, 12, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 1), 14)
                            .withScale(3)
                            .withBounds(6, 12, 12, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 2), 14)
                            .withScale(3)
                            .withBounds(6, 12, 12, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 3), 14)
                            .withScale(3)
                            .withBounds(6, 12, 12, 7)
                            .build()
            });

            put("WALK_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(1, 0), 14)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(6, 12, 12, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 1), 14)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(6, 12, 12, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 2), 14)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(6, 12, 12, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 3), 14)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(6, 12, 12, 7)
                            .build()
            });

              put("SHOOT_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 1), 14)
                            .withScale(3)
                            .withBounds(6, 12, 12, 7)
                            .build()
                    
            });

            put("SHOOT_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 1), 14)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(6, 12, 12, 7)
                            .build()
            });

            put("SHOOT_DOWN", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 2), 14)
                            .withScale(3)
                            .withBounds(6, 12, 12, 7)
                            .build()
            });

            put("SHOOT_UP", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 3), 14)
                            .withScale(3)
                            .withBounds(6, 12, 12, 7)
                            .build()
            });
        }};
    }
}

