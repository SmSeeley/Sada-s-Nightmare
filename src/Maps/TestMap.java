package Maps;

import Enemies.Ogre;
import Enemies.Zombie;
import EnhancedMapTiles.Coin;
import EnhancedMapTiles.Door;
import EnhancedMapTiles.HealthPotion;
import Level.*;
import NPCs.Bug;
import NPCs.Dinosaur;
import NPCs.greenNinja;
import Scripts.SimpleTextScript;
import Scripts.TestMap.*;
import Tilesets.CommonTileset;
import java.util.ArrayList;

// Represents a test map to be used in a level
public class TestMap extends Map {

    public TestMap() {
        super("test_map.txt", new CommonTileset());
        this.playerStartPosition = getMapTile(4, 7).getLocation();
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        //Doors in room 1
        Door            door2 = new Door(getMapTile(4, 3).getLocation());

        //Doors in room 2
        Door            door6 = new Door(getMapTile(21, 3).getLocation());


        // Health potion
        HealthPotion healthPotion = new HealthPotion(getMapTile(2, 7).getLocation());


        //coin in room 1
        Coin            coin1 = new Coin(getMapTile(6,5).getLocation());
        Coin            coin2 = new Coin(getMapTile(3, 5).getLocation());


        //Adjust teleportation so all doors in room 1 teleport to correlated doors in room 2
        door2.setDestination(door6.getLocation());
        door6.setDestination(door2.getLocation());

        //add adjusted doors to enhanced map tiles
        enhancedMapTiles.add(door2);
        enhancedMapTiles.add(door6);
        
        enhancedMapTiles.add(healthPotion);

        enhancedMapTiles.add(coin1);
        enhancedMapTiles.add(coin2);


        return enhancedMapTiles;
    }

    

    @Override
    public ArrayList<NPC> loadNPCs() {
        ArrayList<NPC> npcs = new ArrayList<>();

        greenNinja walrus = new greenNinja(1, getMapTile(5, 5).getLocation());
        walrus.setInteractScript(new greenNinjaScript());
        npcs.add(walrus);

        Dinosaur dinosaur = new Dinosaur(2, getMapTile(13, 4).getLocation());
        dinosaur.setExistenceFlag("hasTalkedToDinosaur");
        dinosaur.setInteractScript(new DinoScript());
        npcs.add(dinosaur);
        
        Bug bug = new Bug(3, getMapTile(7, 12).getLocation().subtractX(20));
        bug.setInteractScript(new BugScript());
        npcs.add(bug);

        return npcs;
    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();

        Zombie zombie = new Zombie(4, getMapTile(18, 7).getLocation());
        enemies.add(zombie);

        Ogre ogre = new Ogre(5, getMapTile(6, 9).getLocation());
        enemies.add(ogre);

        return enemies;
    }

    @Override
    public ArrayList<Trigger> loadTriggers() {
        ArrayList<Trigger> triggers = new ArrayList<>();
        triggers.add(new Trigger(790, 1030, 100, 10, new LostBallScript(), "hasLostBall"));
        triggers.add(new Trigger(790, 960, 10, 80, new LostBallScript(), "hasLostBall"));
        triggers.add(new Trigger(890, 960, 10, 80, new LostBallScript(), "hasLostBall"));
        return triggers;
    }

    @Override
    public void loadScripts() {
        getMapTile(21, 19).setInteractScript(new SimpleTextScript("Cat's house"));

        getMapTile(7, 26).setInteractScript(new SimpleTextScript("Walrus's house"));

        getMapTile(20, 4).setInteractScript(new SimpleTextScript("Dino's house"));

        getMapTile(2, 6).setInteractScript(new TreeScript());
    }   
}

