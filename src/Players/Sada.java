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
import java.util.HashMap;
import java.lang.reflect.Field;
import java.util.Random;

// Sada player character
public class Sada extends Player {

    private boolean hasSword = false;
    private boolean hasBow = false;
    private String currentWeapon = ""; // Track which specific weapon is equipped


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

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
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

    // ===== Get current weapon damage =====
    @Override
    public int getWeaponDamage() {
        switch (currentWeapon.toLowerCase()) {
            case "slimehammer":
                return EnhancedMapTiles.Sword.getSwordDamage(); // 2 damage
            case "angelsword":
                return EnhancedMapTiles.AngelSword.getAngelSwordDamage(); // 10 damage
            case "watermelon":
                return EnhancedMapTiles.Watermelon.getWatermelonDamage(); // 1 damage
            default:
                return 1; // Default fist damage
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
