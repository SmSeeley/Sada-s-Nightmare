package Tilesets;

import Builders.FrameBuilder;
import Builders.MapTileBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import Level.TileType;
import Level.Tileset;

import java.util.ArrayList;

public class DungeonTileset extends Tileset {
    public DungeonTileset() {
     
        super(ImageLoader.load("Tile_Set/PNG/1.png"), 16, 16, 3);
    }

    @Override
    public ArrayList<MapTileBuilder> defineTiles() {
        ArrayList<MapTileBuilder> tiles = new ArrayList<>();

        // Example floor at (0,0)
        Frame floor = new FrameBuilder(getSubImage(0, 0)).withScale(tileScale).build();
        tiles.add(new MapTileBuilder(floor)); // passable

        // Example wall at (1,0)
        Frame wall = new FrameBuilder(getSubImage(1, 0))
                .withScale(tileScale)
                .withBounds(0, 0, 16, 16)
                .build();
        tiles.add(new MapTileBuilder(wall).withTileType(TileType.NOT_PASSABLE));

        return tiles;
        // Add more tiles as you map your spritesheet indices
    }
}
