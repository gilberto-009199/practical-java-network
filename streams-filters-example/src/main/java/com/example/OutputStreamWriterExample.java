package com.example;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class OutputStreamWriterExample implements Example {

	
	
    public void example() {
        System.out.println("\n=== OutputStreamWriter Examples ===\n");
        
        // 1. Escrita básica com encoding padrão
        basicWriting();
        
        // 2. Escrita com UTF-8 explícito
        writingWithEncoding();
        
        // 3. Gravação de arquivo com buffer
        bufferedFileWriting();
        
        // 4. Escrita em rede simulada
        networkWriting();
        
    }
    
    private void basicWriting() {
        System.out.println("1. Escrita básica (encoding padrão):");
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(baos)) {
            
            System.out.println("Encoding usado: " + writer.getEncoding());
            
            writer.write("Texto simples com acentuação: áéíóú\n");
            writer.write("Data atual: " + LocalDateTime.now() + "\n");
            writer.flush();
            
            System.out.println("Bytes escritos: " + Arrays.toString(baos.toByteArray()));
            System.out.println("Conteúdo: " + baos.toString());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void writingWithEncoding() {
        System.out.println("\n2. Escrita com UTF-8 explícito:");
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
            
            writer.write("Texto com caracteres especiais:\n");
            writer.write("Emoji: 😊\n");
            writer.write("Símbolos: ©®™\n");
            writer.write("Japonês: 日本語\n");
            writer.flush();
            
            System.out.println("Tamanho: " + baos.size() + " bytes");
            System.out.println("Hex: " + bytesToHex(baos.toByteArray()));
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void bufferedFileWriting() {
        System.out.println("\n3. Gravação de arquivo com buffer:");
        
        File file = new File("exemplo_saida.example.txt");
        
        try (FileOutputStream fos = new FileOutputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             OutputStreamWriter writer = new OutputStreamWriter(bos, StandardCharsets.UTF_8)) {
            
            List<String> linhas = Arrays.asList(
                "Primeira linha",
                "Segunda linha com acentos: çãõ",
                "Terceira linha com números: 123456",
                "Quarta linha com símbolos: ©®"
            );
            
            for (String linha : linhas) {
                writer.write(linha + "\n");
            }
            
            System.out.println("Arquivo escrito em: " + file.getAbsolutePath());
            System.out.println("Tamanho do arquivo: " + file.length() + " bytes");
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // file.delete(); // Descomente para limpar após teste
        }
    }
    
    private void networkWriting() {
        System.out.println("\n4. Escrita em rede simulada:");
        
        try (PipedOutputStream pos = new PipedOutputStream();
             PipedInputStream pis = new PipedInputStream(pos);
             OutputStreamWriter writer = new OutputStreamWriter(pos, StandardCharsets.UTF_8)) {
            
            // Thread simulando o lado receptor
            new Thread(() -> {
                try (InputStreamReader reader = new InputStreamReader(pis)) {
                    System.out.println("Conteúdo recebido:");
                    char[] buffer = new char[1024];
                    int charsRead;
                    while ((charsRead = reader.read(buffer)) != -1) {
                        System.out.print(new String(buffer, 0, charsRead));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            
            // Escrita no OutputStreamWriter
            writer.write("Dados enviados via rede simulada\n");
            writer.write("Timestamp: " + System.currentTimeMillis() + "\n");
            writer.write("Finalizando transmissão...\n");
            writer.flush();
            
            Thread.sleep(500); // Espera a thread terminar
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    
    // Método auxiliar para exibir bytes em hexadecimal
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x ", b));
        }
        return sb.toString().trim();
    }
    
    public static void main(String[] args) {
        new OutputStreamWriterExample().example();
    }
}