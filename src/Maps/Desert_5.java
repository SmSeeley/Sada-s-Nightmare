package Maps;


import Enemies.Desertboss;
import EnhancedMapTiles.*;
import Level.*;
import Tilesets.DesertTileset;
import Utils.Point;

import java.util.ArrayList;

// Represents a test map to be used in a level
public class Desert_5 extends Map {

    private Desertboss desertboss;

    private boolean keySpawned = false; 

    public Desert_5() {
        super("Desert_5.txt", new DesertTileset());
        this.playerStartPosition = getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        //Doors in room 1
        LockedDreamDoor doorB = new LockedDreamDoor(getMapTile(10, 1).getLocation())
        .toMap("TheHub1", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorB);

        Point potionLoc = getMapTile(6, 10).getLocation();
        if (!HealthPotion.isCollectedAt(potionLoc)) {
            enhancedMapTiles.add(new HealthPotion(potionLoc));
        }

        Point potionLoc2 = getMapTile(14, 10).getLocation();
        if (!HealthPotion.isCollectedAt(potionLoc2)) {
            enhancedMapTiles.add(new HealthPotion(potionLoc2));
        }



        return enhancedMapTiles;

    }

        @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        desertboss = new Desertboss(5, getMapTile(10, 4).getLocation(),this);
        enemies.add(desertboss);
        
        return enemies;
    }
}

