package Screens;

import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.Key;
import Engine.KeyLocker;
import Engine.Keyboard;
import Engine.Screen;
import Game.GameState;
import Game.ScreenCoordinator;
import SpriteFont.SpriteFont;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class MenuScreen extends Screen {
    private final ScreenCoordinator screenCoordinator;

    private SpriteFont playGameText;
    private SpriteFont creditsText;

    private final KeyLocker keyLocker = new KeyLocker();
    private int hoveredIndex = 0;       
    private int keyRepeatTimer = 0;

    private BufferedImage titleBg;

    private static final int GAME_W = 800;   //window width
    private static final int GAME_H = 600;   //window height
    private static final int IMAGE_Y_OFFSET = 40; 

    // Menu text locations
    private static final int PLAY_Y    = 240; // "PLAY GAME" Y
    private static final int CREDITS_Y = 290; // "CREDITS"  Y

    public MenuScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
        
        titleBg = ImageLoader.load("sada/Title_Screen.png");

        // Center text 
        String playStr = "PLAY GAME";
        String creditsStr = "CREDITS";

        int playWidth = playStr.length() * 14;
        int creditsWidth = creditsStr.length() * 14;

        int playX = (GAME_W - playWidth) / 2 - 20;
        int creditsX = (GAME_W - creditsWidth) / 2 - 20;

        playGameText = new SpriteFont(playStr, playX, PLAY_Y, "Comic Sans MS", 28, new Color(80,160,255));
        playGameText.setOutlineColor(Color.BLACK);
        playGameText.setOutlineThickness(3);

        creditsText  = new SpriteFont(creditsStr, creditsX, CREDITS_Y, "Comic Sans MS", 28, new Color(80,160,255));
        creditsText.setOutlineColor(Color.BLACK);
        creditsText.setOutlineThickness(3);

        keyLocker.lockKey(Key.SPACE);
        keyLocker.lockKey(Key.ENTER);
    }

    @Override
    public void update() {

        if (keyRepeatTimer > 0) keyRepeatTimer--;

        if (Keyboard.isKeyDown(Key.DOWN) && keyRepeatTimer == 0) {
            hoveredIndex = (hoveredIndex + 1) % 2;
            keyRepeatTimer = 14;
        }
        if (Keyboard.isKeyDown(Key.UP) && keyRepeatTimer == 0) {
            hoveredIndex = (hoveredIndex + 1) % 2;
            keyRepeatTimer = 14;
        }

        // Select with SPACE or ENTER
        boolean selectPressed =
                (Keyboard.isKeyDown(Key.SPACE) && !keyLocker.isKeyLocked(Key.SPACE)) ||
                (Keyboard.isKeyDown(Key.ENTER) && !keyLocker.isKeyLocked(Key.ENTER));

        if (selectPressed) {
            if (hoveredIndex == 0) screenCoordinator.setGameState(GameState.LEVEL);
            else                    screenCoordinator.setGameState(GameState.CREDITS);
        }

        if (!Keyboard.isKeyDown(Key.SPACE)) keyLocker.unlockKey(Key.SPACE);
        if (!Keyboard.isKeyDown(Key.ENTER)) keyLocker.unlockKey(Key.ENTER);
    }

    @Override
    public void draw(GraphicsHandler g) {
        
        if (titleBg != null) {
            double scale = Math.max(
                (double) GAME_W / titleBg.getWidth(),
                (double) GAME_H / titleBg.getHeight()
            );
            int drawW = (int) (titleBg.getWidth() * scale);
            int drawH = (int) (titleBg.getHeight() * scale);
            int drawX = (GAME_W - drawW) / 2;
            int drawY = (GAME_H - drawH) / 2 - IMAGE_Y_OFFSET; // raise image

            g.drawImage(titleBg, drawX, drawY, drawW, drawH);
        } else {
            g.drawFilledRectangle(0, 0, GAME_W, GAME_H, Color.BLACK);
        }

        // Hover highlight
        if (hoveredIndex == 0) {
            playGameText.setColor(new Color(240,200,80));  // gold
            creditsText.setColor(new Color(80,160,255));
        } else {
            playGameText.setColor(new Color(80,160,255));
            creditsText.setColor(new Color(240,200,80));
        }

        // Draw menu options (manually centered)
        playGameText.draw(g);
        creditsText.draw(g);
    }
}
