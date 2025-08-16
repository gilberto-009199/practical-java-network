package com.example;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class InputStreamExample implements Example {

    public void example() {
        System.out.println("\n=== InputStream Example ===\n");
        
        // 1. Leitura de byte array
        readByteArray();
        
        // 2. Leitura de arquivo nos resources
        readResourceFile();
        
        // 3. Leitura byte a byte
        readByteByByte();
        
        // 4. Leitura em chunks (pedaços)
        readInChunks();
    }
    
    private void readByteArray() {
        System.out.println("1. Lendo de um byte array:");
        String text = "Dados em memória, lendo dados com palavras com acentuação: aâeêií";
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        
        try (InputStream stream = new ByteArrayInputStream(bytes)) {
            int data;
            while ((data = stream.read()) != -1) {
                System.out.write(data);
            }
            System.out.println("\n");
        } catch (IOException e) {
            System.err.println("Erro na leitura: " + e.getMessage());
        }
    }
    
    private void readResourceFile() {
        System.out.println("2. Lendo arquivo dos resources:");
        
        try(
        	InputStream stream = getClass()
        							.getClassLoader()
        							.getResourceAsStream("example.txt")
        ) {
            
            if (stream == null) {
                System.out.println("Arquivo não encontrado nos resources");
                return;
            }
            
            byte[] buffer = new byte[1024];
            int bytesRead;
            StringBuilder content = new StringBuilder();
            
            while ((bytesRead = stream.read(buffer)) != -1) {
                content.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
            }
            
            System.out.println("Conteúdo do arquivo:");
            System.out.println(content.toString());
            System.out.println();
        } catch (IOException e) {
            System.err.println("Erro na leitura do arquivo: " + e.getMessage());
        }
    }
    
    private void readByteByByte() {
        System.out.println("3. Lendo byte a byte:");
                
        
        try(
            	InputStream stream = getClass()
            							.getClassLoader()
            							.getResourceAsStream("example.txt")
        ) {
            System.out.println("Bytes disponíveis: " + stream.available());
            
            System.out.println("Lendo bytes:");
            while (stream.available() > 0) {
                System.out.println("Byte lido: " + stream.read() );
            }
            System.out.println();
        } catch (IOException e) {
            System.err.println("Erro na leitura: " + e.getMessage());
        }
    }
    
    private void readInChunks() {
        System.out.println("4. Lendo em chunks de 5 bytes:");
        String longText = "Esta demonstração mostra a leitura em pedaços";
        
        try(
            	InputStream stream = getClass()
            							.getClassLoader()
            							.getResourceAsStream("example.txt")
        ) {
            byte[] buffer = new byte[5];
            int bytesRead;
            int chunkCount = 0;
            
            while ((bytesRead = stream.read(buffer)) != -1) {
                chunkCount++;
                String chunk = new String(buffer, 0, bytesRead);
                System.out.printf("Chunk %d: %s%n", chunkCount, chunk);
            }
        } catch (IOException e) {
            System.err.println("Erro na leitura: " + e.getMessage());
        }
    }
    
    
    private void markAndResetExample() {
        System.out.println("5. Exemplo com mark() e reset():");
        
        try(
            	InputStream stream = getClass()
            							.getClassLoader()
            							.getResourceAsStream("example.txt")
        ) {
            // Lê e imprime os primeiros 5 bytes
            System.out.println("Lendo os primeiros 5 bytes:");
            for (int i = 0; i < 5; i++) {
                System.out.write(stream.read());
            }
            
            // Marca a posição atual (limitando o buffer para 10 bytes)
            System.out.println("\n\nMarcando posição...");
            stream.mark(10);
            
            // Lê os próximos 5 bytes
            System.out.println("Lendo próximos 5 bytes:");
            for (int i = 0; i < 5; i++) {
                System.out.write(stream.read());
            }
            
            // Reset para a posição marcada
            System.out.println("\n\nResetando para a marca...");
            stream.reset();
            
            // Lê novamente a partir da posição marcada
            System.out.println("Lendo novamente após reset:");
            for (int i = 0; i < 5; i++) {
                System.out.write(stream.read());
            }
            
            System.out.println("\n");
        } catch (IOException e) {
            System.err.println("Erro na leitura: " + e.getMessage());
        }
    }
    
    private void skipExample() {
        System.out.println("6. Exemplo com skip():");
        
        try(
            InputStream stream = getClass()
            							.getClassLoader()
            							.getResourceAsStream("example.txt")
         ) {
            
            // Lê os primeiros 5 bytes
            System.out.print("5 primeiros bytes: ");
            for (int i = 0; i < 5; i++) {
                System.out.write(stream.read());
            }
            
            // Pula os próximos 10 bytes
            long skipped = stream.skip(10);
            System.out.println("\nBytes pulados: " + skipped);
            
            // Lê o restante
            System.out.print("Restante após skip: ");
            int data;
            while ((data = stream.read()) != -1) {
                System.out.write(data);
            }
            
            System.out.println("\n");
        } catch (IOException e) {
            System.err.println("Erro na leitura: " + e.getMessage());
        }
    }
    
}