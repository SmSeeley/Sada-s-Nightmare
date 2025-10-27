package Maps;


import EnhancedMapTiles.*;
import Level.*;
import NPCs.Bug;
import NPCs.Dinosaur;
import NPCs.greenNinja;
import NPCs.Wizard;
import Scripts.SimpleTextScript;
import Scripts.TestMap.*;
import Tilesets.CommonTileset;
import Tilesets.DesertTileset;

import java.util.ArrayList;

// Represents a test map to be used in a level
public class Desert_1 extends Map {

    public Desert_1() {
        super("Desert_1.txt", new DesertTileset());
        this.playerStartPosition = getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        //Doors in room 1
        NormalDoor doorB = new NormalDoor(getMapTile(10, 10).getLocation())
        .toMap("FirstRoom", 8, 8)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorB);


        return enhancedMapTiles;


    }
 
}

