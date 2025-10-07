package Screens;

import Engine.GraphicsHandler;
import Engine.Screen;
import EnhancedMapTiles.HealthPotion;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.*;
import Maps.TestMap;
import Players.Cat;
import Utils.Direction;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

// This class is for when the RPG game is actually being played
public class PlayLevelScreen extends Screen implements GameListener {
    protected ScreenCoordinator screenCoordinator;
    protected Map map;
    protected Player player;
    protected PlayLevelScreenState playLevelScreenState;
    protected WinScreen winScreen;
    protected GameOverScreen gameOverScreen;
    protected FlagManager flagManager;

    // Damage cooldown //
    private long lastDamageTime = 0;
    private final long damageCooldown = 1000; 

    // Heart images for health status
    private BufferedImage fullHeartImage;
    private BufferedImage halfHeartImage;
    private BufferedImage emptyHeartImage;

    private final int heartWidth = 25;
    private final int heartHeight = 25;

    public PlayLevelScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    public void initialize() {
        // setup state
        flagManager = new FlagManager();
        flagManager.addFlag("hasLostBall", false);
        flagManager.addFlag("hasTalkedToWalrus", false);
        flagManager.addFlag("hasTalkedToDinosaur", false);
        flagManager.addFlag("hasFoundBall", false);

        // define/setup map
        map = new TestMap();
        map.setFlagManager(flagManager);

        // setup player
        player = new Cat(map.getPlayerStartPosition().x, map.getPlayerStartPosition().y);
        player.setMap(map);
        playLevelScreenState = PlayLevelScreenState.RUNNING;
        player.setFacingDirection(Direction.LEFT);

        map.setPlayer(player);

        // let pieces of map know which button to listen for as the "interact" button
        map.getTextbox().setInteractKey(player.getInteractKey());

        // add this screen as a "game listener" so other areas of the game that don't normally have direct access to it (such as scripts) can "signal" to have it do something
        // this is used in the "onWin" method -- a script signals to this class that the game has been won by calling its "onWin" method
        map.addListener(this);

        // preloads all scripts ahead of time rather than loading them dynamically
        // both are supported, however preloading is recommended
        map.preloadScripts();

        winScreen = new WinScreen(this);
        // Initialize game over screen
        gameOverScreen = new GameOverScreen(this); 

        // Load heart images //
        try {
            fullHeartImage = ImageIO.read(new File("Resources/Full-Heart.png"));
            halfHeartImage = ImageIO.read(new File("Resources/Half-Heart.png"));
            emptyHeartImage = ImageIO.read(new File("Resources/Empty-Heart.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading heart images");
        }
    }

    public void update() {
        // check for GAME OVER //
        if(player.getHealth() <= 0) {
            playLevelScreenState = PlayLevelScreenState.GAME_OVER;
        }

        // based on screen state, perform specific actions
        switch (playLevelScreenState) {
            // if level is "running" update player and map to keep game logic for the platformer level going
            case RUNNING:
                handleEnemyCollisions();
                handleHealthPotionCollisions();
                player.update();
                map.update(player);
                break;
            // if level has been completed, bring up level cleared screen
            case LEVEL_COMPLETED:
                winScreen.update();
                break;
            // if game is over, update game over screen
            case GAME_OVER:
                gameOverScreen.update();
                break;
        }
    }

    // checks for collisions between player and enemies (NPCs)
    private void handleEnemyCollisions() {
        Player player = map.getPlayer();
        long currentTime = System.currentTimeMillis();
        
        if(playLevelScreenState == PlayLevelScreenState.RUNNING){
            // check on cooldown
            if(currentTime - lastDamageTime >= damageCooldown){
                // all NPCs
                for (NPC npc : map.getNPCs()) {
                if (npc.exists()) {

                    if (player.getBounds().intersects(npc.getBounds())) {
                        System.out.println("Collision detected!");
                        boolean playerDied = player.takeDamage(1);
                        
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
        // when this method is called within the game, it signals the game has been "won"
        playLevelScreenState = PlayLevelScreenState.LEVEL_COMPLETED;
    }

    public void draw(GraphicsHandler graphicsHandler) {
        // based on screen state, draw appropriate graphics
        switch (playLevelScreenState) {
            case RUNNING:
                map.draw(player, graphicsHandler);
                
                // Health Bar Drawing

                // Location of Hearts
                int startX = 20;
                int startY = 20;

                // Players current health from player class
                int currentHealth = player.getHealth();

                // Loop to draw 5 hearts
                for (int i = 0; i < 5; i++) {
                    // Calculates the X position for current heart
                    int heartX = startX + (i * (heartWidth + heartHeight));

                    int heartValue = (i * 2) + 2;

                    if(currentHealth >= heartValue) {
                        // Players health at full hearts 
                        graphicsHandler.drawImage(fullHeartImage, heartX, startY, heartWidth, heartHeight);
                    } else if (currentHealth == heartValue - 1) {
                        graphicsHandler.drawImage(halfHeartImage, heartX, startY, heartWidth, heartHeight);     
                    } else {
                        graphicsHandler.drawImage(emptyHeartImage, heartX, startY, heartWidth, heartHeight);
                    }
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
        screenCoordinator.setGameState(GameState.MENU);
    }

    // This enum represents the different states this screen can be in
    private enum PlayLevelScreenState {
        RUNNING, LEVEL_COMPLETED, GAME_OVER
    }
}
