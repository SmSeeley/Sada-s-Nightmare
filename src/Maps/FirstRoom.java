//Map that user will walk into
package Maps;

import Enemies.Ogre;
import EnhancedMapTiles.*;
import Level.*;
import NPCs.Wizard;
import NPCs.greenNinja;
import Scripts.TestMap.greenNinjaScript;
import Tilesets.DungeonWallsTileSet;
import Utils.Point;
import java.util.ArrayList;

public class FirstRoom extends Map {

    private Ogre ogre;

    public FirstRoom() {
        super("FirstRoom.txt", new DungeonWallsTileSet());
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
        enhancedMapTiles.add(new Emptybarrel(getMapTile(3, 4).getLocation()));
        enhancedMapTiles.add(new Emptybarrel(getMapTile(4, 6).getLocation()));

        Point coinLoc = getMapTile(5, 5).getLocation();
        if (!Coin.isCollectedAt(coinLoc)) {
            enhancedMapTiles.add(new Coin(coinLoc));
        }

        Point potionLoc = getMapTile(8, 5).getLocation();
        if (!HealthPotion.isCollectedAt(potionLoc)) {
            enhancedMapTiles.add(new HealthPotion(potionLoc));
        }

        NormalDoor toSecond = new NormalDoor(getMapTile(8, 2).getLocation())
            .toMap("SecondRoom", 10, 10)
            .withTileSizePixels(48, 48);
        enhancedMapTiles.add(toSecond);

        return enhancedMapTiles;
    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        ogre = new Ogre(5, getMapTile(4, 4).getLocation());
        enemies.add(ogre);
        return enemies;
    }

    // Necessary code to allow dynamic addition to the map's enhanced tiles list
    public ArrayList<EnhancedMapTile> getEnhancedMapTiles() {
        return enhancedMapTiles;
    }
    
    // The method you are calling in update() that needs to be defined
    public void addEnhancedMapTile(EnhancedMapTile tile) {
        getEnhancedMapTiles().add(tile);
    }
    
    @Override
    public void update(Player player) { 
        super.update(player);

        if (ogre != null && ogre.isDead() && !ogre.keyDropped) {
            // Instead of dropping a DoorKey tile, increment the key count stat directly
            EnhancedMapTiles.DoorKey.keysCollected++;
            // Mark that key has been "collected" to prevent multiple increments
            ogre.keyDropped = true;
            System.out.println("[FirstRoom] Ogre died - incremented key count to " + EnhancedMapTiles.DoorKey.keysCollected);
        }
    }
}