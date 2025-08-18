package com.example;

public class SchedulingThreadExample implements Example {

    @Override
    public void example() {
        System.out.println("\n=== Exemplos de Agendamento de Threads ===\n");

        // 1. Prioridades de Thread
        threadPriorityExample();

        // 2. Preempção (interrupção forçada)
        preemptionExample();
    }

    private void threadPriorityExample() {
        System.out.println("1. Exemplo de Prioridade de Threads:");

        // Cria threads com diferentes prioridades
        Thread lowPriorityThread = new Thread(new CounterTask("Baixa Prioridade", 1000));
        Thread normalPriorityThread = new Thread(new CounterTask("Prioridade Normal", 1000));
        Thread highPriorityThread = new Thread(new CounterTask("Alta Prioridade", 1000));

        // Define as prioridades (1 a 10, onde 10 é máxima)
        lowPriorityThread.setPriority(Thread.MIN_PRIORITY);    // Prioridade 1
        normalPriorityThread.setPriority(Thread.NORM_PRIORITY); // Prioridade 5 (padrão)
        highPriorityThread.setPriority(Thread.MAX_PRIORITY);    // Prioridade 10

        System.out.println("Iniciando threads com diferentes prioridades...");
        highPriorityThread.start();
        normalPriorityThread.start();
        lowPriorityThread.start();

        try {
            // Aguarda as threads terminarem
            highPriorityThread.join();
            normalPriorityThread.join();
            lowPriorityThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread principal interrompida");
        }

        System.out.println("Exemplo de prioridades concluído.\n");
    }

    private void preemptionExample() {
        System.out.println("2. Exemplo de Preempção de Threads:");

        // Tarefa que consome muita CPU
        Runnable cpuIntensiveTask = () -> {
            System.out.println(Thread.currentThread().getName() + " iniciando...");
            long count = 0;
            for (long i = 0; i < 1_000_000_000L; i++) {
                count++;
                // Verifica se foi solicitada interrupção
                if (Thread.interrupted()) {
                    System.out.println(Thread.currentThread().getName() + " foi interrompida!");
                    return; // Termina a thread se foi interrompida
                }
            }
            System.out.println(Thread.currentThread().getName() + " concluída: " + count);
        };

        // Cria duas threads com a mesma tarefa pesada
        Thread thread1 = new Thread(cpuIntensiveTask, "Worker-1");
        Thread thread2 = new Thread(cpuIntensiveTask, "Worker-2");

        System.out.println("Iniciando duas threads de alta carga...");
        thread1.start();
        thread2.start();

        // Deixa elas executarem por 2 segundos
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Interrompendo threads após 2 segundos...");
        thread1.interrupt(); // Envia sinal de interrupção
        thread2.interrupt();

        // Aguarda um tempo para as threads terminarem
        try {
            thread1.join(500);
            thread2.join(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Exemplo de preempção concluído.");
    }

    // Tarefa de contagem para demonstrar prioridades
    private static class CounterTask implements Runnable {
        private final String name;
        private final int iterations;

        public CounterTask(String name, int iterations) {
            this.name = name;
            this.iterations = iterations;
        }

        @Override
        public void run() {
            System.out.println(name + " thread iniciando...");
            int count = 0;
            for (int i = 0; i < iterations; i++) {
                count++;
                // Libera voluntariamente o processador
                Thread.yield();
            }
            System.out.println(name + " thread concluída. Contagem: " + count);
        }
    }
}