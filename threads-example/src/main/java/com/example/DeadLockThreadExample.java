package com.example;

public class DeadLockThreadExample implements Example {

    private final Object lock1 = new Object();
    private final Object lock2 = new Object();

    @Override
    public void example() {
        System.out.println("\n=== Deadlock Example ===");

        Thread thread1 = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("Thread 1: Segurando lock1...");

                try {
                    Thread.sleep(100); // Simula processamento
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                System.out.println("Thread 1: Esperando por lock2...");
                synchronized (lock2) {
                    System.out.println("Thread 1: Segurando lock1 e lock2...");
                }
            }
        });

        Thread thread2 = new Thread(() -> {
            synchronized (lock2) {
                System.out.println("Thread 2: Segurando lock2...");

                try {
                    Thread.sleep(100); // Simula processamento
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                System.out.println("Thread 2: Esperando por lock1...");
                synchronized (lock1) {
                    System.out.println("Thread 2: Segurando lock2 e lock1...");
                }
            }
        });

        thread1.start();
        thread2.start();

        try {
            // Adicionando timeout de 2 segundos para o join
            thread1.join(2000);
            thread2.join(2000);

            if (thread1.isAlive() || thread2.isAlive()) {
                System.out.println("\n=== DEADLOCK DETECTED ===");
                System.out.println("Threads não conseguiram terminar dentro do tempo limite");

                // Interrompe as threads (pode não resolver o deadlock, mas permite que o programa continue)
                thread1.interrupt();
                thread2.interrupt();
            } else {
                System.out.println("Todos os threads terminaram com sucesso");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread principal interrompido");
        }

        System.out.println("Programa continuando após tratamento do deadlock");
    }
}