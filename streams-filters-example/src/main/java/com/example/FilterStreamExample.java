package com.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

public class FilterStreamExample implements Example {

    public void example() {
        System.out.println("\n=== Filter Stream Examples ===\n");
        
        gzipExample();
        base64Example();
        customFilterExample();
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
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
                gzos.write(originalText.getBytes(StandardCharsets.UTF_8));
            }
            
            byte[] compressed = baos.toByteArray();
            
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
        
        ByteArrayOutputStream b64Out = new ByteArrayOutputStream();
        try (OutputStream base64Stream = Base64.getEncoder().wrap(b64Out)) {
            base64Stream.write(original.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String encoded = b64Out.toString(StandardCharsets.UTF_8);
        
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

    private void customFilterExample() {
        System.out.println("\n3. Filtro Anti-Palavrões Example:");
        
        String textoComPalavroes = """
            Este é um texto com conteúdo impróprio.
            Palavras como boboca, palerma e idiota devem ser filtradas!
            Mas palavras normais como casa e carro devem permanecer.
        """;
        
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (AntPalavraoFilter filtro = new AntPalavraoFilter(output)) {
                filtro.write(textoComPalavroes.getBytes(StandardCharsets.UTF_8));
            }
            
            String textoFiltrado = output.toString(StandardCharsets.UTF_8);
            
            System.out.println("Original:\n" + textoComPalavroes);
            System.out.println("\nFiltrado:\n" + textoFiltrado);
            
        } catch (IOException e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }
    
    static class AntPalavraoFilter extends FilterOutputStream {
    	
        private final String[] palavrasProibidas = {"boboca", "palerma", "idiota"};
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        
        public AntPalavraoFilter(OutputStream out) {
            super(out);
        }
        
        @Override
        public void write(int b) throws IOException {
            if (isWordCharacter(b)) {
                buffer.write(b);
            } else {
                processBuffer();
                out.write(b);
            }
        }
        
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            // Processa cada byte individualmente para evitar recursão
            for (int i = 0; i < len; i++) {
                write(b[off + i]);
            }
        }
        
        private boolean isWordCharacter(int b) {
            return Character.isLetterOrDigit(b) || b == '\'';
        }
        
        private void processBuffer() throws IOException {
            if (buffer.size() == 0) return;
            
            String palavra = buffer.toString(StandardCharsets.UTF_8);
            buffer.reset();
            
            // Verifica se a palavra está na lista proibida
            for (String proibida : palavrasProibidas) {
                if (palavra.equalsIgnoreCase(proibida)) {
                    palavra = "***"; // Substituição
                    break;
                }
            }
            
            // Escreve a palavra processada o output 
            out.write(palavra.getBytes(StandardCharsets.UTF_8));
        }
        
        @Override
        public void flush() throws IOException {
            processBuffer();
            super.flush();
        }
    }
    
}