package com.example;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class InputStreamReaderExample implements Example {

    public void example() {
        System.out.println("\n=== InputStreamReader Examples ===\n");
        
        // 1. Leitura básica com encoding padrão
        basicReading();
        
        // 2. Leitura com encoding específico (UTF-8)
        readingWithEncoding();
        
        // 3. Conversão de InputStream para Reader
        streamToReaderConversion();
        
        // 4. Leitura de arquivo com buffer
        bufferedFileReading();
    }
    
    private void basicReading() {
        System.out.println("1. Leitura básica (encoding padrão):");
        
        String text = "Texto simples com acentuação: áéíóú";
        byte[] bytes = text.getBytes(); // Usa o encoding padrão do sistema
        
        try (InputStreamReader reader = new InputStreamReader(
                new ByteArrayInputStream(bytes))) {
            
            System.out.println("Encoding usado: " + reader.getEncoding());
            
            StringBuilder content = new StringBuilder();
            int character;
            while ((character = reader.read()) != -1) {
                content.append((char) character);
            }
            
            System.out.println("Conteúdo lido: " + content.toString());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void readingWithEncoding() {
        System.out.println("\n2. Leitura com UTF-8 explícito:");
        
        String text = "Texto com çãõ e emoji 😊";
        byte[] utf8Bytes = text.getBytes(StandardCharsets.UTF_8);
        
        try (InputStreamReader reader = new InputStreamReader(
                new ByteArrayInputStream(utf8Bytes), 
                StandardCharsets.UTF_8)) {
            
            char[] buffer = new char[1024];
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                System.out.print(new String(buffer, 0, charsRead));
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void streamToReaderConversion() {
        System.out.println("\n\n3. Conversão de InputStream para Reader:");
        
        try {
            // Simulando um InputStream de rede/sistema
            InputStream rawStream = new ByteArrayInputStream(
                "Dados de rede simulados".getBytes());
            
            // Convertendo para Reader com encoding específico
            Reader reader = new InputStreamReader(rawStream, StandardCharsets.ISO_8859_1);
            
            // Usando o Reader
            System.out.println("Primeiros 10 caracteres:");
            for (int i = 0; i < 10; i++) {
                System.out.print((char) reader.read());
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void bufferedFileReading() {
        System.out.println("\n\n4. Leitura de arquivo com buffer:");
        
        // Criando um arquivo temporário para o exemplo
        File tempFile = createTempFile();
        
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(tempFile), 
                StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            
            System.out.println("Conteúdo do arquivo:");
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            tempFile.delete();
        }
    }

    private File createTempFile() {
        try {
            File tempFile = File.createTempFile("example", ".txt");
            try (Writer writer = new OutputStreamWriter(
                    new FileOutputStream(tempFile), 
                    StandardCharsets.UTF_8)) {
                writer.write("Conteúdo do arquivo temporário\n");
                writer.write("Segunda linha com acentuação: áéíóú\n");
                writer.write("Terceira linha com caracteres especiais: ©®\n");
            }
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao criar arquivo temporário", e);
        }
    }
    
    
    public static void main(String[] args) {
        new InputStreamReaderExample().example();
    }
}