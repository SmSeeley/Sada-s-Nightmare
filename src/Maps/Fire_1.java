//Creates the second room that the user will walk into

package Maps;

import Enemies.Fireblob;
import EnhancedMapTiles.*;
import Level.Enemy;
import Level.EnhancedMapTile;
import Level.Map;
import Tilesets.FireTileset;
import java.util.ArrayList;

public class Fire_1 extends Map {

    private Fireblob fireblob;

   public Fire_1() {
        super("Fire_1.txt", new FireTileset());
        this.playerStartPosition =  getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();



        NormalDoor doorC = new NormalDoor(getMapTile(10, 2).getLocation())
        .toMap("Fire_2", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorC);
    


        return enhancedMapTiles;

    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        fireblob = new Fireblob(5, getMapTile(4, 4).getLocation());
        enemies.add(fireblob);
        return enemies;
    }
   
}