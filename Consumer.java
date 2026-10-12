import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Consumer extends Thread {
    private final ConcurrentQueue<Job> queue;
    private final AtomicInteger tickets;
    private final int total;
    private final AtomicIntegerArray processed; // processed[id] counts how often a job was processed

    public Consumer(ConcurrentQueue<Job> queue, AtomicInteger tickets, int total, AtomicIntegerArray processed) {
        this.queue = queue;
        this.tickets = tickets;
        this.total = total;
        this.processed = processed;
    }

    // Each consumer takes a ticket per job, and there are exactly as many tickets as jobs,
    // so every consumer stops once all jobs are taken and none is left waiting.
    public void run() {
        while (tickets.getAndIncrement() < total) {
            Job job;
            while ((job = queue.deq()) == null) Thread.yield(); // lock-free queue empty, retry
            processed.incrementAndGet(job.id);
        }
    }
}
