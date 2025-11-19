//Creates the second room that the user will walk into

package Maps;

import Enemies.*;
import EnhancedMapTiles.*;
import Level.Enemy;
import Level.EnhancedMapTile;
import Level.Map;
import Level.NPC;
import NPCs.KeyWizard;
import Tilesets.FireTileset;
import java.util.ArrayList;

public class Fire_3 extends Map {

    private Fireblob fireblob;
    private Fireblob fireblob1;
    private Fireblob fireblob2;
    private Firemonster firemonster;
    private Firemonster firemonster2;

    
   public Fire_3() {
        super("Fire_3.txt", new FireTileset());
        this.playerStartPosition =  getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();



        Door doorC = new Door(getMapTile(10, 2).getLocation())
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

        enhancedMapTiles.add(new HealthPotion(getMapTile(12, 10).getLocation()));

        return enhancedMapTiles;

    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        fireblob = new Fireblob(5, getMapTile(6, 4).getLocation());
        fireblob1 = new Fireblob(5, getMapTile(8, 4).getLocation());
        fireblob2 = new Fireblob(5, getMapTile(10, 4).getLocation());

        firemonster = new Firemonster(5, getMapTile(6, 3).getLocation());
        enemies.add(firemonster);
        firemonster2 = new Firemonster(5, getMapTile(10, 3).getLocation());
        enemies.add(firemonster2);
        
        enemies.add(fireblob);
        enemies.add(fireblob1);
        enemies.add(fireblob2);

        return enemies;
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