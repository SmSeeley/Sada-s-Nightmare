//Map that user will walk into
package Maps;

import EnhancedMapTiles.*;
import Level.*;
import NPCs.Shopkeeper;
import Tilesets.*;
import Utils.Point;
import java.util.ArrayList;

public class TheHub1 extends Map {


    public TheHub1() {
        super("TheHub1.txt", new Cloud());
        this.playerStartPosition = getMapTile(10, 10).getLocation();
    }

    @Override
    public ArrayList<NPC> loadNPCs() {
        ArrayList<NPC> npcs = new ArrayList<>();

        Shopkeeper shopkeeper = new Shopkeeper(2, getMapTile(14, 12).getLocation());
        shopkeeper.setInteractScript(new Scripts.TestMap.ShopkeeperScript(shopkeeper));
        npcs.add(shopkeeper);
        return npcs;
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        Point coinLoc = getMapTile(5, 5).getLocation();
        if (!Coin.isCollectedAt(coinLoc)) {
            enhancedMapTiles.add(new Coin(coinLoc));
        }

        Point potionLoc = getMapTile(8, 5).getLocation();
        if (!HealthPotion.isCollectedAt(potionLoc)) {
            enhancedMapTiles.add(new HealthPotion(potionLoc));
        }

        SandDoor doorB = new SandDoor(getMapTile(6, 3).getLocation())
        .toMap("Desert_1", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorB);
       
        IceDoor doorC = new IceDoor(getMapTile(10, 3).getLocation())
        .toMap("Winter_1", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorC);

        FireDoor doorD = new FireDoor(getMapTile(14, 3).getLocation())
        .toMap("Fire_1", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorD);

        return enhancedMapTiles;
    }

}