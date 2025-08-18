package com.example;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InetAddressExample implements Example{

    @Override
    public void example() {
        System.out.println("\n=== Exemplos de InetAddress ===\n");
        // 1. Tipos de Endereço
        try{
            demonstrateAddressTypes();
        }catch (UnknownHostException e){
            System.out.println("Host não encontrado: "+ e.getMessage());
        }

        // 2. Testando Cache DNS

    }

    private void demonstrateAddressTypes() throws UnknownHostException {
        System.out.println("=== Demonstração de Tipos de Endereço ===");

        // Obter localhost
        InetAddress localHost = InetAddress.getLocalHost();
        System.out.println("Localhost: " + localHost);
        System.out.println("Nome do host: " + localHost.getHostName());
        System.out.println("Endereço IP: " + localHost.getHostAddress());
        System.out.println("É endereço de loopback? " + localHost.isLoopbackAddress());

        // Obter endereço de loopback explícito
        InetAddress loopback = InetAddress.getByName("127.0.0.1");
        System.out.println("\nLoopback 127.0.0.1: " + loopback);
        System.out.println("É endereço de loopback? " + loopback.isLoopbackAddress());

        // Obter endereço IPv4 público (exemplo: Google DNS)

        InetAddress googleDNS = InetAddress.getByName("8.8.8.8");
        System.out.println("\nGoogle DNS (8.8.8.8): " + googleDNS);
        System.out.println("É endereço IPv4? " + (googleDNS.getAddress().length == 4));
        System.out.println("É endereço IPv6? " + (googleDNS.getAddress().length == 16));
        System.out.println("É endereço multicast? " + googleDNS.isMulticastAddress());

        // Obter endereço IPv6 (exemplo: Google IPv6 DNS)

        InetAddress googleIPv6DNS = InetAddress.getByName("2001:4860:4860::8888");
        System.out.println("\nGoogle IPv6 DNS: " + googleIPv6DNS);
        System.out.println("É endereço IPv6? " + (googleIPv6DNS.getAddress().length == 16));
        System.out.println("É endereço link-local? " + googleIPv6DNS.isLinkLocalAddress());
        System.out.println("É endereço site-local? " + googleIPv6DNS.isSiteLocalAddress());


        // Obter todos os endereços de um hostname

        System.out.println("\nTodos os endereços para google.com:");
        InetAddress[] googleAddresses = InetAddress.getAllByName("google.com");
        for (InetAddress addr : googleAddresses) {
            System.out.println("- " + addr);
        }

    }
}
