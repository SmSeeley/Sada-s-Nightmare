//Map that user will walk into
package Maps;

import EnhancedMapTiles.*;
import Level.*;
import Tilesets.DungeonWallsTileSet;
import Utils.Point;
import java.util.ArrayList;

public class TheHub1 extends Map {


    public TheHub1() {
        super("TheHub1.txt", new DungeonWallsTileSet());
        this.playerStartPosition = getMapTile(10, 10).getLocation();
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        enhancedMapTiles.add(new WaterBarrel(getMapTile(4, 9).getLocation()));
        enhancedMapTiles.add(new Emptybarrel(getMapTile(12, 7).getLocation()));
        enhancedMapTiles.add(new Emptybarrel(getMapTile(4, 6).getLocation()));

        enhancedMapTiles.add(new WaterBarrel(getMapTile(15, 8).getLocation()));
        enhancedMapTiles.add(new WaterBarrel(getMapTile(4, 14).getLocation()));

        Point coinLoc = getMapTile(5, 5).getLocation();
        if (!Coin.isCollectedAt(coinLoc)) {
            enhancedMapTiles.add(new Coin(coinLoc));
        }

        Point potionLoc = getMapTile(8, 5).getLocation();
        if (!HealthPotion.isCollectedAt(potionLoc)) {
            enhancedMapTiles.add(new HealthPotion(potionLoc));
        }

        NormalDoor doorB = new NormalDoor(getMapTile(6, 2).getLocation())
        .toMap("Desert_1", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorB);
        
        NormalDoor doorC = new NormalDoor(getMapTile(10, 2).getLocation())
        .toMap("Winter_1", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorC);

        NormalDoor doorD = new NormalDoor(getMapTile(14, 2).getLocation())
        .toMap("ThirdRoomDungeon", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorD);

        return enhancedMapTiles;
    }


}