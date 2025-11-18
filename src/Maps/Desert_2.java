package Maps;


import EnhancedMapTiles.*;
import Level.*;
import Tilesets.DesertTileset;
import Utils.Point;
import NPCs.KeyWizard;

import java.util.ArrayList;

// Represents a test map to be used in a level
public class Desert_2 extends Map {

    public Desert_2() {
        super("Desert_1.txt", new DesertTileset());
        this.playerStartPosition = getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        //Doors in room 1
        NormalDoor doorB = new NormalDoor(getMapTile(10, 1).getLocation())
        .toMap("Desert_3", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorB);

        //Doors in room 1
        NormalDoor doorC = new NormalDoor(getMapTile(10, 12).getLocation())
        .toMap("Desert_1", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorC);

        Point potionLoc = getMapTile(12, 5).getLocation();
        if (!HealthPotion.isCollectedAt(potionLoc)) {
            enhancedMapTiles.add(new HealthPotion(potionLoc));
        }

        return enhancedMapTiles;


    }

    @Override
    public ArrayList<NPC> loadNPCs() {
        ArrayList<NPC> npcs = new ArrayList<>();
        KeyWizard keywizard1 = new KeyWizard(1, getMapTile(15, 2).getLocation());
        keywizard1.setInteractScript(new Scripts.TestMap.KeyWizardRiddleScript1(getPlayer(), keywizard1));
        npcs.add(keywizard1);

        return npcs;
    }

    
 
}

