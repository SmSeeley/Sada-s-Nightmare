package Screens;

import Engine.GraphicsHandler;
import Engine.Key;
import Engine.Keyboard;
import Engine.Screen;
import Engine.ScreenManager;
import Game.GameState;
import Game.ScreenCoordinator;
import java.awt.Color;
import java.awt.Font;

public class GameOverScreen extends Screen {
    protected ScreenCoordinator screenCoordinator;
    private PlayLevelScreen playLevelScreen;

    public GameOverScreen(PlayLevelScreen playLevelScreen){
        this.playLevelScreen = playLevelScreen;
        this.screenCoordinator = playLevelScreen.screenCoordinator;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void update() {
        // check for space key press to restart level
        if(Keyboard.isKeyDown(Key.SPACE)){
        
            // switches the main game state back to the level
            screenCoordinator.setGameState(GameState.MENU);
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        // draws black background overlay
       graphicsHandler.drawFilledRectangle(0,0,ScreenManager.getScreenWidth(), ScreenManager.getScreenHeight(), new Color(0,0,0,200));

       // draws text
       graphicsHandler.drawString(
        "GAME OVER",
        (ScreenManager.getScreenWidth()/2) - 150,
        (ScreenManager.getScreenHeight()/2) -50,
        new Font("Times New Roman", Font.BOLD, 50),
        Color.RED
     
       );

        graphicsHandler.drawString(
            "Press space to restart",
            (ScreenManager.getScreenWidth()/2) -130,
            (ScreenManager.getScreenHeight()/2) +20,
            new Font("Time New Roman", Font.PLAIN, 30),
            Color.WHITE
        );
    }
}
