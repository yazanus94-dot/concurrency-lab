# Concurrency Lab

An advanced concurrent programming utilities library for Java 17+. Provides lock-free data structures, thread pool management, and synchronization primitives designed for high-throughput applications.

![Java](https://img.shields.io/badge/Java-17-orange)
![Maven](https://img.shields.io/badge/Maven-3.9.1-blue)
![License](https://img.shields.io/badge/License-MIT-green)

## Overview

Concurrency Lab provides production-ready concurrent primitives that eliminate common pitfalls in multi-threaded Java applications. The library focuses on lock-free algorithms that maximize throughput while minimizing contention, making it ideal for trading systems, message brokers, and real-time data processing.

## Key Features

- **Lock-Free Stack**: CAS-based stack with O(1) push/pop operations
- **Lock-Free Queue**: Bounded ring-buffer queue with atomic index management
- **Configurable ThreadPool**: Worker pool with graceful shutdown support
- **Thread-Safe Operations**: All data structures are safe for concurrent access
- **Zero-Lock Contention**: Minimal blocking under high concurrency
- **Graceful Shutdown**: Context-aware worker termination

## Architecture

```
┌─────────────────────────────────────────┐
│              Concurrency Lab             │
├─────────────────────────────────────────┤
│  LockFreeStack<T>  │  LockFreeQueue<T>  │
│  CAS-based stack   │  Ring-buffer queue │
├─────────────────────────────────────────┤
│  ThreadPool        │  WorkerThread      │
│  Task queue        │  Graceful shutdown │
└─────────────────────────────────────────┘
```

## Installation

```bash
git clone https://github.com/yourusername/concurrency-lab
cd concurrency-lab
mvn install
```

## Quick Start

```java
import io.github.concurrencylab.ConcurrencyLab;
import io.github.concurrencylab.LockFreeStack;
import io.github.concurrencylab.ThreadPool;

public class Example {
    public static void main(String[] args) {
        // Lock-free stack
        LockFreeStack<String> stack = ConcurrencyLab.lockFreeStack();
        stack.push("hello");
        stack.push("world");
        System.out.println(stack.pop()); // "world"

        // Thread pool
        ThreadPool pool = ConcurrencyLab.threadPool(4);
        pool.submit(() -> System.out.println("Task executed"));
        pool.shutdown();
    }
}
```

## Testing

```bash
mvn test
mvn test -Dtest=LockFreeStackTest
```

## License

MIT