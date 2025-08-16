package com.example;

import java.io.*;

public class BufferStreamExample  implements Example {

    private static final int ITERATIONS = 128;
    private static final String TEST_FILE = "test_data.example.txt";
    private static final String LARGE_TEXT = generateLargeText();

    // a cpu é bem mais rapida que o sistema de arquivos
    public void example() {
        System.out.println("\n=== Comparação de Eficiência: Com vs Sem Buffer ===\n");

        // Pré-requisito: Criar arquivo de teste
        createTestFile();

        // 1. Comparação de escrita
        compareWritePerformance();

        // 2. Comparação de leitura
        compareReadPerformance();

        // 3. Comparação de cópia
        compareCopyPerformance();

        // Limpeza
        new File(TEST_FILE).delete();
    }

    private static String generateLargeText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("Linha ").append(i).append(": ")
              .append("Este é um texto de exemplo para teste de performance ")
              .append(System.currentTimeMillis()).append("\n");
        }
        return sb.toString();
    }

    private void createTestFile() {
        try (FileWriter writer = new FileWriter(TEST_FILE)) {
            writer.write(LARGE_TEXT);
        } catch (IOException e) {
            System.err.println("Erro ao criar arquivo de teste: " + e.getMessage());
        }
    }

    private void compareWritePerformance() {
        System.out.println("1. Teste de Escrita:");

        // Sem buffer
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            try (FileWriter writer = new FileWriter("no_buffer.example.txt")) {
                writer.write(LARGE_TEXT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long noBufferTime = System.nanoTime() - start;

        // Com buffer
        start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter("with_buffer.example.txt"))) {
                writer.write(LARGE_TEXT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long bufferTime = System.nanoTime() - start;

        printResults(noBufferTime, bufferTime);
        new File("no_buffer.example.txt").delete();
        new File("with_buffer.example.txt").delete();
    }

    private void compareReadPerformance() {
        System.out.println("\n2. Teste de Leitura:");

        // Sem buffer
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            try (FileReader reader = new FileReader(TEST_FILE)) {
                while (reader.read() != -1) {
                    // Simples leitura byte a byte
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long noBufferTime = System.nanoTime() - start;

        // Com buffer
        start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            try (BufferedReader reader = new BufferedReader(
                    new FileReader(TEST_FILE))) {
                while (reader.read() != -1) {
                    // Leitura com buffer
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long bufferTime = System.nanoTime() - start;

        printResults(noBufferTime, bufferTime);
    }

    private void compareCopyPerformance() {
        System.out.println("\n3. Teste de Cópia:");

        // Sem buffer
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            try (FileInputStream in = new FileInputStream(TEST_FILE);
                 FileOutputStream out = new FileOutputStream("no_buffer_copy.example.txt")) {
                int b;
                while ((b = in.read()) != -1) {
                    out.write(b);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long noBufferTime = System.nanoTime() - start;

        // Com buffer
        start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            try (BufferedInputStream in = new BufferedInputStream(
                    new FileInputStream(TEST_FILE));
                 BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream("with_buffer_copy.example.txt"))) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long bufferTime = System.nanoTime() - start;

        printResults(noBufferTime, bufferTime);
        new File("no_buffer_copy.example.txt").delete();
        new File("with_buffer_copy.example.txt").delete();
    }

    private void printResults(long noBufferTime, long bufferTime) {
        System.out.printf("Sem buffer: %,d ns%n", noBufferTime);
        System.out.printf("Com buffer: %,d ns%n", bufferTime);
        System.out.printf("Diferença: %d%% mais rápido%n%n", 
            ((noBufferTime - bufferTime) * 100) / noBufferTime);
    }

    public static void main(String[] args) {
        new BufferStreamExample().example();
    }
}
