package Maps;

import Enemies.Skeleton;
import EnhancedMapTiles.*;
import Level.*;
import Tilesets.DesertTileset;
import java.util.ArrayList;

// Represents a test map to be used in a level
public class Desert_3 extends Map {
    
    private Skeleton skeleton;

    public Desert_3() {
        super("Desert_3.txt", new DesertTileset());
        this.playerStartPosition = getMapTile(4, 7).getLocation();
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        //Doors in room 1
        NormalDoor doorB = new NormalDoor(getMapTile(10, 1).getLocation())
        .toMap("Desert_4", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorB);

        //Doors in room 1
        NormalDoor doorC = new NormalDoor(getMapTile(10, 12).getLocation())
        .toMap("Desert_2", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorC);

        enhancedMapTiles.add(new HealthPotion(getMapTile(4, 8).getLocation()));


        return enhancedMapTiles;
    }


    @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        skeleton = new Skeleton(11, getMapTile(10, 4).getLocation());
        enemies.add(skeleton);
        return enemies;
    } 
    
}

