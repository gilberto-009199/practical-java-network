package com.example;

public class Main {

    public static void main( String[] args ){

        // socket client http simple example
        new ClientHttpExample().example();

        // socket client dict simple example
        new ClientDictSimple().example();

        // socket client proxy socks5 http example
        new ClientHttpWithProxyExample().example();

        // socket client mapping port in host

        // @todo create program client translate
        // @todo create program port scan
        // @todo create program client http
        // @todo create program client http with buffer

    }

}
