package com.example;

public class Main {

    public static void main( String[] args ){

        System.out.println( "InputStream:" );
        InputStreamExample.example();

        
        System.out.println( "OutputStream:" );
        OutputStreamExample.example();

        
        System.out.println( "FilterStream:" );
        FilterStreamExample.example();
        
        
        System.out.println( "BufferStream:" );
        BufferStreamExample.example();
        
        
        System.out.println( "InputStreamReader:" );
        InputStreamReaderExample.example();
        
        
        System.out.println( "OutputStreamWriter:" );
        OutputStreamWriterExample.example();
        
        
        System.out.println( "FilterReader:" );
        FilterReadersExample.example();
        
        
        System.out.println( "FilterWriters:" );
        FilterWritersExample.example();
        
        
        
        System.out.println( "PrintWriter:" );
        PrintWriterExample.example();
        
        
    }
    
}
