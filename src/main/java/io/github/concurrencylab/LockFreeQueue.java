package io.github.concurrencylab;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * A bounded lock-free queue implementation using a ring buffer.
 * Provides O(1) enqueue and dequeue operations with atomic index management.
 *
 * @param <T> the type of elements in this queue
 */
public class LockFreeQueue<T> {

    private static class Cell<T> {
        final T value;
        Cell(T value) { this.value = value; }
    }

    private final AtomicReferenceArray<Cell<T>> buffer;
    private final AtomicInteger head = new AtomicInteger(0);
    private final AtomicInteger tail = new AtomicInteger(0);
    private final int capacity;
    private final int mask;

    /**
     * Creates a new lock-free queue with the specified capacity.
     *
     * @param capacity the maximum number of elements (must be a power of 2)
     */
    public LockFreeQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        if ((capacity & (capacity - 1)) != 0) {
            throw new IllegalArgumentException("Capacity must be a power of 2");
        }
        this.capacity = capacity;
        this.mask = capacity - 1;
        this.buffer = new AtomicReferenceArray<>(capacity);
    }

    /**
     * Adds an item to the tail of this queue.
     *
     * @param item the item to be added
     * @return true if the item was added, false if the queue is full
     */
    public boolean offer(T item) {
        int currentTail;
        int currentHead;
        do {
            currentTail = tail.get();
            currentHead = head.get();
            if (currentTail - currentHead >= capacity) {
                return false; // Queue is full
            }
        } while (!tail.compareAndSet(currentTail, currentTail + 1));

        buffer.set(currentTail & mask, new Cell<>(item));
        return true;
    }

    /**
     * Removes and returns the head item from this queue.
     *
     * @return the head item, or null if the queue is empty
     */
    public T poll() {
        int currentHead;
        int currentTail;
        do {
            currentHead = head.get();
            currentTail = tail.get();
            if (currentHead >= currentTail) {
                return null; // Queue is empty
            }
        } while (!head.compareAndSet(currentHead, currentHead + 1));

        Cell<T> cell = buffer.getAndSet(currentHead & mask, null);
        return cell != null ? cell.value : null;
    }

    /**
     * Returns true if this queue contains no elements.
     */
    public boolean isEmpty() {
        return head.get() >= tail.get();
    }

    /**
     * Returns the approximate number of elements in this queue.
     */
    public int size() {
        return Math.max(0, tail.get() - head.get());
    }
}