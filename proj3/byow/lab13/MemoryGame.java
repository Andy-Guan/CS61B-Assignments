package byow.lab13;

import byow.Core.RandomUtils;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

public class MemoryGame {
    /** The width of the window of this game. */
    private int width;
    /** The height of the window of this game. */
    private int height;
    /** The current round the user is on. */
    private int round;
    /** The Random object used to randomly generate Strings. */
    private Random rand;
    /** Whether or not the game is over. */
    private boolean gameOver;
    /** Whether or not it is the player's turn. Used in the last section of the
     * spec, 'Helpful UI'. */
    private boolean playerTurn;
    /** The characters we generate random Strings from. */
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    /** Encouraging phrases. Used in the last section of the spec, 'Helpful UI'. */
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};

    private String encouragetext;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }

        long seed = Long.parseLong(args[0]);
        MemoryGame game = new MemoryGame(40, 40, seed);
        game.startGame();
    }

    public MemoryGame(int width, int height, long seed) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

        rand = new Random(seed);
    }

    public String generateRandomString(int n) {
        String result = "";
        for (int i = 0; i < n; i += 1) {
            int idx = rand.nextInt(CHARACTERS.length);
            char c = CHARACTERS[idx];
            result = result + c;
        }
        return result;
    }

    public void drawFrame(String mainText, int round, String mode) {
        StdDraw.clear();
        Font BIGfont = new Font("SansSerif", Font.BOLD,30);
        Font SMALLfont = new Font("SansSerif", Font.BOLD,15);
        StdDraw.setPenColor(StdDraw.BLACK);

        if(mode != null) {
            StdDraw.setFont(SMALLfont);
            StdDraw.text(0.1 * width, 0.92 * height, "Round: " + round);
            StdDraw.text(0.5 * width, 0.92 * height, mode);
            StdDraw.text(0.85 * width, 0.92 * height, encouragetext);
        }

        StdDraw.setFont(BIGfont);
        StdDraw.text(0.5 * width, 0.5 * height, mainText);
        StdDraw.show();
    }

    public void drawFrame(String s) {
        drawFrame(s, 0, null);
    }

    public void flashSequence(String letters, int round ) {
        for (int i = 0; i < letters.length(); i += 1) {
            char ch = letters.charAt(i);
            drawFrame(String.valueOf(ch), round, "Watch!");
            StdDraw.pause(1000);

            drawFrame(" ", round, "Watch!");
            StdDraw.show();
            StdDraw.pause(500);
        }
    }

    public String solicitNCharsInput(int n, int round) {
        String input = "";
        while (input.length() < n) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                input = input + key;
            }
            drawFrame(input, round, "Type!");
        }
        StdDraw.pause(500);
        return input;
    }

    public void startGame() {
        int round = 1;
        while (true) {
            int randomIdx = rand.nextInt(ENCOURAGEMENT.length);
            encouragetext = ENCOURAGEMENT[randomIdx];

            drawFrame("Round: " + round, round, "Watch!");
            StdDraw.pause(1000);

            String target = generateRandomString(round);
            flashSequence(target, round);
            String playerAnswer = solicitNCharsInput(round, round);

            if (playerAnswer.equals(target)) {
                round += 1;
            } else {
                drawFrame("Game Over! You made it to round: " + round);
                break;
            }
        }
    }

}
