package com.example;

import java.io.*;
import java.net.Socket;

public class ClientDictSimple implements Example{

    @Override
    public void example() {

        System.out.println("===  Client DICT Example  ===");

        try (
                Socket socket = new Socket("dict.org", 2628);
                var out = socket.getOutputStream();
                var in = socket.getInputStream();
        ) {

            var writer = new OutputStreamWriter(out, "UTF-8");
            var reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            writer.write("DEFINE fd-por-eng mesa\r\n");
            writer.flush(); // Força envio imediato

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(".")) break; // Fim da definição
                System.out.println(line);
            }

            writer.write("quit\r\n");
            writer.flush();
            socket.close();

        }catch(Exception e){
            e.printStackTrace();
        }











    }

}
