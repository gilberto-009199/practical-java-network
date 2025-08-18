package com.example;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InetAddressExample implements Example{

    @Override
    public void example() {
        System.out.println("\n=== Exemplos de InetAddress ===\n");

        try{

            // 1. Tipos de Endereço
            demonstrateAddressTypes();

            // 2. Testando Cache DNS
            testDNSCache();

        }catch (UnknownHostException e){
            System.out.println("Host não encontrado: "+ e.getMessage());
        }



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

    private void testDNSCache() throws UnknownHostException {
        System.out.println("=== Testando Cache DNS ===");

        // Primeira consulta (pode ser mais lenta)
        long startTime = System.currentTimeMillis();
        InetAddress address1 = InetAddress.getByName("google.com");
        long endTime = System.currentTimeMillis();
        System.out.println("Primeira consulta para google.com: " + (endTime - startTime) + "ms");

        // Segunda consulta (deve ser mais rápida por causa do cache)
        startTime = System.currentTimeMillis();
        InetAddress address2 = InetAddress.getByName("google.com");
        endTime = System.currentTimeMillis();
        System.out.println("Segunda consulta para google.com: " + (endTime - startTime) + "ms");

        // Mostrar que é o mesmo objeto (cacheado)
        System.out.println("Mesmo objeto? " + (address1 == address2));
        System.out.println("Endereços iguais? " + address1.equals(address2));

        // Limpar cache DNS (operação de reflection pois não há API pública)
        try {
            // Isso é uma implementação específica da JVM - pode não funcionar em todas as versões
            Class<?> inetAddressClass = Class.forName("java.net.InetAddress");
            Object addressCache = inetAddressClass.getDeclaredField("addressCache").get(null);
            Class<?> cacheClass = Class.forName("java.net.InetAddress$AddressCache");
            cacheClass.getDeclaredMethod("clear").invoke(addressCache);
            System.out.println("\nCache DNS limpo!");

            // Consulta após limpar cache
            startTime = System.currentTimeMillis();
            InetAddress address3 = InetAddress.getByName("google.com");
            endTime = System.currentTimeMillis();
            System.out.println("Consulta após limpar cache: " + (endTime - startTime) + "ms");

        } catch (Exception e) {
            System.out.println("\nNão foi possível limpar cache DNS (método não suportado nesta JVM)");
        }

        // Configurar tempo de cache DNS (propriedade do sistema)
        System.out.println("\nTempo de cache DNS atual (networkaddress.cache.ttl): " +
                System.getProperty("networkaddress.cache.ttl", "padrão da JVM"));
        System.out.println("Tempo de cache DNS para falhas (networkaddress.cache.negative.ttl): " +
                System.getProperty("networkaddress.cache.negative.ttl", "padrão da JVM"));
    }
}
