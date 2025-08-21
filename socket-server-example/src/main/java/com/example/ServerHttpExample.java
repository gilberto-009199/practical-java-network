package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ServerHttpExample implements Example {

    private final int PORT = 8080;

    @Override
    public void example() {
        System.out.println("== Http server run in port: " + PORT + " ===");

        var thread = new Thread(() -> {
            try {
                var serverSocket = new ServerSocket(PORT);
                System.out.println("access: http://localhost:" + PORT);
                System.out.println("Server is running. Press Ctrl+C to stop.");

                while (!serverSocket.isClosed()) {
                    var client = serverSocket.accept();
                    handlerHttp(client);
                }

            } catch (Exception e) {
                System.err.println("Server error: " + e.getMessage());
            }
        });

        thread.start();
    }

    private void handlerHttp(Socket client) {
        String clientAddress = client.getInetAddress().getHostAddress();
        System.out.println("\n📨 Nova conexão de: " + clientAddress);

        try (client;
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             OutputStream out = client.getOutputStream()) {


            Thread.sleep(4000);

            // Ler request line
            String requestLine = in.readLine();
            if (requestLine == null) {
                sendErrorResponse(out, 400, "Bad Request - No request line");
                return;
            }

            System.out.println("📍 Request: " + requestLine + " (from: " + clientAddress + ")");

            // Ler todos os headers
            Map<String, String> headers = new HashMap<>();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                int colonIndex = line.indexOf(':');
                if (colonIndex > 0) {
                    String key = line.substring(0, colonIndex).trim();
                    String value = line.substring(colonIndex + 1).trim();
                    headers.put(key, value);
                }
            }

            // Parse do request
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 3) {
                sendErrorResponse(out, 400, "Bad Request - Invalid request line");
                return;
            }

            String method = requestParts[0];
            String path = requestParts[1];
            String httpVersion = requestParts[2];

            // Exibir informações no console
            System.out.println("⚡ Método: " + method);
            System.out.println("🌐 Path: " + path);
            System.out.println("🔗 HTTP Version: " + httpVersion);
            System.out.println("📋 Headers recebidos: " + headers.size());




            // Processar request baseado no método
            switch (method) {
                case "GET":
                    handleGetRequest(out, path, headers, clientAddress);
                    break;
                case "POST":
                    handlePostRequest(out, path, headers, clientAddress);
                    break;
                case "HEAD":
                    handleHeadRequest(out, path, headers, clientAddress);
                    break;
                default:
                    handleOtherRequest(out, method, path, headers, clientAddress);
            }

        } catch (IOException e) {
            System.err.println("❌ Erro ao processar request: " + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleGetRequest(OutputStream out, String path, Map<String, String> headers, String clientAddress) throws IOException {
        System.out.println("✅ GET request para: " + path);

        String responseBody = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset='UTF-8'>
                <title>GET Request</title>
            </head>
            <body>
                <div class='container'>
                    <h1>🌐 GET Request Recebida</h1>
                    <div class='info'><strong>📡 Client:</strong> %s</div>
                    <div class='info'><strong>🛣️ Path:</strong> %s</div>
                    <div class='info'><strong>📊 Headers:</strong> %d headers recebidos</div>
                    <div class='info'><strong>⏰ Status:</strong> ✅ Sucesso</div>
                </div>
            </body>
            </html>
            """.formatted(clientAddress, path, headers.size());



        sendResponse(out, 200, "OK", responseBody);
    }

    private void handlePostRequest(OutputStream out, String path, Map<String, String> headers, String clientAddress) throws IOException {
        System.out.println("✅ POST request para: " + path);

        String responseBody = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset='UTF-8'>
                <title>POST Request</title>
            </head>
            <body>
                <div class='container'>
                    <h1>📨 POST Request Recebida</h1>
                    <div class='info'><strong>📡 Client:</strong> %s</div>
                    <div class='info'><strong>🛣️ Path:</strong> %s</div>
                    <div class='info'><strong>📊 Headers:</strong> %d headers recebidos</div>
                    <div class='info'><strong>⏰ Status:</strong> ✅ Dados recebidos com sucesso</div>
                </div>
            </body>
            </html>
            """.formatted(clientAddress, path, headers.size());

        sendResponse(out, 200, "OK", responseBody);
    }

    private void handleHeadRequest(OutputStream out, String path, Map<String, String> headers, String clientAddress) throws IOException {
        System.out.println("✅ HEAD request para: " + path);

        // Para HEAD, só enviar headers sem body
        String response = "HTTP/1.0 200 OK\r\n" +
                "Content-Type: text/html; charset=utf-8\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "Server: SimpleJavaHTTPServer/1.0\r\n" +
                "\r\n";

        out.write(response.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private void handleOtherRequest(OutputStream out, String method, String path, Map<String, String> headers, String clientAddress) throws IOException {
        System.out.println("⚠️  Método não suportado: " + method);

        String responseBody = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset='UTF-8'>
                <title>Método não suportado</title>
            </head>
            <body>
                <div class='container'>
                    <h1>⚠️ Método não suportado</h1>
                    <div class='info'><strong>Método:</strong> %s</div>
                    <div class='info'><strong>Path:</strong> %s</div>
                    <div class='info'><strong>Client:</strong> %s</div>
                    <div class='info'><strong>Status:</strong> ❌ Método HTTP não implementado</div>
                </div>
            </body>
            </html>
            """.formatted(method, path, clientAddress);

        sendResponse(out, 405, "Method Not Allowed", responseBody);
    }

    private void sendErrorResponse(OutputStream out, int statusCode, String message) throws IOException {
        String responseBody = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset='UTF-8'>
                <title>Erro</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; background: #ffe6e6; }
                    .container { background: white; padding: 20px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    h1 { color: #c0392b; }
                </style>
            </head>
            <body>
                <div class='container'>
                    <h1>❌ Erro %d</h1>
                    <p>%s</p>
                </div>
            </body>
            </html>
            """.formatted(statusCode, message);

        sendResponse(out, statusCode, message, responseBody);
    }

    private void sendResponse(
            OutputStream out,
            int statusCode,
            String statusMessage,
            String body
    ) throws IOException {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

        String response = "HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n" +
                "Content-Type: text/html; charset=utf-8\r\n" +
                "Content-Length: " + bodyBytes.length + "\r\n" +
                "Connection: close\r\n" +
                "Server: SimpleJavaHTTPServer/1.0\r\n" +
                "\r\n";

        out.write(response.getBytes(StandardCharsets.UTF_8));
        out.write(bodyBytes);
        out.flush();

        System.out.println("✅ Resposta enviada: " + statusCode + " " + statusMessage);
    }

}