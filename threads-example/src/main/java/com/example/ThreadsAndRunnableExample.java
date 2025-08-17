package com.example;

public class ThreadsAndRunnableExample implements Example {

    public void example() {
        // 1. Demonstração básica de execução de threads
        runningThreads();

        // 2. Demonstração de herança de Thread
        subclassingThread();

        // 3. Demonstração de implementação de Runnable
        implementingRunnable();
    }

    private void runningThreads() {
        System.out.println("\n=== 1. Running Threads ===");

        // Criando uma thread simples usando lambda
        Thread simpleThread = new Thread(() -> {
            System.out.println("Thread básica iniciada - " + Thread.currentThread().getName());
            try {
                // Simula algum processamento
                Thread.sleep(800);
            } catch (InterruptedException e) {
                System.out.println("Thread interrompida!");
            }
            System.out.println("Thread básica concluída - " + Thread.currentThread().getName());
        }, "Basic-Thread");

        simpleThread.start();

        try {
            simpleThread.join(); // Espera a thread terminar (apenas para demonstração)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Erro ao aguardar thread: " + e.getMessage());
        }
    }

    private void subclassingThread() {
        System.out.println("\n=== 2. Subclassing Thread ===");

        // Criando uma thread estendendo a classe Thread
        ThreadCounter threadCounter = new ThreadCounter("Counter-Thread", 5);
        threadCounter.start();

        try {
            threadCounter.join(); // Espera a thread terminar
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Erro ao aguardar thread: " + e.getMessage());
        }
    }

    private void implementingRunnable() {
        System.out.println("\n=== 3. Implementing Runnable ===");

        // Criando uma thread implementando Runnable
        RunnableTask task = new RunnableTask("Runnable-Task", 3);
        Thread runnableThread = new Thread(task, "Task-Thread");
        runnableThread.start();

        try {
            runnableThread.join(); // Espera a thread terminar
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Erro ao aguardar thread: " + e.getMessage());
        }
    }

    // Classe interna que estende Thread
    private static class ThreadCounter extends Thread {
        private final int count;

        public ThreadCounter(String name, int count) {
            super(name); // Define o nome da thread
            this.count = count;
        }

        @Override
        public void run() {
            System.out.println(getName() + " iniciada");
            for (int i = 1; i <= count; i++) {
                System.out.println(getName() + " - contagem: " + i);
                try {
                    Thread.sleep(500); // Pausa entre contagens
                } catch (InterruptedException e) {
                    System.out.println(getName() + " interrompida!");
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            System.out.println(getName() + " concluída");
        }
    }

    // Classe interna que implementa Runnable
    private static class RunnableTask implements Runnable {
        private final String taskName;
        private final int iterations;

        public RunnableTask(String taskName, int iterations) {
            this.taskName = taskName;
            this.iterations = iterations;
        }

        @Override
        public void run() {
            System.out.println(taskName + " iniciada - " + Thread.currentThread().getName());
            for (int i = 0; i < iterations; i++) {
                System.out.println(taskName + " em execução (" + (i+1) + "/" + iterations + ")");
                try {
                    Thread.sleep(700); // Simula trabalho
                } catch (InterruptedException e) {
                    System.out.println(taskName + " interrompida!");
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            System.out.println(taskName + " concluída - " + Thread.currentThread().getName());
        }
    }
}