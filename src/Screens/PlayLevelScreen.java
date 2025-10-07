package Screens;

import Engine.GraphicsHandler;
import Engine.Screen;
import EnhancedMapTiles.Coin;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.*;
import Maps.TestMap;
import Players.Man;
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
    protected FlagManager flagManager;

    // Heart images for health bar
    private BufferedImage fullHeartImage;
    private BufferedImage halfHeartImage;
    private BufferedImage emptyHeartImage;

    private final int heartWidth = 25;
    private final int heartHeight = 25;

    //coin slot
    private BufferedImage coinIcon;

    private int coinWidth = 75;
    private int coinHeight = 75;
    private int coinCount = 0;


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
        player = new Man(map.getPlayerStartPosition().x, map.getPlayerStartPosition().y);
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

        // Load heart images //
        try {
            fullHeartImage = ImageIO.read(new File("Resources/Full-Heart.png"));
            halfHeartImage = ImageIO.read(new File("Resources/Half-Heart.png"));
            emptyHeartImage = ImageIO.read(new File("Resources/Empty-Heart.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading heart images");
        }

        // Load coin image //
        try {
            coinIcon = ImageIO.read(new File("Resources/coin.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading coin image");
        }
    }

    public void update() {
        // based on screen state, perform specific actions
        switch (playLevelScreenState) {
            // if level is "running" update player and map to keep game logic for the platformer level going
            case RUNNING:
                player.update();
                map.update(player);
                coinCount = Coin.coinsCollected;
                break;
            // if level has been completed, bring up level cleared screen
            case LEVEL_COMPLETED:
                winScreen.update();
                break;
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
                //show coin icon
                int coinX = 275;
                int coinY = 5;
                graphicsHandler.drawImage(coinIcon, coinX, coinY, coinWidth,coinHeight);

                //show coin counter
                int coinTextX = coinX + coinWidth + 5;
                int coinTextY = coinY + (coinHeight / 2) + 5;
                graphicsHandler.drawString(" " + coinCount, coinTextX, coinTextY, new java.awt.Font("Arial", java.awt.Font.BOLD, 24), java.awt.Color.WHITE);

                


                break;
            case LEVEL_COMPLETED:
                winScreen.draw(graphicsHandler);
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
        RUNNING, LEVEL_COMPLETED
    }
}
