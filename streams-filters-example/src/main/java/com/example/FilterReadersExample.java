package com.example;

import java.io.*;

public class FilterReadersExample implements Example {

    public void example() {
        System.out.println("\n=== Exemplos de Filtros para Texto ===");

        // 1. Filtro para maiúsculas
        System.out.println("\n1. Convertendo para MAIÚSCULAS:");
        filterExample("Texto Original com Acentos: áéíóú çãõ", new UppercaseFilter(new StringReader("")));

        // 2. Filtro para minúsculas
        System.out.println("\n2. Convertendo para minúsculas:");
        filterExample("TEXTO EM CAIXA ALTA COM ACENTOS: ÁÉÍÓÚ ÇÃÕ", new LowercaseFilter(new StringReader("")));

        // 3. Filtro para remover espaços extras
        System.out.println("\n3. Removendo espaços extras:");
        String textoEspacoso =  "Texto   com    muitos     espaços    \t  entre   palavras.";
        System.out.println("Original:\n"+ textoEspacoso);
        System.out.println("Alterado:");
        filterExample(textoEspacoso, new WhitespaceFilter(new StringReader("")));
    }

    private void filterExample(String input, FilterReader filter) {
        try (StringReader sr = new StringReader(input)) {
            // Configura o filtro com o StringReader
            if (filter instanceof UppercaseFilter) {
                filter = new UppercaseFilter(sr);
            } else if (filter instanceof LowercaseFilter) {
                filter = new LowercaseFilter(sr);
            } else if (filter instanceof WhitespaceFilter) {
                filter = new WhitespaceFilter(sr);
            }

            char[] buffer = new char[1024];
            int charsRead;
            while ((charsRead = filter.read(buffer)) != -1) {
                System.out.print(new String(buffer, 0, charsRead));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Filtro para converter para MAIÚSCULAS
    static class UppercaseFilter extends FilterReader {
        protected UppercaseFilter(Reader in) {
            super(in);
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            int charsRead = super.read(cbuf, off, len);
            if (charsRead != -1) {
                for (int i = off; i < off + charsRead; i++) {
                    cbuf[i] = Character.toUpperCase(cbuf[i]);
                }
            }
            return charsRead;
        }
    }

    // Filtro para converter para minúsculas
    static class LowercaseFilter extends FilterReader {
        protected LowercaseFilter(Reader in) {
            super(in);
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            int charsRead = super.read(cbuf, off, len);
            if (charsRead != -1) {
                for (int i = off; i < off + charsRead; i++) {
                    cbuf[i] = Character.toLowerCase(cbuf[i]);
                }
            }
            return charsRead;
        }
    }

    // Filtro para remover espaços extras
    static class WhitespaceFilter extends FilterReader {
        private boolean lastWasWhitespace = false;

        protected WhitespaceFilter(Reader in) {
            super(in);
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            int pos = off;
            int count = 0;
            
            while (count < len) {
                int nextChar = super.read();
                
                if (nextChar == -1) {
                    if (count == 0) return -1;
                    break;
                }
                
                char c = (char) nextChar;
                
                if (Character.isWhitespace(c)) {
                    if (!lastWasWhitespace) {
                        cbuf[pos++] = ' ';
                        count++;
                        lastWasWhitespace = true;
                    }
                } else {
                    cbuf[pos++] = c;
                    count++;
                    lastWasWhitespace = false;
                }
            }
            
            return count;
        }
    }

    public static void main(String[] args) {
        new FilterReadersExample().example();
    }
}