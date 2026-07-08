package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {

    @Test
    public void testThreeAddThreeRemove(){
        BuggyAList<Integer> B = new BuggyAList<>();
        AListNoResizing<Integer> A = new AListNoResizing<>();
        B.addLast(4);
        B.addLast(5);
        B.addLast(6);
        A.addLast(4);
        A.addLast(5);
        A.addLast(6);

        int B1 = B.removeLast();
        int B2 = B.removeLast();
        int B3 = B.removeLast();

        int A1 = A.removeLast();
        int A2 = A.removeLast();
        int A3 = A.removeLast();

        assertEquals(B1,A1);
        assertEquals(B2,A2);
        assertEquals(B3,A3);
    }


    @Test
    public void randomizedTest(){
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> J = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
                J.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int Lsize = L.size();
                System.out.println("size: " + Lsize);
                int Jsize = J.size();
                System.out.println("size: " + Jsize);
            } else if (operationNumber == 2) {
                // removeLast
                if(L.size() == 0){
                    continue;
                }
                int LlastVal = L.removeLast();
                System.out.println("removeLast(" + LlastVal + ")");
                if(J.size() == 0){
                    continue;
                }
                int JlastVal = J.removeLast();
                System.out.println("removeLast(" + JlastVal + ")");
            } else if (operationNumber == 3) {
                // getLast
                if(L.size() == 0){
                    continue;
                }
                int LlastVal = L.getLast();
                System.out.println("getLast(" + LlastVal + ")");
                if(J.size() == 0){
                    continue;
                }
                int JlastVal = L.getLast();
                System.out.println("getLast(" + JlastVal + ")");
            }
        }
    }
}
