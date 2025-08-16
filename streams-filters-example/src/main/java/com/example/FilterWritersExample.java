package com.example;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FilterWritersExample implements Example {

    public void example() {
        System.out.println("\n=== Exemplos de FilterWriter ===");

        // 1. Filtro para converter para maiúsculas
        uppercaseWriterExample();

        // 2. Filtro para compactar espaços em branco
        whitespaceCompressWriterExample();

        // 3. Filtro para adicionar timestamp
        timestampWriterExample();

        // 4. Filtro para escape de caracteres especiais
        escapeHtmlWriterExample();
    }

    private void uppercaseWriterExample() {
        System.out.println("\n1. Filtro de Maiúsculas:");

        try (StringWriter sw = new StringWriter();
             UppercaseFilterWriter writer = new UppercaseFilterWriter(sw)) {
            
            writer.write("Este texto será convertido para MAIÚSCULAS.\n");
            writer.write("Incluindo acentuação: áéíóú çãõ");
            writer.flush();

            System.out.println("Saída:\n" + sw.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void whitespaceCompressWriterExample() {
        System.out.println("\n2. Filtro de Compactação de Espaços:");

        try (StringWriter sw = new StringWriter();
             WhitespaceFilterWriter writer = new WhitespaceFilterWriter(sw)) {
            
            writer.write("Texto    com    muitos     espaços    \t  entre   palavras.\n");
            writer.write("E  também  \n\nquebras  \t de  linha  extras.");
            writer.flush();

            System.out.println("Saída:\n" + sw.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void timestampWriterExample() {
        System.out.println("\n3. Filtro de Timestamp:");

        try (StringWriter sw = new StringWriter();
             TimestampFilterWriter writer = new TimestampFilterWriter(sw)) {
            
            writer.write("Mensagem de log 1\n");
            writer.write("Mensagem de log 2");
            writer.flush();

            System.out.println("Saída:\n" + sw.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void escapeHtmlWriterExample() {
        System.out.println("\n4. Filtro de Escape HTML:");

        try (StringWriter sw = new StringWriter();
             HtmlEscapeFilterWriter writer = new HtmlEscapeFilterWriter(sw)) {
            
            writer.write("<script>alert('XSS');</script>\n");
            writer.write("Tags HTML: <b>negrito</b> & <i>itálico</i>");
            writer.flush();

            System.out.println("Saída:\n" + sw.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Filtro para converter para maiúsculas
    static class UppercaseFilterWriter extends FilterWriter {
        protected UppercaseFilterWriter(Writer out) {
            super(out);
        }

        @Override
        public void write(int c) throws IOException {
        	out.write(Character.toUpperCase(c));
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            char[] upper = new char[len];
            for (int i = 0; i < len; i++) {
                upper[i] = Character.toUpperCase(cbuf[off + i]);
            }
            out.write(upper, 0, len);
        }

        @Override
        public void write(String str, int off, int len) throws IOException {
            write(str.toCharArray(), off, len);
        }
    }

    // Filtro para compactar espaços em branco
    static class WhitespaceFilterWriter extends FilterWriter {
        private boolean lastWasWhitespace = false;

        protected WhitespaceFilterWriter(Writer out) {
            super(out);
        }

        @Override
        public void write(int c) throws IOException {
            if (Character.isWhitespace(c)) {
                if (!lastWasWhitespace) {
                	out.write(' ');
                    lastWasWhitespace = true;
                }
            } else {
            	out.write(c);
                lastWasWhitespace = false;
            }
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            for (int i = off; i < off + len; i++) {
                write(cbuf[i]);
            }
        }

        @Override
        public void write(String str, int off, int len) throws IOException {
            for (int i = off; i < off + len; i++) {
                write(str.charAt(i));
            }
        }
    }

    // Filtro para adicionar timestamp
    static class TimestampFilterWriter extends FilterWriter {
        private boolean startOfLine = true;

        protected TimestampFilterWriter(Writer out) {
            super(out);
        }

        @Override
        public void write(int c) throws IOException {
            if (startOfLine) {
                out.write("[" + System.currentTimeMillis() + "] ");
                startOfLine = false;
            }
            out.write(c);
            if (c == '\n') {
                startOfLine = true;
            }
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            for (int i = off; i < off + len; i++) {
                write(cbuf[i]);
            }
        }

        @Override
        public void write(String str, int off, int len) throws IOException {
            for (int i = off; i < off + len; i++) {
                write(str.charAt(i));
            }
        }
    }

    // Filtro para escape de caracteres HTML
    static class HtmlEscapeFilterWriter extends FilterWriter {
        protected HtmlEscapeFilterWriter(Writer out) {
            super(out);
        }

        @Override
        public void write(int c) throws IOException {
            switch (c) {
                case '<': out.write("&lt;"); break;
                case '>': out.write("&gt;"); break;
                case '&': out.write("&amp;"); break;
                case '"': out.write("&quot;"); break;
                case '\'': out.write("&#39;"); break;
                default: out.write(c);
            }
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            for (int i = off; i < off + len; i++) {
                write(cbuf[i]);
            }
        }

        @Override
        public void write(String str, int off, int len) throws IOException {
            for (int i = off; i < off + len; i++) {
                write(str.charAt(i));
            }
        }
    }

    public static void main(String[] args) {
        new FilterWritersExample().example();
    }
}