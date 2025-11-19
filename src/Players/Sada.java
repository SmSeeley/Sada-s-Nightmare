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
    private String currentWeapon = ""; // Track which specific weapon is equipped
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

   // Handle shooting when player has archer's bow specifically
   if (hasBow && "archersbow".equals(currentWeapon) && isInShootingState()) {
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
        // Start from Sada's bounds center (world space)
        var b = getBounds();

        // Start at Sada's center:
        float arrowX = b.getX() + b.getWidth() / 2;
        float arrowY = b.getY() + b.getHeight() / 2;

        String anim = getCurrentAnimationName().toLowerCase();

        // Adjust arrow spawn depending on shooting direction
        if (anim.contains("right")) {
            arrowX = b.getX() + b.getWidth() + 6;   // spawn slightly to right
            arrowY = b.getY() - 11;                  // raise arrow a bit
        } 
        else if (anim.contains("left")) {
            arrowX = b.getX() - 6;                  // left of Sada
            arrowY = b.getY() - 11;                  // raise arrow a bit
        } 
        else if (anim.contains("up")) {
            arrowX = b.getX() + 15;                 // slightly right to match bow sprite
            arrowY = b.getY() - 20;                  // above her head
        } 
        else if (anim.contains("down")) {
            arrowX = b.getX() + 15;                 // slight right
            arrowY = b.getY() + b.getHeight() + 5;  // below Sada
        }

        // Create arrow
        Arrow arrow = new Arrow(arrowX, arrowY, anim);
        //give arrows the same map as sada
        arrow.setMap(this.map);

        arrows.add(arrow);

        System.out.println("Arrow shot! Direction: " + anim +
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
        equipWeapon("slimehammer");
    }

    // New method to equip any weapon by name
    public void equipWeapon(String weaponType) {
        if (hasSword) return; // Already has a weapon equipped
        hasSword = true;

        String spriteFileName;
        switch (weaponType.toLowerCase()) {
            case "angelsword":
                spriteFileName = "sada-angelSword.png";
                break;
            case "watermelon":
                spriteFileName = "sada-watermelon.png";
                break;
            case "slimehammer":
            default:
                spriteFileName = "Sada-slimehammer.png";
                break;
        }

        try {
            // swap to weapon spritesheet
            SpriteSheet newSheet = new SpriteSheet(ImageLoader.load(spriteFileName), 24, 24);

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
        equipBowWeapon("archersbow");
    }

    // New method to equip any bow-type weapon by name
    public void equipBowWeapon(String weaponType) {
        if (hasBow) return; // Already has a bow-type weapon equipped
        hasBow = true;

        String spriteFileName;
        switch (weaponType.toLowerCase()) {
            case "angelsword":
                spriteFileName = "sada-angelSword.png";
                break;
            case "watermelon":
                spriteFileName = "sada-watermelon.png";
                break;
            case "archersbow":
            default:
                spriteFileName = "sada-ArchersBow.png";
                break;
        }

        try {
            // swap to weapon spritesheet
            SpriteSheet newSheet = new SpriteSheet(ImageLoader.load(spriteFileName), 24, 24);

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

    // ===== Universal weapon equip method =====
    @Override
    public void setHasWeapon(String weaponName) {
        String spriteFileName;
        boolean isSwordType = false;
        
        switch (weaponName.toLowerCase()) {
            case "slimehammer":
                spriteFileName = "Sada-slimehammer.png";
                isSwordType = true;
                break;
            case "angelsword":
                spriteFileName = "sada-angelSword.png";
                isSwordType = false;
                break;
            case "watermelon":
                spriteFileName = "sada-watermelon.png";
                isSwordType = false;
                break;
            case "archersbow":
                spriteFileName = "sada-ArchersBow.png";
                isSwordType = false;
                break;
            default:
                System.out.println("Unknown weapon: " + weaponName);
                return;
        }

        // Check if already equipped
        if (isSwordType && hasSword) return;
        if (!isSwordType && hasBow) return;

        // Set the appropriate flag and track current weapon
        if (isSwordType) {
            hasSword = true;
        } else {
            hasBow = true;
        }
        currentWeapon = weaponName.toLowerCase(); // Store the weapon name

        try {
            // Swap to weapon spritesheet
            SpriteSheet newSheet = new SpriteSheet(ImageLoader.load(spriteFileName), 24, 24);

            // Set spriteSheet on superclass chain
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

            // Rebuild animations and set on superclass
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

            // Reset to safe default animation
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
            System.out.println("Failed to equip weapon sprite: " + spriteFileName);
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
