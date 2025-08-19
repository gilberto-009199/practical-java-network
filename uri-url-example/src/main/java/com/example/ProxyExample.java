package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

public class ProxyExample implements Example {

    // https://free-proxy-list.net/en/
    // 115.72.2.169	10022	VN	Vietnam
    String proxyHost = "115.72.2.169";
    int proxyPort = 10022;

    @Override
    public void example() {
        try {

            // 1. pegar System Properties do proxy
            getSystemProxyProperties();

            // 2. configurar proxy
            configureProxy();

            // 3. enviar request para exemplo atraves do proxy
            sendRequestThroughProxy();

        } catch (IOException e) {
            System.err.println("Erro na operação com proxy: " + e.getMessage());
        }
    }

    // 1. Pegar System Properties do proxy
    private void getSystemProxyProperties() {
        System.out.println("\n1. Propriedades de Proxy do Sistema:");

        Properties systemProps = System.getProperties();
        String[] proxyProps = {
                "http.proxyHost", "http.proxyPort",
                "https.proxyHost", "https.proxyPort",
                "ftp.proxyHost", "ftp.proxyPort",
                "socksProxyHost", "socksProxyPort",
                "java.net.useSystemProxies"
        };

        for (String prop : proxyProps) {
            System.out.println(prop + " = " + systemProps.getProperty(prop, "não definido"));
        }
    }

    // 2. Configurar proxy
    private void configureProxy() {
        System.out.println("\n2. Configurando Proxy:");

        // Configuração básica de proxy HTTP


        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyPort", String.valueOf(proxyPort));

        System.out.println("Proxy HTTP configurado: " + proxyHost + ":" + proxyPort);

        // Configuração com autenticação

        String proxyUser = "";
        String proxyPass = "";

        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(proxyUser, proxyPass.toCharArray());
            }
        });

        System.out.println("Autenticação configurada para o proxy");
    }

    // 3. Enviar request através do proxy
    private void sendRequestThroughProxy() throws IOException {
        System.out.println("\n3. Enviando requisição através do proxy:");

        // Usando proxy configurado via system properties
        URL url = new URL("https://www.example.com");
        URLConnection conn = url.openConnection();

        // Alternativa: Usando proxy diretamente
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        URLConnection connWithExplicitProxy = url.openConnection(proxy);



        // Fazendo a requisição (exemplo simplificado)
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connWithExplicitProxy.getInputStream()))) {

            System.out.println("Cabeçalhos da resposta:");
            connWithExplicitProxy.getHeaderFields().forEach((k, v) ->
                    System.out.println(k + ": " + v));

            System.out.println("\nPrimeiras linhas do conteúdo:");
            for (int i = 0; i < 5 && reader.ready(); i++) {
                System.out.println(reader.readLine());
            }
        }
    }
}