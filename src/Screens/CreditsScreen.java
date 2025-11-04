package Screens;

import Engine.*;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.Map;
import Maps.TitleScreenMap;
import SpriteFont.SpriteFont;

import java.awt.*;
import java.awt.image.BufferedImage;

// This class is for the credits screen
public class CreditsScreen extends Screen {
    protected ScreenCoordinator screenCoordinator;
    protected KeyLocker keyLocker = new KeyLocker();
    protected SpriteFont controlsLabel;
    protected SpriteFont forwardLabel;
    protected SpriteFont leftLabel;
    protected SpriteFont rightLabel;
    protected SpriteFont backwardLabel;
    protected SpriteFont atkUpLabel;
    protected SpriteFont atkStraightLabel;
    protected SpriteFont atkDownLabel;
    protected SpriteFont interactLabel;
    protected SpriteFont returnInstructionsLabel;
    protected SpriteFont movementLabel;
    protected SpriteFont attackLabel;
    protected SpriteFont interactingLabel;
    private BufferedImage creditsBG;

    public CreditsScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
        // setup graphics on screen (background map, spritefont text)
        creditsBG = ImageLoader.load("Sada/CreditsScreen.png");
        controlsLabel = new SpriteFont("Controls", 15, 7, "Times New Roman", 35, Color.white);
        movementLabel = new SpriteFont("Movement Keys", 15, 50, "Times New Roman", 25, Color.white);
        forwardLabel = new SpriteFont("To move forward press W", 15, 80, "Times New Roman", 20, Color.white);
        leftLabel = new SpriteFont("To move left press A", 15, 100, "Times New Roman", 20, Color.white);
        rightLabel = new SpriteFont("To move right press D", 15, 120, "Times New Roman", 20, Color.white);
        backwardLabel = new SpriteFont("To move backwards press S", 15, 140, "Times New Roman", 20, Color.white);
        attackLabel = new SpriteFont("Attack Keys", 265, 50, "Times New Roman", 25, Color.white);
        atkUpLabel = new SpriteFont("To attack up press the up arrow",265, 80, "Times New Roman", 20, Color.white);
        atkStraightLabel = new SpriteFont("To attack straight press the right or left arrow", 265, 100, "Times New Roman", 20, Color.white);
        atkDownLabel = new SpriteFont("To attack left press A", 265, 120, "Times New Roman", 20, Color.white);
        interactingLabel = new SpriteFont("Interacting Key", 15, 183, "Times New Roman", 25, Color.white);
        interactLabel = new SpriteFont("To interact press E", 15, 216, "Times New Roman", 20, Color.white);
        returnInstructionsLabel = new SpriteFont("Press Space to return to the menu", 20, 532, "Times New Roman", 30, Color.white);
        keyLocker.lockKey(Key.SPACE);
    }

    public void update() {

        if (Keyboard.isKeyUp(Key.SPACE)) {
            keyLocker.unlockKey(Key.SPACE);
        }

        // if space is pressed, go back to main menu
        if (!keyLocker.isKeyLocked(Key.SPACE) && Keyboard.isKeyDown(Key.SPACE)) {
            screenCoordinator.setGameState(GameState.MENU);
        }
    }

    public void draw(GraphicsHandler graphicsHandler) {
        if (creditsBG != null) {
            double scale = Math.max(
                (double) 800 / creditsBG.getWidth(),   // use your game's width
                (double) 600 / creditsBG.getHeight()   // use your game's height
            );
            int drawW = (int) (creditsBG.getWidth() * scale);
            int drawH = (int) (creditsBG.getHeight() * scale);
            int drawX = (800 - drawW) / 2;  // center horizontally
            int drawY = (600 - drawH) / 2;  // center vertically
            graphicsHandler.drawImage(creditsBG, drawX, drawY, drawW, drawH); // use graphicsHandler, not g
        } else {
            graphicsHandler.drawFilledRectangle(0, 0, 800, 600, Color.BLACK);
        }
        controlsLabel.draw(graphicsHandler);
        forwardLabel.draw(graphicsHandler);
        leftLabel.draw(graphicsHandler);
        rightLabel.draw(graphicsHandler);
        backwardLabel.draw(graphicsHandler);
        atkUpLabel.draw(graphicsHandler);
        atkStraightLabel.draw(graphicsHandler);
        atkDownLabel.draw(graphicsHandler);
        interactLabel.draw(graphicsHandler);
        movementLabel.draw(graphicsHandler);
        attackLabel.draw(graphicsHandler);
        interactingLabel.draw(graphicsHandler);
        returnInstructionsLabel.draw(graphicsHandler);
    }
}
