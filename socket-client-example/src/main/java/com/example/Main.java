package com.example;

public class Main {

    public static void main( String[] args ){

        // socket client http simple example
        new ClientHttpExample().example();

        // socket client dict simple example
        new ClientDictSimple().example();

        // socket client proxy socks5 http example
        new ClientHttpWithProxyExample().example();

        // socket mapping port in host
        new SocketMappingHost().example();

        // @todo create program client translate
        // @todo create program port scan

    }

}
