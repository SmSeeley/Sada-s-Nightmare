package NPCs;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.Key;
import Engine.KeyLocker;
import Engine.Keyboard;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.NPC;
import Level.Player;
import Level.Script;
import Level.ScriptState;
import ScriptActions.ScriptAction;
import java.util.ArrayList;
import SpriteFont.SpriteFont;
import Utils.Point;
import java.awt.Color;
import java.util.HashMap;

//Class for shopkeeper NPC
public class Shopkeeper extends NPC {
    private boolean shopMenuOpen = false;
    private int selectedOption = 0;
    private final int maxOptions = 4; // Updated from 5 to 4
    private KeyLocker menuKeyLocker = new KeyLocker();
    
    // Shop items and prices
    private final String[] shopItems = {
        "Health Potion (+1 heart) - 3 coins", 
        "Angel Sword (+5 Damage) - 10 coins",
        "Watermelon (+3 Damage) - 5 coins",
        "Exit Shop"
    };
    private final int[] itemPrices = {3, 10, 5, 0}; // 0 for exit
    
    public Shopkeeper(int id, Point location) {
        super(id, location.x, location.y, new SpriteSheet(ImageLoader.load("Shopkeeper.png"), 24, 24), "STAND_DOWN");
        this.setIsUncollidable(true);
        this.interactScript = new ShopScript();
    }

    // Add the openShop method that's called by the script
    public void openShop() {
        shopMenuOpen = true;
        selectedOption = 0;
    }

    @Override
    public void update(Player player) {
        super.update(player);
        
        if (shopMenuOpen) {
            handleShopMenu(player);
        }
    }
    
    private void handleShopMenu(Player player) {
        // Navigate menu with arrow keys
        if (Keyboard.isKeyDown(Key.UP) && !menuKeyLocker.isKeyLocked(Key.UP)) {
            selectedOption = (selectedOption - 1 + maxOptions) % maxOptions;
            menuKeyLocker.lockKey(Key.UP);
        }
        if (Keyboard.isKeyDown(Key.DOWN) && !menuKeyLocker.isKeyLocked(Key.DOWN)) {
            selectedOption = (selectedOption + 1) % maxOptions;
            menuKeyLocker.lockKey(Key.DOWN);
        }
        
        // Select option with SPACE
        if (Keyboard.isKeyDown(Key.SPACE) && !menuKeyLocker.isKeyLocked(Key.SPACE)) {
            selectShopOption(player);
            menuKeyLocker.lockKey(Key.SPACE);
        }
        
        // Close menu with ESCAPE
        if (Keyboard.isKeyDown(Key.ESC) && !menuKeyLocker.isKeyLocked(Key.ESC)) {
            shopMenuOpen = false;
            menuKeyLocker.lockKey(Key.ESC);
        }
        
        // Unlock keys when released
        if (!Keyboard.isKeyDown(Key.UP)) menuKeyLocker.unlockKey(Key.UP);
        if (!Keyboard.isKeyDown(Key.DOWN)) menuKeyLocker.unlockKey(Key.DOWN);
        if (!Keyboard.isKeyDown(Key.SPACE)) menuKeyLocker.unlockKey(Key.SPACE);
        if (!Keyboard.isKeyDown(Key.ESC)) menuKeyLocker.unlockKey(Key.ESC);
    }
    
    private void selectShopOption(Player player) {
        if (selectedOption == 3) { // Exit shop (updated from 4 to 3)
            shopMenuOpen = false;
            return;
        }
        
        // Check if player has enough coins
        try {
            Class<?> coinClass = Class.forName("EnhancedMapTiles.Coin");
            java.lang.reflect.Field coinsField = coinClass.getDeclaredField("coinsCollected");
            coinsField.setAccessible(true);
            int playerCoins = coinsField.getInt(null);
            
            if (playerCoins >= itemPrices[selectedOption]) {
                // Purchase item
                coinsField.setInt(null, playerCoins - itemPrices[selectedOption]);
                
                switch (selectedOption) {
                    case 0: // Health Potion
                       // Give player 1 heart using player's heal method
                       player.heal(5);
                       System.out.println("Purchased Health Potion! +1 Heart added!");
                        break;
                    case 1: // Angel Sword
                        // Give player Angel Sword
                        try {
                            Class<?> angelSwordClass = Class.forName("EnhancedMapTiles.AngelSword");
                            java.lang.reflect.Method giveMethod = angelSwordClass.getMethod("giveToPlayer", Player.class);
                            giveMethod.invoke(null, player);
                            System.out.println("Purchased Angel Sword! Divine power acquired!");
                        } catch (Exception e) {
                            System.out.println("Purchased Angel Sword! Failed to equip.");
                        }
                        break;
                    case 2: // Watermelon
                        // Give player Watermelon
                        try {
                            Class<?> watermelonClass = Class.forName("EnhancedMapTiles.Watermelon");
                            java.lang.reflect.Method giveMethod = watermelonClass.getMethod("giveToPlayer", Player.class);
                            giveMethod.invoke(null, player);
                            System.out.println("Purchased Watermelon! Fruity power obtained!");
                        } catch (Exception e) {
                            System.out.println("Purchased Watermelon! Failed to equip.");
                        }
                        break;
                }
                shopMenuOpen = false;
            } else {
                System.out.println("Not enough coins!");
            }
        } catch (Exception e) {
            System.out.println("Error accessing coin count.");
        }
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("STAND_DOWN", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(3)
                    .withBounds(7, 13, 11, 7)
                    .build()
            });
            put("STAND_UP", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 1))
                    .withScale(3)
                    .withBounds(7, 13, 11, 7)
                    .build()
            });
            put("STAND_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 2))
                    .withScale(3)
                    .withBounds(7, 13, 11, 7)
                    .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .build()
            });
            put("STAND_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 2))
                    .withScale(3)
                    .withBounds(7, 13, 11, 7)
                    .build()
            });
        }};
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
        
        if (shopMenuOpen) {
            drawShopMenu(graphicsHandler);
        }
    }
    
    private void drawShopMenu(GraphicsHandler graphicsHandler) {
        // Draw shop background with overlay
        graphicsHandler.drawFilledRectangle(0, 0, 800, 600, new Color(0, 0, 0, 150));
        graphicsHandler.drawFilledRectangle(50, 100, 500, 300, Color.BLACK);
        graphicsHandler.drawRectangle(50, 100, 500, 300, Color.WHITE, 3);
        
        // Draw shop title
        SpriteFont titleFont = new SpriteFont("SHOP", 250, 130, "Arial", 24, Color.WHITE);
        titleFont.draw(graphicsHandler);
        
        // Draw coin count
        try {
            Class<?> coinClass = Class.forName("EnhancedMapTiles.Coin");
            java.lang.reflect.Field coinsField = coinClass.getDeclaredField("coinsCollected");
            coinsField.setAccessible(true);
            int playerCoins = coinsField.getInt(null);
            SpriteFont coinFont = new SpriteFont("Coins: " + playerCoins, 70, 160, "Arial", 16, Color.YELLOW);
            coinFont.draw(graphicsHandler);
        } catch (Exception e) {
            // ignore if can't access coins
        }
        
        // Draw shop options
        for (int i = 0; i < shopItems.length; i++) {
            Color textColor = (i == selectedOption) ? Color.YELLOW : Color.WHITE;
            String prefix = (i == selectedOption) ? "> " : "  ";
            
            SpriteFont optionFont = new SpriteFont(prefix + shopItems[i], 70, 200 + (i * 30), "Arial", 16, textColor);
            optionFont.draw(graphicsHandler);
        }
        
        // Draw controls
        SpriteFont controlsFont = new SpriteFont("UP/DOWN: Navigate  SPACE: Select  ESC: Exit", 70, 350, "Arial", 12, Color.GRAY);
        controlsFont.draw(graphicsHandler);
    }

    // Script class to handle interaction
    private class ShopScript extends Script {
        @Override
    public ArrayList<ScriptAction> loadScriptActions() {
        ArrayList<ScriptAction> scriptActions = new ArrayList<>();
        
        scriptActions.add(new ScriptAction() {
            @Override
            public ScriptState execute() {
                openShop();
                return ScriptState.COMPLETED;
            }
        });
        
            return scriptActions;
        }
    }
}