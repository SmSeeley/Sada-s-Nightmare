package Level;

import Engine.GraphicsHandler;
import Engine.Key;
import Engine.KeyLocker;
import Engine.Keyboard;
import Engine.ScreenManager;
import SpriteFont.SpriteFont;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Textbox {
    // visibility
    protected boolean isActive;

    // ---- configurable layout (now non-final so we can adjust) ----
    protected int x = 24;
    protected int y = 420;                 // higher on screen (was 490)
    protected int width = 752;             // fits 800x600 with small margins
    protected int height = 120;            // shorter so it doesn't cover gameplay

    // text padding / layout
    protected int padding = 16;
    protected int fontSize = 26;           // smaller so it’s not huge
    protected String fontFamily = "Arial";

    // options panel (drawn ABOVE textbox by default)
    protected int optionHeight = 96;
    protected int optionLineSpacing = 36;

    // computed each frame
    protected int textStartX = x + padding;
    protected int textStartY = y + padding + 8;   // slight breathing room
    protected int optionX = x;
    protected int optionY = y - optionHeight - 10;
    protected int optionWidth = width;
    protected int optionPointerX = optionX + 8;
    protected int optionPointerYOffset = 14;      // pointer aligns mid text

    // core vars
    private final Queue<TextboxItem> textQueue;
    private TextboxItem currentTextItem;
    protected int selectedOptionIndex = 0;
    private SpriteFont text = null;
    private ArrayList<SpriteFont> options = null;
    private final KeyLocker keyLocker = new KeyLocker();
    private Key interactKey = Key.SPACE;

    private final Map map;

    public Textbox(Map map) {
        this.map = map;
        this.textQueue = new LinkedList<>();
        recalcLayout();
    }

    // ---- Public simple setters (use from PlayLevelScreen if desired) ----
    public void setPosition(int x, int y) { this.x = x; this.y = y; recalcLayout(); }
    public void setBoxSize(int w, int h) { this.width = Math.max(200, w); this.height = Math.max(80, h); recalcLayout(); }
    public void setPadding(int p) { this.padding = Math.max(0, p); recalcLayout(); }
    public void setFont(String family, int size) { this.fontFamily = family; this.fontSize = Math.max(10, size); }
    public void setOptionHeight(int h) { this.optionHeight = Math.max(60, h); recalcLayout(); }
    public void setOptionLineSpacing(int s) { this.optionLineSpacing = Math.max(16, s); }

    private void recalcLayout() {
        // clamp into screen so it never goes off-screen
        int screenW = ScreenManager.getScreenWidth();
        int screenH = ScreenManager.getScreenHeight();

        width = Math.min(width, screenW - 4);
        height = Math.min(height, screenH - 4);

        if (x + width > screenW) x = Math.max(0, screenW - width - 2);
        if (y + height > screenH) y = Math.max(0, screenH - height - 2);
        if (x < 0) x = 0;
        if (y < 0) y = 0;

        textStartX = x + padding;
        textStartY = y + padding + 8;

        optionWidth = width;
        optionX = x;
        optionY = y - optionHeight - 10; // draw options above the box by default
        optionPointerX = optionX + 8;
    }

    // ---- Existing API kept intact below ----
    public void addText(String text) {
        if (textQueue.isEmpty()) keyLocker.lockKey(interactKey);
        textQueue.add(new TextboxItem(text));
    }

    public void addText(String[] text) {
        if (textQueue.isEmpty()) keyLocker.lockKey(interactKey);
        for (String t : text) textQueue.add(new TextboxItem(t));
    }

    public void addText(TextboxItem text) {
        if (textQueue.isEmpty()) keyLocker.lockKey(interactKey);
        textQueue.add(text);
    }

    public void addText(TextboxItem[] text) {
        if (textQueue.isEmpty()) keyLocker.lockKey(interactKey);
        for (TextboxItem t : text) textQueue.add(t);
    }

    public boolean isTextQueueEmpty() { return textQueue.isEmpty(); }

    public void update() {
        if (!isActive) return;

        if (!textQueue.isEmpty() && keyLocker.isKeyLocked(interactKey)) {
            currentTextItem = textQueue.peek();
            options = null;

            // build the main text sprite (smaller font, draws inside padded area)
            text = new SpriteFont(currentTextItem.getText(), textStartX, textStartY, fontFamily, fontSize, Color.black);

            if (currentTextItem.getOptions() != null) {
                options = new ArrayList<>();
                for (int i = 0; i < currentTextItem.getOptions().size(); i++) {
                    options.add(new SpriteFont(
                            currentTextItem.getOptions().get(i),
                            optionX + padding + 24,
                            optionY + padding + (i * optionLineSpacing),
                            fontFamily,
                            fontSize,
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

    public void draw(GraphicsHandler g) {
        if (!isActive) return;

        // (re)clamp in case the screen size changed
        recalcLayout();

        // textbox panel
        g.drawFilledRectangleWithBorder(x, y, width, height, Color.white, Color.black, 2);

        // text
        if (text != null) {
            // You can increase the "10" to add more line spacing if needed
            text.drawWithParsedNewLines(g, 8);
        }

        // options panel
        if (options != null) {
            g.drawFilledRectangleWithBorder(optionX, optionY, optionWidth, optionHeight, Color.white, Color.black, 2);

            for (int i = 0; i < options.size(); i++) {
                options.get(i).draw(g);
            }

            int ptrY = optionY + padding + optionPointerYOffset + (selectedOptionIndex * optionLineSpacing);
            g.drawFilledRectangle(optionPointerX, ptrY, 12, 12, Color.black);
        }
    }

    public boolean isActive() { return isActive; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }
    public void setInteractKey(Key interactKey) { this.interactKey = interactKey; }
}
