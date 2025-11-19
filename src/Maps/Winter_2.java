package Maps;

import Enemies.Watermonster;
import EnhancedMapTiles.*;
import Level.*;
import Tilesets.*;
import java.util.ArrayList;

// Represents a test map to be used in a level
public class Winter_2 extends Map {

    private Watermonster watermonster;

    public Winter_2() {
        super("Winter_2.txt", new WinterTileset());
        this.playerStartPosition = getMapTile(4, 7).getLocation();
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        //Doors in room 1
        NormalDoor doorB = new NormalDoor(getMapTile(10, 2).getLocation())
        .toMap("Winter_3", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorB);

        //Doors in room 1
        NormalDoor doorC = new NormalDoor(getMapTile(10, 12).getLocation())
        .toMap("Winter_2", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorC);

        enhancedMapTiles.add(new HealthPotion(getMapTile(12, 5).getLocation()));


        return enhancedMapTiles;
    }

    

    @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        watermonster = new Watermonster(5, getMapTile(8, 4).getLocation());
        enemies.add(watermonster);
        
        return enemies;
    }
 
}

