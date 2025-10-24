package Level;

import Engine.GraphicsHandler;
import Engine.Key;
import Engine.KeyLocker;
import Engine.Keyboard;
import SpriteFont.SpriteFont;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Textbox {
    // whether textbox is shown or not
    protected boolean isActive;

    // ===== TEXTBOX POSITION & SIZE (moved 50px up) =====
    protected final int x = 22;
    protected final int y = 490;                // Moved up (was 540)
    protected final int fontX = 40;
    protected final int fontY = 490;            // Adjusted text start height
    protected final int width = 850;
    protected final int height = 240;

    protected final int optionX = 22;               
    protected final int optionY = y - 130;          
    protected final int optionWidth = 850;             
    protected final int optionHeight = 100;        
    protected final int fontOptionX = 60;           
    protected final int fontOptionYStart = optionY + 20;
    protected final int fontOptionSpacing = 40;     
    protected final int optionPointerX = 30;        
    protected final int optionPointerYStart = fontOptionYStart + 12; 

    // core vars
    private Queue<TextboxItem> textQueue;
    private TextboxItem currentTextItem;
    protected int selectedOptionIndex = 0;
    private SpriteFont text = null;
    private ArrayList<SpriteFont> options = null;
    private KeyLocker keyLocker = new KeyLocker();
    private Key interactKey = Key.SPACE;

    private Map map;

    public Textbox(Map map) {
        this.map = map;
        this.textQueue = new LinkedList<>();
    }

    public void addText(String text) {
        if (textQueue.isEmpty()) keyLocker.lockKey(interactKey);
        textQueue.add(new TextboxItem(text));
    }

    public void addText(String[] text) {
        if (textQueue.isEmpty()) keyLocker.lockKey(interactKey);
        for (String textItem : text) textQueue.add(new TextboxItem(textItem));
    }

    public void addText(TextboxItem text) {
        if (textQueue.isEmpty()) keyLocker.lockKey(interactKey);
        textQueue.add(text);
    }

    public void addText(TextboxItem[] text) {
        if (textQueue.isEmpty()) keyLocker.lockKey(interactKey);
        for (TextboxItem textItem : text) textQueue.add(textItem);
    }

    public boolean isTextQueueEmpty() {
        return textQueue.isEmpty();
    }

    public void update() {
        if (!textQueue.isEmpty() && keyLocker.isKeyLocked(interactKey)) {
            currentTextItem = textQueue.peek();
            options = null;

            // always draw at bottom now (no more top/bottom switch)
            text = new SpriteFont(currentTextItem.getText(), fontX, fontY, "Arial", 32, Color.black);

            if (currentTextItem.getOptions() != null) {
                options = new ArrayList<>();
                for (int i = 0; i < currentTextItem.getOptions().size(); i++) {
                    options.add(new SpriteFont(
                            currentTextItem.options.get(i),
                            fontOptionX,
                            fontOptionYStart + (i * fontOptionSpacing),
                            "Arial",
                            30,
                            Color.black
                    ));
                }
                selectedOptionIndex = 0;
            }
        }

        if (Keyboard.isKeyDown(interactKey) && !keyLocker.isKeyLocked(interactKey)) {
            keyLocker.lockKey(interactKey);
            textQueue.poll();

            if (options != null) {
                map.getActiveScript().getScriptActionOutputManager()
                        .addFlag("TEXTBOX_OPTION_SELECTION", selectedOptionIndex);
            }
        } else if (Keyboard.isKeyUp(interactKey)) {
            keyLocker.unlockKey(interactKey);
        }

        if (options != null) {
            if (Keyboard.isKeyDown(Key.DOWN) && !keyLocker.isKeyLocked(Key.DOWN)) {
                keyLocker.lockKey(Key.DOWN);
                if (selectedOptionIndex < options.size() - 1) selectedOptionIndex++;
            }
            if (Keyboard.isKeyDown(Key.UP) && !keyLocker.isKeyLocked(Key.UP)) {
                keyLocker.lockKey(Key.UP);
                if (selectedOptionIndex > 0) selectedOptionIndex--;
            }
            if (Keyboard.isKeyUp(Key.DOWN)) keyLocker.unlockKey(Key.DOWN);
            if (Keyboard.isKeyUp(Key.UP)) keyLocker.unlockKey(Key.UP);
        }
    }

    public void draw(GraphicsHandler graphicsHandler) {
        // draw textbox (slightly higher)
        graphicsHandler.drawFilledRectangleWithBorder(x, y, width, height, Color.white, Color.black, 2);

        if (text != null) {
            text.drawWithParsedNewLines(graphicsHandler, 10);

            if (options != null) {
                // draw options box below
                graphicsHandler.drawFilledRectangleWithBorder(optionX, optionY, optionWidth, optionHeight, Color.white, Color.black, 2);

                for (SpriteFont option : options) {
                    option.draw(graphicsHandler);
                }

                // draw pointer next to selected option
                graphicsHandler.drawFilledRectangle(optionPointerX, optionPointerYStart + (selectedOptionIndex * fontOptionSpacing), 12, 12, Color.black);
            }
        }
    }

    public boolean isActive() { return isActive; }

    public void setIsActive(boolean isActive) { this.isActive = isActive; }

    public void setInteractKey(Key interactKey) { this.interactKey = interactKey; }
}