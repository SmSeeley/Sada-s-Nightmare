//Creates the second room that the user will walk into

package Maps;

import Enemies.Zombie;
import EnhancedMapTiles.*;
import Level.Enemy;
import Level.EnhancedMapTile;
import Level.Map;
import Level.NPC;
import NPCs.Wizard;
import Tilesets.DungeonWallsTileSet;
import Utils.Point;
import java.util.ArrayList;

public class SecondRoom extends Map {
   public SecondRoom() {
        super("SecondRoom.txt", new DungeonWallsTileSet());
        this.playerStartPosition =  getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        //Doors in room 1
        NormalDoor doorB = new NormalDoor(getMapTile(10, 12).getLocation())
        .toMap("FirstRoom", 8, 2)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorB);

        NormalDoor doorC = new NormalDoor(getMapTile(10, 1).getLocation())
        .toMap("ThirdRoomDungeon", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorC);
        
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

        Blood Blood_4 = new Blood(getMapTile(14, 6).getLocation());
        enhancedMapTiles.add(Blood_4);

        Emptybarrel EmptyBarrel_1 = new Emptybarrel(getMapTile(14, 9).getLocation());
        enhancedMapTiles.add(EmptyBarrel_1);

        Emptybarrel EmptyBarrel_2 = new Emptybarrel(getMapTile(4, 6).getLocation());
        enhancedMapTiles.add(EmptyBarrel_2);

        Point swordLoc = getMapTile(6, 5).getLocation(); 
        if (!Sword.isCollectedAt(swordLoc)) {
            Sword sword = new Sword(swordLoc);
            enhancedMapTiles.add(sword);
        }

        Point potionLoc = getMapTile(10, 10).getLocation();
        if (!HealthPotion.isCollectedAt(potionLoc)) {
            enhancedMapTiles.add(new HealthPotion(potionLoc));
        }


        return enhancedMapTiles;

    }

    @Override
    public ArrayList<NPC> loadNPCs() {
        ArrayList<NPC> npcs = new ArrayList<>();
        Wizard wizard = new Wizard(1, getMapTile(15, 2).getLocation());
        wizard.setInteractScript(new Scripts.TestMap.WizardRiddleScript());
        npcs.add(wizard);
        return npcs;
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

        return enemies;
    }
}