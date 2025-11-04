//Creates the second room that the user will walk into

package Maps;

import EnhancedMapTiles.*;
import Level.EnhancedMapTile;
import Level.Map;
import Tilesets.FireTileset;
import java.util.ArrayList;

public class Fire_5 extends Map {
   public Fire_5() {
        super("Fire_5.txt", new FireTileset());
        this.playerStartPosition =  getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();



        NormalDoor doorC = new NormalDoor(getMapTile(10, 2).getLocation())
        .toMap("TheHub1", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorC);


        return enhancedMapTiles;

    }
   
}