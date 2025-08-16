package com.example;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class OutputStreamExample implements Example {

    public void example() {
        System.out.println("\n=== OutputStream Example ===\n");
        
        // 1. Demonstra escrita em memória (ByteArrayOutputStream)
        writeToByteArray();
        
        // 2. Escrita básica em arquivo (FileOutputStream)
        writeToFile();
        
        // 3. Escrita com buffer para melhor performance
        writeWithBufferedStream();
        
        // 4. Escrita de tipos primitivos (DataOutputStream)
        writeWithDataStream();
        
        // 5. Escrita de texto como bytes UTF-8
        writeTextAsBytes();
        
        // 6. Operação de append em arquivo existente
        appendToFile();
    }
    
    private void writeToByteArray() {
        System.out.println("1. Escrevendo em ByteArrayOutputStream:");
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            String text = "Exemplo com acentuação: çãõáéíóú";
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            
            outputStream.write(bytes);
            outputStream.write("\nSegunda linha ©®™\n".getBytes(StandardCharsets.UTF_8));
            
            // Convertendo os bytes de volta para String para exibição
            String writtenContent = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            System.out.println(writtenContent);
        } catch (IOException e) {
            System.err.println("Erro na escrita: " + e.getMessage());
        }
    }
    
    private void writeToFile() {
        System.out.println("\n2. Escrevendo em arquivo (FileOutputStream):");
        File file = new File("output_direct.example.txt");
        
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            String content = "Conteúdo direto no arquivo\n";
            content += "Acentuação: çãõáéíóú\n";
            content += "Símbolos: ©®™ €\n";
            
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            outputStream.write(bytes);
            
            System.out.println("Arquivo escrito em: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Erro ao escrever arquivo: " + e.getMessage());
        }
    }
    
    private void writeWithBufferedStream() {
        System.out.println("\n3. Escrevendo com BufferedOutputStream:");
        File file = new File("buffered_output.example.txt");
        
        try (FileOutputStream fileStream = new FileOutputStream(file);
             BufferedOutputStream bufferedStream = new BufferedOutputStream(fileStream)) {
            
            String header = "== Dados Buffered ==\n";
            bufferedStream.write(header.getBytes(StandardCharsets.UTF_8));
            
            for (int i = 1; i <= 5; i++) {
                String line = "Linha " + i + " - buffer eficiente\n";
                bufferedStream.write(line.getBytes(StandardCharsets.UTF_8));
            }
            
            bufferedStream.flush();
            System.out.println("Arquivo com buffer escrito em: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Erro na escrita com buffer: " + e.getMessage());
        }
    }
    
    private void writeWithDataStream() {
        System.out.println("\n4. Escrevendo dados binários (DataOutputStream):");
        
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DataOutputStream dataStream = new DataOutputStream(byteStream)) {
            
            // Escrevendo diferentes tipos de dados
            dataStream.writeBoolean(true);
            dataStream.writeInt(42);
            dataStream.writeUTF("Texto com UTF-8 e acentuação: áéíóú"); // writeUTF usa UTF-8 modificado
            
            byte[] writtenBytes = byteStream.toByteArray();
            System.out.println("Tamanho dos dados binários: " + writtenBytes.length + " bytes");
            
            // Demonstração de leitura
            try (DataInputStream inputStream = new DataInputStream(
                    new ByteArrayInputStream(writtenBytes))) {
                System.out.println("Lido: " + inputStream.readBoolean());
                System.out.println("Lido: " + inputStream.readInt());
                System.out.println("Lido: " + inputStream.readUTF());
            }
        } catch (IOException e) {
            System.err.println("Erro na escrita de dados: " + e.getMessage());
        }
    }
    
    private void writeTextAsBytes() {
        System.out.println("\n5. Escrevendo texto como bytes (UTF-8):");
        File file = new File("text_bytes.example.txt");
        
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            String[] lines = {
                "Linha 1: Texto simples",
                "Linha 2: Acentuação: çãõáéíóú",
                "Linha 3: Símbolos: ©®™ € ¥",
                "Linha 4: Emojis: 😀🌟🎉"
            };
            
            for (String line : lines) {
                byte[] lineBytes = (line + "\n").getBytes(StandardCharsets.UTF_8);
                outputStream.write(lineBytes);
            }
            
            System.out.println("Texto escrito como bytes em: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Erro ao escrever texto como bytes: " + e.getMessage());
        }
    }
    
    private void appendToFile() {
        System.out.println("\n6. Append em arquivo existente:");
        File file = new File("append_example.example.txt");
        
        // Escreve conteúdo inicial
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            String content = "Conteúdo inicial\n";
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Erro ao criar arquivo: " + e.getMessage());
            return;
        }
        
        // Faz append de novo conteúdo
        try (FileOutputStream outputStream = new FileOutputStream(file, true)) {
            String additionalContent = "Conteúdo adicionado após\n";
            additionalContent += "Mais dados com acentuação: çãõ\n";
            
            outputStream.write(additionalContent.getBytes(StandardCharsets.UTF_8));
            System.out.println("Conteúdo adicionado ao arquivo em: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Erro ao fazer append: " + e.getMessage());
        }
    }

}