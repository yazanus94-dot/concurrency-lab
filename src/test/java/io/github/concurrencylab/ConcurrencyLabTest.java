package io.github.concurrencylab;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class LockFreeStackTest {

    @Test
    void pushAndPopReturnsItemsInLIFOOrder() {
        LockFreeStack<Integer> stack = new LockFreeStack<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);

        assertEquals(3, stack.pop());
        assertEquals(2, stack.pop());
        assertEquals(1, stack.pop());
        assertNull(stack.pop());
    }

    @Test
    void isEmptyReturnsTrueForEmptyStack() {
        LockFreeStack<String> stack = new LockFreeStack<>();
        assertTrue(stack.isEmpty());
    }

    @Test
    void isEmptyReturnsFalseAfterPush() {
        LockFreeStack<String> stack = new LockFreeStack<>();
        stack.push("hello");
        assertFalse(stack.isEmpty());
    }

    @Test
    @Timeout(10)
    void concurrentPushPopMaintainsConsistency() throws InterruptedException {
        LockFreeStack<Integer> stack = new LockFreeStack<>();
        int threadCount = 8;
        int itemsPerThread = 1000;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            Thread thread = new Thread(() -> {
                for (int i = 0; i < itemsPerThread; i++) {
                    stack.push(i);
                }
                latch.countDown();
            });
            thread.start();
        }

        latch.await();
        // All items should be poppable
        int totalItems = threadCount * itemsPerThread;
        int popped = 0;
        while (!stack.isEmpty()) {
            stack.pop();
            popped++;
        }
        assertEquals(totalItems, popped);
    }
}

class LockFreeQueueTest {

    @Test
    void offerAndPollReturnsItemsInFIFOOrder() {
        LockFreeQueue<String> queue = new LockFreeQueue<>(8);
        queue.offer("first");
        queue.offer("second");
        queue.offer("third");

        assertEquals("first", queue.poll());
        assertEquals("second", queue.poll());
        assertEquals("third", queue.poll());
        assertNull(queue.poll());
    }

    @Test
    void offerReturnsFalseWhenFull() {
        LockFreeQueue<Integer> queue = new LockFreeQueue<>(4);
        assertTrue(queue.offer(1));
        assertTrue(queue.offer(2));
        assertTrue(queue.offer(3));
        assertTrue(queue.offer(4));
        assertFalse(queue.offer(5));
    }

    @Test
    void isEmptyReturnsTrueForEmptyQueue() {
        LockFreeQueue<String> queue = new LockFreeQueue<>(8);
        assertTrue(queue.isEmpty());
    }

    @Test
    void sizeReturnsCorrectCount() {
        LockFreeQueue<Integer> queue = new LockFreeQueue<>(16);
        assertEquals(0, queue.size());
        queue.offer(1);
        assertEquals(1, queue.size());
        queue.offer(2);
        assertEquals(2, queue.size());
        queue.poll();
        assertEquals(1, queue.size());
    }

    @Test
    @Timeout(10)
    void concurrentOfferPollMaintainsConsistency() throws InterruptedException {
        LockFreeQueue<Integer> queue = new LockFreeQueue<>(1024);
        int threadCount = 4;
        int itemsPerThread = 500;
        CountDownLatch producerLatch = new CountDownLatch(threadCount);
        CountDownLatch consumerLatch = new CountDownLatch(threadCount);
        AtomicInteger totalConsumed = new AtomicInteger(0);

        // Producers
        for (int t = 0; t < threadCount; t++) {
            final int base = t * itemsPerThread;
            Thread producer = new Thread(() -> {
                for (int i = 0; i < itemsPerThread; i++) {
                    while (!queue.offer(base + i)) {
                        Thread.yield();
                    }
                }
                producerLatch.countDown();
            });
            producer.start();
        }

        // Consumers
        for (int t = 0; t < threadCount; t++) {
            Thread consumer = new Thread(() -> {
                int consumed = 0;
                while (consumed < itemsPerThread) {
                    Integer item = queue.poll();
                    if (item != null) {
                        consumed++;
                        totalConsumed.incrementAndGet();
                    } else {
                        Thread.yield();
                    }
                }
                consumerLatch.countDown();
            });
            consumer.start();
        }

        producerLatch.await();
        consumerLatch.await();

        int expected = threadCount * itemsPerThread;
        assertEquals(expected, totalConsumed.get());
    }

    @Test
    void invalidCapacityThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new LockFreeQueue<>(0));
        assertThrows(IllegalArgumentException.class, () -> new LockFreeQueue<>(7));
    }
}

class ThreadPoolTest {

    @Test
    void submitExecutesTask() throws InterruptedException {
        ThreadPool pool = new ThreadPool(4);
        CountDownLatch latch = new CountDownLatch(1);
        pool.submit(latch::countDown);
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        pool.shutdown();
    }

    @Test
    void submitAfterShutdownThrows() {
        ThreadPool pool = new ThreadPool(2);
        pool.shutdown();
        assertThrows(IllegalStateException.class, () -> pool.submit(() -> {}));
    }

    @Test
    void invalidWorkerCountThrows() {
        assertThrows(IllegalArgumentException.class, () -> new ThreadPool(0));
        assertThrows(IllegalArgumentException.class, () -> new ThreadPool(-1));
    }

    @Test
    void closeShutsDownPool() throws InterruptedException {
        ThreadPool pool = new ThreadPool(2);
        pool.close();
        // After close, no tasks should be accepted
        assertThrows(IllegalStateException.class, () -> pool.submit(() -> {}));
    }
}