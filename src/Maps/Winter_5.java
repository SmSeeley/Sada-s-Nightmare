package Maps;


import Enemies.Snowboss;
import EnhancedMapTiles.*;
import Level.*;
import Tilesets.*;
import java.util.ArrayList;

// Represents a test map to be used in a level
public class Winter_5 extends Map {

    private Snowboss snowboss;

    private Map currentMap;

    public Winter_5() {
        super("Winter_5.txt", new WinterTileset());
        this.playerStartPosition = getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {

        

        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        //Doors in room 1
         //Doors in room 1
        LockedDreamDoor doorB = new LockedDreamDoor(getMapTile(10, 2).getLocation())
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
        snowboss = new Snowboss(12, getMapTile(10, 4).getLocation(),this);
        enemies.add(snowboss);
        return enemies;
    }      
 
 
}

