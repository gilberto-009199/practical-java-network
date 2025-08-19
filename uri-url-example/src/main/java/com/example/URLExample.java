package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class URLExample implements Example {

    @Override
    public void example() {
        try {

            System.out.println("\n=== URL Exemplos ===\n");

            // 1. Criando URL
            createURL();

            // 2. pegando partes da URL
            getURLComponents();

            // 3. pegando dados da URL
            fetchURLData();

        } catch (MalformedURLException e) {
            System.err.println("URL mal formada: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erro de I/O: " + e.getMessage());
        }
    }

    // 1. Criando URL
    private void createURL() throws MalformedURLException {
        URL url = new URL("https://www.example.com:443/path/to/resource?query=param#fragment");
        System.out.println("\n1. URL criada: " + url);
    }

    // 2. Pegando partes da URL
    private void getURLComponents() throws MalformedURLException {
        URL url = new URL("https://user:pass@www.example.com:443/path/to/resource?query=param#fragment");

        System.out.println("\n2. Partes da URL:");
        System.out.println("Protocolo: " + url.getProtocol());
        System.out.println("Autoridade: " + url.getAuthority());
        System.out.println("Usuário: " + url.getUserInfo());
        System.out.println("Host: " + url.getHost());
        System.out.println("Porta: " + url.getPort());
        System.out.println("Porta padrão: " + url.getDefaultPort());
        System.out.println("Caminho: " + url.getPath());
        System.out.println("Query: " + url.getQuery());
        System.out.println("Fragmento: " + url.getRef());
        System.out.println("Arquivo: " + url.getFile());
    }

    // 3. Obtendo dados da URL
    private void fetchURLData() throws IOException {
        URL url = new URL("https://www.example.com");

        System.out.println("\n3. Dados da URL:");

        // Abrindo conexão básica
        URLConnection connection = url.openConnection();
        System.out.println("Tipo de conteúdo: " + connection.getContentType());
        System.out.println("Tamanho do conteúdo: " + connection.getContentLength());
        System.out.println("Última modificação: " + connection.getLastModified());

        // Lendo o conteúdo (exemplo com texto)
        System.out.println("\nConteúdo (exemplo):");
        try (InputStream is = url.openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null && lineCount < 5) {
                System.out.println(line);
                lineCount++;
            }
            if (line != null) {
                System.out.println("... (conteúdo truncado para demonstração)");
            }
        }
    }
}