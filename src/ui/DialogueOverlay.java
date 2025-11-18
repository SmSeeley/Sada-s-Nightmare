package ui;

import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.Key;
import Engine.KeyLocker;
import Engine.Keyboard;
import SpriteFont.SpriteFont;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * JRPG-style blocking dialogue overlay with portrait + textbox.
 * - Uses a pixel font loaded from Resources/fonts/ (PixelOperator.ttf by default)
 * - Typewriter effect (SPACE to fast-forward / next)
 * - Blocks gameplay while active (use isBlocking())
 * - Gold border drawn as 4 filled strips to avoid edge clipping
 */
public class DialogueOverlay {

    // Match your window size
    public static final int SCREEN_W = 800;
    public static final int SCREEN_H = 600;

    // --- Panel layout ---
    private static final int BOX_HEIGHT       = 120;
    private static final int BOX_MARGIN_SIDE  = 16;
    private static final int PANEL_BOTTOM_GAP = 35;
    private static final int SAFE_PAD         = 5;

    // --- Border (drawn as 4 strips) ---
    private static final int  BORDER_THICK  = 3;
    private static final Color PANEL_BORDER = new Color(210, 170, 60);

    // --- Portrait + placement ---
    private static final int PORTRAIT_SIZE     = 90;  // balanced
    private static final int PORTRAIT_INSET    = 14;
    private static final int PORTRAIT_GAP      = 10;
    private static final int PORTRAIT_Y_OFFSET = -2;  // a touch higher
    // Nudge the image inside its frame to reduce the top gap
    private static final int PORTRAIT_IMAGE_DX = 0;
    private static final int PORTRAIT_IMAGE_DY = 2;   // move image down slightly in the frame

    // --- Pixel Font config ---
    private static final String PIXEL_FONT_PATH = "Resources/fonts/PixelOperator.ttf";
    private static final int    TEXT_FONT_SIZE  = 18;  // crisp size for pixel fonts
    private static final int    TEXT_LINE_SPACING = 4;
    private static final int    LINE_HEIGHT = TEXT_FONT_SIZE + TEXT_LINE_SPACING;

    // --- Colors ---
    private static final Color PANEL_FILL  = new Color(16, 16, 20, 235);
    private static final Color NAME_FILL   = new Color(24, 24, 28, 240);
    private static final Color NAME_BORDER = new Color(210, 170, 60);

    // --- Typewriter timing ---
    private static final int CHARS_PER_SECOND = 60;
    private static final int CHAR_FRAMES = Math.max(1, 60 / CHARS_PER_SECOND);

    // --- Computed panel rect (with safe padding) ---
    private static final int PANEL_X = BOX_MARGIN_SIDE + SAFE_PAD;
    private static final int PANEL_W = SCREEN_W - (BOX_MARGIN_SIDE + SAFE_PAD) * 2;
    private static final int PANEL_Y = SCREEN_H - BOX_HEIGHT - PANEL_BOTTOM_GAP - SAFE_PAD;
    private static final int PANEL_H = BOX_HEIGHT - SAFE_PAD * 2;

    // --- Computed portrait + text anchors ---
    private static final int PORTRAIT_X = PANEL_X + PORTRAIT_INSET;
    private static final int PORTRAIT_Y = PANEL_Y + PORTRAIT_INSET + PORTRAIT_Y_OFFSET;

    private static final int TEXT_X = PORTRAIT_X + PORTRAIT_SIZE + PORTRAIT_GAP;
    private static final int TEXT_Y = PANEL_Y + 10;
    private static final int TEXT_W = PANEL_X + PANEL_W - TEXT_X - 14;

    // --- State ---
    private final KeyLocker keyLocker = new KeyLocker();
    private BufferedImage portrait;
    private final Deque<String> pages = new ArrayDeque<>();

    private SpriteFont line1, line2, line3, continueHint;
    private int typeIndex = 0, typeTimer = 0;
    private String visibleText = "", currentPageFull = "";
    private boolean active = false, pageComplete = false;

    // Font + metrics for wrapping
    private Font pixelFont;
    private FontMetrics metrics;

    public DialogueOverlay() {
        // Load and register pixel font
        loadPixelFont();

        // Continue hint (uses pixel font family name + a small size)
        continueHint = new SpriteFont(
                "Press SPACE",
                PANEL_X + PANEL_W - 130,
                PANEL_Y + 14,
                pixelFont.getFontName(), 14,
                new Color(240, 200, 80)
        );
        continueHint.setOutlineColor(Color.BLACK);
        continueHint.setOutlineThickness(2);

        // Create line SpriteFonts using the pixel font family + size
        line1 = makeLine(TEXT_X, TEXT_Y);
        line2 = makeLine(TEXT_X, TEXT_Y + LINE_HEIGHT);
        line3 = makeLine(TEXT_X, TEXT_Y + LINE_HEIGHT * 2);
    }

    private void loadPixelFont() {
        try {
            pixelFont = Font.createFont(Font.TRUETYPE_FONT, new File(PIXEL_FONT_PATH))
                            .deriveFont(Font.PLAIN, TEXT_FONT_SIZE);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(pixelFont);
        } catch (Exception e) {
            System.out.println("[DialogueOverlay] Could not load " + PIXEL_FONT_PATH + " -> falling back to Monospaced.");
            pixelFont = new Font("Monospaced", Font.PLAIN, TEXT_FONT_SIZE);
        }

        // Build metrics for wrapping with this font
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = tmp.createGraphics();
        g2.setFont(pixelFont);
        metrics = g2.getFontMetrics();
        g2.dispose();
    }

    private SpriteFont makeLine(int x, int y) {
        SpriteFont line = new SpriteFont("", x, y, pixelFont.getFontName(), pixelFont.getSize(), Color.WHITE);
        line.setOutlineColor(Color.BLACK);
        line.setOutlineThickness(3);
        return line;
    }

    /** Start dialogue with optional portrait and 1..N pages. */
    public void start(String portraitPath, String... textPages) {
        portrait = null;
        if (portraitPath != null && !portraitPath.isEmpty()) {
            portrait = ImageLoader.load(portraitPath);
        }
        pages.clear();
        pages.addAll(Arrays.asList(textPages));

        loadNextPage();
        active = true;
        pageComplete = false;

        keyLocker.lockKey(Key.SPACE);
    }

    /** True when overlay is visible and should block gameplay. */
    public boolean isActive()   { return active; }
    public boolean isBlocking() { return active; }

    /** Update typewriter & input. Call from your Screen.update(). */
    public void update() {
        if (!active) return;

        if (!pageComplete && ++typeTimer >= CHAR_FRAMES) {
            typeTimer = 0;
            typeIndex = Math.min(typeIndex + 1, currentPageFull.length());
            visibleText = currentPageFull.substring(0, typeIndex);
            if (typeIndex >= currentPageFull.length()) pageComplete = true;
        }

        if (!Keyboard.isKeyDown(Key.SPACE)) keyLocker.unlockKey(Key.SPACE);

        if (Keyboard.isKeyDown(Key.SPACE) && !keyLocker.isKeyLocked(Key.SPACE)) {
            keyLocker.lockKey(Key.SPACE);
            if (!pageComplete) {
                typeIndex = currentPageFull.length();
                visibleText = currentPageFull;
                pageComplete = true;
            } else if (!pages.isEmpty()) {
                loadNextPage();
            } else {
                active = false;
            }
        }
    }

    /** Draw panel, portrait, text. Call last in Screen.draw(). */
    public void draw(GraphicsHandler g) {
        if (!active) return;

        // Panel background (slightly shrunk so borders sit fully inside)
        g.drawFilledRectangle(PANEL_X, PANEL_Y, PANEL_W - 1, PANEL_H - 1, PANEL_FILL);

        // Gold border (four strips)
        g.drawFilledRectangle(PANEL_X, PANEL_Y, PANEL_W - 1, BORDER_THICK, PANEL_BORDER);
        g.drawFilledRectangle(PANEL_X, PANEL_Y + PANEL_H - 1 - BORDER_THICK, PANEL_W - 1, BORDER_THICK, PANEL_BORDER);
        g.drawFilledRectangle(PANEL_X, PANEL_Y, BORDER_THICK, PANEL_H - 1, PANEL_BORDER);
        g.drawFilledRectangle(PANEL_X + PANEL_W - 1 - BORDER_THICK, PANEL_Y, BORDER_THICK, PANEL_H - 1, PANEL_BORDER);

        // Portrait frame + image (image nudged down to reduce top gap)
        if (portrait != null) {
            // frame
            g.drawFilledRectangle(
                    PORTRAIT_X - 5, PORTRAIT_Y - 5,
                    PORTRAIT_SIZE + 10, PORTRAIT_SIZE + 10, NAME_FILL
            );
            g.drawFilledRectangle(PORTRAIT_X - 5, PORTRAIT_Y - 5, PORTRAIT_SIZE + 10, 2, NAME_BORDER);
            g.drawFilledRectangle(PORTRAIT_X - 5, PORTRAIT_Y + PORTRAIT_SIZE + 5, PORTRAIT_SIZE + 10, 2, NAME_BORDER);
            g.drawFilledRectangle(PORTRAIT_X - 5, PORTRAIT_Y - 5, 2, PORTRAIT_SIZE + 10, NAME_BORDER);
            g.drawFilledRectangle(PORTRAIT_X + PORTRAIT_SIZE + 5, PORTRAIT_Y - 5, 2, PORTRAIT_SIZE + 10, NAME_BORDER);

            // image inside frame (with tiny downward nudge)
            g.drawImage(
                    portrait,
                    PORTRAIT_X + PORTRAIT_IMAGE_DX,
                    PORTRAIT_Y + PORTRAIT_IMAGE_DY,
                    PORTRAIT_SIZE,
                    PORTRAIT_SIZE
            );
        }

        // Text (wrapped)
        layoutText(visibleText);
        line1.draw(g);
        line2.draw(g);
        line3.draw(g);

        if (pageComplete) continueHint.draw(g);
    }

    // ----- helpers -----

    private void loadNextPage() {
        currentPageFull = pages.removeFirst().replace("\r", "");
        typeIndex = 0;
        typeTimer = 0;
        visibleText = "";
        pageComplete = false;

        line1.setText("");
        line2.setText("");
        line3.setText("");
    }

    /** Greedy wrap into at most 3 lines using FontMetrics. */
    private void layoutText(String text) {
        line1.setText("");
        line2.setText("");
        line3.setText("");
        if (text == null || text.isEmpty()) return;

        String[] words = text.split("\\s+");
        StringBuilder b1 = new StringBuilder();
        StringBuilder b2 = new StringBuilder();
        StringBuilder b3 = new StringBuilder();

        for (String w : words) {
            if (tryAppend(b1, w)) continue;
            if (tryAppend(b2, w)) continue;
            tryAppend(b3, w);
        }

        line1.setText(b1.toString());
        line2.setText(b2.toString());
        line3.setText(b3.toString());
    }

    /** Try to append a word to a line if the width stays within bounds. */
    private boolean tryAppend(StringBuilder sb, String word) {
        String candidate = (sb.length() == 0 ? word : sb + " " + word);
        int width = metrics.stringWidth(candidate);
        if (width <= TEXT_W) {
            sb.setLength(0);
            sb.append(candidate);
            return true;
        }
        return false;
    }
}
