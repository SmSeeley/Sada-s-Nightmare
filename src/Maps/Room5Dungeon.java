//Map that user will walk into
package Maps;

import EnhancedMapTiles.*;
import Level.*;
import Tilesets.DungeonWallsTileSet;
import Utils.Point;
import java.util.ArrayList;

public class Room5Dungeon extends Map {


    public Room5Dungeon() {
        super("Room4Dungeon.txt", new DungeonWallsTileSet());
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
        enhancedMapTiles.add(new WaterBarrel(getMapTile(8, 11).getLocation()));
        enhancedMapTiles.add(new Blood(getMapTile(15, 2).getLocation()));
        enhancedMapTiles.add(new Skull(getMapTile(15, 8).getLocation()));
        enhancedMapTiles.add(new Skull(getMapTile(5, 3).getLocation()));
        enhancedMapTiles.add(new Blood(getMapTile(12, 6).getLocation()));
        enhancedMapTiles.add(new Blood(getMapTile(6, 12).getLocation()));

        Point coinLoc = getMapTile(5, 5).getLocation();
        if (!Coin.isCollectedAt(coinLoc)) {
            enhancedMapTiles.add(new Coin(coinLoc));
        }

        Point potionLoc = getMapTile(8, 5).getLocation();
        if (!HealthPotion.isCollectedAt(potionLoc)) {
            enhancedMapTiles.add(new HealthPotion(potionLoc));
        }

        NormalDoor toSecond = new NormalDoor(getMapTile(10, 12).getLocation())
            .toMap("Room4Dungeon", 10, 2)
            .withTileSizePixels(48, 48);
        enhancedMapTiles.add(toSecond);

        //Doors in room 1
        DreamDoor doorB = new DreamDoor(getMapTile(10, 1).getLocation())
        .toMap("TheHub1", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorB);

        return enhancedMapTiles;
    }


}