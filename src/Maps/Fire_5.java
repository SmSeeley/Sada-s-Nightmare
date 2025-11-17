//Creates the second room that the user will walk into

package Maps;

import EnhancedMapTiles.*;
import Level.Enemy;
import Level.EnhancedMapTile;
import Level.Map;
import Tilesets.FireTileset;
import java.util.ArrayList;

import Enemies.Ogre;
import Enemies.Vladmir;

public class Fire_5 extends Map {
    private Vladmir vladmir;
   public Fire_5() {
        super("Fire_5.txt", new FireTileset());
        this.playerStartPosition =  getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();


        return enhancedMapTiles;

    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        vladmir = new Vladmir(1, getMapTile(9,7).getLocation());
        enemies.add(vladmir);
        return enemies;
    }
}