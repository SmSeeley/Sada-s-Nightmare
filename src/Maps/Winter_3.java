package Maps;


import EnhancedMapTiles.*;
import Level.*;
import Tilesets.*;
import NPCs.KeyWizard;
import java.util.ArrayList;
import Enemies.IceMan;

// Represents a test map to be used in a level
public class Winter_3 extends Map {
    private IceMan iceman;
    public Winter_3() {
        super("Winter_3.txt", new WinterTileset());
        this.playerStartPosition = getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        //Doors in room 1
        NormalDoor doorB = new NormalDoor(getMapTile(10, 2).getLocation())
        .toMap("Winter_4", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorB);

        //Doors in room 1
        NormalDoor doorC = new NormalDoor(getMapTile(10, 12).getLocation())
        .toMap("Winter_3", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorC);


        return enhancedMapTiles;


    }

    @Override
    public ArrayList<NPC> loadNPCs() {
        ArrayList<NPC> npcs = new ArrayList<>();
        KeyWizard keywizard2 = new KeyWizard(1, getMapTile(15, 2).getLocation());
        keywizard2.setInteractScript(new Scripts.TestMap.KeyWizardRiddleScript2(getPlayer(), keywizard2));
        npcs.add(keywizard2);

        return npcs;
    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        iceman = new IceMan(5, getMapTile(14, 5).getLocation());
        enemies.add(iceman);
        
        return enemies;
    }
 
}

