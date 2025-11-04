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
import Level.FlagManager;
import Level.GameListener;
import Level.Map;
import Level.MapEntity;
import Level.MapEntityStatus;
import Level.NPC;
import Level.Player;
import Players.Sada;
import Utils.Direction;
import Utils.Point;
import Maps.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Font;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    // --- UI: Transient banner message (non-blocking “textbox”) ---
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

    public PlayLevelScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
        // Stop any previous loops/SFX (fresh start for this screen)
        AudioPlayer.stopAll();

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

        // reset game-over guard
        gameOverTriggered = false;

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
    }

    @Override
    public void update() {
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

            // Show welcome banner (6s, as in your copy)
            triggerTransientMessage(
                "Welcome to the Dream Hall! Here you can trade coins for better weapons, and access other regions!",
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
        } else if ("Winter_1".equalsIgnoreCase(next))  {
            nextMap = new Winter_1();
            AudioPlayer.stopAll();
            AudioPlayer.playLoop("Resources/audio/WinterBliss.wav", -3.0f);
        } else {
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

    // collisions with NPCs (enemies)
    private void handleEnemyCollisions() {
        Player p = map.getPlayer();
        long now = System.currentTimeMillis();

        if (playLevelScreenState == PlayLevelScreenState.RUNNING) {
            if (now - lastDamageTime >= damageCooldown) {
                for (NPC npc : map.getNPCs()) {
                    // no damage if this NPC is a Wizard (projectiles handle that)
                    if (npc instanceof NPCs.Wizard) continue;

                    if (npc.exists() && p.getBounds().intersects(npc.getBounds())) {
                        boolean died = p.takeDamage(1);

                        // DAMAGE SFX
                        AudioPlayer.playSound("Resources/audio/Damage_Effect.wav", -4.0f);

                        lastDamageTime = now;
                        if (died) {
                            triggerGameOver(); // centralized, fast SFX
                        }
                        return;
                    }
                }
            }
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
        playLevelScreenState = PlayLevelScreenState.LEVEL_COMPLETED;
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        switch (playLevelScreenState) {
            case RUNNING:
                map.draw(player, graphicsHandler);

                // --- transient banner (non-blocking) with word-wrap ---
                if (transientMessage != null && System.currentTimeMillis() < transientMessageUntil) {
                    Font bannerFont = new Font("Arial", Font.BOLD, 24);
                    int textX = 50;
                    int textY = 100;
                    int maxWidthPx = 600; // adjust if needed
                    int lineSpacing = 6;

                    List<String> lines = wrapToWidth(transientMessage, bannerFont, maxWidthPx);

                    int y = textY;
                    for (String line : lines) {
                        // shadow
                        graphicsHandler.drawString(
                                line,
                                textX + 2, y + 2,
                                bannerFont,
                                new Color(0, 0, 0, 200)
                        );
                        // main
                        graphicsHandler.drawString(
                                line,
                                textX, y,
                                bannerFont,
                                Color.WHITE
                        );
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
