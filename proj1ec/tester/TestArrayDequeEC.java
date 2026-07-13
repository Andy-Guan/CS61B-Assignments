package tester;

import static org.junit.Assert.*;
import org.junit.Test;
import student.StudentArrayDeque;
import edu.princeton.cs.algs4.StdRandom;

public class TestArrayDequeEC {

    /**
        @Test
        public void testRandomized() {
            StudentArrayDeque<Integer> bugDeque = new StudentArrayDeque<>();
            ArrayDequeSolution<Integer> correctDeque = new ArrayDequeSolution<>();

            int N = 5000;
            for (int i = 0; i < N; i += 1) {
                int operationNumber = StdRandom.uniform(0, 4);

                if (operationNumber == 0) {
                    // addFirst
                    int randVal = StdRandom.uniform(0, 100);
                    bugDeque.addFirst(randVal);
                    correctDeque.addFirst(randVal);

                } else if (operationNumber == 1) {
                    //addLast
                    int randVal = StdRandom.uniform(0, 100);
                    bugDeque.addLast(randVal);
                    correctDeque.addLast(randVal);

                } else if (operationNumber == 2) {
                    // removeFirst
                    if (correctDeque.size() > 0) {
                        Integer expected = correctDeque.removeFirst();
                        Integer actual = bugDeque.removeFirst();
                        assertEquals(expected, actual);
                    }

                } else if (operationNumber == 3) {
                    // removeLast
                    if (correctDeque.size() > 0) {
                        Integer expected = correctDeque.removeLast();
                        Integer actual = bugDeque.removeLast();
                        assertEquals(expected, actual);
                    }
                }
            }
        }
    */

        @Test
        public void testRandomizedAugmented() {
            StudentArrayDeque<Integer> student = new StudentArrayDeque<>();
            ArrayDequeSolution<Integer> solution = new ArrayDequeSolution<>();


            StringBuilder operationHistory = new StringBuilder();

            int N = 5000;
            for (int i = 0; i < N; i += 1) {
                int operationNumber = StdRandom.uniform(0, 4);

                if (operationNumber == 0) {
                    // addFirst
                    int randVal = StdRandom.uniform(0, 100);
                    student.addFirst(randVal);
                    solution.addFirst(randVal);

                    operationHistory.append("addFirst(").append(randVal).append(")\n");

                } else if (operationNumber == 1) {
                    // addLast
                    int randVal = StdRandom.uniform(0, 100);
                    student.addLast(randVal);
                    solution.addLast(randVal);

                    operationHistory.append("addLast(").append(randVal).append(")\n");

                } else if (operationNumber == 2) {
                    // removeFirst
                    if (solution.size() > 0) {
                        operationHistory.append("removeFirst()\n");

                        Integer expected = solution.removeFirst();
                        Integer actual = student.removeFirst();

                        assertEquals(operationHistory.toString(), expected, actual);
                    }
                } else if (operationNumber == 3) {
                    // removeLast
                    if (solution.size() > 0) {
                        operationHistory.append("removeLast()\n");

                        Integer expected = solution.removeLast();
                        Integer actual = student.removeLast();

                        assertEquals(operationHistory.toString(), expected, actual);
                    }
                }
            }
        }


}
