package com.example;

import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CallableAndExecutorExample implements Example {

    @Override
    public void example() {
        System.out.println("\n=== Callable and ExecutorService  ===\n");

        // 1. Exemplo básico de Callable com ExecutorService
        basicCallableExample();

        // 2. Execução de múltiplas Callables e processamento dos Futures
        multipleCallablesExample();

        // 3. Exemplo com timeout
        callableWithTimeoutExample();

        // 4. Exemplo com invokeAll()
        invokeAllExample();

        // 5. Exemplo com invokeAny()
        invokeAnyExample();
    }

    private void basicCallableExample() {
        System.out.println("\n1. Basic Callable Example:");

        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Criando uma tarefa Callable
        Callable<String> task = () -> {
            Thread.sleep(500); // Simula processamento
            return "Resultado da tarefa Callable";
        };

        Future<String> future = executor.submit(task);

        try {
            System.out.println("Aguardando resultado...");
            String result = future.get(); // Bloqueia até obter o resultado
            System.out.println("Resultado: " + result);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    private void multipleCallablesExample() {
        System.out.println("\n2. Multiple Callables Example:");

        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<Integer>> futures = new ArrayList<>();

        // Criando 5 tarefas Callable
        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            Callable<Integer> task = () -> {
                int sleepTime = new Random().nextInt(1000);
                Thread.sleep(sleepTime);
                return taskId * 100;
            };
            futures.add(executor.submit(task));
        }

        // Processando os resultados conforme ficam disponíveis
        for (Future<Integer> future : futures) {
            try {
                Integer result = future.get();
                System.out.println("Resultado recebido: " + result);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
    }

    private void callableWithTimeoutExample() {
        System.out.println("\n3. Callable with Timeout Example:");

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Callable<String> longRunningTask = () -> {
            Thread.sleep(2000); // Tarefa demorada
            return "Resultado da tarefa longa";
        };

        Future<String> future = executor.submit(longRunningTask);

        try {
            System.out.println("Tentando obter resultado com timeout de 1 segundo...");
            String result = future.get(1, TimeUnit.SECONDS);
            System.out.println("Resultado: " + result);
        } catch (TimeoutException e) {
            System.err.println("Timeout: A tarefa não foi concluída a tempo");
            future.cancel(true); // Cancela a tarefa se ainda estiver rodando
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    private void invokeAllExample() {
        System.out.println("\n4. invokeAll() Example:");

        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Callable<String>> tasks = new ArrayList<>();

        // Criando várias tarefas
        for (int i = 1; i <= 4; i++) {
            final int taskId = i;
            tasks.add(() -> {
                int sleepTime = new Random().nextInt(1000);
                Thread.sleep(sleepTime);
                return "Tarefa " + taskId + " concluída em " + sleepTime + "ms";
            });
        }

        try {
            System.out.println("Executando todas as tarefas...");
            List<Future<String>> futures = executor.invokeAll(tasks);

            for (Future<String> future : futures) {
                System.out.println(future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    private void invokeAnyExample() {
        System.out.println("\n5. invokeAny() Example:");

        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Callable<String>> tasks = new ArrayList<>();

        // Criando várias tarefas com diferentes tempos de execução
        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            tasks.add(() -> {
                int sleepTime = 500 + new Random().nextInt(1000);
                Thread.sleep(sleepTime);
                return "Tarefa " + taskId + " (tempo: " + sleepTime + "ms)";
            });
        }

        try {
            System.out.println("Executando tarefas e retornando a primeira a completar...");
            String firstResult = executor.invokeAny(tasks);
            System.out.println("Primeiro resultado: " + firstResult);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }
}