//Creates the second room that the user will walk into

package Maps;

import EnhancedMapTiles.*;
import Level.EnhancedMapTile;
import Level.Map;
import Level.NPC;
import NPCs.KeyWizard;
import Tilesets.FireTileset;
import java.util.ArrayList;

public class Fire_3 extends Map {
   public Fire_3() {
        super("Fire_3.txt", new FireTileset());
        this.playerStartPosition =  getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();



        NormalDoor doorC = new NormalDoor(getMapTile(10, 2).getLocation())
        .toMap("Fire_4", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorC);

        NormalDoor doorD = new NormalDoor(getMapTile(10, 12).getLocation())
        .toMap("Fire_2", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorD);

        enhancedMapTiles.add(new Skull(getMapTile(3, 8).getLocation()));
        enhancedMapTiles.add(new Skull(getMapTile(9, 3).getLocation()));
        enhancedMapTiles.add(new Skull(getMapTile(11, 9).getLocation()));

        return enhancedMapTiles;

    }

    @Override
    public ArrayList<NPC> loadNPCs() {
        ArrayList<NPC> npcs = new ArrayList<>();
        KeyWizard keywizard3 = new KeyWizard(1, getMapTile(15, 2).getLocation());
        keywizard3.setInteractScript(new Scripts.TestMap.KeyWizardRiddleScript3(getPlayer(), keywizard3));
        npcs.add(keywizard3);

        return npcs;
    }
   
}