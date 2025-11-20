package Screens;

import Enemies.*;
import Engine.AudioPlayer;
import Engine.GraphicsHandler;
import Engine.Screen;
import EnhancedMapTiles.Coin;
import EnhancedMapTiles.DoorKey;
import EnhancedMapTiles.HealthPotion;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.Enemy;
import Level.FlagManager;
import Level.GameListener;
import Level.Map;
import Level.MapEntity;
import Level.MapEntityStatus;
import Level.Player;
import Maps.Desert_1;
import Maps.Desert_2;
import Maps.Desert_3;
import Maps.Desert_4;
import Maps.Desert_5;
import Maps.Fire_1;
import Maps.Fire_2;
import Maps.Fire_3;
import Maps.Fire_4;
import Maps.Fire_5;
import Maps.FirstRoom;
import Maps.Room4Dungeon;
import Maps.Room5Dungeon;
import Maps.SecondRoom;
import Maps.TheHub1;
import Maps.ThirdRoomDungeon;
import Maps.Winter_1;
import Maps.Winter_2;
import Maps.Winter_3;
import Maps.Winter_4;
import Maps.Winter_5;
import Players.Sada;
import Utils.Direction;
import Utils.Point;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

import Enemies.Projectile;
import ui.DialogueOverlay;

public class PlayLevelScreen extends Screen implements GameListener {

    // --- Map transition handshake ---
    private static volatile String pendingMapName = null;
    private static volatile Point pendingSpawnPixels = null;

    public static void queueMapChange(String mapName, Point spawnPixels) {
        pendingMapName = mapName;
        pendingSpawnPixels = spawnPixels;
    }

    // --- Core game state ---
    protected ScreenCoordinator screenCoordinator;
    protected Map map;
    protected Player player;
    protected PlayLevelScreenState playLevelScreenState;
    protected WinScreen winScreen;
    protected GameOverScreen gameOverScreen;
    protected FlagManager flagManager;

    // --- Damage cooldown ---
    private long lastDamageTime = 0;
    private final long damageCooldown = 500;

    // --- UI: Hearts ---
    private BufferedImage fullHeartImage;
    private BufferedImage halfHeartImage;
    private BufferedImage emptyHeartImage;
    private final int heartWidth = 25;
    private final int heartHeight = 25;

    // --- UI: Coins ---
    private BufferedImage coinIcon;
    private int coinWidth = 75;
    private int coinHeight = 75;
    private int coinCount = 0;

    // --- UI: Keys ---
    private BufferedImage keyIcon;
    private int keyWidth = 75;
    private int keyHeight = 75;
    private int keyCount = 0;

    // --- UI: Transient banner message (non-blocking) ---
    private String transientMessage = null;
    private long transientMessageUntil = 0L;

    private void triggerTransientMessage(String text, long durationMs) {
        transientMessage = text;
        transientMessageUntil = System.currentTimeMillis() + Math.max(0, durationMs);
    }

    // --- Game over trigger guard + constants ---
    private boolean gameOverTriggered = false;
    private static final String GAME_OVER_SFX = "Resources/audio/gameover.wav";

    private void triggerGameOver() {
        if (gameOverTriggered) return;
        gameOverTriggered = true;

        playLevelScreenState = PlayLevelScreenState.GAME_OVER;

        // Only stop the looping music so our preloaded SFX stays cached
        AudioPlayer.stopLoop();
        AudioPlayer.playSound(GAME_OVER_SFX, -3.0f);
    }

    // === Dialogue overlay for blocking textboxes ===
    private DialogueOverlay dialogue;
    private boolean firstRoomIntroShown = false; // ensure it only happens once

    // Desert_5 boss intro sequence
    private boolean desert5IntroShown = false;      // run once
    private boolean desert5SequenceActive = false;  // are we in that sequence?
    private int desert5Phase = 0;                   // 0 = boss, 1 = Sada

    // Winter_5 boss intro sequence
    private boolean Winter5IntroShown = false;
    private boolean Winter5SequenceActive = false;
    private int Winter5Phase = 0;

    // Fire_5 Final boss intro sequence
    private boolean Fire5IntroShown = false;
    private boolean Fire5SequenceActive = false;
    private int Fire5Phase = 0;

    // Fire_5 Final boss ENDING sequence (after Vlad is defeated)
    private boolean Fire5EndingActive = false;
    private int Fire5EndingPhase = 0;

    // Portrait paths (relative to Resources/, since ImageLoader adds that)
    private static final String SADA_SAD_PORTRAIT   = "sada/SadaSad.png";
    private static final String BOSS_PORTRAIT_PATH  = "sada/Desert_Boss.png"; // desert boss
    private static final String ANGRY_SAD_PORTRAIT  = "sada/SadaAngry.png";
    private static final String Vladimir_PORTRAIT   = "sada/Vladimir_Ui_Image.png";
    private static final String Boomer_Portrait     = "sada/meow.png";
    private static final String BOSS_PORTRAIT_PATH2 = "sada/boss_image.png"; // winter boss

    // Final boss victory guard – make sure we only trigger ending once
    private boolean finalBossVictoryHandled = false;

    public PlayLevelScreen(ScreenCoordinator screenCoordinator) {
        //reset collectibles
        System.out.println("PlayLevelScreen: Resetting coins...");
        EnhancedMapTiles.Coin.resetAllCoinsTest();
        System.out.println("PlayLevelScreen: Reset complete, continuing initialization...");
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
        // Stop any previous loops/SFX (fresh start for this screen)
        AudioPlayer.stopAll();

        // Reset Vlad flag for a fresh run
        Enemy.vladmirDefeated = false;

        // flags
        flagManager = new FlagManager();
        flagManager.addFlag("hasLostBall", false);
        flagManager.addFlag("hasTalkedToWalrus", false);
        flagManager.addFlag("hasTalkedToDinosaur", false);
        flagManager.addFlag("hasFoundBall", false);

        map = new FirstRoom();
        map.setFlagManager(flagManager);

        // player
        player = new Sada(map.getPlayerStartPosition().x, map.getPlayerStartPosition().y);
        player.setMap(map);
        player.setFacingDirection(Direction.LEFT);
        map.setPlayer(player);

        // input and listeners/scripts
        map.getTextbox().setInteractKey(player.getInteractKey());
        map.addListener(this);
        map.preloadScripts();

        winScreen = new WinScreen(this);
        gameOverScreen = new GameOverScreen(this);
        playLevelScreenState = PlayLevelScreenState.RUNNING;

        // reset guards
        gameOverTriggered = false;
        finalBossVictoryHandled = false;

        // reset ending state
        Fire5EndingActive = false;
        Fire5EndingPhase = 0;

        // Preload ONLY the gameover SFX so first play has zero I/O latency
        AudioPlayer.preloadSound(GAME_OVER_SFX, 1);

        // load UI images
        try {
            fullHeartImage  = ImageIO.read(new File("Resources/Full-Heart.png"));
            halfHeartImage  = ImageIO.read(new File("Resources/Half-Heart.png"));
            emptyHeartImage = ImageIO.read(new File("Resources/Empty-Heart.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading heart images");
        }

        try {
            coinIcon = ImageIO.read(new File("Resources/coin.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading coin image");
        }

        try {
            keyIcon  = ImageIO.read(new File("Resources/UI_Key.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading key images");
        }

        // background music — safe now (we stopped everything above)
        AudioPlayer.playLoop("Resources/audio/main_game.wav", -5.0f);

        // create dialogue overlay
        dialogue = new DialogueOverlay();

        // First room intro cutscene (Sada is sad) – only once
        if (!firstRoomIntroShown && map instanceof FirstRoom) {
            dialogue.start(
                SADA_SAD_PORTRAIT,
                "Huh??? Am I Dreaming???",
                "I Need to Find Boomer, I Miss Him So Much "
            );
            AudioPlayer.playSound("Resources/audio/Sad1.wav", -10.0f);
            firstRoomIntroShown = true;
        }

        // reset boss sequence state flags (but keep IntroShown booleans so they only fire once)
        desert5SequenceActive = false;
        desert5Phase = 0;

        Winter5SequenceActive = false;
        Winter5Phase = 0;

        Fire5SequenceActive = false;
        Fire5Phase = 0;
    }

    @Override
    public void update() {
        // 1) If any blocking dialogue is active, freeze EVERYTHING.
        if (dialogue != null && dialogue.isBlocking()) {
            dialogue.update();
            return;
        }

        // 2) Desert_5 boss sequence
        if (desert5SequenceActive) {
            handleDesert5SequenceProgression();

            if (dialogue != null && dialogue.isBlocking()) {
                dialogue.update();
                return;
            }

            if (desert5SequenceActive) {
                return;
            }
        }

        // 3) Winter_5 boss sequence
        if (Winter5SequenceActive) {
            handleWinter5SequenceProgression();

            if (dialogue != null && dialogue.isBlocking()) {
                dialogue.update();
                return;
            }

            if (Winter5SequenceActive) {
                return;
            }
        }

        // 4) Fire_5 final boss INTRO sequence
        if (Fire5SequenceActive) {
            handleFire5SequenceProgression();

            if (dialogue != null && dialogue.isBlocking()) {
                dialogue.update();
                return;
            }

            if (Fire5SequenceActive) {
                return;
            }
        }

        // 5) Fire_5 final boss ENDING sequence (after Vlad dies)
        if (Fire5EndingActive) {
            handleFire5EndingProgression();

            if (dialogue != null && dialogue.isBlocking()) {
                dialogue.update();
                return;
            }

            if (Fire5EndingActive) {
                return; // still in ending cutscene
            }
        }

        // 6) From this point on, no dialogue is blocking and all boss intros are over.
        //    Gameplay is allowed to run.

        // Immediate health check -> trigger game over fast
        if (player.getHealth() <= 0) {
            triggerGameOver();
        }

        // handle queued door map changes
        consumePendingMapChangeIfAny();

        switch (playLevelScreenState) {
            case RUNNING:
                handleEnemyCollisions();
                handleHealthPotionCollisions();
                handleProjectileCollisions();
                player.update();
                map.update(player);
                coinCount = Coin.coinsCollected;
                keyCount = DoorKey.keysCollected;

                // === NEW: Final boss ENDING trigger for Fire_5 ===
                if (!finalBossVictoryHandled && map instanceof Fire_5 && Enemy.vladmirDefeated) {
                    finalBossVictoryHandled = true;
                    System.out.println("[PlayLevelScreen] Detected Vladmir defeat flag, starting ending sequence.");
                    startFire5Ending();
                    return;
                }

                break;

            case LEVEL_COMPLETED:
                winScreen.update();
                break;

            case GAME_OVER:
                gameOverScreen.update();
                break;
        }
    }

    /** Swap maps if a door has queued a change. */
    private void consumePendingMapChangeIfAny() {
        if (pendingMapName == null) return;

        String next = pendingMapName;
        Point spawn = pendingSpawnPixels;
        pendingMapName = null;
        pendingSpawnPixels = null;

        Map nextMap = null;
        if ("FirstRoom".equalsIgnoreCase(next)) {
            nextMap = new FirstRoom();
        } else if ("SecondRoom".equalsIgnoreCase(next)) {
            nextMap = new SecondRoom();
        } else if ("ThirdRoomDungeon".equalsIgnoreCase(next)) {
            nextMap = new ThirdRoomDungeon();
        } else if ("Room4Dungeon".equalsIgnoreCase(next)) {
            nextMap = new Room4Dungeon();
        } else if ("Room5Dungeon".equalsIgnoreCase(next)) {
            nextMap = new Room5Dungeon();
        } else if ("TheHub1".equalsIgnoreCase(next)) {
            nextMap = new TheHub1();
            AudioPlayer.stopAll();
            AudioPlayer.playLoop("Resources/audio/TheHubMusic.wav", -3.0f);

            // Show welcome banner (6s)
            triggerTransientMessage(
                "Welcome to the Dream Hall Here you can trade coins for better weapons, and access other regions!",
                6000
            );
        } else if ("Desert_1".equalsIgnoreCase(next))  {
            nextMap = new Desert_1();
            AudioPlayer.stopAll();
            AudioPlayer.playLoop("Resources/audio/Desert.wav", -3.0f);
        } else if ("Desert_2".equalsIgnoreCase(next))  {
            nextMap = new Desert_2();
        } else if ("Desert_3".equalsIgnoreCase(next))  {
            nextMap = new Desert_3();
        } else if ("Desert_4".equalsIgnoreCase(next))  {
            nextMap = new Desert_4();
        } else if ("Desert_5".equalsIgnoreCase(next))  {
            nextMap = new Desert_5();
            AudioPlayer.stopAll();
            AudioPlayer.playLoop("Resources/audio/SandBattle.wav", -3.0f);

            // Start Desert_5 boss intro if not yet shown
            if (!desert5IntroShown) {
                startDesert5Intro();
            }
        } else if ("Winter_1".equalsIgnoreCase(next))  {
            nextMap = new Winter_1();
            AudioPlayer.stopAll();
            AudioPlayer.playLoop("Resources/audio/WinterBliss.wav", -3.0f);
        } else if ("Winter_2".equalsIgnoreCase(next)) {
            nextMap = new Winter_2();
        } else if ("Winter_3".equalsIgnoreCase(next)) {
            nextMap = new Winter_3();
        } else if ("Winter_4".equalsIgnoreCase(next)) {
            nextMap = new Winter_4();
        } else if ("Winter_5".equalsIgnoreCase(next)) {
            nextMap = new Winter_5();
            AudioPlayer.stopAll();
            AudioPlayer.playLoop("Resources/audio/IceBattle.wav", -3.0f);

            if (!Winter5IntroShown) {
                startWinter5Intro();
            }
        } else if ("Fire_1".equalsIgnoreCase(next))  {
            nextMap = new Fire_1();
            AudioPlayer.stopAll();
            AudioPlayer.playLoop("Resources/audio/FireMusic.wav", -3.0f);
        } else if ("Fire_2".equalsIgnoreCase(next)) {
            nextMap = new Fire_2();
        } else if ("Fire_3".equalsIgnoreCase(next)) {
            nextMap = new Fire_3();
        } else if ("Fire_4".equalsIgnoreCase(next)) {
            nextMap = new Fire_4();
        } else if ("Fire_5".equalsIgnoreCase(next)) {
            nextMap = new Fire_5();
            AudioPlayer.stopAll();
            AudioPlayer.playLoop("Resources/audio/FinalBattle.wav", -3.0f);

            if (!Fire5IntroShown) {
                startFire5Intro();
            }
        }
        else {
            System.out.println("[PlayLevelScreen] Unknown map: " + next);
            return;
        }

        nextMap.setFlagManager(flagManager);
        nextMap.setPlayer(player);
        nextMap.preloadScripts();

        map = nextMap;
        player.setMap(map);

        if (spawn != null) {
            try {
                player.setLocation((float) spawn.x, (float) spawn.y);
            } catch (Throwable ignored) {
                try {
                    player.setX((float) spawn.x);
                    player.setY((float) spawn.y);
                } catch (Throwable t2) {
                    System.out.println("[PlayLevelScreen] Could not set player location: " + t2);
                }
            }
        }

        // rewire input/listeners
        map.getTextbox().setInteractKey(player.getInteractKey());
        map.addListener(this);

        System.out.println("[PlayLevelScreen] Switched to " + next +
                " spawn=" + (spawn != null ? (spawn.x + "," + spawn.y) : "null"));
    }

    // =========================
    // Desert_5 Intro Sequence Helpers
    // =========================

    /** Called when we first load Desert_5. */
    private void startDesert5Intro() {
        desert5SequenceActive = true;
        desert5Phase = 0;

        if (dialogue == null) {
            dialogue = new DialogueOverlay();
        }

        // Phase 0: Sand boss speaks
        AudioPlayer.playSound("Resources/audio/SandbossSound.wav", -10.0f);
        dialogue.start(
            BOSS_PORTRAIT_PATH,
            "EAT SAND SADA. YOUR TIME HAS COME"
        );
    }

    /** Called from update() after dialogue stops blocking, to chain boss -> Sada -> end. */
    private void handleDesert5SequenceProgression() {
        if (!desert5SequenceActive) return;

        if (dialogue != null && dialogue.isBlocking()) {
            return;
        }

        // Phase 0 just finished: boss line -> now Sada responds
        if (desert5Phase == 0) {
            desert5Phase = 1;
            AudioPlayer.playSound("Resources/audio/you will never defeat me.wav", -3.0f);
            dialogue.start(
                ANGRY_SAD_PORTRAIT,
                "You will never defeat me",
                "Guess I'll have to kill you"
            );
            return;
        }

        // Phase 1 just finished: sequence done
        if (desert5Phase == 1) {
            desert5SequenceActive = false;
            desert5Phase = 0;
            desert5IntroShown = true;
        }
    }

    // =========================
    // Winter_5 Intro Sequence
    // =========================

    /** Called when we first load Winter_5 */
    private void startWinter5Intro() {
        Winter5SequenceActive = true;
        Winter5Phase = 0;

        if (dialogue == null) {
            dialogue = new DialogueOverlay();
        }

        AudioPlayer.playSound("Resources/audio/IceBossSound.wav", -10.0f);
        dialogue.start(
            BOSS_PORTRAIT_PATH2,
            "Your fate has been decided"
        );
    }

    private void handleWinter5SequenceProgression() {
        if (!Winter5SequenceActive) return;

        if (dialogue != null && dialogue.isBlocking()) {
            return;
        }

        if (Winter5Phase == 0) {
            Winter5Phase = 1;
            AudioPlayer.playSound("Resources/audio/Time to change fate.wav", -3.0f);
            dialogue.start(
                ANGRY_SAD_PORTRAIT,
                "Its Time To Change Fate",
                "You are going down!"
            );
            return;
        }

        if (Winter5Phase == 1) {
            Winter5SequenceActive = false;
            Winter5Phase = 0;
            Winter5IntroShown = true;
        }
    }

    // =========================
    // Fire_5 Final Boss INTRO Sequence
    // =========================

    /** Called when we first load Fire_5 */
    private void startFire5Intro() {
        Fire5SequenceActive = true;
        Fire5Phase = 0;

        if (dialogue == null) {
            dialogue = new DialogueOverlay();
        }

        // Phase 0: Sada confronts Vladimir
        dialogue.start(
            ANGRY_SAD_PORTRAIT,
            "Vladimir I finally found you...",
            "GIVE ME BACK MY BOOMER"
        );
    }

    private void handleFire5SequenceProgression() {
        if (!Fire5SequenceActive) return;

        if (dialogue != null && dialogue.isBlocking()) {
            return;
        }

        // Phase 0 just finished: Boomer cries for help
        if (Fire5Phase == 0) {
            Fire5Phase = 1;
            AudioPlayer.playSound("Resources/audio/boomer.wav", -3.0f);
            dialogue.start(
                Boomer_Portrait,
                "*bobcat noise* SADA HELP ME"
            );
            return;
        }

        // Phase 1 just finished: Vladimir mocks Sada (with specific SFX)
        if (Fire5Phase == 1) {
            Fire5Phase = 2;
            AudioPlayer.playSound("Resources/audio/Vla2_Cant help but feel sorry for you.wav", -3.0f);
            dialogue.start(
                Vladimir_PORTRAIT,
                "I cant help but feel sorry for you"
            );
            return;
        }

        // Phase 2 just finished: Sada snaps (SadaAngry SFX + "ILL KILL YOU")
        if (Fire5Phase == 2) {
            Fire5Phase = 3;
            AudioPlayer.playSound("Resources/audio/SadaAngry.wav", -3.0f);
            dialogue.start(
                ANGRY_SAD_PORTRAIT,
                "ILL KILL YOU"
            );
            return;
        }

        // Phase 3 finished: sequence over, fight can start
        if (Fire5Phase == 3) {
            Fire5SequenceActive = false;
            Fire5Phase = 0;
            Fire5IntroShown = true;

            // === START VLADMIR FIGHT HERE ===
            for (Enemy enemy : map.getEnemies()) {
                if (enemy instanceof Vladmir) {
                    ((Vladmir) enemy).startFight();
                    break;
                }
            }
        }
    }

    // =========================
    // Fire_5 Final Boss ENDING Sequence (after Vlad dies)
    // =========================

    /** Called when Vladmir is detected as defeated in Fire_5. */
    private void startFire5Ending() {
        Fire5EndingActive = true;
        Fire5EndingPhase = 0;

        if (dialogue == null) {
            dialogue = new DialogueOverlay();
        }

        // Phase 0: Vladmir admits defeat
        dialogue.start(
            Vladimir_PORTRAIT,
            "I've... been defeated..."
        );
        AudioPlayer.stopAll();
        AudioPlayer.playLoop("Resources/audio/EndingMusic.wav", -3.0f);
    }

    /** Vlad -> Boomer -> Sada -> back to main menu. */
    private void handleFire5EndingProgression() {
        if (!Fire5EndingActive) return;

        if (dialogue != null && dialogue.isBlocking()) {
            return;
        }

        // Phase 0 finished: Boomer thanks Sada
        if (Fire5EndingPhase == 0) {
            Fire5EndingPhase = 1;
            AudioPlayer.playSound("Resources/audio/boomer.wav", -3.0f);
            dialogue.start(
                Boomer_Portrait,
                "SADA YOU SAVED ME!!"
            );
            return;
        }

        // Phase 1 finished: Sada celebrates
        if (Fire5EndingPhase == 1) {
            Fire5EndingPhase = 2;
            AudioPlayer.playSound("Resources/audio/SadaAngry.wav", -3.0f); // reuse as happy yell
            dialogue.start(
                ANGRY_SAD_PORTRAIT,
                "HOORAY!"
            );
            return;
        }

        // Phase 2 finished: ending done -> back to main menu
        if (Fire5EndingPhase == 2) {
            Fire5EndingActive = false;
            Fire5EndingPhase = 0;

            AudioPlayer.stopAll();
            screenCoordinator.setGameState(GameState.MENU);
        }
    }

    // =========================
    // Gameplay collision handlers
    // =========================

    /** Unified enemy touch damage, works in all rooms; Vladmir does no contact damage. */
    private void handleEnemyCollisions() {
        if (playLevelScreenState != PlayLevelScreenState.RUNNING) {
            return;
        }

        Player p = map.getPlayer();
        long now = System.currentTimeMillis();

        if (now - lastDamageTime < damageCooldown) {
            return;
        }

        for (Enemy enemy : map.getEnemies()) {
            if (enemy == null || !enemy.exists()) {
                continue;
            }

            // Vladmir: no contact damage
            if (enemy instanceof Vladmir) {
                continue;
            }

            if (!p.getBounds().intersects(enemy.getBounds())) {
                continue;
            }

            double damageAmount;
            if (enemy instanceof Fireblob || enemy instanceof Desertboss || enemy instanceof Twoheadedogre) {
                damageAmount = 0.5;
            } else {
                damageAmount = 1.0;
            }

            boolean died = p.takeDamage(damageAmount);

            // DAMAGE SFX
            AudioPlayer.playSound("Resources/audio/Damage_Effect.wav", -4.0f);

            lastDamageTime = now;

            if (died) {
                triggerGameOver();
            }

            // Only one enemy hit per frame
            return;
        }
    }

    private void handleProjectileCollisions() {
        Player p = map.getPlayer();
        long now = System.currentTimeMillis();

        if (now - lastDamageTime >= damageCooldown) {
            for (Projectile projectile : map.getCamera().getActiveProjectiles()) {
                if (projectile.exists() && p.getBounds().intersects(projectile.getBounds())) {
                    boolean playerDied = p.takeDamage(1);

                    // DAMAGE SFX
                    AudioPlayer.playSound("Resources/audio/Damage_Effect.wav", -4.0f);

                    projectile.setMapEntityStatus(MapEntityStatus.REMOVED);
                    lastDamageTime = now;
                    if (playerDied) {
                        triggerGameOver(); // unified
                    }
                    return;
                }
            }
        }
    }

    private void handleHealthPotionCollisions() {
        for (MapEntity entity : map.getMapTiles()) {
            if (entity instanceof HealthPotion && entity.exists()) {
                if (player.getBounds().intersects(entity.getBounds())) {
                    player.heal(HealthPotion.HEAL_AMOUNT);

                    String flag = "potion_" + entity.hashCode();
                    entity.setExistenceFlag(flag);
                    map.getFlagManager().setFlag(flag);

                    // HEALING SFX (optional)
                    AudioPlayer.playSound("Resources/audio/Healing.wav", -6.0f);
                }
            }
        }
    }

    @Override
    public void onWin() {
        // Keep this as a fallback for non-final levels
        playLevelScreenState = PlayLevelScreenState.LEVEL_COMPLETED;
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        switch (playLevelScreenState) {
            case RUNNING:
                map.draw(player, graphicsHandler);

                // --- transient banner (non-blocking) with word-wrap & pink highlight for "Dream Hall" ---
                if (transientMessage != null && System.currentTimeMillis() < transientMessageUntil) {
                    Font bannerFont = new Font("Arial", Font.BOLD, 24);
                    int textX = 50;
                    int textY = 100;
                    int maxWidthPx = 600; // adjust if needed
                    int lineSpacing = 6;

                    // Keep 'Dream Hall' together if possible using non-breaking space
                    String wrappedText = transientMessage.replace("Dream Hall", "Dream\u00A0Hall");

                    List<String> lines = wrapToWidth(wrappedText, bannerFont, maxWidthPx);

                    int y = textY;
                    for (String line : lines) {
                        // draw with pink highlight
                        drawLineWithDreamHallHighlight(graphicsHandler, line, bannerFont, textX, y);
                        y += getFontHeight(bannerFont) + lineSpacing;
                    }
                } else if (transientMessage != null) {
                    transientMessage = null;
                }

                // --- hearts ---
                int startX = 20;
                int startY = 20;
                int currentHealth = player.getHealth();

                for (int i = 0; i < 5; i++) {
                    int heartX = startX + (i * (heartWidth + heartHeight));
                    int heartValue = (i * 2) + 2;

                    if (currentHealth >= heartValue) {
                        graphicsHandler.drawImage(fullHeartImage, heartX, startY, heartWidth, heartHeight);
                    } else if (currentHealth == heartValue - 1) {
                        graphicsHandler.drawImage(halfHeartImage, heartX, startY, heartWidth, heartHeight);
                    } else {
                        graphicsHandler.drawImage(emptyHeartImage, heartX, startY, heartWidth, heartHeight);
                    }
                }

                // --- coin icon + counter ---
                int coinX = 275;
                int coinY = 5;
                graphicsHandler.drawImage(coinIcon, coinX, coinY, coinWidth, coinHeight);
                int coinTextX = coinX + coinWidth + 5;
                int coinTextY = coinY + (coinHeight / 2) + 5;
                graphicsHandler.drawString(" " + coinCount, coinTextX, coinTextY,
                        new Font("Arial", Font.BOLD, 24), Color.WHITE);

                // --- key icon + counter ---
                int keyX = 380;
                int keyY = 5;
                graphicsHandler.drawImage(keyIcon, keyX, keyY, keyWidth, keyHeight);
                int keyTextX = keyX + keyWidth + 5;
                int keyTextY = keyY + (keyHeight / 2) + 5;
                graphicsHandler.drawString(" " + keyCount, keyTextX, keyTextY,
                        new Font("Arial", Font.BOLD, 24), Color.WHITE);

                // draw dialogue overlay last (on top of HUD)
                if (dialogue != null) {
                    dialogue.draw(graphicsHandler);
                }
                break;

            case LEVEL_COMPLETED:
                winScreen.draw(graphicsHandler);
                break;

            case GAME_OVER:
                map.draw(player, graphicsHandler);
                gameOverScreen.draw(graphicsHandler);
                break;
        }
    }

    public PlayLevelScreenState getPlayLevelScreenState() {
        return playLevelScreenState;
    }

    public void resetLevel() {
        initialize();
    }

    public void goBackToMenu() {
        AudioPlayer.stopAll();
        screenCoordinator.setGameState(GameState.MENU);
    }

    private enum PlayLevelScreenState {
        RUNNING, LEVEL_COMPLETED, GAME_OVER
    }

    // =========================
    // Highlight helpers for "Dream Hall"
    // =========================

    // Pink used for "Dream Hall"
    private static final Color DREAM_HALL_PINK = new Color(255, 105, 180); // Hot pink; tweak to taste

    /** Draw a single line of text, coloring every occurrence of "Dream Hall" (or with NBSP) in pink, with a shadow. */
    private void drawLineWithDreamHallHighlight(GraphicsHandler g, String line, Font font, int x, int y) {
        final String targetA = "Dream Hall";
        final String targetB = "Dream\u00A0Hall";

        // Prepare font metrics for X advances
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = tmp.createGraphics();
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        int cursorX = x;
        int i = 0;
        while (i < line.length()) {
            int idx = indexOfEither(line, targetA, targetB, i);
            if (idx < 0) {
                String rest = line.substring(i);
                // shadow
                g.drawString(rest, cursorX + 2, y + 2, font, new Color(0, 0, 0, 200));
                // main
                g.drawString(rest, cursorX, y, font, Color.WHITE);
                cursorX += fm.stringWidth(rest);
                break;
            }

            if (idx > i) {
                String pre = line.substring(i, idx);
                g.drawString(pre, cursorX + 2, y + 2, font, new Color(0, 0, 0, 200));
                g.drawString(pre, cursorX, y, font, Color.WHITE);
                cursorX += fm.stringWidth(pre);
            }

            String target = line.startsWith(targetA, idx) ? targetA : targetB;

            // Draw highlighted segment in pink
            g.drawString(target, cursorX + 2, y + 2, font, new Color(0, 0, 0, 200));
            g.drawString(target, cursorX, y, font, DREAM_HALL_PINK);
            cursorX += fm.stringWidth(target);

            i = idx + target.length();
        }

        g2.dispose();
    }

    /** Finds next index of either s1 or s2 in 'text' starting at 'from'. Returns -1 if neither found. */
    private int indexOfEither(String text, String s1, String s2, int from) {
        int i1 = text.indexOf(s1, from);
        int i2 = text.indexOf(s2, from);
        if (i1 == -1) return i2;
        if (i2 == -1) return i1;
        return Math.min(i1, i2);
    }

    // =========================
    // Word-wrap helpers
    // =========================

    private List<String> wrapToWidth(String text, Font font, int maxWidthPx) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;

        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = tmp.createGraphics();
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            String test = current.length() == 0 ? word : current + " " + word;
            int width = fm.stringWidth(test);

            if (width <= maxWidthPx) {
                current.setLength(0);
                current.append(test);
            } else {
                if (current.length() > 0) {
                    lines.add(current.toString());
                    current.setLength(0);
                }
                if (fm.stringWidth(word) > maxWidthPx) {
                    lines.addAll(hardBreakWord(word, fm, maxWidthPx));
                } else {
                    current.append(word);
                }
            }
        }

        if (current.length() > 0) {
            lines.add(current.toString());
        }

        g2.dispose();
        return lines;
    }

    private List<String> hardBreakWord(String word, FontMetrics fm, int maxWidthPx) {
        List<String> parts = new ArrayList<>();
        StringBuilder chunk = new StringBuilder();

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            String test = chunk.toString() + c;
            if (fm.stringWidth(test) <= maxWidthPx) {
                chunk.append(c);
            } else {
                if (chunk.length() > 0) {
                    parts.add(chunk.toString());
                    chunk.setLength(0);
                }
                chunk.append(c);
            }
        }
        if (chunk.length() > 0) parts.add(chunk.toString());
        return parts;
    }

    private int getFontHeight(Font font) {
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = tmp.createGraphics();
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int h = fm.getAscent() + fm.getDescent();
        g2.dispose();
        return h;
    }
}
