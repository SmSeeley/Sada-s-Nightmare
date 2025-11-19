//Creates the second room that the user will walk into

package Maps;

import Enemies.Fireblob;
import Enemies.Firemonster;
import EnhancedMapTiles.*;
import Level.Enemy;
import Level.EnhancedMapTile;
import Level.Map;
import Tilesets.FireTileset;
import java.util.ArrayList;

public class Fire_1 extends Map {

    private Fireblob fireblob;
    private Fireblob fireblob1;
    private Fireblob fireblob2;
    private Firemonster firemonster;

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
        
        enhancedMapTiles.add(new HealthPotion(getMapTile(12, 10).getLocation()));



        return enhancedMapTiles;

    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        fireblob = new Fireblob(5, getMapTile(4, 4).getLocation());
        fireblob1 = new Fireblob(5, getMapTile(8, 4).getLocation());
        fireblob2 = new Fireblob(5, getMapTile(10, 4).getLocation());

        firemonster = new Firemonster(5, getMapTile(8, 3).getLocation());
        enemies.add(firemonster);
        
        enemies.add(fireblob);
        enemies.add(fireblob1);
        enemies.add(fireblob2);

        return enemies;
    }
   
}