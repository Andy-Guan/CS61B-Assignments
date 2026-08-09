package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {

    /**
     * python version
     *
     * def draw_hex_helper(b, t, n):
     *     print(" " * b + "X" * t)
     *
     *     if b > 0:
     *         draw_hex_helper(b - 1, t + 2, n)
     *     print(" " * b + "X" * t)
     *
     * def draw_hex(n):
     *     draw_hex_helper(n - 1, n, n)
     *
     */

    private static final int WIDTH = 60;
    private static final int HEIGHT = 60;

    private static final long SEED = 2872345;
    private static final Random RANDOM = new Random(SEED);

    /**
     * Record the position
     */
    private static class Position {
        int x;
        int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Position shift(int dx, int dy) {
            return new Position(this.x + dx, this.y + dy);
        }
    }


    public static void drawRow(TETile[][] tiles, Position p, TETile tile, int length) {
        for (int dx = 0; dx < length; dx++) {
            tiles[p.x + dx][p.y] = tile;
        }
    }

    /**
     * @param b blanks
     * @param t number of tiles
     */
    public static void addHexagonHelper(TETile[][] tiles, Position p, TETile tile, int b, int t) {
        Position startOfRow = p.shift(b, 0);
        drawRow(tiles, startOfRow, tile, t);

        if (b > 0) {
            Position nextP = p.shift(0, -1);
            addHexagonHelper(tiles, nextP, tile, b - 1, t + 2);
        }

        Position startOfReflectedRow = startOfRow.shift(0, -(2 * b + 1));
        drawRow(tiles, startOfReflectedRow, tile, t);
    }

    public static void addHexagon(TETile[][] tiles, Position p, TETile tile, int size) {
        if (size < 2) return;
        addHexagonHelper(tiles, p, tile, size - 1, size);
    }

    public static TETile randomBiome() {
        int tileNum = RANDOM.nextInt(5);
        switch (tileNum) {
            case 0: return Tileset.GRASS;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.SAND;
            case 3: return Tileset.MOUNTAIN;
            case 4: return Tileset.TREE;
            default: return Tileset.NOTHING;
        }
    }


    public static Position getBottomNeighbor(Position p, int size) {
        return p.shift(0, -2 * size);
    }

    public static Position getTopRightNeighbor(Position p, int size) {
        return p.shift(2 * size - 1, size);
    }

    public static Position getBottomRightNeighbor(Position p, int size) {
        return p.shift(2 * size - 1, -size);
    }


    public static void addHexColumn(TETile[][] tiles, Position p, int size, int num) {
        if (num < 1) return;
        addHexagon(tiles, p, randomBiome(), size);
        if (num > 1) {
            Position bottomNeighbor = getBottomNeighbor(p, size);
            addHexColumn(tiles, bottomNeighbor, size, num - 1);
        }
    }


    public static void drawWorld(TETile[][] tiles, Position p, int hexSize, int tessSize) {
        addHexColumn(tiles, p, hexSize, tessSize);

        for (int i = 1; i < tessSize; i++) {
            p = getTopRightNeighbor(p, hexSize);
            addHexColumn(tiles, p, hexSize, tessSize + i);
        }

        for (int i = tessSize - 2; i >= 0; i--) {
            p = getBottomRightNeighbor(p, hexSize);
            addHexColumn(tiles, p, hexSize, tessSize + i);
        }
    }


    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] randomTiles = new TETile[WIDTH][HEIGHT];

        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                randomTiles[x][y] = Tileset.NOTHING;
            }
        }

        Position p = new Position(10, 40);
        TETile t = randomBiome();
        //addHexagon(randomTiles, p, t, 3);

        drawWorld(randomTiles, p, 3, 3);

        ter.renderFrame(randomTiles);
    }
}
