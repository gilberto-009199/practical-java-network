package com.example;

import java.net.URI;
import java.net.URISyntaxException;

public class URIExample implements Example {

    @Override
    public void example() {

        System.out.println("\n=== URI Exemplos ===\n");

        try {
            // 1. Criando URI
            createURI();

            // 2. pegando partes da URI
            getPicesURI();

            // 3. resolvendo URI relativas
            resolverURIRelative();

            // 4. criando shema personalizado
            createSchemaPersonalizad();

        } catch (URISyntaxException e) {
            System.err.println("Erro ao criar URI: " + e.getMessage());
        }
    }

    // 1. Criando URI
    private void createURI() throws URISyntaxException {
        URI uri = new URI("https://www.example.com:8080/path/to/resource?query=param#fragment");
        System.out.println("\n1. URI criada: " + uri);
    }

    // 2. Pegando partes da URI
    private void getPicesURI() throws URISyntaxException {
        URI uri = new URI("https://user:pass@www.example.com:8080/path/to/resource?query=param#fragment");

        System.out.println("\n2. Partes da URI:");
        System.out.println("Scheme: " + uri.getScheme());
        System.out.println("Scheme-specific part: " + uri.getSchemeSpecificPart());
        System.out.println("Authority: " + uri.getAuthority());
        System.out.println("User info: " + uri.getUserInfo());
        System.out.println("Host: " + uri.getHost());
        System.out.println("Port: " + uri.getPort());
        System.out.println("Path: " + uri.getPath());
        System.out.println("Query: " + uri.getQuery());
        System.out.println("Fragment: " + uri.getFragment());
    }

    // 3. Resolvendo URI relativas
    private void resolverURIRelative() throws URISyntaxException {
        URI baseUri = new URI("https://www.example.com/base/path/");
        URI relativeUri = new URI("relative/path");

        URI resolvedUri = baseUri.resolve(relativeUri);
        System.out.println("\n3. URI resolvida: " + resolvedUri);
    }

    // 4. Criando schema personalizado
    private void createSchemaPersonalizad() throws URISyntaxException {
        URI customUri = new URI("myapp://user:1234/data?type=profile");

        System.out.println("\n4. URI com schema personalizado:");
        System.out.println("URI completa: " + customUri);
        System.out.println("Scheme personalizado: " + customUri.getScheme());
        System.out.println("Parte específica do scheme: " + customUri.getSchemeSpecificPart());
        System.out.println("Path: " + customUri.getPath());
    }
}