package io.github.concurrencylab;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A lock-free stack implementation using compare-and-swap operations.
 * Provides O(1) push and pop operations without using explicit locks.
 *
 * @param <T> the type of elements in this stack
 */
public class LockFreeStack<T> {

    private static class Node<T> {
        final T value;
        Node<T> next;

        Node(T value) {
            this.value = value;
        }
    }

    private final AtomicReference<Node<T>> top = new AtomicReference<>();

    /**
     * Pushes an item onto the top of this stack.
     *
     * @param item the item to be pushed
     */
    public void push(T item) {
        Node<T> newNode = new Node<>(item);
        Node<T> oldTop;
        do {
            oldTop = top.get();
            newNode.next = oldTop;
        } while (!top.compareAndSet(oldTop, newNode));
    }

    /**
     * Removes and returns the top item from this stack.
     *
     * @return the top item, or null if the stack is empty
     */
    public T pop() {
        Node<T> oldTop;
        Node<T> newTop;
        do {
            oldTop = top.get();
            if (oldTop == null) {
                return null;
            }
            newTop = oldTop.next;
        } while (!top.compareAndSet(oldTop, newTop));

        return oldTop.value;
    }

    /**
     * Returns true if this stack contains no elements.
     *
     * @return true if this stack is empty
     */
    public boolean isEmpty() {
        return top.get() == null;
    }

    /**
     * Returns the number of elements in this stack.
     * Note: This is an approximate count and may be inaccurate under concurrent modification.
     *
     * @return the approximate size of the stack
     */
    public int size() {
        int count = 0;
        Node<T> current = top.get();
        while (current != null) {
            count++;
            current = current.next;
        }
        return count;
    }
}