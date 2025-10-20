package Tilesets;

import Builders.FrameBuilder;
import Builders.MapTileBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import Level.TileType;
import Level.Tileset;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * DungeonTileset2
 * - 2.png = floors/walls (assumed 16x16 with spacing)
 * - 3.png = decor (assumed 32x32, no spacing)
 * - Decor is published as 2x2 overlays (NW, NE, SW, SE) so it fits the 16x16 map grid.
 * - Overlays draw on top of a base floor, avoiding black squares.
 */
public class DungeonTileset2 extends Tileset {

    // ---------- Sheet 2 (floors/walls) ----------
    private static final int S2_TILE = 16;
    private static final int S2_SPACING = 1;   // set to 0 if your 2.png has no gaps
    private static final int S2_MARGIN  = 0;
    // ---------- Sheet 3 (decor) ----------
    private static final int S3_TILE = 32;     // decor tiles are 32x32 on the sheet
    private static final int S3_SPACING = 0;   // typically no spacing
    private static final int S3_MARGIN  = 0;

    private static final int SCALE = 3;        // render scale (16*3 = 48 px)

    public DungeonTileset2() {
        // Base sheet; engine reads tileWidth/Height from here but we’ll slice manually below.
        super(ImageLoader.load("Tile_Set/PNG/2.png"), 16, 16, SCALE);
    }

    @Override
    public ArrayList<MapTileBuilder> defineTiles() {
        ArrayList<MapTileBuilder> list = new ArrayList<>();

        BufferedImage s2 = ImageLoader.load("Tile_Set/PNG/2.png");
        BufferedImage s3 = ImageLoader.load("Tile_Set/PNG/3.png");

        // Pick a base floor that will sit under overlays
        Frame baseFloor = framePassable(s2, 1, 0, S2_TILE, S2_SPACING, S2_MARGIN);

        // ---- Floors (passable) from 2.png ----
        list.add(new MapTileBuilder(framePassable(s2, 0, 0, S2_TILE, S2_SPACING, S2_MARGIN))); // floor A
        list.add(new MapTileBuilder(framePassable(s2, 1, 0, S2_TILE, S2_SPACING, S2_MARGIN))); // floor B

        // ---- Walls (solid) from 2.png ----
        list.add(new MapTileBuilder(frameSolid(s2, 0, 1, S2_TILE, S2_SPACING, S2_MARGIN)).withTileType(TileType.NOT_PASSABLE));
        list.add(new MapTileBuilder(frameSolid(s2, 1, 1, S2_TILE, S2_SPACING, S2_MARGIN)).withTileType(TileType.NOT_PASSABLE));
        list.add(new MapTileBuilder(frameSolid(s2, 2, 1, S2_TILE, S2_SPACING, S2_MARGIN)).withTileType(TileType.NOT_PASSABLE)); // top
        list.add(new MapTileBuilder(frameSolid(s2, 3, 1, S2_TILE, S2_SPACING, S2_MARGIN)).withTileType(TileType.NOT_PASSABLE)); // bottom
        list.add(new MapTileBuilder(frameSolid(s2, 0, 2, S2_TILE, S2_SPACING, S2_MARGIN)).withTileType(TileType.NOT_PASSABLE)); // left
        list.add(new MapTileBuilder(frameSolid(s2, 1, 2, S2_TILE, S2_SPACING, S2_MARGIN)).withTileType(TileType.NOT_PASSABLE)); // right
        list.add(new MapTileBuilder(frameSolid(s2, 2, 2, S2_TILE, S2_SPACING, S2_MARGIN)).withTileType(TileType.NOT_PASSABLE)); // TL
        list.add(new MapTileBuilder(frameSolid(s2, 3, 2, S2_TILE, S2_SPACING, S2_MARGIN)).withTileType(TileType.NOT_PASSABLE)); // TR
        list.add(new MapTileBuilder(frameSolid(s2, 0, 3, S2_TILE, S2_SPACING, S2_MARGIN)).withTileType(TileType.NOT_PASSABLE)); // BL
        list.add(new MapTileBuilder(frameSolid(s2, 1, 3, S2_TILE, S2_SPACING, S2_MARGIN)).withTileType(TileType.NOT_PASSABLE)); // BR

        // ---- Example arch (solid + overlay) from 2.png ----
        list.add(new MapTileBuilder(frameSolid(s2, 1, 10, S2_TILE, S2_SPACING, S2_MARGIN)).withTileType(TileType.NOT_PASSABLE)); // closed
        list.add(tileOverlay(baseFloor, framePassable(s2, 2, 10, S2_TILE, S2_SPACING, S2_MARGIN))); // open overlay

        // =========================
        // DECOR from 3.png (32x32 → 2x2 overlays)
        // =========================
        // We’ll slice the whole sheet as 32x32 cells. Each cell becomes 4 overlay tiles (NW/NE/SW/SE).
        int cols3 = countCols(s3.getWidth(), S3_TILE, S3_SPACING, S3_MARGIN);
        int rows3 = countRows(s3.getHeight(), S3_TILE, S3_SPACING, S3_MARGIN);

        for (int r = 0; r < rows3; r++) {
            for (int c = 0; c < cols3; c++) {
                addDecor32As2x2(s3, list, baseFloor, c, r);
            }
        }

        return list;
    }

    // -------- 32x32 decor → four 16x16 overlay tiles --------
    private void addDecor32As2x2(BufferedImage sheet32, ArrayList<MapTileBuilder> out, Frame baseFloor, int col32, int row32) {
        // top-left px of the 32x32 cell
        int cellX = S3_MARGIN + col32 * (S3_TILE + S3_SPACING);
        int cellY = S3_MARGIN + row32 * (S3_TILE + S3_SPACING);

        if (cellX < 0 || cellY < 0 || cellX + S3_TILE > sheet32.getWidth() || cellY + S3_TILE > sheet32.getHeight()) return;

        // Split into 4 x 16x16 quadrants
        BufferedImage nw = sheet32.getSubimage(cellX + 0,  cellY + 0,  16, 16);
        BufferedImage ne = sheet32.getSubimage(cellX + 16, cellY + 0,  16, 16);
        BufferedImage sw = sheet32.getSubimage(cellX + 0,  cellY + 16, 16, 16);
        BufferedImage se = sheet32.getSubimage(cellX + 16, cellY + 16, 16, 16);

        out.add(tileOverlay(baseFloor, new FrameBuilder(nw).withScale(tileScale).build())); // NW
        out.add(tileOverlay(baseFloor, new FrameBuilder(ne).withScale(tileScale).build())); // NE
        out.add(tileOverlay(baseFloor, new FrameBuilder(sw).withScale(tileScale).build())); // SW
        out.add(tileOverlay(baseFloor, new FrameBuilder(se).withScale(tileScale).build())); // SE
    }

    // -------- frames & overlays (like CommonTileset) --------
    private MapTileBuilder tileOverlay(Frame baseFloor, Frame top) {
        return new MapTileBuilder(baseFloor)
                .withTopLayer(top)
                .withTileType(TileType.PASSABLE);
    }

    private Frame framePassable(BufferedImage sheet, int col, int row, int tile, int spacing, int margin) {
        BufferedImage sub = subTile(sheet, col, row, tile, spacing, margin);
        if (sub == null) sub = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        return new FrameBuilder(sub).withScale(tileScale).build();
    }

    private Frame frameSolid(BufferedImage sheet, int col, int row, int tile, int spacing, int margin) {
        BufferedImage sub = subTile(sheet, col, row, tile, spacing, margin);
        if (sub == null) sub = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        return new FrameBuilder(sub)
                .withScale(tileScale)
                .withBounds(0, 0, 16, 16)
                .build();
    }

    private BufferedImage subTile(BufferedImage img, int col, int row, int tile, int spacing, int margin) {
        int x = margin + col * (tile + spacing);
        int y = margin + row * (tile + spacing);
        if (x < 0 || y < 0 || x + tile > img.getWidth() || y + tile > img.getHeight()) return null;

        // If tile==32 (decor), we still use the full 32 to grab before splitting
        return img.getSubimage(x, y, tile, tile);
    }

    private int countCols(int imgW, int tile, int spacing, int margin) {
        int usable = imgW - 2 * margin;
        return Math.max(0, (usable + spacing) / (tile + spacing));
    }

    private int countRows(int imgH, int tile, int spacing, int margin) {
        int usable = imgH - 2 * margin;
        return Math.max(0, (usable + spacing) / (tile + spacing));
    }
}
