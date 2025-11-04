package Maps;


import EnhancedMapTiles.*;
import Level.*;
import Tilesets.*;

import java.util.ArrayList;

// Represents a test map to be used in a level
public class Winter_5 extends Map {

    public Winter_5() {
        super("Winter_5.txt", new WinterTileset());
        this.playerStartPosition = getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        //Doors in room 1
        NormalDoor doorB = new NormalDoor(getMapTile(10, 2).getLocation())
        .toMap("TheHub1", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorB);

        //Doors in room 1
        NormalDoor doorC = new NormalDoor(getMapTile(10, 12).getLocation())
        .toMap("Winter_4", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorC);


        return enhancedMapTiles;


    }
 
}

