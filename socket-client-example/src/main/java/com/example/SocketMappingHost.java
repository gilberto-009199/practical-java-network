package com.example;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class SocketMappingHost implements Example {

    @Override
    public void example() {
        System.out.println("\n=== Socket Mapping Host - Scanner de Portas ===\n");

        try {
            Scanner scanner = new Scanner(System.in);

            String host = "google.com";
            int startPort = 1;
            int endPort = 1024;
            int timeout = 2000;
            boolean showHandshake = true;

            System.out.println("\nIniciando scan em " + host + " portas " + startPort + "-" + endPort);
            System.out.println("==============================================");

            scanPorts(host, startPort, endPort, timeout, showHandshake);

        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }

    private void scanPorts(String host, int startPort, int endPort, int timeout, boolean showHandshake) {
        ExecutorService executor = Executors.newFixedThreadPool(50);
        List<Future<PortResult>> results = new ArrayList<>();

        System.out.println("Scanning...");
        System.out.printf("%-8s %-8s %-15s %s\n", "PORTA", "STATUS", "SERVIÇO", "DETALHES");
        System.out.println("--------------------------------------------------");

        for (int port = startPort; port <= endPort; port++) {
            final int currentPort = port;
            Future<PortResult> future = executor.submit(() -> {
                return checkPort(host, currentPort, timeout, showHandshake);
            });
            results.add(future);
        }

        // Coletar resultados
        List<PortResult> openPorts = new ArrayList<>();
        for (Future<PortResult> future : results) {
            try {
                PortResult result = future.get(2, TimeUnit.SECONDS); // Timeout para evitar bloqueio
                if (result.isOpen()) {
                    openPorts.add(result);
                    printPortResult(result);
                }
            } catch (Exception e) {
                // Ignorar timeouts das threads
            }
        }

        executor.shutdown();

        System.out.println("\n==============================================");
        System.out.println("Scan completo! " + openPorts.size() + " portas abertas encontradas.");

        if (!openPorts.isEmpty()) {
            System.out.println("\n=== RESUMO DAS PORTAS ABERTAS ===");
            for (PortResult result : openPorts) {
                System.out.printf("Porta %d: %s - %s\n",
                        result.getPort(), result.getServiceName(), result.getBanner());
            }
        }
    }

    private PortResult checkPort(String host, int port, int timeout, boolean showHandshake) {
        PortResult result = new PortResult(port, false, "Fechada", "", "");

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            result.setOpen(true);
            result.setStatus("Aberta");
            result.setServiceName(guessServiceName(port));

            if (showHandshake) {
                try {
                    // Tentar ler banner/service info
                    socket.setSoTimeout(2000);

                    // Enviar probe básico para alguns serviços
                    String banner = readBanner(socket, port);
                    result.setBanner(banner);

                } catch (Exception e) {
                    result.setBanner("Sem banner disponível");
                }
            }

        } catch (ConnectException e) {
            result.setStatus("Fechada");
        } catch (SocketTimeoutException e) {
            result.setStatus("Filtrada/Timeout");
        } catch (IOException e) {
            result.setStatus("Erro: " + e.getMessage());
        }

        return result;
    }

    private String readBanner(Socket socket, int port) throws IOException {
        StringBuilder banner = new StringBuilder();

        try {
            InputStream in = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            // Enviar probes específicos baseados na porta
            switch (port) {
                case 21: // FTP
                    os.write("QUIT\r\n".getBytes());
                    break;
                case 22: // SSH
                    // SSH envia banner automaticamente
                    break;
                case 25: // SMTP
                    os.write("QUIT\r\n".getBytes());
                    break;
                case 80: // HTTP
                    os.write("HEAD / HTTP/1.0\r\n\r\n".getBytes());
                    break;
                case 443: // HTTPS
                    os.write("HEAD / HTTP/1.0\r\n\r\n".getBytes());
                    break;
                default:
                    os.write("\r\n".getBytes()); // Probe genérico
            }

            // Ler resposta
            byte[] buffer = new byte[1024];
            int bytesRead;
            long startTime = System.currentTimeMillis();

            while ((bytesRead = in.read(buffer)) != -1 &&
                    (System.currentTimeMillis() - startTime) < 2000) {
                banner.append(new String(buffer, 0, bytesRead));
                if (banner.length() > 512) break; // Limitar tamanho
            }

            // Limpar e formatar banner
            String cleanedBanner = banner.toString()
                    .replace("\r", "\\r")
                    .replace("\n", "\\n")
                    .replace("\t", "\\t")
                    .trim();

            return cleanedBanner.length() > 100 ?
                    cleanedBanner.substring(0, 100) + "..." : cleanedBanner;

        } catch (Exception e) {
            return "Erro ao ler banner: " + e.getMessage();
        }
    }

    private String guessServiceName(int port) {
        switch (port) {
            case 21: return "FTP";
            case 22: return "SSH";
            case 23: return "Telnet";
            case 25: return "SMTP";
            case 53: return "DNS";
            case 80: return "HTTP";
            case 110: return "POP3";
            case 143: return "IMAP";
            case 443: return "HTTPS";
            case 993: return "IMAPS";
            case 995: return "POP3S";
            case 3306: return "MySQL";
            case 3389: return "RDP";
            case 5432: return "PostgreSQL";
            case 27017: return "MongoDB";
            default:
                if (port < 1024) return "Sistema";
                else if (port < 49152) return "Registrada";
                else return "Dinâmica/Privada";
        }
    }

    private void printPortResult(PortResult result) {
        String bannerPreview = result.getBanner();
        if (bannerPreview.length() > 30) {
            bannerPreview = bannerPreview.substring(0, 30) + "...";
        }

        System.out.printf("%-8d %-8s %-15s %s\n",
                result.getPort(),
                result.getStatus(),
                result.getServiceName(),
                bannerPreview);
    }

    // Classe para armazenar resultados
    private static class PortResult {
        private int port;
        private boolean isOpen;
        private String status;
        private String serviceName;
        private String banner;

        public PortResult(int port, boolean isOpen, String status, String serviceName, String banner) {
            this.port = port;
            this.isOpen = isOpen;
            this.status = status;
            this.serviceName = serviceName;
            this.banner = banner;
        }

        // Getters e Setters
        public int getPort() { return port; }
        public boolean isOpen() { return isOpen; }
        public void setOpen(boolean open) { isOpen = open; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public String getBanner() { return banner; }
        public void setBanner(String banner) { this.banner = banner; }
    }

    public static void main(String[] args) {
        new SocketMappingHost().example();
    }
}