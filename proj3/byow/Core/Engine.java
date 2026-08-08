package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.StdDraw;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    private int playerX;
    private int playerY;
    private long currentSeed;

    private int score = 0;

    /**
     * Process the movement
     * @param c moving instruction (w/a/s/d)
     * @param world
     */
    private boolean processMovement(char c, TETile[][] world) {
        c = Character.toLowerCase(c);
        int nextX = playerX;
        int nextY = playerY;

        switch (c) {
            case 'w': nextY++; break;
            case 's': nextY--; break;
            case 'a': nextX--; break;
            case 'd': nextX++; break;
            default: return false;
        }

        if (nextX < 0 || nextX >= WIDTH || nextY < 0 || nextY >= HEIGHT) {
            return false;
        }

        TETile nextTile = world[nextX][nextY];

        if (nextTile == Tileset.FLOOR) {
            world[playerX][playerY] = Tileset.FLOOR;
            playerX = nextX;
            playerY = nextY;
            world[playerX][playerY] = Tileset.AVATAR;
            return true;
        } else if (nextTile == Tileset.CHEST_CLOSED) {
            world[nextX][nextY] = Tileset.CHEST_OPENED;
            score += 100;
            return true;
        }

        return false;
    }

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        drawMenu();
        TETile[][] worldFrame = null;
        String moveHistory = "";

        String seedInputStr = "";
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toLowerCase(StdDraw.nextKeyTyped());
                if (c == 'n') {
                    String rawSeed = solicitSeedInput();
                    seedInputStr = "N" + rawSeed + "S";
                    worldFrame = interactWithInputString(seedInputStr);
                    break;
                } else if (c == 'l') {
                    moveHistory = loadGameHistory();
                    if (moveHistory.isEmpty()) {
                        System.exit(0);
                    }
                    moveHistory = moveHistory.replace(":q", "").replace(":Q", "");
                    worldFrame = interactWithInputString(moveHistory);
                    break;
                } else if (c == 'q') {
                    System.exit(0);
                }
            }
        }

        boolean colonPressed = false;
        boolean lightsOn = false;

        TETile[][] initialDisplay = lightsOn ? worldFrame : applyLineOfSight(worldFrame);
        ter.renderFrame(initialDisplay);

        while (true) {
            TETile[][] displayWorld = lightsOn ? worldFrame : applyLineOfSight(worldFrame);

            ter.renderFrame(displayWorld);
            drawHUD(displayWorld);
            StdDraw.pause(15);

            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toLowerCase(StdDraw.nextKeyTyped());


                if (colonPressed && c == 'q') {
                    saveGameHistory(moveHistory);
                    System.exit(0);
                }
                if (c == ':') {
                    colonPressed = true;
                    continue;
                }
                if (colonPressed) {
                    colonPressed = false;
                    continue;
                }

                if (c == 'v') {
                    lightsOn = !lightsOn;
                    continue;
                }

                moveHistory += c;
                processMovement(c, worldFrame);
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
        if (input == null || input.isEmpty()) {
            return new TETile[WIDTH][HEIGHT];
        }
        String upperInput = input.toUpperCase();
        char firstChar = upperInput.charAt(0);

        if (firstChar == 'N') {
            return runGameFromSeed(upperInput);
        } else if (firstChar == 'L') {
            String savedHistory = loadGameHistory().toUpperCase().replace(":Q", "");
            String restMoves = upperInput.substring(1);
            String fullCommand = savedHistory + restMoves;

            if (fullCommand.isEmpty() || fullCommand.charAt(0) != 'N') {
                return new TETile[WIDTH][HEIGHT];
            }
            return runGameFromSeed(fullCommand);
        } else {
            return new TETile[WIDTH][HEIGHT];
        }
    }

    private TETile[][] runGameFromSeed(String upperCommand) {
        int startIndex = upperCommand.indexOf('N') + 1;
        int endIndex = upperCommand.indexOf('S');

        if (startIndex < 0 || endIndex < 0 || startIndex >= endIndex) {
            return new TETile[WIDTH][HEIGHT];
        }

        String seedString = upperCommand.substring(startIndex, endIndex);
        long seed = Long.parseLong(seedString);
        this.currentSeed = seed;
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
        spawnPlayer(finalWorldFrame, rooms);
        spawnChests(finalWorldFrame, rooms, random, 5);

        this.score = 0;

        boolean colonPressed = false;
        for (int i = endIndex + 1; i < upperCommand.length(); i++) {
            char c = upperCommand.charAt(i);

            if (colonPressed && c == 'Q') {
                saveGameHistory(upperCommand.substring(0, i + 1));
                break;
            }
            if (c == ':') {
                colonPressed = true;
                continue;
            } else {
                colonPressed = false;
            }
            processMovement(c, finalWorldFrame);
        }

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
                for (int cx = newRoom.getX(); cx < newRoom.getX() + newRoom.getWidth(); cx++) {
                    for (int cy = newRoom.getY(); cy < newRoom.getY() + newRoom.getHeight(); cy++) {
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
        engine.interactWithKeyboard();

        /**
        TETile[][] testWorld = engine.interactWithInputString("N1456S");

        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        ter.renderFrame(testWorld); */
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

    /**
     * Draw the initial menu
     */
    private void drawMenu() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);

        Font titleFont = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(titleFont);
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.75, "CS61B: The Game");

        Font menuFont = new Font("Monaco", Font.PLAIN, 20);
        StdDraw.setFont(menuFont);
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.5, "New Game (N)");
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.4, "Load Game (L)");
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.3, "Quit (Q)");

        StdDraw.show();
    }

    /**
     * Get and show the seed on the menu
     */
    private String solicitSeedInput() {
        String seedStr = "";

        drawSeedFrame(seedStr);

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                c = Character.toLowerCase(c);

                if (c == 's') {
                    break;
                } else if (Character.isDigit(c)) {
                    seedStr += c;
                    drawSeedFrame(seedStr);
                }
            }
        }
        return seedStr;
    }

    /**
     * Draw the temporary seed
     */
    private void drawSeedFrame(String currentSeed) {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);

        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);

        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.6, "Enter Seed:");
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.4,
                currentSeed + (currentSeed.length() % 2 == 0 ? "_" : ""));

        Font smallFont = new Font("Monaco", Font.PLAIN, 15);
        StdDraw.setFont(smallFont);
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.2, "Press 'S' to Start");

        StdDraw.show();
    }

    /**
     * Save the game
     */
    private void saveGameHistory(String history) {
        try {
            FileWriter writer = new FileWriter("savefile.txt");
            writer.write(history);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load the game
     */
    private String loadGameHistory() {
        try {
            File file = new File("savefile.txt");
            if (!file.exists()) {
                return "";
            }
            Scanner scanner = new Scanner(file);
            String history = scanner.hasNextLine() ? scanner.nextLine() : "";
            scanner.close();
            return history.trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Draw the hud according to the mouse
     */
    private void drawHUD(TETile[][] world) {
        int mouseX = (int) StdDraw.mouseX();
        int mouseY = (int) StdDraw.mouseY();

        String tileName = "";

        if (mouseX >= 0 && mouseX < WIDTH && mouseY >= 0 && mouseY < HEIGHT) {
            tileName = world[mouseX][mouseY].description();
        }

        StdDraw.setPenColor(Color.BLACK);
        StdDraw.filledRectangle(WIDTH / 2.0, HEIGHT + 1, WIDTH / 2.0, 1);

        StdDraw.setPenColor(Color.WHITE);
        Font smallFont = new Font("Monaco", Font.PLAIN, 16);
        StdDraw.setFont(smallFont);

        StdDraw.textLeft(1, HEIGHT + 0.5, tileName);

        StdDraw.textRight(WIDTH - 1, HEIGHT + 0.5, "Score: " + score);

        StdDraw.show();
    }

    /**
     * Generate the world with limited sight
     */
    private TETile[][] applyLineOfSight(TETile[][] world) {
        TETile[][] visibleWorld = new TETile[WIDTH][HEIGHT];
        int visionRadius = 5;

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                double distance = Math.sqrt(Math.pow(x - playerX, 2) + Math.pow(y - playerY, 2));

                if (distance <= visionRadius) {
                    visibleWorld[x][y] = world[x][y];
                } else {
                    visibleWorld[x][y] = Tileset.NOTHING;
                }
            }
        }
        return visibleWorld;
    }

    /**
     * Place the chest
     */
    private void spawnChests(TETile[][] world, java.util.List<Room> rooms,
                             Random random, int numChests) {
        if (rooms.size() <= 1) {
            return;
        }

        for (int i = 0; i < numChests; i++) {
            int roomIndex = random.nextInt(rooms.size() - 1) + 1;
            Room room = rooms.get(roomIndex);

            int rx = random.nextInt(room.getWidth()) + room.getX();
            int ry = random.nextInt(room.getHeight()) + room.getY();

            if (world[rx][ry].character() == Tileset.FLOOR.character()) {
                world[rx][ry] = Tileset.CHEST_CLOSED;
            }
        }
    }
}
