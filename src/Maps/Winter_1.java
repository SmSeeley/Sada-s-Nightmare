package Maps;


import EnhancedMapTiles.*;
import Level.*;
import NPCs.Bug;
import NPCs.Dinosaur;
import NPCs.greenNinja;
import NPCs.Wizard;
import Scripts.SimpleTextScript;
import Scripts.TestMap.*;
import Tilesets.*;

import java.util.ArrayList;

// Represents a test map to be used in a level
public class Winter_1 extends Map {

    public Winter_1() {
        super("Winter_1.txt", new WinterTileset());
        this.playerStartPosition = getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();


        //Doors in room 1
        NormalDoor doorB = new NormalDoor(getMapTile(10, 2).getLocation())
        .toMap("Winter_2", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorB);

        return enhancedMapTiles;


    }
 
}

