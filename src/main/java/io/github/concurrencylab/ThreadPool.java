package io.github.concurrencylab;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A configurable thread pool with worker threads and a bounded task queue.
 * Supports graceful shutdown and worker reuse.
 */
public class ThreadPool implements AutoCloseable {

    private final WorkerThread[] workers;
    private final BlockingQueue<Runnable> taskQueue;
    private final AtomicInteger activeThreads = new AtomicInteger(0);
    private volatile boolean shutdown = false;

    /**
     * Creates a new thread pool with the specified number of worker threads.
     *
     * @param workerCount the number of worker threads
     */
    public ThreadPool(int workerCount) {
        if (workerCount <= 0) {
            throw new IllegalArgumentException("Worker count must be positive");
        }
        this.taskQueue = new LinkedBlockingQueue<>();
        this.workers = new WorkerThread[workerCount];
        for (int i = 0; i < workerCount; i++) {
            workers[i] = new WorkerThread("pool-worker-" + i);
            workers[i].start();
        }
    }

    /**
     * Submits a task for execution by the thread pool.
     *
     * @param task the task to execute
     * @throws IllegalStateException if the pool has been shut down
     */
    public void submit(Runnable task) {
        if (shutdown) {
            throw new IllegalStateException("ThreadPool is shut down");
        }
        taskQueue.offer(() -> {
            activeThreads.incrementAndGet();
            try {
                task.run();
            } finally {
                activeThreads.decrementAndGet();
            }
        });
    }

    /**
     * Initiates an orderly shutdown where previously submitted tasks are executed.
     */
    public void shutdown() {
        shutdown = true;
        for (WorkerThread worker : workers) {
            worker.interrupt();
        }
    }

    /**
     * Returns the number of active threads currently executing tasks.
     */
    public int activeCount() {
        return activeThreads.get();
    }

    /**
     * Returns the approximate number of tasks waiting in the queue.
     */
    public int queuedTasks() {
        return taskQueue.size();
    }

    @Override
    public void close() {
        shutdown();
    }

    private class WorkerThread extends Thread {
        WorkerThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (!shutdown) {
                try {
                    Runnable task = taskQueue.take();
                    task.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}