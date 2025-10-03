package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class ProxyTester extends JFrame {

    private JTextField proxyHostField;
    private JTextField proxyPortField;
    private JTextField urlField;
    private JTextArea resultArea;
    private JCheckBox useProxyCheckbox;


    public static void main(String[] args) {

        new ProxyTester();

    }

    public ProxyTester() {
        super("Testador de Proxy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout(10, 10));

        // Painel de configuração
        JPanel configPanel = new JPanel(new GridLayout(0, 2, 5, 5));

        useProxyCheckbox = new JCheckBox("Usar Proxy", true);
        configPanel.add(useProxyCheckbox);
        configPanel.add(new JLabel()); // Espaço vazio

        configPanel.add(new JLabel("Host do Proxy:"));
        proxyHostField = new JTextField("20.27.15.111");
        configPanel.add(proxyHostField);

        configPanel.add(new JLabel("Porta do Proxy:"));
        proxyPortField = new JTextField("8561");
        configPanel.add(proxyPortField);

        configPanel.add(new JLabel("URL para testar:"));
        urlField = new JTextField("http://www.example.com");
        configPanel.add(urlField);

        configPanel.add(new JLabel("Sites de Proxy: https://free-proxy-list.net/en/"));

        JButton testButton = new JButton("Testar Conexão");
        testButton.addActionListener(this::testConnection);

        configPanel.add(testButton);

        // Área de resultados
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // Adicionando componentes ao frame
        add(configPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void testConnection(ActionEvent e) {
        String proxyHost = proxyHostField.getText();
        String proxyPortStr = proxyPortField.getText();
        String urlStr = urlField.getText();

        if (urlStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, informe uma URL", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            URL url = new URL(urlStr);
            URLConnection connection;

            if (useProxyCheckbox.isSelected() && !proxyHost.isEmpty()) {
                int proxyPort = proxyPortStr.isEmpty() ? 8080 : Integer.parseInt(proxyPortStr);
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                connection = url.openConnection(proxy);
                resultArea.append("Conectando via proxy: " + proxyHost + ":" + proxyPort + "\n");
            } else {
                connection = url.openConnection();
                resultArea.append("Conectando diretamente (sem proxy)\n");
            }

            // Configura timeout
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            resultArea.append("Conectando a: " + urlStr + "\n");
            resultArea.append("----------------------------------\n");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    resultArea.append(line + "\n");
                }
                resultArea.append("\nConexão bem-sucedida!\n");
            }

        } catch (NumberFormatException ex) {
            resultArea.append("Erro: Porta do proxy inválida\n");
        } catch (MalformedURLException ex) {
            resultArea.append("Erro: URL inválida\n");
        } catch (ConnectException ex) {
            resultArea.append("Erro: Não foi possível conectar ao proxy\n");
        } catch (SocketTimeoutException ex) {
            resultArea.append("Erro: Timeout na conexão\n");
        } catch (IOException ex) {
            resultArea.append("Erro: " + ex.getMessage() + "\n");
        } finally {
            resultArea.append("----------------------------------\n\n");
        }
    }

}