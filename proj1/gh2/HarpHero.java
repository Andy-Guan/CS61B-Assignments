package gh2;
import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;


public class HarpHero {
    private static final String KEYBOARD = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";
    private static final int NUM_STRINGS = 37;

    public static void main(String[] args) {
        HarpString[] strings = new HarpString[NUM_STRINGS];

        for (int i = 0; i < NUM_STRINGS; i++) {
            double frequency = 440.0 * Math.pow(2, (i - 24.0) / 12.0);
            strings[i] = new HarpString(frequency);
        }

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                int index = KEYBOARD.indexOf(key);
                if (index != -1) {
                    strings[index].pluck();
                }
            }

            double sample = 0.0;
            for (int i = 0; i < NUM_STRINGS; i++) {
                sample += strings[i].sample();
            }

            StdAudio.play(sample);

            for (int i = 0; i < NUM_STRINGS; i++) {
                strings[i].tic();
            }
        }
    }
}
