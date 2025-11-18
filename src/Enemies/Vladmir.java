package Enemies;

import Builders.FrameBuilder;
import Engine.AudioPlayer;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.MapEntityStatus;
import Level.Player;
import Utils.Point;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Final boss: Vladmir
 *
 * Health:
 *  - Uses Enemy's built-in health system with max health 500 (for health bar).
 *
 * Behavior:
 *  - Does NOT deal contact damage (PlayLevelScreen skips Vladmir in touch-damage).
 *  - Fight only starts after startFight() is called.
 *  - When fight starts, he immediately teleports once.
 *  - Then teleports every 10 seconds to one of 4 tiles (never same twice in a row):
 *        (6,10), (14,10), (14,3), (6,3)
 *      On teleport:
 *        * Plays Warp.wav
 *        * Spawns Smoke.png particles at his previous location
 *  - 2 seconds after each teleport, performs ONE random attack:
 *        0: 3-shot spread burst toward Sada
 *        1: Summon 2 Firemonsters at (6,3) and (14,3)
 *        2: "Die-rain" style downward vladballs across columns 4–17
 *        3: Twin bombs at x=7 and x=14 that fall for 2s then explode in a circle of bullets
 *
 *  - Uses NProjectile for all damaging Vlad bullets.
 */
public class Vladmir extends Enemy {

    // -------------------------
    // Fight state
    // -------------------------

    // Fight not active until PlayLevelScreen calls startFight()
    private boolean fightStarted = false;

    // Teleport & attack timings (assuming 60 FPS)
    private static final int TELEPORT_COOLDOWN_FRAMES = 800; // 10 seconds

    private static final int ATTACK_DELAY_FRAMES      = 120; // 2 seconds

    private int teleportTimer    = TELEPORT_COOLDOWN_FRAMES;
    private int attackDelayTimer = -1; // <0 = no attack scheduled

    // Teleport tiles (col, row)
    private static final int[][] TELEPORT_TILES = {
        { 6, 10 },
        {14, 10 },
        {14,  3 },
        { 6,  3 }
    };
    private int lastTeleportIndex = -1;

    private final Random rng = new Random();

    // Vlad’s own projectiles
    private final List<NProjectile> activeProjectiles = new ArrayList<>();

    // Smoke particle effect for teleports
    private static final BufferedImage SMOKE_IMG = ImageLoader.load("Smoke.png");
    private static final BufferedImage Light_IMG = ImageLoader.load("Lightning.png");
    private static final BufferedImage Electric_IMG = ImageLoader.load("Electroball.png");

    private static class SmokeParticle {
        float x;
        float y;
        int lifeFrames;

        SmokeParticle(float x, float y, int lifeFrames) {
            this.x = x;
            this.y = y;
            this.lifeFrames = lifeFrames;
        }
    }

    private final List<SmokeParticle> smokeParticles = new ArrayList<>();

    // -------------------------
    // Bombs for attack #4
    // -------------------------

    private static class Bomb {
        float x;
        float y;
        int lifeFrames;
        float speedY;

        Bomb(float x, float y, int lifeFrames, float speedY) {
            this.x = x;
            this.y = y;
            this.lifeFrames = lifeFrames;
            this.speedY = speedY;
        }
    }

    private final List<Bomb> activeBombs = new ArrayList<>();

    // -------------------------
    // Constructor
    // -------------------------

    public Vladmir(int id, Point location) {
        // 500 health so the health bar is correct
        super(
            id,
            location.x,
            location.y,
            new SpriteSheet(ImageLoader.load("Vladmir.png"), 24, 24),
            "STAND_RIGHT",
            500
        );
    }

    // -------------------------
    // Animations
    // -------------------------

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

            // Shooting poses
            put("SHOOT_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 1), 56)
                    .withScale(3)
                    .withBounds(6, 12, 12, 7)
                    .build()
            });

            put("SHOOT_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 1), 56)
                    .withScale(3)
                    .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .withBounds(6, 12, 12, 7)
                    .build()
            });

            put("SHOOT_DOWN", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 2), 56)
                    .withScale(3)
                    .withBounds(6, 12, 12, 7)
                    .build()
            });

            put("SHOOT_UP", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 3), 56)
                    .withScale(3)
                    .withBounds(6, 12, 12, 7)
                    .build()
            });
        }};
    }

    // -------------------------
    // Fight control
    // -------------------------

    /**
     * Called by PlayLevelScreen AFTER the Fire_5 cutscene finishes.
     * This starts Vladmir's AI and forces a teleport immediately.
     */
    public void startFight() {
        if (!fightStarted) {
            fightStarted = true;
            teleportTimer = 0;      // trigger teleport next update
            attackDelayTimer = -1;  // no attack scheduled yet
        }
    }

    // -------------------------
    // Core update
    // -------------------------

    @Override
    public void update(Player player) {

        // Only run AI if fight has started and enemy still exists
        if (fightStarted && exists()) {
            handleTeleportAndAttack(player);
            updateProjectiles(player);
            updateBombs();
        }

        // Always update smoke (visual only)
        updateSmokeParticles();

        // Maintain animation state, etc.
        super.update(player);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        // Draw Vlad
        super.draw(graphicsHandler);

        // Draw projectiles
        for (NProjectile p : activeProjectiles) {
            p.draw(graphicsHandler);
        }

        // Draw bombs (using Smoke.png as a placeholder visual)
        for (Bomb b : activeBombs) {
            int w = Light_IMG.getWidth() * 2;
            int h = Light_IMG.getHeight() * 2;
            graphicsHandler.drawImage(
                Light_IMG,
                (int) b.x - w / 2,
                (int) b.y - h / 2,
                w,
                h
            );
        }

        // Draw smoke particles
        for (SmokeParticle sp : smokeParticles) {
            int w = SMOKE_IMG.getWidth() * 2;
            int h = SMOKE_IMG.getHeight() * 2;
            graphicsHandler.drawImage(
                SMOKE_IMG,
                (int) sp.x - w / 2,
                (int) sp.y - h / 2,
                w,
                h
            );
        }
    }

    // -------------------------
    // Teleport + attack logic
    // -------------------------

    private void handleTeleportAndAttack(Player player) {
        // Teleport timing
        if (teleportTimer > 0) {
            teleportTimer--;
        }

        if (teleportTimer <= 0) {
            teleportRandomWithSmoke();
            teleportTimer = TELEPORT_COOLDOWN_FRAMES;
            attackDelayTimer = ATTACK_DELAY_FRAMES; // schedule an attack after 2s
        }

        // Attack timing
        if (attackDelayTimer >= 0) {
            attackDelayTimer--;
            if (attackDelayTimer == 0) {
                performRandomAttack(player);
                attackDelayTimer = -1;
            }
        }

        // Always face the player with shoot poses
        facePlayerDirection(player);
    }

    private void facePlayerDirection(Player player) {
        float vladCenterX = getBounds().getX() + getBounds().getWidth() / 2f;
        float vladCenterY = getBounds().getY() + getBounds().getHeight() / 2f;

        float playerCenterX = player.getBounds().getX() + player.getBounds().getWidth() / 2f;
        float playerCenterY = player.getBounds().getY() + player.getBounds().getHeight() / 2f;

        float dx = playerCenterX - vladCenterX;
        float dy = playerCenterY - vladCenterY;

        if (Math.abs(dx) > Math.abs(dy)) {
            currentAnimationName = dx >= 0 ? "SHOOT_RIGHT" : "SHOOT_LEFT";
        } else {
            currentAnimationName = dy >= 0 ? "SHOOT_DOWN" : "SHOOT_UP";
        }
    }

    private void teleportRandomWithSmoke() {
        if (map == null) return;

        // Previous center for smoke
        float oldCenterX = getBounds().getX() + getBounds().getWidth() / 2f;
        float oldCenterY = getBounds().getY() + getBounds().getHeight() / 2f;

        // Spawn smoke around old location
        spawnSmokeBurst(oldCenterX, oldCenterY);

        // Choose a new teleport index different from the last
        int newIndex;
        if (lastTeleportIndex == -1) {
            newIndex = rng.nextInt(TELEPORT_TILES.length);
        } else {
            do {
                newIndex = rng.nextInt(TELEPORT_TILES.length);
            } while (newIndex == lastTeleportIndex && TELEPORT_TILES.length > 1);
        }
        lastTeleportIndex = newIndex;

        int col = TELEPORT_TILES[newIndex][0];
        int row = TELEPORT_TILES[newIndex][1];

        Point loc = map.getMapTile(col, row).getLocation();

        // Move Vlad to target tile
        setX(loc.x);
        setY(loc.y);

        // Teleport sound
        AudioPlayer.playSound("Resources/audio/Warp.wav", -3.0f);
    }

    private void spawnSmokeBurst(float x, float y) {
        // 10 big puffs around old location
        for (int i = 0; i < 10; i++) {
            float offsetX = (rng.nextFloat() - 0.5f) * 40f; // +/- 20
            float offsetY = (rng.nextFloat() - 0.5f) * 40f; // +/- 20
            int life = 30 + rng.nextInt(20);                // 30–50 frames
            smokeParticles.add(new SmokeParticle(x + offsetX, y + offsetY, life));
        }
    }

    private void updateSmokeParticles() {
        for (int i = smokeParticles.size() - 1; i >= 0; i--) {
            SmokeParticle sp = smokeParticles.get(i);
            sp.lifeFrames--;
            if (sp.lifeFrames <= 0) {
                smokeParticles.remove(i);
            }
        }
    }

    // -------------------------
    // Attack selection
    // -------------------------

    private void performRandomAttack(Player player) {
        // 4 attacks: 0 = burst, 1 = summon, 2 = lane rain, 3 = twin bombs
        int attackIndex = rng.nextInt(4);

        switch (attackIndex) {
            case 0:
                performBurstAttack(player);
                break;
            case 1:
                performSummonAttack();
                break;
            case 2:
                performDieRainAttack();
                break;
            case 3:
                performTwinBombAttack();
                break;
        }
    }

    // -------------------------
    // Attack #1: 3-shot burst toward player
    // -------------------------

    private void performBurstAttack(Player player) {
        if (map == null || player == null) return;

        // SFX for the burst
        AudioPlayer.playSound("Resources/audio/BurstFire.wav", -3.0f);

        float vladCenterX = getBounds().getX() + getBounds().getWidth() / 2f;
        float vladCenterY = getBounds().getY() + getBounds().getHeight() / 2f;

        float playerCenterX = player.getBounds().getX() + player.getBounds().getWidth() / 2f;
        float playerCenterY = player.getBounds().getY() + player.getBounds().getHeight() / 2f;

        float dirX = playerCenterX - vladCenterX;
        float dirY = playerCenterY - vladCenterY;
        if (dirX == 0 && dirY == 0) {
            dirY = 1;
        }

        float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        float ux = dirX / len;
        float uy = dirY / len;

        // Perpendicular for side shots
        float px = -uy;
        float py = ux;

        float speed  = 3.5f;
        float spread = 30f;
        int damageUnits = 3; // treated as ~0.5 hearts in collision

        // center
        spawnVladProjectile(vladCenterX, vladCenterY, ux, uy, speed, damageUnits);
        // left
        spawnVladProjectile(vladCenterX + px * spread, vladCenterY + py * spread, ux, uy, speed, damageUnits);
        // right
        spawnVladProjectile(vladCenterX - px * spread, vladCenterY - py * spread, ux, uy, speed, damageUnits);
    }

    // -------------------------
    // Attack #2: summon 2 Firemonsters at (6,3) and (14,3)
    // -------------------------

    private void performSummonAttack() {
        if (map == null) return;

        // Optional SFX
        AudioPlayer.playSound("Resources/audio/Grunt.wav", -3.0f);

        // Tile positions for the summons
        Point spawn1 = map.getMapTile(7, 3).getLocation();
        Point spawn2 = map.getMapTile(14, 8).getLocation();
        Point spawn3 = map.getMapTile(10, 4).getLocation();
        Point spawn4 = map.getMapTile(12, 9).getLocation();

        Firemonster fm1 = new Firemonster(9991, spawn1);
        Firemonster fm2 = new Firemonster(9992, spawn2);
        Firemonster fm3 = new Firemonster(9993, spawn3);
        Firemonster fm4 = new Firemonster(9994, spawn4);


        // IMPORTANT: set their map so chase/collisions work correctly
        fm1.setMap(map);
        fm2.setMap(map);
        fm3.setMap(map);
        fm4.setMap(map);

        // Add to map's enemy list so they behave normally
        map.getEnemies().add(fm1);
        map.getEnemies().add(fm2);
        map.getEnemies().add(fm3);
        map.getEnemies().add(fm4);
    }

    // -------------------------
    // Attack #3: "Die-rain" downward vladballs across columns 4–17
    // -------------------------

    private void performDieRainAttack() {
        if (map == null) return;

        // You already lowered damage/speed globally via collision logic.
        int startCol = 4;
        int endCol   = 17;
        int row      = 1; // y:1 row at the top

        float speed = 1.5f;
        int damageUnits = 4;

        for (int col = startCol; col <= endCol; col++) {
            Point tileLoc = map.getMapTile(col, row).getLocation();

            float startX = tileLoc.x;
            float startY = tileLoc.y;

            // Straight down
            float dirX = 0f;
            float dirY = 1f;

            spawnVladProjectile(startX, startY, dirX, dirY, speed, damageUnits);
        }
    }

    // -------------------------
    // Attack #4: twin bombs at x:7 and x:14 that fall, then explode in a circle
    // -------------------------

    private void performTwinBombAttack() {
        if (map == null) return;

        // Optional "charging" SFX
        AudioPlayer.playSound("Resources/audio/Grunt.wav", -3.0f);

        // Bombs start at row 1, columns 7 and 14
        Point tile1 = map.getMapTile(6, 1).getLocation();
        Point tile2 = map.getMapTile(13, 1).getLocation();
        Point tile3 = map.getMapTile(6, 10).getLocation();
        Point tile4 = map.getMapTile(13, 10).getLocation();

        // SLOWER fall now
        float speedY = 0.8f;   // was 1.5f
        int lifeFrames = 120;  // 2 seconds at 60 FPS

        activeBombs.add(new Bomb(tile1.x, tile1.y, lifeFrames, speedY));
        activeBombs.add(new Bomb(tile2.x, tile2.y, lifeFrames, speedY));
        activeBombs.add(new Bomb(tile3.x, tile3.y, lifeFrames, speedY));
        activeBombs.add(new Bomb(tile4.x, tile4.y, lifeFrames, speedY));
    }

    private void updateBombs() {
        if (map == null) return;

        for (int i = activeBombs.size() - 1; i >= 0; i--) {
            Bomb b = activeBombs.get(i);

            // Move bomb downward
            b.y += b.speedY;
            b.lifeFrames--;

            // When timer hits 0, explode into a circle of projectiles
            if (b.lifeFrames <= 0) {
                explodeBomb(b.x, b.y);
                activeBombs.remove(i);
            }
        }
    }

    private void explodeBomb(float centerX, float centerY) {
        // Circle of bullets around the bomb
        int bulletCount = 15;      // tweak for more/less dense circle

        // SLOWER explosion bullets now
        float speed = 0.5f;        // was 2.2f
        int damageUnits = 1;       // treated as ~0.5 hearts in collision

        for (int i = 0; i < bulletCount; i++) {
            double angle = (2 * Math.PI * i) / bulletCount;
            float dirX = (float) Math.cos(angle);
            float dirY = (float) Math.sin(angle);

            spawnVladProjectile(centerX, centerY, dirX, dirY, speed, damageUnits);
        }
    }

    // -------------------------
    // Helper: spawn Vlad NProjectile
    // -------------------------

    private void spawnVladProjectile(float startX, float startY,
                                     float dirX, float dirY,
                                     float speed,
                                     int damageUnits) {
        NProjectile proj = new NProjectile(
            new Point((int) startX, (int) startY),
            dirX,
            dirY,
            speed,
            damageUnits
        );
        proj.setMap(this.map);
        activeProjectiles.add(proj);
    }

    // -------------------------
    // Projectile update & collision
    // -------------------------

    private void updateProjectiles(Player player) {
        for (int i = activeProjectiles.size() - 1; i >= 0; i--) {
            NProjectile p = activeProjectiles.get(i);

            p.update();

            if (p.getMapEntityStatus() == MapEntityStatus.REMOVED) {
                activeProjectiles.remove(i);
                continue;
            }

            // Hit Sada?
            if (p.getBounds().intersects(player.getBounds())) {
                // Each projectile ≈ 0.5 hearts
                player.takeDamage(0.5);

                // Damage SFX
                AudioPlayer.playSound("Resources/audio/Damage_Effect.wav", -4.0f);

                p.setMapEntityStatus(MapEntityStatus.REMOVED);
                activeProjectiles.remove(i);
            }
        }
    }
}
