//Creates the second room that the user will walk into

package Maps;

import Enemies.Firemonster;
import EnhancedMapTiles.*;
import Level.Enemy;
import Level.EnhancedMapTile;
import Level.Map;
import Tilesets.FireTileset;
import java.util.ArrayList;

public class Fire_2 extends Map {
    
    private Firemonster firemonster;

   public Fire_2() {
        super("Fire_2.txt", new FireTileset());
        this.playerStartPosition =  getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();



        NormalDoor doorC = new NormalDoor(getMapTile(10, 2).getLocation())
        .toMap("Fire_3", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorC);

        NormalDoor doorD = new NormalDoor(getMapTile(10, 12).getLocation())
        .toMap("Fire_1", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorD);

        enhancedMapTiles.add(new Skull(getMapTile(11, 8).getLocation()));
        enhancedMapTiles.add(new Skull(getMapTile(4, 3).getLocation()));

        return enhancedMapTiles;

    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        firemonster = new Firemonster(5, getMapTile(8, 3).getLocation());
        enemies.add(firemonster);
        return enemies;
    }
   
}