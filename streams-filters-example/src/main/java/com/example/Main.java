package com.example;

public class Main {

    public static void main( String[] args ){

        new InputStreamExample().example();
        
        new OutputStreamExample().example();

        
        System.out.println( "FilterStream:" );
        new FilterStreamExample().example();
        
        
        System.out.println( "BufferStream:" );
        new BufferStreamExample().example();
        
        
        System.out.println( "InputStreamReader:" );
        new InputStreamReaderExample().example();
        
        
        System.out.println( "OutputStreamWriter:" );
        new OutputStreamWriterExample().example();
        
        
        System.out.println( "FilterReader:" );
        new FilterReadersExample().example();
        
        
        System.out.println( "FilterWriters:" );
        new FilterWritersExample().example();
        
        
        
        System.out.println( "PrintWriter:" );
        new PrintWriterExample().example();
        
        
    }
    
}
