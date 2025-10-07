package Maps;

import EnhancedMapTiles.Door;
import EnhancedMapTiles.LeftFacingDoor;
import EnhancedMapTiles.PushableRock;
import EnhancedMapTiles.RightFacingDoor;
import EnhancedMapTiles.UpsideDownDoor;
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
        RightFacingDoor door1 = new RightFacingDoor(getMapTile(8, 7).getLocation());
        Door            door2 = new Door(getMapTile(4, 3).getLocation());
        LeftFacingDoor  door3 = new LeftFacingDoor(getMapTile(0, 7).getLocation());
        UpsideDownDoor  door4 = new UpsideDownDoor(getMapTile(4, 11).getLocation());

        //Doors in room 2
        RightFacingDoor door5 = new RightFacingDoor(getMapTile(25, 7).getLocation());
        Door            door6 = new Door(getMapTile(21, 3).getLocation());
        LeftFacingDoor  door7 = new LeftFacingDoor(getMapTile(17, 7).getLocation());
        UpsideDownDoor  door8 = new UpsideDownDoor(getMapTile(21, 11).getLocation());

        //Adjust teleportation so all doors in room 1 teleport to correlated doors in room 2
        door1.setDestination(door5.getLocation());
        door5.setDestination(door1.getLocation());
        door2.setDestination(door6.getLocation());
        door6.setDestination(door2.getLocation());
        door3.setDestination(door7.getLocation());
        door7.setDestination(door3.getLocation());
        door4.setDestination(door8.getLocation());
        door8.setDestination(door4.getLocation());

        //add adjusted doors to enhanced map tiles
        enhancedMapTiles.add(door1);
        enhancedMapTiles.add(door2);
        enhancedMapTiles.add(door3);
        enhancedMapTiles.add(door4);
        enhancedMapTiles.add(door5);
        enhancedMapTiles.add(door6);
        enhancedMapTiles.add(door7);
        enhancedMapTiles.add(door8);

        return enhancedMapTiles;
    }

    @Override
    public ArrayList<NPC> loadNPCs() {
        ArrayList<NPC> npcs = new ArrayList<>();

        greenNinja walrus = new greenNinja(1, getMapTile(5, 8).getLocation());
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

