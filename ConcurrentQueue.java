public interface ConcurrentQueue<T> {
    void enq(T item);
    T deq(); // lock-free queue returns null if empty
}
