package Maps;


import EnhancedMapTiles.*;
import Level.*;
import Tilesets.DesertTileset;

import java.util.ArrayList;

// Represents a test map to be used in a level
public class Desert_4 extends Map {

    public Desert_4() {
        super("Desert_4.txt", new DesertTileset());
        this.playerStartPosition = getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        //Doors in room 1
        NormalDoor doorB = new NormalDoor(getMapTile(10, 1).getLocation())
        .toMap("Desert_5", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorB);

        //Doors in room 1
        NormalDoor doorC = new NormalDoor(getMapTile(10, 12).getLocation())
        .toMap("Desert_3", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorC);

        return enhancedMapTiles;


    }

    
 
}

