package com.example;

public class Main {

    public static void main( String[] args ){

        new InputStreamExample().example();
        
        new OutputStreamExample().example();
        
        new FilterStreamExample().example();
                
        new BufferStreamExample().example();
        
        new InputStreamReaderExample().example();
        
        new OutputStreamWriterExample().example();
        
        System.out.println( "FilterReader:" );
        new FilterReadersExample().example();

        System.out.println( "FilterWriters:" );
        new FilterWritersExample().example();

        System.out.println( "PrintWriter:" );
        new PrintWriterExample().example();

    }

}
