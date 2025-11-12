package Screens;

import Engine.AudioPlayer;
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

/**
 * Intro screen shown after PLAY on the menu.
 * Displays OpenBook.png and plays Sad.wav.
 * Slight zoom added for clearer text visibility.
 * Fades in and out cleanly.
 */
public class IntroScreen extends Screen {
    private final ScreenCoordinator screenCoordinator;

    private static final int GAME_W = 800;
    private static final int GAME_H = 600;

    private BufferedImage introImage;

    private SpriteFont pressSpaceCenterText;
    private SpriteFont pressSpaceCornerText;

    private final KeyLocker keyLocker = new KeyLocker();

    private static final String INTRO_MUSIC = "Resources/audio/Introduction.wav";

    private int fadeTimer = 0;
    private static final int FADE_FRAMES = 30;
    private static final int FADE_OUT_FRAMES = 30;
    private boolean fadingOut = false;
    private int fadeOutTimer = 0;

    // Slight zoom factor to make text easier to read
    private static final double ZOOM_FACTOR = 1.00; // 7% closer

    public IntroScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
        introImage = ImageLoader.load("Final_Intro.png");

        String prompt = "Press SPACE to start";
        int promptWidth = prompt.length() * 14;
        int px = (GAME_W - promptWidth) / 2 - 12;
        int py = GAME_H - 40;

        pressSpaceCenterText = new SpriteFont(prompt, px, py,
                "Comic Sans MS", 24, new Color(80,160,255));
        pressSpaceCenterText.setOutlineColor(Color.BLACK);
        pressSpaceCenterText.setOutlineThickness(3);

        String cornerPrompt = "Press SPACE to begin";
        int cornerWidth = cornerPrompt.length() * 10;
        int cornerX = GAME_W - cornerWidth - 20;
        int cornerY = 20;

        pressSpaceCornerText = new SpriteFont(cornerPrompt, cornerX, cornerY,
                "Comic Sans MS", 18, new Color(240,200,80));
        pressSpaceCornerText.setOutlineColor(Color.BLACK);
        pressSpaceCornerText.setOutlineThickness(2);

        keyLocker.lockKey(Key.SPACE);
        fadeTimer = 0;
        fadeOutTimer = 0;
        fadingOut = false;

        AudioPlayer.playLoop(INTRO_MUSIC, -6.0f);
    }

    @Override
    public void update() {
        if (!Keyboard.isKeyDown(Key.SPACE)) keyLocker.unlockKey(Key.SPACE);

        if (Keyboard.isKeyDown(Key.SPACE) && !keyLocker.isKeyLocked(Key.SPACE) && !fadingOut) {
            keyLocker.lockKey(Key.SPACE);
            fadingOut = true;
            fadeOutTimer = 0;
        }

        if (fadingOut) {
            fadeOutTimer++;
            if (fadeOutTimer >= FADE_OUT_FRAMES) {
                AudioPlayer.stopAll();
                screenCoordinator.setGameState(GameState.LEVEL);
            }
        } else if (fadeTimer < FADE_FRAMES) {
            fadeTimer++;
        }
    }

    @Override
    public void draw(GraphicsHandler g) {
        // Match background tone to top of book image
        g.drawFilledRectangle(0, 0, GAME_W, GAME_H, new Color(74, 49, 21));

        if (introImage != null) {
            // Fit image, then apply a small zoom (1.07x) without hard crop
            double baseScale = Math.min(
                    (double) GAME_W / introImage.getWidth(),
                    (double) GAME_H / introImage.getHeight()
            );
            double scale = baseScale * ZOOM_FACTOR;

            int w = (int) (introImage.getWidth() * scale);
            int h = (int) (introImage.getHeight() * scale);
            int x = (GAME_W - w) / 2;
            int y = (GAME_H - h) / 2 - 5; // slight upward shift for centering

            g.drawImage(introImage, x, y, w, h);
        }

        // Fade-in
        int fadeAlpha = 255 - (int)(255.0 * Math.min(1.0, fadeTimer / (double) FADE_FRAMES));
        if (fadeAlpha > 0 && !fadingOut) {
            g.drawFilledRectangle(0, 0, GAME_W, GAME_H, new Color(0, 0, 0, fadeAlpha));
        }

        // Fade-out
        if (fadingOut) {
            int alpha = (int)(255.0 * Math.min(1.0, fadeOutTimer / (double) FADE_OUT_FRAMES));
            g.drawFilledRectangle(0, 0, GAME_W, GAME_H, new Color(0, 0, 0, alpha));
        }

        // Texts
        pressSpaceCenterText.draw(g);
        pressSpaceCornerText.draw(g);
    }

    public void unload() {
        AudioPlayer.stopAll();
    }
}
