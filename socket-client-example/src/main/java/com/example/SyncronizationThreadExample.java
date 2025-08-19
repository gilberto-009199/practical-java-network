package com.example;

import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

public class SyncronizationThreadExample implements Example {

    private int sharedCounter = 0;
    private final Object lockObject = new Object();
    private final AtomicInteger atomicCounter = new AtomicInteger(0);
    private final ReentrantLock reentrantLock = new ReentrantLock();

    @Override
    public void example() {
        System.out.println("\n=== Synchronization in Threads ===\n");

        // 1. Synchronized Blocks
        synchronizedBlocksExample();

        // 2. Synchronized Methods
        synchronizedMethodsExample();

        // 3. Alternatives to Synchronization
        alternativesToSynchronizationExample();
    }

    private void synchronizedBlocksExample() {
        System.out.println("\n1. Synchronized Blocks Example:");

        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                synchronized (lockObject) { // Bloco sincronizado
                    sharedCounter++;
                }
            }
        };

        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Counter with synchronized blocks: " + sharedCounter);
    }

    private synchronized void incrementCounter() { // Método sincronizado
        sharedCounter++;
    }

    private void synchronizedMethodsExample() {
        System.out.println("\n2. Synchronized Methods Example:");

        sharedCounter = 0; // Reset counter

        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                incrementCounter();
            }
        };

        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Counter with synchronized methods: " + sharedCounter);
    }

    private void alternativesToSynchronizationExample() {
        System.out.println("\n3. Alternatives to Synchronization:");

        // a. Variáveis locais (thread-safe por natureza)
        System.out.println("a. Local variables (thread-safe by nature):");
        Runnable localVarTask = () -> {
            int localCounter = 0; // Variável local é thread-safe
            for (int i = 0; i < 1000; i++) {
                localCounter++;
            }
            System.out.println(Thread.currentThread().getName() + " local counter: " + localCounter);
        };

        new Thread(localVarTask, "Thread-1").start();
        new Thread(localVarTask, "Thread-2").start();

        // b. Classes do pacote java.util.concurrent.atomic
        System.out.println("\nb. java.util.concurrent.atomic classes:");
        Runnable atomicTask = () -> {
            for (int i = 0; i < 1000; i++) {
                atomicCounter.incrementAndGet();
            }
        };

        Thread atomicThread1 = new Thread(atomicTask);
        Thread atomicThread2 = new Thread(atomicTask);

        atomicThread1.start();
        atomicThread2.start();

        try {
            atomicThread1.join();
            atomicThread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Atomic counter: " + atomicCounter.get());

        // c. ReentrantLock
        System.out.println("\nc. ReentrantLock:");
        Runnable lockTask = () -> {
            for (int i = 0; i < 1000; i++) {
                reentrantLock.lock();
                try {
                    sharedCounter++;
                } finally {
                    reentrantLock.unlock();
                }
            }
        };

        Thread lockThread1 = new Thread(lockTask);
        Thread lockThread2 = new Thread(lockTask);

        sharedCounter = 0; // Reset counter
        lockThread1.start();
        lockThread2.start();

        try {
            lockThread1.join();
            lockThread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Counter with ReentrantLock: " + sharedCounter);
    }
}