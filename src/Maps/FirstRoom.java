//Map that user will walk into
package Maps;

import EnhancedMapTiles.*;
import Level.*;
import Tilesets.DungeonWallsTileSet;
import Utils.Point;
import java.util.ArrayList;
// ...

public class FirstRoom extends Map {

    public FirstRoom() {
        super("FirstRoom.txt", new DungeonWallsTileSet());
        this.playerStartPosition =  getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();


        WaterBarrel WaterBarrel_2 = new WaterBarrel(getMapTile(4, 9).getLocation());
        enhancedMapTiles.add(WaterBarrel_2);

        Skull Skull_1 = new Skull(getMapTile(12, 4).getLocation());
        enhancedMapTiles.add(Skull_1);

        Skull Skull_2 = new Skull(getMapTile(11, 9).getLocation());
        enhancedMapTiles.add(Skull_2);

        Blood Blood_1 = new Blood(getMapTile(6, 8).getLocation());
        enhancedMapTiles.add(Blood_1);

        Blood Blood_2 = new Blood(getMapTile(9, 4).getLocation());
        enhancedMapTiles.add(Blood_2);

        Blood Blood_3 = new Blood(getMapTile(12, 7).getLocation());
        enhancedMapTiles.add(Blood_3);

        Emptybarrel EmptyBarrel_1 = new Emptybarrel(getMapTile(3, 4).getLocation());
        enhancedMapTiles.add(EmptyBarrel_1);

        Emptybarrel EmptyBarrel_2 = new Emptybarrel(getMapTile(4, 6).getLocation());
        enhancedMapTiles.add(EmptyBarrel_2);

        DoorKey key1 = new DoorKey(getMapTile(4,4).getLocation());
        enhancedMapTiles.add(key1);

        Point coinLoc = getMapTile(5, 5).getLocation();
        if (!Coin.isCollectedAt(coinLoc)) {
            Coin coin = new Coin(coinLoc);
            enhancedMapTiles.add(coin);
        }
        

        Door toSecond = new Door(getMapTile(8, 2).getLocation())
        .toMap("SecondRoom", 10, 12)         // spawn tile (x=21, y=3) in SecondRoom
        .withTileSizePixels(48, 48);        // your rendered tile size
        enhancedMapTiles.add(toSecond);


        return enhancedMapTiles;

    }

}