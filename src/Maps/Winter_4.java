package Maps;


import EnhancedMapTiles.*;
import Level.*;
import Tilesets.*;

import java.util.ArrayList;

// Represents a test map to be used in a level
public class Winter_4 extends Map {

    public Winter_4() {
        super("Winter_4.txt", new WinterTileset());
        this.playerStartPosition = getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        //Doors in room 1
        BossDoor doorB = new BossDoor(getMapTile(10, 2).getLocation())
        .toMap("Winter_5", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorB);

        //Doors in room 1
        NormalDoor doorC = new NormalDoor(getMapTile(10, 12).getLocation())
        .toMap("Winter_3", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorC);


        return enhancedMapTiles;


    }
 
}

