package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.StdDraw;

import java.util.Random;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    private int playerX;
    private int playerY;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] worldFrame = interactWithInputString("N123456S");
        ter.renderFrame(worldFrame);

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                c = Character.toLowerCase(c);

                int nextX = playerX;
                int nextY = playerY;

                if (c == 'w') {
                    nextY += 1;
                } else if (c == 's') {
                    nextY -= 1;
                } else if (c == 'a') {
                    nextX -= 1;
                } else if (c == 'd') {
                    nextX += 1;
                } else if (c == 'q') {
                    System.exit(0);
                }

                if (worldFrame[nextX][nextY].character() == Tileset.FLOOR.character()) {
                    worldFrame[playerX][playerY] = Tileset.FLOOR;

                    playerX = nextX;
                    playerY = nextY;

                    worldFrame[playerX][playerY] = Tileset.AVATAR;

                    ter.renderFrame(worldFrame);
                }
            }
        }
    }



    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().

        String upperInput = input.toUpperCase();

        int startIndex = upperInput.indexOf('N') + 1;
        int endIndex = upperInput.indexOf('S');
        String seedString = upperInput.substring(startIndex, endIndex);

        long seed = Long.parseLong(seedString);
        Random random = new Random(seed);

        TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                finalWorldFrame[x][y] = Tileset.NOTHING;
            }
        }

        java.util.List<Room> rooms = generateRooms(finalWorldFrame, random, 100);
        connectRooms(finalWorldFrame, rooms, random);
        generateWalls(finalWorldFrame);

        return finalWorldFrame;
    }

    /**
     * Generate rooms randomly
     * @param world
     * @param random
     * @param numAttempts the number of rooms
     * @return
     */
    private java.util.List<Room> generateRooms(TETile[][] world, Random random, int numAttempts) {
        java.util.List<Room> rooms = new java.util.ArrayList<>();

        for (int i = 0; i < numAttempts; i++) {
            int width = random.nextInt(7) + 4;  // random.nextInt(n) 生成 0 到 n-1 的数
            int height = random.nextInt(7) + 4;

            int x = random.nextInt(WIDTH - width - 2) + 1;
            int y = random.nextInt(HEIGHT - height - 2) + 1;

            Room newRoom = new Room(x, y, width, height);

            boolean overlap = false;
            for (Room existingRoom : rooms) {
                if (newRoom.overlaps(existingRoom)) {
                    overlap = true;
                    break;
                }
            }

            if (!overlap) {
                rooms.add(newRoom);
                for (int cx = newRoom.x; cx < newRoom.x + newRoom.width; cx++) {
                    for (int cy = newRoom.y; cy < newRoom.y + newRoom.height; cy++) {
                        world[cx][cy] = Tileset.FLOOR;
                    }
                }
            }
        }
        return rooms;
    }

    /**
     * Draw horizontal hallway
     */
    private void drawHorizontalHallway(TETile[][] world, int x1, int x2, int y) {
        int startX = Math.min(x1, x2);
        int endX = Math.max(x1, x2);
        for (int i = startX; i <= endX; i++) {
            world[i][y] = Tileset.FLOOR;
        }
    }

    /**
     * Draw vertical hallway
     */
    private void drawVerticalHallway(TETile[][] world, int y1, int y2, int x) {
        int startY = Math.min(y1, y2);
        int endY = Math.max(y1, y2);
        for (int j = startY; j <= endY; j++) {
            world[x][j] = Tileset.FLOOR;
        }
    }

    /**
     * Connect all rooms in the world
     */
    private void connectRooms(TETile[][] world, java.util.List<Room> rooms, Random random) {
        if (rooms.size() < 2) {
            return;
        }

        for (int i = 0; i < rooms.size() - 1; i++) {
            Room roomA = rooms.get(i);
            Room roomB = rooms.get(i + 1);

            int startX = roomA.getCenterX();
            int startY = roomA.getCenterY();
            int endX = roomB.getCenterX();
            int endY = roomB.getCenterY();

            if (random.nextInt(2) == 0) {
                drawHorizontalHallway(world, startX, endX, startY);
                drawVerticalHallway(world, startY, endY, endX);
            } else {
                drawVerticalHallway(world, startY, endY, startX);
                drawHorizontalHallway(world, startX, endX, endY);
            }
        }
    }

    /**
     * Decide whether there is floor around it
     */
    private boolean isAdjacentToFloor(TETile[][] world, int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }

                int nx = x + dx;
                int ny = y + dy;

                if (nx >= 0 && nx < WIDTH && ny >= 0 && ny < HEIGHT) {
                    if (world[nx][ny] == Tileset.FLOOR) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Generate all the walls
     */
    private void generateWalls(TETile[][] world) {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (world[x][y] == Tileset.NOTHING) {
                    if (isAdjacentToFloor(world, x, y)) {
                        world[x][y] = Tileset.WALL;
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        Engine engine = new Engine();

        TETile[][] testWorld = engine.interactWithInputString("N1456S");

        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        ter.renderFrame(testWorld);
    }

    /**
     * Born the player
     */
    private void spawnPlayer(TETile[][] world, java.util.List<Room> rooms) {
        Room startRoom = rooms.get(0);
        playerX = startRoom.getCenterX();
        playerY = startRoom.getCenterY();

        world[playerX][playerY] = Tileset.AVATAR;
    }
}
