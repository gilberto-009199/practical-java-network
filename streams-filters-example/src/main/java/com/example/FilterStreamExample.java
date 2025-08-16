package com.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
import java.util.Base64;

public class FilterStreamExample implements Example {

    public void example() {
        System.out.println("\n=== Filter Stream Examples ===\n");
        
        // 1. Exemplo com GZIP (compressão)
        gzipExample();
        
        // 2. Exemplo com Base64 (codificação)
        base64Example();
        
    }
    
    private void gzipExample() {
        System.out.println("1. GZIP Compression Example:");
        String originalText = """ 
        	Este é um texto que será compactado usando GZIP.
        	asdasdasd ouyhuypouypouy khlgkgkglkj
        	asdasdasd lkhçljçhouhh jpũjpijpijpiojçjijnljkhui
        	asdasdasdasd asdasdasdasdasdasdasdasdasd
        	asdasdasdas dasdasdasd
        	asdasdasdas dasdasdasd
        	43fr43r3rf49rh493r3 h0f58h3´598j39p84yg593 n597y05miurhurh 
        """;
        
        try {
            // Compactação
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
                gzos.write(originalText.getBytes(StandardCharsets.UTF_8));
            }
            
            byte[] compressed = baos.toByteArray();
            
            // Descompactação
            ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
            try (GZIPInputStream gzis = new GZIPInputStream(bais);
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gzis.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                String decompressedText = out.toString(StandardCharsets.UTF_8);
                
                System.out.println("Original: " + originalText.length() + " bytes");
                System.out.println("Compressed: " + compressed.length + " bytes");
                System.out.println("Decompressed matches original: " + 
                    originalText.equals(decompressedText));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void base64Example() {
        System.out.println("\n2. Base64 Encoding Example:");
        String original = "Dados sensíveis que precisam ser codificados!";
        
        // Codificação
        ByteArrayOutputStream b64Out = new ByteArrayOutputStream();
        try (OutputStream base64Stream = Base64.getEncoder().wrap(b64Out)) {
            base64Stream.write(original.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String encoded = b64Out.toString(StandardCharsets.UTF_8);
        
        // Decodificação
        ByteArrayInputStream b64In = new ByteArrayInputStream(encoded.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream decodedOut = new ByteArrayOutputStream();
        try (InputStream base64Stream = Base64.getDecoder().wrap(b64In)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = base64Stream.read(buffer)) != -1) {
                decodedOut.write(buffer, 0, len);
            }
            String decoded = decodedOut.toString(StandardCharsets.UTF_8);
            
            System.out.println("Original: " + original);
            System.out.println("Encoded: " + encoded);
            System.out.println("Decoded matches original: " + original.equals(decoded));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}