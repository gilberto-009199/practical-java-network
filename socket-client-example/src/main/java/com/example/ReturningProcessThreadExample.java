package com.example;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ReturningProcessThreadExample implements Example {

    @Override
    public void example() {
        System.out.println("\n=== Returning info process ===\n");

        // 1. Race Conditions
        raceConditionExample();

        // 2. Polling
        pollingExample();

        // 3. Callbacks
        callbackExample();

        // 4. Futures, Callables, and Executors
        futureCallableExecutorExample();
    }

    // 1. Demonstração de Race Condition
    private void raceConditionExample() {
        System.out.println("\n1. Race Condition Example:");

        class SharedCounter {
            private int count = 0;
            // Versão thread-safe com AtomicInteger
            private AtomicInteger safeCount = new AtomicInteger(0);

            public void increment() {
                count++; // Operação não thread-safe
                safeCount.incrementAndGet(); // Operação thread-safe
            }

            public void printResults() {
                System.out.println("Valor inseguro (race condition): " + count);
                System.out.println("Valor seguro (AtomicInteger): " + safeCount.get());
            }
        }

        SharedCounter counter = new SharedCounter();

        // Criando 10 threads que incrementam o contador
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter.increment();
                }
            });
            threads[i].start();
        }

        // Esperando todas as threads terminarem
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        counter.printResults();
    }

    // 2. Demonstração de Polling
    private void pollingExample() {
        System.out.println("\n2. Polling Example:");

        class TaskRunner {
            private volatile boolean taskCompleted = false;
            private String result;

            public void executeTask() {
                new Thread(() -> {
                    try {
                        // Simula trabalho demorado
                        Thread.sleep(2000);
                        result = "Resultado do processamento";
                        taskCompleted = true;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }

            public String getResult() {
                // Polling - verificando periodicamente se a tarefa foi completada
                while (!taskCompleted) {
                    try {
                        Thread.sleep(100); // Verifica a cada 100ms
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return "Interrompido";
                    }
                }
                return result;
            }
        }

        TaskRunner runner = new TaskRunner();
        System.out.println("Iniciando tarefa...");
        runner.executeTask();

        System.out.println("Fazendo polling pelo resultado...");
        String result = runner.getResult();
        System.out.println("Resultado obtido: " + result);
    }

    // 3. Demonstração de Callback
    private void callbackExample() {
        System.out.println("\n3. Callback Example:");

        interface TaskCallback {
            void onComplete(String result);
            void onError(Exception e);
        }

        class AsyncTask {
            public void execute(TaskCallback callback) {
                new Thread(() -> {
                    try {
                        // Simula trabalho demorado
                        Thread.sleep(1500);
                        // Simula resultado
                        String result = "Dados processados via callback";
                        callback.onComplete(result);
                    } catch (Exception e) {
                        callback.onError(e);
                    }
                }).start();
            }
        }

        AsyncTask task = new AsyncTask();
        System.out.println("Iniciando tarefa assíncrona com callback...");

        task.execute(new TaskCallback() {
            @Override
            public void onComplete(String result) {
                System.out.println("Callback recebido: " + result);
            }

            @Override
            public void onError(Exception e) {
                System.out.println("Erro no processamento: " + e.getMessage());
            }
        });

        // Espera um pouco para o callback ser executado
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // 4. Demonstração de Futures, Callables e Executors
    private void futureCallableExecutorExample() {
        System.out.println("\n4. Futures, Callables and Executors Example:");

        // Criando um pool de threads
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Criando uma tarefa Callable que retorna um resultado
        Callable<String> task = () -> {
            // Simula trabalho demorado
            Thread.sleep(1000);
            return "Resultado da tarefa Callable";
        };

        // Submetendo a tarefa para execução
        Future<String> future = executor.submit(task);

        System.out.println("Tarefa submetida. Fazendo outras coisas...");

        try {
            // Obtendo o resultado (bloqueante)
            String result = future.get(2, TimeUnit.SECONDS); // Timeout de 2 segundos
            System.out.println("Resultado obtido: " + result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Tarefa interrompida");
        } catch (ExecutionException e) {
            System.out.println("Erro na execução: " + e.getCause().getMessage());
        } catch (TimeoutException e) {
            System.out.println("Tempo de espera excedido");
        } finally {
            executor.shutdown();
        }
    }
}