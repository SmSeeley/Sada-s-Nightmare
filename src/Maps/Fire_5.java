//Creates the second room that the user will walk into

package Maps;

import Enemies.Vladmir;
import EnhancedMapTiles.*;
import Level.Enemy;
import Level.EnhancedMapTile;
import Level.Map;
import Tilesets.FireTileset;
import java.util.ArrayList;

public class Fire_5 extends Map {
    private Vladmir vladmir;
   public Fire_5() {
        super("Fire_5.txt", new FireTileset());
        this.playerStartPosition =  getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        Emptybarrel EmptyBarrel_1 = new Emptybarrel(getMapTile(10, 8).getLocation());
        Emptybarrel EmptyBarrel_2 = new Emptybarrel(getMapTile(11, 8).getLocation());
        Emptybarrel EmptyBarrel_3 = new Emptybarrel(getMapTile(9, 8).getLocation());
        enhancedMapTiles.add(EmptyBarrel_1);
        enhancedMapTiles.add(EmptyBarrel_2);
        enhancedMapTiles.add(EmptyBarrel_3);

        

        enhancedMapTiles.add(new HealthPotion(getMapTile(5, 12).getLocation()));

        enhancedMapTiles.add(new HealthPotion(getMapTile(7, 12).getLocation()));

        enhancedMapTiles.add(new HealthPotion(getMapTile(16, 12).getLocation()));

        enhancedMapTiles.add(new HealthPotion(getMapTile(14, 12).getLocation()));


        return enhancedMapTiles;

    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        vladmir = new Vladmir(1, getMapTile(10,3).getLocation());
        enemies.add(vladmir);
        return enemies;
    }
}