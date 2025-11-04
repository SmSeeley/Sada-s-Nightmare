//Creates the second room that the user will walk into

package Maps;

import Enemies.Zombie;
import EnhancedMapTiles.*;
import Level.Enemy;
import Level.EnhancedMapTile;
import Level.Map;
import Level.NPC;
import NPCs.Wizard;
import Tilesets.DungeonWallsTileSet;
import Tilesets.FireTileset;
import Utils.Point;
import java.util.ArrayList;

public class Fire_1 extends Map {
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
   
}