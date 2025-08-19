package com.example;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;

public class DeadLockSolutionExample implements Example {

    private final ReentrantLock lock1 = new ReentrantLock();
    private final ReentrantLock lock2 = new ReentrantLock();

    @Override
    public void example() {
        System.out.println("\n=== Deadlock Solution Example ===");

        Thread thread1 = new Thread(() -> {
            try {
                if (lock1.tryLock(1, TimeUnit.SECONDS)) {
                    try {
                        System.out.println("Thread 1: Segurando lock1...");
                        Thread.sleep(100); // Simula processamento

                        System.out.println("Thread 1: Tentando obter lock2...");
                        if (lock2.tryLock(1, TimeUnit.SECONDS)) {
                            try {
                                System.out.println("Thread 1: Segurando ambos locks!");
                            } finally {
                                lock2.unlock();
                            }
                        } else {
                            System.out.println("Thread 1: Não conseguiu lock2, liberando lock1");
                        }
                    } finally {
                        lock1.unlock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread 1 interrompida");
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                if (lock2.tryLock(1, TimeUnit.SECONDS)) {
                    try {
                        System.out.println("Thread 2: Segurando lock2...");
                        Thread.sleep(100); // Simula processamento

                        System.out.println("Thread 2: Tentando obter lock1...");
                        if (lock1.tryLock(1, TimeUnit.SECONDS)) {
                            try {
                                System.out.println("Thread 2: Segurando ambos locks!");
                            } finally {
                                lock1.unlock();
                            }
                        } else {
                            System.out.println("Thread 2: Não conseguiu lock1, liberando lock2");
                        }
                    } finally {
                        lock2.unlock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread 2 interrompida");
            }
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join(3000);
            thread2.join(3000);

            if (thread1.isAlive() || thread2.isAlive()) {
                System.out.println("\nAlgumas threads não terminaram:");
                printThreadStatus(thread1, "Thread 1");
                printThreadStatus(thread2, "Thread 2");

                // Forçar término se necessário
                System.exit(0);
            } else {
                System.out.println("Todas as threads terminaram com sucesso");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread principal interrompido");
        }

        System.out.println("Programa finalizado corretamente");
    }

    private void printThreadStatus(Thread thread, String name) {
        System.out.println(name + " estado: " + thread.getState());
        System.out.println(name + " isAlive: " + thread.isAlive());
    }
}