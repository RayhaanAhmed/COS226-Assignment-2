import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

// Usage: java Main            (runs the experiment)
//        java Main bounded 4 4 (one run: bounded|lockfree producers consumers)
public class Main {
    static final int TOTAL = 120000;
    static final int CAPACITY = 100;
    static final int RUNS = 5;
    static boolean verbose = false;

    // runs one test, returns time in seconds, prints an error if a job was lost or duplicated
    static double run(String type, int p, int c) throws InterruptedException {
        ConcurrentQueue<Job> queue = type.equals("bounded") ? new BoundedQueue<Job>(CAPACITY) : new LockFreeQueue<Job>();
        AtomicInteger produced = new AtomicInteger(), tickets = new AtomicInteger();
        AtomicIntegerArray processed = new AtomicIntegerArray(TOTAL);
        Thread[] threads = new Thread[p + c];
        int perProducer = TOTAL / p;
        for (int i = 0; i < p; i++) threads[i] = new Producer(queue, i * perProducer, perProducer, produced);
        for (int i = 0; i < c; i++) threads[p + i] = new Consumer(queue, tickets, TOTAL, processed);

        long start = System.nanoTime();
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        double seconds = (System.nanoTime() - start) / 1e9;

        int total = 0;
        for (int i = 0; i < TOTAL; i++) {
            if (processed.get(i) != 1) System.out.println("ERROR: job " + i + " processed " + processed.get(i) + " times");
            total += processed.get(i);
        }
        if (verbose) System.out.println(type + " " + p + "P/" + c + "C: produced " + produced.get() + ", processed " + total);
        return seconds;
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length == 3) {
            verbose = true;
            double s = run(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            System.out.println("time " + String.format("%.4f", s) + " s, throughput " + (long) (TOTAL / s) + " jobs/s");
            return;
        }
        int[][] configs = {{4, 4}, {6, 2}, {2, 6}};
        String[] types = {"bounded", "lockfree"};
        for (String t : types) for (int[] c : configs) run(t, c[0], c[1]); // warm-up
        System.out.println("queue producers consumers time(s) throughput(jobs/s)");
        for (int[] c : configs) {
            for (String t : types) {
                double sum = 0;
                for (int i = 0; i < RUNS; i++) sum += run(t, c[0], c[1]);
                double avg = sum / RUNS;
                System.out.println(t + " " + c[0] + " " + c[1] + " " + String.format("%.4f", avg) + " " + (long) (TOTAL / avg));
            }
        }
    }
}
