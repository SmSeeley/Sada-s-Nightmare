package Tilesets;

import Builders.FrameBuilder;
import Builders.MapTileBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import Level.TileType;
import Level.Tileset;

import java.util.ArrayList;


public class DungeonWallsTileSet extends Tileset {

    public DungeonWallsTileSet() {
       
        super(ImageLoader.load("Tile_Set/PNG/2.png"), 16, 16, 3);
    }

    @Override
    public ArrayList<MapTileBuilder> defineTiles() {
        ArrayList<MapTileBuilder> tiles = new ArrayList<>();

 
        Frame baseFloor = new FrameBuilder(getSubImage(3, 1))
                .withScale(tileScale)
                .build();

        Frame floorA = new FrameBuilder(getSubImage(3, 1))
                .withScale(tileScale)
                .build();
        tiles.add(new MapTileBuilder(floorA));                   // index 0

        Frame floorB = new FrameBuilder(getSubImage(2, 1))
                .withScale(tileScale)
                .build();
        tiles.add(new MapTileBuilder(floorB));                   // index 1

        // ===== SOLID WALL BLOCKS =====
        // Full collision bounds (16x16). Adjust coordinates to the wall bricks you want.
        Frame wallSolid1 = new FrameBuilder(getSubImage(0, 1))
                .withScale(tileScale)
                .withBounds(0, 0, 16, 16)
                .build();
        tiles.add(new MapTileBuilder(wallSolid1).withTileType(TileType.NOT_PASSABLE)); // index 2

        Frame wallSolid2 = new FrameBuilder(getSubImage(1, 1))
                .withScale(tileScale)
                .withBounds(0, 0, 16, 16)
                .build();
        tiles.add(new MapTileBuilder(wallSolid2).withTileType(TileType.NOT_PASSABLE)); // index 3

        // ===== WALL EDGES / CORNERS (SOLID) =====
        // Top edge
        Frame wallTop = new FrameBuilder(getSubImage(2, 1))
                .withScale(tileScale)
                .withBounds(0, 0, 16, 16)
                .build();
        tiles.add(new MapTileBuilder(wallTop).withTileType(TileType.NOT_PASSABLE));    // index 4

        // Bottom edge
        Frame wallBottom = new FrameBuilder(getSubImage(3, 1))
                .withScale(tileScale)
                .withBounds(0, 0, 16, 16)
                .build();
        tiles.add(new MapTileBuilder(wallBottom).withTileType(TileType.NOT_PASSABLE)); // index 5

        // Left edge
        Frame wallLeft = new FrameBuilder(getSubImage(0, 2))
                .withScale(tileScale)
                .withBounds(0, 0, 16, 16)
                .build();
        tiles.add(new MapTileBuilder(wallLeft).withTileType(TileType.NOT_PASSABLE));   // index 6

        // Right edge
        Frame wallRight = new FrameBuilder(getSubImage(1, 2))
                .withScale(tileScale)
                .withBounds(0, 0, 16, 16)
                .build();
        tiles.add(new MapTileBuilder(wallRight).withTileType(TileType.NOT_PASSABLE));  // index 7

        // Top-left corner
        Frame cornerTL = new FrameBuilder(getSubImage(2, 2))
                .withScale(tileScale)
                .withBounds(0, 0, 16, 16)
                .build();
        tiles.add(new MapTileBuilder(cornerTL).withTileType(TileType.NOT_PASSABLE));   // index 8

        // Top-right corner
        Frame cornerTR = new FrameBuilder(getSubImage(3, 2))
                .withScale(tileScale)
                .withBounds(0, 0, 16, 16)
                .build();
        tiles.add(new MapTileBuilder(cornerTR).withTileType(TileType.NOT_PASSABLE));   // index 9

        // Bottom-left corner
        Frame cornerBL = new FrameBuilder(getSubImage(0, 3))
                .withScale(tileScale)
                .withBounds(0, 0, 16, 16)
                .build();
        tiles.add(new MapTileBuilder(cornerBL).withTileType(TileType.NOT_PASSABLE));   // index 10

        // Bottom-right corner
        Frame cornerBR = new FrameBuilder(getSubImage(1, 3))
                .withScale(tileScale)
                .withBounds(0, 0, 16, 16)
                .build();
        tiles.add(new MapTileBuilder(cornerBR).withTileType(TileType.NOT_PASSABLE));   // index 11

        // ===== ARCHES / DOOR FRONTS =====
        // Closed arch (solid) – adjust (col,row) to your closed arch sprite
        Frame archClosed = new FrameBuilder(getSubImage(1, 10))
                .withScale(tileScale)
                .withBounds(0, 0, 16, 16)
                .build();
        tiles.add(new MapTileBuilder(archClosed).withTileType(TileType.NOT_PASSABLE)); // index 12

        // Open arch (overlay over floor, passable)
        // We draw the arch sprite as a top layer over a floor base
        Frame archOpenTop = new FrameBuilder(getSubImage(2, 10))
                .withScale(tileScale)
                .build();
        tiles.add(
                new MapTileBuilder(baseFloor)
                        .withTopLayer(archOpenTop)
                        .withTileType(TileType.PASSABLE)
        );                                                                             // index 13

        // ===== VERTICAL PILLARS (SOLID) =====
        Frame pillar1 = new FrameBuilder(getSubImage(0, 5))
                .withScale(tileScale)
                .withBounds(0, 0, 16, 16)
                .build();
        tiles.add(new MapTileBuilder(pillar1).withTileType(TileType.NOT_PASSABLE));    // index 14

        Frame pillar2 = new FrameBuilder(getSubImage(1, 5))
                .withScale(tileScale)
                .withBounds(0, 0, 16, 16)
                .build();
        tiles.add(new MapTileBuilder(pillar2).withTileType(TileType.NOT_PASSABLE));    // index 15

        // ===== SMALL WINDOW / TRIM (OVERLAY, PASSABLE) =====
        Frame trim = new FrameBuilder(getSubImage(3, 5))
                .withScale(tileScale)
                .build();
        tiles.add(
                new MapTileBuilder(baseFloor)
                        .withTopLayer(trim)
                        .withTileType(TileType.PASSABLE)
        );                                                                             // index 16

        // Mirror trim example using image effect
        Frame trimFlip = new FrameBuilder(getSubImage(3, 5))
                .withScale(tileScale)
                .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                .build();
        tiles.add(
                new MapTileBuilder(baseFloor)
                        .withTopLayer(trimFlip)
                        .withTileType(TileType.PASSABLE)
        );                                                                             // index 17

        // Add more entries as you identify tiles in your sheet; order here is the palette order.
        return tiles;
    }
}
