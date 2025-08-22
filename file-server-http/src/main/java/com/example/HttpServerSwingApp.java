package com.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServerSwingApp extends JFrame {

    private final JTextField portField;
    private final JTextField webrootField;
    private final JButton startButton;
    private final JButton stopButton;
    private final JButton browseButton;
    private final JTextArea logArea;
    private final JTable connectionsTable;
    private final DefaultTableModel connectionsModel;

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private volatile boolean isRunning = false;
    private File webRootDirectory;
    private int currentPort = 8080;


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new HttpServerSwingApp();
        });
    }


    public HttpServerSwingApp() {
        setTitle("HTTP File Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Painel de configuração
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(new TitledBorder("Configurações do Servidor"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Porta
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        configPanel.add(new JLabel("Porta:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        portField = new JTextField("8080");
        configPanel.add(portField, gbc);

        // Webroot
        gbc.gridx = 0; gbc.gridy = 1;
        configPanel.add(new JLabel("Webroot:"), gbc);

        gbc.gridx = 1;
        webrootField = new JTextField();
        webrootField.setEditable(false);
        configPanel.add(webrootField, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        browseButton = new JButton("Procurar...");
        browseButton.addActionListener(this::browseWebroot);
        configPanel.add(browseButton, gbc);

        // Botões de controle
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        startButton = new JButton("Iniciar Servidor");
        startButton.addActionListener(this::startServer);
        stopButton = new JButton("Parar Servidor");
        stopButton.addActionListener(this::stopServer);
        stopButton.setEnabled(false);

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        configPanel.add(buttonPanel, gbc);

        // Tabela de conexões
        connectionsModel = new DefaultTableModel(new String[]{"IP", "Método", "Path", "Status", "Hora"}, 0);
        connectionsTable = new JTable(connectionsModel);
        JScrollPane tableScroll = new JScrollPane(connectionsTable);
        tableScroll.setBorder(new TitledBorder("Conexões Ativas"));
        tableScroll.setPreferredSize(new Dimension(0, 150));

        // Área de log
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(new TitledBorder("Log do Servidor"));

        // Layout
        mainPanel.add(configPanel, BorderLayout.NORTH);
        mainPanel.add(tableScroll, BorderLayout.CENTER);
        mainPanel.add(logScroll, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private void browseWebroot(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Selecionar Diretório Webroot");

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            webRootDirectory = chooser.getSelectedFile();
            webrootField.setText(webRootDirectory.getAbsolutePath());
            log("Diretório webroot selecionado: " + webRootDirectory.getAbsolutePath());
        }
    }

    private void startServer(ActionEvent e) {
        if (webRootDirectory == null || !webRootDirectory.exists() || !webRootDirectory.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Selecione um diretório webroot válido!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            currentPort = Integer.parseInt(portField.getText());
            if (currentPort < 1024 || currentPort > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Porta inválida! Use uma porta entre 1024 e 65535.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            serverSocket = new ServerSocket(currentPort);
            executorService = Executors.newFixedThreadPool(10);
            isRunning = true;

            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            portField.setEnabled(false);
            browseButton.setEnabled(false);

            log("Servidor iniciado na porta " + currentPort);
            log("Webroot: " + webRootDirectory.getAbsolutePath());
            log("Acesse: http://localhost:" + currentPort);
            log("Servidor rodando...");

            // Thread para aceitar conexões
            new Thread(() -> {
                while (isRunning && !serverSocket.isClosed()) {
                    try {
                        Socket client = serverSocket.accept();
                        executorService.submit(() -> handleClient(client));
                    } catch (IOException ex) {
                        if (isRunning) {
                            log("Erro ao aceitar conexão: " + ex.getMessage());
                        }
                    }
                }
            }).start();

        } catch (IOException ex) {
            log("Erro ao iniciar servidor: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Erro ao iniciar servidor: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stopServer(ActionEvent e) {
        isRunning = false;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (executorService != null) {
                executorService.shutdown();
            }
        } catch (IOException ex) {
            log("Erro ao parar servidor: " + ex.getMessage());
        }

        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        portField.setEnabled(true);
        browseButton.setEnabled(true);

        log("Servidor parado");
    }

    private void handleClient(Socket client) {
        String clientAddress = client.getInetAddress().getHostAddress();

        try (client;
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             OutputStream out = client.getOutputStream()) {

            // Ler request line
            String requestLine = in.readLine();
            if (requestLine == null) {
                sendErrorResponse(out, 400, "Bad Request");
                return;
            }

            // Ler headers
            Map<String, String> headers = new HashMap<>();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                int colonIndex = line.indexOf(':');
                if (colonIndex > 0) {
                    String key = line.substring(0, colonIndex).trim();
                    String value = line.substring(colonIndex + 1).trim();
                    headers.put(key, value);
                }
            }

            // Parse request
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 3) {
                sendErrorResponse(out, 400, "Bad Request");
                return;
            }

            String method = requestParts[0];
            String path = requestParts[1];
            String httpVersion = requestParts[2];

            // Log da conexão
            SwingUtilities.invokeLater(() -> {
                String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                connectionsModel.addRow(new Object[]{clientAddress, method, path, "Processando", timestamp});
                log("📨 " + method + " " + path + " from " + clientAddress);
            });

            // Processar request
            if (method.equals("GET")) {
                handleGetRequest(out, path, clientAddress);
            } else {
                sendErrorResponse(out, 405, "Method Not Allowed");
            }

            // Atualizar status na tabela
            SwingUtilities.invokeLater(() -> {
                int rowCount = connectionsModel.getRowCount();
                if (rowCount > 0) {
                    connectionsModel.setValueAt("Concluído", rowCount - 1, 3);
                }
            });

        } catch (IOException ex) {
            log("❌ Erro na conexão com " + clientAddress + ": " + ex.getMessage());
        }
    }

    private void handleGetRequest(OutputStream out, String path, String clientAddress) throws IOException {
        // Prevenir path traversal attacks
        if (path.contains("..")) {
            sendErrorResponse(out, 403, "Forbidden");
            return;
        }

        // Mapear path para arquivo
        File requestedFile;
        if (path.equals("/")) {
            // Listar diretório
            listDirectory(out, webRootDirectory, clientAddress);
            return;
        } else {
            String filePath = path.substring(1); // Remover a barra inicial
            requestedFile = new File(webRootDirectory, filePath);
        }

        // Verificar se o arquivo existe
        if (!requestedFile.exists() || !requestedFile.getCanonicalPath().startsWith(webRootDirectory.getCanonicalPath())) {
            sendErrorResponse(out, 404, "File Not Found");
            return;
        }

        if (requestedFile.isDirectory()) {
            // Listar conteúdo do diretório
            listDirectory(out, requestedFile, clientAddress);
        } else {
            // Servir arquivo
            serveFile(out, requestedFile);
        }
    }

    private void listDirectory(OutputStream out, File directory, String clientAddress) throws IOException {
        File[] files = directory.listFiles();
        if (files == null) {
            sendErrorResponse(out, 403, "Forbidden");
            return;
        }

        StringBuilder html = new StringBuilder();
        html.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset='UTF-8'>
                <title>Index of %s</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }
                    .container { background: white; padding: 20px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    h1 { color: #333; border-bottom: 2px solid #007bff; padding-bottom: 10px; }
                    table { width: 100%%; border-collapse: collapse; margin-top: 20px; }
                    th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
                    th { background-color: #007bff; color: white; }
                    tr:hover { background-color: #f8f9fa; }
                    .file { color: #28a745; }
                    .dir { color: #007bff; }
                    .size { text-align: right; }
                    .back { margin-bottom: 20px; }
                </style>
            </head>
            <body>
                <div class='container'>
                    <h1>📁 Index of %s</h1>
                    <div class='back'>
                        <a href='%s'>↩ Voltar</a>
                    </div>
                    <table>
                        <tr>
                            <th>Nome</th>
                            <th>Tamanho</th>
                            <th>Modificado</th>
                            <th>Tipo</th>
                        </tr>
            """.formatted(directory.getName(), directory.getName(),
                directory.getParentFile().equals(webRootDirectory) ? "/" : "../"));

        // Link para diretório pai (se não for o root)
        if (!directory.equals(webRootDirectory)) {
            html.append("""
                <tr>
                    <td colspan='4'><a href='../'>📁 ../</a></td>
                </tr>
                """);
        }

        // Listar arquivos e diretórios
        for (File file : files) {
            String name = file.getName();
            String size = file.isDirectory() ? "-" : formatFileSize(file.length());
            String modified = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(file.lastModified()));
            String type = file.isDirectory() ? "Diretório" : "Arquivo";
            String cssClass = file.isDirectory() ? "dir" : "file";
            String icon = file.isDirectory() ? "📁" : "📄";
            String link = file.isDirectory() ? name + "/" : name;

            html.append(String.format("""
                <tr>
                    <td><a href='%s' class='%s'>%s %s</a></td>
                    <td class='size'>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                </tr>
                """, link, cssClass, icon, name, size, modified, type));
        }

        html.append("""
                    </table>
                </div>
            </body>
            </html>
            """);

        sendResponse(out, 200, "OK", html.toString(), "text/html");
        log("📋 Listagem de diretório: " + directory.getName() + " para " + clientAddress);
    }

    private void serveFile(OutputStream out, File file) throws IOException {
        String contentType = getContentType(file.getName());
        long fileLength = file.length();

        // Headers
        String headers = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + fileLength + "\r\n" +
                "Content-Disposition: inline; filename=\"" + file.getName() + "\"\r\n" +
                "Connection: close\r\n" +
                "Server: SwingHTTPServer/1.0\r\n" +
                "\r\n";

        out.write(headers.getBytes(StandardCharsets.UTF_8));

        // Enviar arquivo
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        out.flush();
        log("📤 Arquivo enviado: " + file.getName() + " (" + formatFileSize(fileLength) + ")");
    }

    private String getContentType(String fileName) {
        if (fileName.endsWith(".html") || fileName.endsWith(".htm")) return "text/html";
        if (fileName.endsWith(".css")) return "text/css";
        if (fileName.endsWith(".js")) return "application/javascript";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".gif")) return "image/gif";
        if (fileName.endsWith(".pdf")) return "application/pdf";
        if (fileName.endsWith(".zip")) return "application/zip";
        if (fileName.endsWith(".txt")) return "text/plain";
        return "application/octet-stream";
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private void sendErrorResponse(OutputStream out, int statusCode, String message) throws IOException {
        String responseBody = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset='UTF-8'>
                <title>Erro %d</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; background: #ffe6e6; }
                    .container { background: white; padding: 20px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); text-align: center; }
                    h1 { color: #c0392b; }
                    .error-code { font-size: 4em; color: #c0392b; margin: 0; }
                </style>
            </head>
            <body>
                <div class='container'>
                    <h1 class='error-code'>%d</h1>
                    <h2>%s</h2>
                    <p>O servidor encontrou um erro ao processar sua requisição.</p>
                    <p><a href='/'>Voltar para a página inicial</a></p>
                </div>
            </body>
            </html>
            """.formatted(statusCode, statusCode, statusCode, message);

        sendResponse(out, statusCode, message, responseBody, "text/html");
        log("❌ Erro " + statusCode + ": " + message);
    }

    private void sendResponse(OutputStream out, int statusCode, String statusMessage,
                              String body, String contentType) throws IOException {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

        String response = "HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n" +
                "Content-Type: " + contentType + "; charset=utf-8\r\n" +
                "Content-Length: " + bodyBytes.length + "\r\n" +
                "Connection: close\r\n" +
                "Server: SwingHTTPServer/1.0\r\n" +
                "\r\n";

        out.write(response.getBytes(StandardCharsets.UTF_8));
        out.write(bodyBytes);
        out.flush();
    }

    private void sendResponse(OutputStream out, int statusCode, String statusMessage, String body) throws IOException {
        sendResponse(out, statusCode, statusMessage, body, "text/html");
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

}