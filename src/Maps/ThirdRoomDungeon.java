//Map that user will walk into
package Maps;

import EnhancedMapTiles.*;
import Level.*;
import Tilesets.DungeonWallsTileSet;
import Utils.Point;
import java.util.ArrayList;

public class ThirdRoomDungeon extends Map {


    public ThirdRoomDungeon() {
        super("ThirdRoomDungeon.txt", new DungeonWallsTileSet());
        this.playerStartPosition = getMapTile(4, 7).getLocation();
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        enhancedMapTiles.add(new WaterBarrel(getMapTile(4, 9).getLocation()));
        enhancedMapTiles.add(new Skull(getMapTile(12, 4).getLocation()));
        enhancedMapTiles.add(new Skull(getMapTile(11, 9).getLocation()));
        enhancedMapTiles.add(new Blood(getMapTile(6, 8).getLocation()));
        enhancedMapTiles.add(new Blood(getMapTile(9, 4).getLocation()));
        enhancedMapTiles.add(new Blood(getMapTile(12, 7).getLocation()));
        enhancedMapTiles.add(new Emptybarrel(getMapTile(4, 6).getLocation()));
        enhancedMapTiles.add(new Emptybarrel(getMapTile(14, 10).getLocation()));
        enhancedMapTiles.add(new WaterBarrel(getMapTile(14, 4).getLocation()));
        enhancedMapTiles.add(new WaterBarrel(getMapTile(8, 11).getLocation()));
        enhancedMapTiles.add(new Blood(getMapTile(15, 2).getLocation()));
        enhancedMapTiles.add(new Skull(getMapTile(15, 8).getLocation()));
        enhancedMapTiles.add(new Skull(getMapTile(5, 3).getLocation()));

        Point coinLoc = getMapTile(5, 5).getLocation();
        if (!Coin.isCollectedAt(coinLoc)) {
            enhancedMapTiles.add(new Coin(coinLoc));
        }

        Point potionLoc = getMapTile(8, 5).getLocation();
        if (!HealthPotion.isCollectedAt(potionLoc)) {
            enhancedMapTiles.add(new HealthPotion(potionLoc));
        }

        NormalDoor toSecond = new NormalDoor(getMapTile(10, 12).getLocation())
            .toMap("SecondRoom", 10, 2)
            .withTileSizePixels(48, 48);
        enhancedMapTiles.add(toSecond);

        //Doors in room 1
        NormalDoor doorB = new NormalDoor(getMapTile(10, 2).getLocation())
        .toMap("Desert_1", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorB);

        return enhancedMapTiles;
    }


}