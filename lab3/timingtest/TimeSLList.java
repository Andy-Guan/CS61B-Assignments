package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        SLList testing = new SLList();
        AList Ns = new AList();
        AList Time = new AList();
        AList Op = new AList();
        int maxepochs = 256;
        int[] arr = {1,2,4,8,16,32,64,128,256,512,1024,2048};
        int i = 1;
        int M = 10000; //取出次数
        int count = 0;
        for(; i <= maxepochs * 1000; i++ ){
            testing.addLast(5);
            if(i/1000 == arr[count]){
                Stopwatch sw = new Stopwatch();
                int op = 1;
                for(;op <= M;op++) {
                    testing.getLast();
                }
                double timeInSeconds = sw.elapsedTime();
                Ns.addLast(i);
                Time.addLast(timeInSeconds);
                Op.addLast(M);
                count++;
            }
        }
        printTimingTable(Ns,Time,Op);

    }

}
