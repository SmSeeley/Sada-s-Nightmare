//Map that user will walk into
package Maps;

import Enemies.*;
import EnhancedMapTiles.*;
import Level.*;
import Tilesets.DungeonWallsTileSet;
import Utils.Point;
import java.util.ArrayList;

public class Room4Dungeon extends Map {


    public Room4Dungeon() {
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
        enhancedMapTiles.add(new WaterBarrel(getMapTile(14, 4).getLocation()));
        enhancedMapTiles.add(new Blood(getMapTile(15, 2).getLocation()));
        enhancedMapTiles.add(new Skull(getMapTile(15, 8).getLocation()));
        enhancedMapTiles.add(new Skull(getMapTile(5, 3).getLocation()));
        enhancedMapTiles.add(new Skull(getMapTile(14, 9).getLocation()));
        enhancedMapTiles.add(new Blood(getMapTile(5, 10).getLocation()));

        Point coinLoc = getMapTile(5, 5).getLocation();
        if (!Coin.isCollectedAt("Room4Dungeon", coinLoc)) {
            enhancedMapTiles.add(new Coin(coinLoc, "Room4Dungeon"));
        }

        enhancedMapTiles.add(new HealthPotion(getMapTile(8, 5).getLocation()));
        

        NormalDoor toSecond = new NormalDoor(getMapTile(10, 12).getLocation())
            .toMap("ThirdRoomDungeon", 10, 2)
            .withTileSizePixels(48, 48);
        enhancedMapTiles.add(toSecond);

        //Doors in room 1
        NormalDoor doorB = new NormalDoor(getMapTile(10, 1).getLocation())
        .toMap("Room5Dungeon", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorB);


        return enhancedMapTiles;
    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();

        Zombie zombie = new Zombie(4, getMapTile(5, 7).getLocation());
        enemies.add(zombie);

        Zombie zombie2 = new Zombie(4, getMapTile(4, 4).getLocation());
        enemies.add(zombie2);

        Zombie zombie3 = new Zombie(4, getMapTile(4, 10).getLocation());
        enemies.add(zombie3);

        Ogre ogre = new Ogre(5, getMapTile(10, 2).getLocation(),this);
        enemies.add(ogre);

        return enemies;
    }


}