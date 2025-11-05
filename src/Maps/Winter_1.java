package Maps;

import Enemies.Snowmonster;
import EnhancedMapTiles.*;
import Level.*;
import Tilesets.*;
import java.util.ArrayList;

// Represents a test map to be used in a level
public class Winter_1 extends Map {

    private Snowmonster snowmonster;

    public Winter_1() {
        super("Winter_1.txt", new WinterTileset());
        this.playerStartPosition = getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();


        //Doors in room 1
        NormalDoor doorB = new NormalDoor(getMapTile(10, 2).getLocation())
        .toMap("Winter_2", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorB);

        return enhancedMapTiles;


    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        snowmonster = new Snowmonster(5, getMapTile(14, 5).getLocation());
        enemies.add(snowmonster);
        
        return enemies;
    }
 
}

