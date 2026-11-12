package io.github.concurrencylab;

/**
 * Advanced concurrent programming utilities for Java.
 * Provides lock-free data structures, thread pool management, and synchronization primitives.
 */
public final class ConcurrencyLab {

    private ConcurrencyLab() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a lock-free stack using compare-and-swap operations.
     * @param <T> the type of elements in the stack
     * @return a new lock-free stack instance
     */
    public static <T> LockFreeStack<T> lockFreeStack() {
        return new LockFreeStack<>();
    }

    /**
     * Creates a bounded lock-free queue.
     * @param capacity the maximum number of elements
     * @param <T> the type of elements in the queue
     * @return a new bounded lock-free queue instance
     */
    public static <T> LockFreeQueue<T> lockFreeQueue(int capacity) {
        return new LockFreeQueue<>(capacity);
    }

    /**
     * Creates a thread pool with configurable worker count and queue.
     * @param workerCount the number of worker threads
     * @return a new ThreadPool instance
     */
    public static ThreadPool threadPool(int workerCount) {
        return new ThreadPool(workerCount);
    }
}