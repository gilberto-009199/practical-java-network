package com.example;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Arrays;

public class PrintWriterExample implements Example {

    public void example() {
        System.out.println("\n=== Exemplos de PrintWriter ===");

        // 1. Escrita básica em arquivo
        basicFileWriting();

        // 2. Formatação de texto
        formattedWriting();

        // 3. Escrita em console com auto-flush
        consoleWriting();

        // 4. Escrita combinada com OutputStream
        combinedStreamWriting();

    }

    private void basicFileWriting() {
        System.out.println("\n1. Escrita básica em arquivo:");

        File file = new File("printwriters.exemplo.txt");
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("Linha 1: Texto simples");
            writer.println("Linha 2: " + LocalDateTime.now());
            writer.printf("Linha 3: Formato numérico: %.2f%n", 123.456);

            System.out.println("Arquivo escrito em: " + file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            System.err.println("Erro ao criar arquivo: " + e.getMessage());
        }
    }
    private void formattedWriting() {
        System.out.println("\n2. Formatação de texto:");
        StringWriter stringWriter = new StringWriter();
        
        try (PrintWriter writer = new PrintWriter(stringWriter)) {
        	
            // Formatação de diversos tipos de dados
            writer.printf("Data: %tF %<tT%n", LocalDateTime.now());
            writer.printf("Número: %+,d%n", 1000000);
            writer.printf("Hexadecimal: %x%n", 255);
            writer.printf("Notação científica: %e%n", 1234567.89);
            writer.printf("Alinhamento: |%-20s|%20s|%n", "Esquerda", "Direita");
            
            System.out.print(stringWriter.toString());
        }
    }

    private void consoleWriting() {
        System.out.println("\n3. Escrita no console com auto-flush:");

        // PrintWriter com auto-flush (segundo parâmetro como true)
        PrintWriter writer = new PrintWriter(System.out, true);
        
        writer.println("Mensagem com auto-flush");
        writer.printf("Número aleatório: %f%n", Math.random());
        writer.println("Esta linha também é exibida imediatamente");
    }

    private void combinedStreamWriting() {
        System.out.println("\n4. Escrita combinada com OutputStream:");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(baos)) {
            writer.println("Dados escritos no OutputStream");
            writer.printf("Valor formatado: %.3f%n", Math.PI);
            writer.flush();

            System.out.println("Conteúdo do OutputStream:");
            System.out.println(baos.toString());
        }
    }

    public static void main(String[] args) {
        new PrintWriterExample().example();
    }
}