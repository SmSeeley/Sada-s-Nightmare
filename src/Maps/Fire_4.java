//Creates the second room that the user will walk into

package Maps;

import EnhancedMapTiles.*;
import Level.EnhancedMapTile;
import Level.Map;
import Tilesets.FireTileset;
import java.util.ArrayList;

public class Fire_4 extends Map {
   public Fire_4() {
        super("Fire_4.txt", new FireTileset());
        this.playerStartPosition =  getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();



        NormalDoor doorC = new NormalDoor(getMapTile(10, 2).getLocation())
        .toMap("Fire_5", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorC);

        NormalDoor doorD = new NormalDoor(getMapTile(10, 12).getLocation())
        .toMap("Fire_3", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorD);

        enhancedMapTiles.add(new Skull(getMapTile(6, 8).getLocation()));
        enhancedMapTiles.add(new Skull(getMapTile(9, 3).getLocation()));
        enhancedMapTiles.add(new Skull(getMapTile(4, 6).getLocation()));

        return enhancedMapTiles;

    }
   
}