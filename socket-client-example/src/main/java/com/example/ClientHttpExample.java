package com.example;

import java.io.*;
import java.net.Socket;

public class ClientHttpExample implements Example{

    @Override
    public void example() {

        System.out.println("===  Client Http Example  ===");

        try(
                var socket = new Socket("example.com", 80);
                var out = socket.getOutputStream();
                var in = socket.getInputStream();
        ) {

            // Envia uma requisição HTTP GET
            String request = "GET / HTTP/1.0\r\n"+
                             "Host: example.com\r\n"+
                             "\r\n";

            out.write(request.getBytes());

            // Lê a resposta
            var reader = new BufferedReader(new InputStreamReader(in));
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
