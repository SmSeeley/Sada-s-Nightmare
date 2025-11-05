package Players;

import Builders.FrameBuilder;
import Engine.AudioPlayer;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.Key;
import Engine.Keyboard;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Player;
import java.util.ArrayList;
import Level.Arrow;
import Engine.Keyboard;
import Engine.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.reflect.Field;
import java.util.Random;

// Sada player character
public class Sada extends Player {

    private boolean hasSword = false;
    private boolean hasBow = false;
    private ArrayList<Arrow> arrows = new ArrayList<>(); // Track active arrows
    private long lastArrowTime = 0; // Rate limiting
    private static final long ARROW_COOLDOWN = 1000; // 1000ms between arrows


    // --- swing SFX state ---
    private String lastAnimName = "";
    private long lastSwingSfxTimeMs = 0;
    private static final long SWING_SFX_COOLDOWN_MS = 120; // prevents double-fires if animation flickers
    private final Random rng = new Random();

    // --- footsteps SFX state ---
    private long lastFootstepMs = 0;
    private static final long FOOTSTEP_INTERVAL_MS = 220; // cadence between steps
    private static final String FOOTSTEP_SFX = "Resources/audio/Footsteps.wav";

    public Sada(float x, float y) {
        super(new SpriteSheet(ImageLoader.load("Sada.png"), 24, 24), x, y, "STAND_RIGHT");
        walkSpeed = 2.3f;
    }

    @Override
    public void update() {
        // Run base update first (so current animation reflects latest inputs)
        super.update();

        // Update all active arrows
   for (int i = arrows.size() - 1; i >= 0; i--) {
       Arrow arrow = arrows.get(i);
       arrow.update();

       // Remove arrows that are off-screen or hit something
       if (arrow.shouldRemove()) {
           arrows.remove(i);
       }
   }

   // Handle shooting when player has bow and is in shooting state
   if (hasBow && isInShootingState()) {
       long currentTime = System.currentTimeMillis();
       if (currentTime - lastArrowTime >= ARROW_COOLDOWN) {
           shootArrow();
           lastArrowTime = currentTime;
       }
   }

        // Current animation name after base update
        String anim = getCurrentAnimationNameSafe();

        // ---- Attack swing SFX (randomized) ----
        boolean isAttackNow = isAttackAnimation(anim);
        boolean wasAttackBefore = isAttackAnimation(lastAnimName);
        if (isAttackNow && !wasAttackBefore) {
            long now = System.currentTimeMillis();
            if (now - lastSwingSfxTimeMs >= SWING_SFX_COOLDOWN_MS) {
                playRandomSwingSfx();
                lastSwingSfxTimeMs = now;
            }
        }

        // ---- Footstep SFX while walking ----
        if (isWalkingAnimation(anim)) {
            long now = System.currentTimeMillis();
            if (now - lastFootstepMs >= FOOTSTEP_INTERVAL_MS) {
                AudioPlayer.playSound(FOOTSTEP_SFX, -11.0f); // a little quieter; tweak to taste
                lastFootstepMs = now;
            }
        } else {
            // Optional: reset cadence so first step fires immediately on next walk
            lastFootstepMs = System.currentTimeMillis();
        }

        lastAnimName = anim;
    }

    private boolean isInShootingState() {
        // Check if player is pressing arrow keys (for bow shooting)
        return Keyboard.isKeyDown(Key.UP) || Keyboard.isKeyDown(Key.DOWN) ||
               Keyboard.isKeyDown(Key.LEFT) || Keyboard.isKeyDown(Key.RIGHT);
    }

    // Add method to shoot arrows:
    private void shootArrow() {
        try {
            
            float arrowX = this.getX();  // Center arrow horizontally
            float arrowY = this.getY();  // Center arrow vertically
            String direction = getCurrentAnimationName();

            System.out.println("Player actual position: (" + this.getX() + "," + this.getY() + ")");
            System.out.println("Arrow spawn position: (" + arrowX + "," + arrowY + ")");
            // Create arrow based on direction
            Arrow arrow = new Arrow(arrowX, arrowY, direction);
            arrows.add(arrow);

            System.out.println("Arrow shot! Direction: " + direction + 
                           " Position: (" + arrowX + "," + arrowY + ") " +
                           " Total arrows: " + arrows.size());

        } catch (Exception e) {
            System.out.println("Failed to shoot arrow: " + e.getMessage());
            e.printStackTrace();
        }
    }



    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);

        // Draw all active arrows
        for (Arrow arrow : arrows) {
            arrow.draw(graphicsHandler);
        }
    }

    // Add getter for arrows (for collision detection with enemies):
    public ArrayList<Arrow> getArrows() {
        return arrows;
    }

    // ===== Sword equip (kept from your version) =====
    @Override
    public void setHasSword(boolean v) {
        if (!v || hasSword) return;
        hasSword = true;

        try {
            // swap to slimehammer spritesheet
            SpriteSheet newSheet = new SpriteSheet(ImageLoader.load("Sada-slimehammer.png"), 24, 24);

            // set spriteSheet on superclass chain
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

            // rebuild animations and set on superclass
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

            // reset to safe default animation
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

    // ===== Bow equip (similar to sword) =====
    @Override
    public void setHasBow(boolean v) {
        if (!v || hasBow) return;
        hasBow = true;

        try {
            // swap to sada-ArchersBow spritesheet
            SpriteSheet newSheet = new SpriteSheet(ImageLoader.load("sada-ArchersBow.png"), 24, 24);

            // set spriteSheet on superclass chain
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
        

        // rebuild animations and set on superclass
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

        // reset to safe default animation
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
        System.out.println("Failed to equip bow sprite — ensure Sada-ArchersBow.png exists and matches sprite layout.");
    }
}


    // ===== Animations =====
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

            // Treat SHOOT_* as the swing/attack animations
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

    // ===== Helpers =====

    private boolean isAttackAnimation(String animName) {
        if (animName == null) return false;
        // Your attack names are SHOOT_RIGHT/LEFT/UP/DOWN — treat those as swings
        return animName.startsWith("SHOOT_");
    }

    private boolean isWalkingAnimation(String animName) {
        if (animName == null) return false;
        return animName.startsWith("WALK_");
    }

    private void playRandomSwingSfx() {
        int pick = rng.nextInt(3); // 0, 1, or 2
        String file;
        switch (pick) {
            case 0:
                file = "Resources/audio/Sada_2.wav";
                break;
            case 1:
                file = "Resources/audio/Sada_4.wav";
                break;
            default:
                file = "Resources/audio/Sada_5.wav";
                break;
        }
        AudioPlayer.playSound(file, -3.0f);
    }

    private String getCurrentAnimationNameSafe() {
        // Try reflection to read "currentAnimationName" from Player/AnimatedSprite
        Class<?> cls = this.getClass();
        while (cls != null) {
            try {
                Field f = cls.getDeclaredField("currentAnimationName");
                f.setAccessible(true);
                Object v = f.get(this);
                return (v != null) ? v.toString() : "";
            } catch (NoSuchFieldException e) {
                cls = cls.getSuperclass();
            } catch (Exception e) {
                break;
            }
        }
        return "";
    }
}
