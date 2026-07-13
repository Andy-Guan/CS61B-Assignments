package deque;

import org.junit.Test;
import java.util.Comparator;
import static org.junit.Assert.assertEquals;

public class MaxArrayDequeTest {
    public class StringLengthComparator implements Comparator<String> {
        @Override
        public int compare(String a, String b) {
            // 返回正数说明 a 比 b 长，返回负数说明 a 比 b 短
            return a.length() - b.length();
        }
    }
    public class StringAlphaComparator implements Comparator<String> {
        @Override
        public int compare(String a, String b) {
            return a.compareTo(b);
        }
    }


    @Test
    public void lengthtest() {
        Comparator<String> lenCmp = new StringLengthComparator();

        MaxArrayDeque<String> mad = new MaxArrayDeque<>(lenCmp);

        mad.addLast("dog");
        mad.addLast("watermelon");
        mad.addLast("apple");
        mad.addLast("zebra");


        String maxLen = mad.max();
        assertEquals(maxLen, "watermelon");

    }

    @Test
    public void alphatest() {
        Comparator<String> alphaCmp = new StringAlphaComparator();

        MaxArrayDeque<String> mad = new MaxArrayDeque<>(alphaCmp);

        mad.addLast("dog");
        mad.addLast("watermelon");
        mad.addLast("apple");
        mad.addLast("zebra");

        String maxAlpha = mad.max(alphaCmp);
        assertEquals(maxAlpha, "zebra");
    }


    @Test
    public void mixtest() {
        Comparator<String> lenCmp = new StringLengthComparator();
        Comparator<String> alphaCmp = new StringAlphaComparator();

        MaxArrayDeque<String> mad = new MaxArrayDeque<>(lenCmp);

        mad.addLast("dog");
        mad.addLast("watermelon");
        mad.addLast("apple");
        mad.addLast("zebra");

        String maxAlpha = mad.max(alphaCmp);
        assertEquals(maxAlpha, "zebra");
    }
}
