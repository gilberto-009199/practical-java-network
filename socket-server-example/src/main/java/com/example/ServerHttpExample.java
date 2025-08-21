package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ServerHttpExample implements Example{

    private final int PORT = 8080;

    @Override
    public void example() {

        System.out.println("== Http server run in port: "+ PORT + " ===");

        var thread = new Thread(() -> {

            try{
                var socket = new ServerSocket(PORT);

                System.out.println("access: http://localhost:"+ PORT);


                do{

                    var client = socket.accept();

                    handlerHttp(client);

                }while(!socket.isClosed() || socket.isBound());


            } catch(Exception e) {
                e.printStackTrace();
            }


        });

        thread.start();

    }

    private void handlerHttp(Socket client) {

        String clientAddress = client.getInetAddress().getHostAddress();
        System.out.println(" Nova conexão de: " + clientAddress);

        try (client;
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             OutputStream out = client.getOutputStream()) {

            // Ler request headers
            String requestLine = in.readLine();
            if (requestLine == null) {
                sendErrorResponse(out, "", null);
                return;
            }

            System.out.println(" Request: " + requestLine + " (from: " + clientAddress + ")");

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
                sendErrorResponse(out, "", headers);
                return;
            }

            String method = requestParts[0];
            String path = requestParts[1];
            String httpVersion = requestParts[2];

            // Processar request baseado no método
            switch (method) {
                case "GET":
                    handleGetRequest(out, path, headers);
                    break;
                case "POST":
                    handlePostRequest(out, in, path, headers);
                    break;
                case "HEAD":
                    handleHeadRequest(out, path, headers);
                    break;
                default:
                    sendErrorResponse(out, path, headers);
            }

        } catch (IOException e) {
            System.err.println("Erro ao processar request: " + e.getMessage());
        }
    }

    private void handleGetRequest(OutputStream out, String path, Map<String, String> headers) throws IOException {
        System.out.println("GET request para: " + path);

        out.write("".getBytes(StandardCharsets.UTF_8));
        out.write("".getBytes(StandardCharsets.UTF_8));
        out.write("".getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private void handlePostRequest(OutputStream out, BufferedReader in, String path, Map<String, String> headers) throws IOException {
        System.out.println("POST request para: " + path);

        out.write("".getBytes(StandardCharsets.UTF_8));
        out.write("".getBytes(StandardCharsets.UTF_8));
        out.write("".getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private void handleHeadRequest(OutputStream out, String path, Map<String, String> headers) throws IOException {
        System.out.println("HEAD request para: " + path);

        out.write("".getBytes(StandardCharsets.UTF_8));
        out.write("".getBytes(StandardCharsets.UTF_8));
        out.write("".getBytes(StandardCharsets.UTF_8));
        out.flush();
    }


    private void sendErrorResponse(OutputStream out, String path, Map<String, String> headers) throws IOException {
        System.out.println("ERROR request para: " + path);

        out.write("".getBytes(StandardCharsets.UTF_8));
        out.write("".getBytes(StandardCharsets.UTF_8));
        out.write("".getBytes(StandardCharsets.UTF_8));
        out.flush();

    }




}
