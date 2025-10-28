package Screens;

import Enemies.Projectile;
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
import Maps.FirstRoom;
import Maps.SecondRoom;
import Maps.TestMap;
import Players.Sada;
import Utils.Direction;
import Utils.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


public class PlayLevelScreen extends Screen implements GameListener {


    private static volatile String pendingMapName = null;
    private static volatile Point pendingSpawnPixels = null;


    public static void queueMapChange(String mapName, Point spawnPixels) {
        pendingMapName = mapName;
        pendingSpawnPixels = spawnPixels;
    }

    protected ScreenCoordinator screenCoordinator;
    protected Map map;
    protected Player player;
    protected PlayLevelScreenState playLevelScreenState;
    protected WinScreen winScreen;
    protected GameOverScreen gameOverScreen;
    protected FlagManager flagManager;

    // Damage cooldown
    private long lastDamageTime = 0;
    private final long damageCooldown = 1000;

    // Health UI
    private BufferedImage fullHeartImage;
    private BufferedImage halfHeartImage;
    private BufferedImage emptyHeartImage;
    private final int heartWidth = 25;
    private final int heartHeight = 25;

    // Coins UI
    private BufferedImage coinIcon;
    private int coinWidth = 75;
    private int coinHeight = 75;
    private int coinCount = 0;

    //Keys UI
    private BufferedImage keyIcon;
    private int keyWidth = 75;
    private int keyHeight = 75;
    private int keyCount = 0;


    public PlayLevelScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
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

        // start main game music (loops across all maps)
        // Uses Engine.AudioPlayer (javax.sound.sampled-based). Ensure file exists.
        AudioPlayer.playLoop("Resources/audio/main_game.wav", -5.0f); // 70% volume
    }

    @Override
    public void update() {
        // game over check
        if (player.getHealth() <= 0) {
            playLevelScreenState = PlayLevelScreenState.GAME_OVER;
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

        // construct target map use this for all map teleportations
        Map nextMap = null;
        if ("FirstRoom".equalsIgnoreCase(next)) {
            nextMap = new FirstRoom();
        } else if ("SecondRoom".equalsIgnoreCase(next)) {
            nextMap = new SecondRoom();
        } else if ("TestMap".equalsIgnoreCase(next)) {
            nextMap = new TestMap();
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
                // Skip damage if this NPC is a Wizard
                if (npc instanceof NPCs.Wizard) {
                    continue;
                }

                if (npc.exists() && p.getBounds().intersects(npc.getBounds())) {
                    System.out.println("Collision detected!");
                    boolean died = p.takeDamage(1);
                    lastDamageTime = now;
                    if (died) {
                        playLevelScreenState = PlayLevelScreenState.GAME_OVER;
                    }
                    return;
                }
            }
        }
    }
}

    private void handleProjectileCollisions() {
        Player player = map.getPlayer();
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDamageTime >= damageCooldown) {
            for (Projectile projectile : map.getCamera().getActiveProjectiles()) {
                if (projectile.exists()) {
                    if (player.getBounds().intersects(projectile.getBounds())) {
                        boolean playerDied = player.takeDamage(1); 
                        projectile.setMapEntityStatus(MapEntityStatus.REMOVED); 
                        lastDamageTime = currentTime;
                        if (playerDied) {
                            playLevelScreenState = PlayLevelScreenState.GAME_OVER;
                        }
                        return; 
                    }
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

                // draw hearts
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

                // coin icon + counter
                int coinX = 275;
                int coinY = 5;
                graphicsHandler.drawImage(coinIcon, coinX, coinY, coinWidth, coinHeight);
                int coinTextX = coinX + coinWidth + 5;
                int coinTextY = coinY + (coinHeight / 2) + 5;
                graphicsHandler.drawString(" " + coinCount, coinTextX, coinTextY,
                        new java.awt.Font("Arial", java.awt.Font.BOLD, 24), java.awt.Color.WHITE);

                // key icon + counter
                
                int keyX = 680;  
                int keyY = 5;
                graphicsHandler.drawImage(keyIcon, keyX, keyY, keyWidth, keyHeight);
                int keyTextX = keyX + keyWidth + 5;
                int keyTextY = keyY + (keyHeight / 2) + 5;
                graphicsHandler.drawString(" " + keyCount, keyTextX, keyTextY, new java.awt.Font("Arial", java.awt.Font.BOLD, 24), java.awt.Color.WHITE);
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
        // stop main game music when leaving gameplay for menu
        AudioPlayer.stop();
        screenCoordinator.setGameState(GameState.MENU);
    }

    // Screen state
    private enum PlayLevelScreenState {
        RUNNING, LEVEL_COMPLETED, GAME_OVER
    }
}
    
