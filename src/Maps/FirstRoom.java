//Map that user will walk into
package Maps;

import java.util.ArrayList;
import EnhancedMapTiles.*;
import EnhancedMapTiles.Door;
import Level.*;
import Tilesets.CommonTileset;
import Tilesets.DungeonTileset;
import Tilesets.DungeonWallsTileSet;

import Utils.Point; // your Map subclass that uses DungeonTileset
// ...

public class FirstRoom extends Map {

    public FirstRoom() {
        super("FirstRoom.txt", new DungeonWallsTileSet());
        this.playerStartPosition =  getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        Door toSecond = new Door(getMapTile(8, 2).getLocation())
        .toMap("SecondRoom", 10, 12)         // spawn tile (x=21, y=3) in SecondRoom
        .withTileSizePixels(48, 48);        // your rendered tile size
        enhancedMapTiles.add(toSecond);


        return enhancedMapTiles;

    }

}