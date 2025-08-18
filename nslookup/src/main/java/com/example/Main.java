package com.example;

import javax.swing.*;
import java.net.*;

public class Main {

    public static void main(String[] args) {
        String domain = JOptionPane.showInputDialog(null, "Digite o domínio:", "NSLookup", JOptionPane.QUESTION_MESSAGE);

        if(domain == null || domain.trim().isEmpty()) {
            System.exit(0);
        }

        try {
            InetAddress[] ips = InetAddress.getAllByName(domain.trim());
            StringBuilder resultado = new StringBuilder();

            for(InetAddress ip : ips) {
                resultado
                        .append(ip.getHostAddress())
                        .append("\n");
            }

            JOptionPane.showMessageDialog(null, resultado.toString(), "Resultado", JOptionPane.INFORMATION_MESSAGE);
        } catch(UnknownHostException e) {
            JOptionPane.showMessageDialog(null, "Domínio não encontrado", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}