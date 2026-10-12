import java.util.concurrent.atomic.AtomicInteger;

public class Producer extends Thread {
    private final ConcurrentQueue<Job> queue;
    private final int start, count;
    private final AtomicInteger produced;

    public Producer(ConcurrentQueue<Job> queue, int start, int count, AtomicInteger produced) {
        this.queue = queue;
        this.start = start;
        this.count = count;
        this.produced = produced;
    }

    public void run() {
        for (int i = 0; i < count; i++) {
            queue.enq(new Job(start + i)); // ids are unique across producers
            produced.incrementAndGet();
        }
    }
}
