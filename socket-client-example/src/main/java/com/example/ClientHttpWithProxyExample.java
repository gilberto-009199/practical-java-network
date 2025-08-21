package com.example;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientHttpWithProxyExample implements Example {

    // https://free-proxy-list.net/en/
    // 27.79.246.238	16000	VN	Vietnam
    String proxyHost = "27.79.246.238";
    int proxyPort = 16000;

    @Override
    public void example() {
        System.out.println("\n=== Cliente HTTP com Proxy Exemplos ===\n");

        try {
            // 1. connect proxy
            testProxyConnection();

            // 2. send request http with proxy
            sendHttpRequestThroughProxy();

        } catch (IOException e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void testProxyConnection() throws IOException {
        System.out.println("1. Testando conexão com proxy...");
        System.out.println("Proxy: " + proxyHost + ":" + proxyPort);

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(proxyHost, proxyPort), 10000);
            System.out.println("✓ Conexão com proxy estabelecida com sucesso!");
            System.out.println("Timeout: " + socket.getSoTimeout() + "ms");
        } catch (ConnectException e) {
            System.err.println("✗ Não foi possível conectar ao proxy: " + e.getMessage());
            throw e;
        } catch (SocketTimeoutException e) {
            System.err.println("✗ Timeout ao conectar com proxy");
            throw e;
        }
    }

    private void sendHttpRequestThroughProxy() throws IOException {
        System.out.println("\n2. Enviando requisição HTTP através do proxy...");

        Scanner scanner = new Scanner(System.in);
        System.out.print("URL para acessar (ex: http://httpbin.org/ip): ");
        String targetUrl = scanner.nextLine();

        if (targetUrl.isEmpty()) {
            targetUrl = "http://httpbin.org/ip"; // URL padrão
        }

        System.out.println("Target URL: " + targetUrl);
        System.out.println("Usando proxy: " + proxyHost + ":" + proxyPort);

        try (Socket proxySocket = new Socket(proxyHost, proxyPort);
             PrintWriter out = new PrintWriter(proxySocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(proxySocket.getInputStream()))) {

            proxySocket.setSoTimeout(30000); // 30 segundos timeout

            URL url = new URL(targetUrl);
            String host = url.getHost();
            String path = url.getPath().isEmpty() ? "/" : url.getPath();
            String query = url.getQuery() != null ? "?" + url.getQuery() : "";

            System.out.println("Construindo requisição HTTP...");

            StringBuilder request = new StringBuilder();
            request.append("CONNECT ").append(host).append(":80 HTTP/1.1\r\n");
            request.append("Host: ").append(host).append(":80\r\n");
            request.append("User-Agent: Java-Proxy-Test/1.0\r\n");
            request.append("Proxy-Connection: keep-alive\r\n");
            request.append("\r\n");

            System.out.println("Enviando requisição CONNECT...");
            out.print(request.toString());
            out.flush();

            // Ler resposta do proxy
            StringBuilder proxyResponse = new StringBuilder();
            String line;
            int lineCount = 0;

            while ((line = in.readLine()) != null && lineCount < 20) {
                proxyResponse.append(line).append("\n");
                System.out.println("PROXY: " + line);

                if (line.isEmpty() || line.startsWith("HTTP/1.1 200") || line.startsWith("HTTP/1.0 200")) {
                    break;
                }
                lineCount++;
            }

            // Verificar se o proxy aceitou a conexão CONNECT
            if (proxyResponse.toString().contains("200 Connection established") ||
                    proxyResponse.toString().contains("200 OK")) {

                System.out.println("✓ Conexão CONNECT estabelecida!");

                // Agora enviar a requisição HTTP real
                StringBuilder httpRequest = new StringBuilder();
                httpRequest.append("GET ").append(path).append(query).append(" HTTP/1.1\r\n");
                httpRequest.append("Host: ").append(host).append("\r\n");
                httpRequest.append("User-Agent: Java-Proxy-Client/1.0\r\n");
                httpRequest.append("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n");
                httpRequest.append("Connection: close\r\n");
                httpRequest.append("\r\n");

                out.print(httpRequest.toString());
                out.flush();
                System.out.println("Requisição GET enviada...");

            } else {
                // Se CONNECT falhar, tentar método HTTP 1.0
                System.out.println("Proxy não suporta CONNECT, tentando método transparente...");

                // Fechar e reconectar
                proxySocket.close();
                sendTransparentProxyRequest(targetUrl);
                return;
            }

            // Ler resposta completa
            System.out.println("\n=== RESPOSTA DO SERVIDOR ===");

            boolean inHeaders = true;
            while ((line = in.readLine()) != null) {
                if (line.isEmpty()) {
                    inHeaders = false;
                    System.out.println("--- CORPO ---");
                    continue;
                }

                if (inHeaders) {
                    System.out.println("HEADER: " + line);
                } else {
                    System.out.println("BODY: " + line);
                }
            }

        } catch (SocketTimeoutException e) {
            System.err.println("✗ Timeout na comunicação com proxy");
        } catch (ConnectException e) {
            System.err.println("✗ Conexão recusada pelo proxy");
        }

        scanner.close();
    }

    private void sendTransparentProxyRequest(String targetUrl) throws IOException {
        System.out.println("Tentando método de proxy transparente...");

        try (Socket proxySocket = new Socket(proxyHost, proxyPort);
             PrintWriter out = new PrintWriter(proxySocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(proxySocket.getInputStream()))) {

            proxySocket.setSoTimeout(30000);

            URL url = new URL(targetUrl);
            String host = url.getHost();
            String path = url.getPath().isEmpty() ? "/" : url.getPath();
            String query = url.getQuery() != null ? "?" + url.getQuery() : "";

            // Requisição HTTP completa para proxy transparente
            StringBuilder request = new StringBuilder();
            request.append("GET http://").append(host).append(path).append(query).append(" HTTP/1.0\r\n");
            request.append("Host: ").append(host).append("\r\n");
            request.append("User-Agent: Java-Proxy-Client/1.0\r\n");
            request.append("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n");
            request.append("Connection: close\r\n");
            request.append("\r\n");

            out.print(request.toString());
            out.flush();

            System.out.println("\n=== RESPOSTA (Método Transparente) ===");

            String line;
            boolean inHeaders = true;
            while ((line = in.readLine()) != null) {
                if (line.isEmpty()) {
                    inHeaders = false;
                    System.out.println("--- CORPO ---");
                    continue;
                }

                if (inHeaders) {
                    System.out.println("HEADER: " + line);
                } else {
                    System.out.println("BODY: " + line);
                }
            }
        }
    }

}