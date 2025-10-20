//Creates the second room that the user will walk into

package Maps;

import java.util.ArrayList;

import EnhancedMapTiles.Door;
import Level.EnhancedMapTile;
import Level.Map;
import Tilesets.CommonTileset;
import Tilesets.DungeonTileset;
import Tilesets.DungeonTileset2;
import Tilesets.DungeonWallsTileSet;
import Utils.Point;

public class SecondRoom extends Map {
   public SecondRoom() {
        super("SecondRoom.txt", new DungeonWallsTileSet());
        this.playerStartPosition =  getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        //Doors in room 1

        Door doorB = new Door(getMapTile(10, 12).getLocation())
        .toMap("FirstRoom", 12, 5)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorB);

        Door doorC = new Door(getMapTile(10, 1).getLocation())
        .toMap("FirstRoom", 12, 5)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorC);

        return enhancedMapTiles;

    }
}