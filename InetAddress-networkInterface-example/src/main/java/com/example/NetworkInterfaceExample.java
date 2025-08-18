package com.example;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

public class NetworkInterfaceExample implements Example {

    @Override
    public void example() {

        System.out.println("\n=== Exemplos de Network Interfaces ===\n");

        try {
            // 1. Listar NetworkInterface
            listNetworkInterfaces();

            System.out.println("\n--------------------------------\n");

            // 2. Mostrar informações detalhadas de cada NetworkInterface
            displayDetailedInterfaceInfo();

        } catch (SocketException e) {
            System.err.println("Erro ao acessar interfaces de rede: " + e.getMessage());
        }
    }

    private void listNetworkInterfaces() throws SocketException {
        System.out.println("=== Lista de Interfaces de Rede ===");

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        System.out.println("Interfaces disponíveis:");
        for (NetworkInterface iface : Collections.list(interfaces)) {
            System.out.println("- " + iface.getName() + " (" + iface.getDisplayName() + ")");
        }
    }

    private void displayDetailedInterfaceInfo() throws SocketException {
        System.out.println("=== Informações Detalhadas das Interfaces ===");

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        for (NetworkInterface iface : Collections.list(interfaces)) {
            System.out.println("\nInterface: " + iface.getName());
            System.out.println("  Nome de exibição: " + iface.getDisplayName());
            System.out.println("  Está ativa? " + iface.isUp());
            System.out.println("  É loopback? " + iface.isLoopback());
            System.out.println("  É ponto-a-ponto? " + iface.isPointToPoint());
            System.out.println("  Suporta multicast? " + iface.supportsMulticast());
            System.out.println("  Endereço MAC: " + formatMACAddress(iface.getHardwareAddress()));
            System.out.println("  MTU: " + iface.getMTU());

            // Mostrar endereços IP associados
            System.out.println("  Endereços IP:");
            iface.getInterfaceAddresses().forEach(addr -> {
                System.out.println("    - " + addr.getAddress().getHostAddress());
                if (addr.getBroadcast() != null) {
                    System.out.println("      Broadcast: " + addr.getBroadcast().getHostAddress());
                }
                System.out.println("      Prefixo de rede: " + addr.getNetworkPrefixLength());
            });

            // Mostrar interfaces filhas (sub-interfaces)
            Enumeration<NetworkInterface> subInterfaces = iface.getSubInterfaces();
            if (subInterfaces.hasMoreElements()) {
                System.out.println("  Sub-interfaces:");
                for (NetworkInterface subIface : Collections.list(subInterfaces)) {
                    System.out.println("    * " + subIface.getName() + " (" + subIface.getDisplayName() + ")");
                }
            }
        }
    }

    private String formatMACAddress(byte[] mac) {
        if (mac == null) {
            return "Não disponível";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
        }
        return sb.toString();
    }
}