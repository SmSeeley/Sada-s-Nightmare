package Maps;


import Enemies.Snake;
import EnhancedMapTiles.*;
import Level.*;
import Tilesets.DesertTileset;
import java.util.ArrayList;

// Represents a test map to be used in a level
public class Desert_1 extends Map {

    private Snake snake;
    private Snake snake2;
    private Snake snake3;

    public Desert_1() {
        super("Desert_1.txt", new DesertTileset());
        this.playerStartPosition = getMapTile(4, 7).getLocation();
    }


    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        //Doors in room 1
        NormalDoor doorB = new NormalDoor(getMapTile(10, 1).getLocation())
        .toMap("Desert_2", 10, 10)
        .withTileSizePixels(48, 48);
        enhancedMapTiles.add(doorB);

        enhancedMapTiles.add(new HealthPotion(getMapTile(10, 10).getLocation()));

        return enhancedMapTiles;


    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        snake = new Snake(11, getMapTile(4, 4).getLocation());
        enemies.add(snake);

        snake2 = new Snake(11, getMapTile(10, 4).getLocation());
        enemies.add(snake2);

        snake3 = new Snake(11, getMapTile(13, 4).getLocation());
        enemies.add(snake3);

        return enemies;
        
    }

}