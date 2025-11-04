package Scripts.TestMap;

import Level.Script;
import Level.ScriptState;
import NPCs.Shopkeeper;
import ScriptActions.*;
import java.util.ArrayList;

// Script for shopkeeper interaction that opens the shop menu
public class ShopkeeperScript extends Script {
    private Shopkeeper shopkeeper;
    
    public ShopkeeperScript(Shopkeeper shopkeeper) {
        this.shopkeeper = shopkeeper;
    }
    
    @Override
    public ArrayList<ScriptAction> loadScriptActions() {
        ArrayList<ScriptAction> scriptActions = new ArrayList<>();
        
        // Lock player during interaction
        scriptActions.add(new LockPlayerScriptAction());
        
        // Show greeting message
        scriptActions.add(new TextboxScriptAction("Welcome to my shop! What can I get for you?"));
        
        // Custom script action to open shop menu
        scriptActions.add(new ScriptAction() {
            @Override
            public ScriptState execute() {
                shopkeeper.openShop();
                return ScriptState.COMPLETED;
            }
        });
        
        // Unlock player after interaction
        scriptActions.add(new UnlockPlayerScriptAction());
        
        return scriptActions;
    }
}