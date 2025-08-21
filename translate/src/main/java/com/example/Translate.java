package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class Translate extends JFrame {

    private JComboBox<String> languageComboBox;
    private JTextField wordTextField;
    private JTextArea resultTextArea;
    private JButton searchButton;
    private JButton refreshButton;
    private JLabel statusLabel;

    // Mapeamento de linguagens: nome -> código
    private Map<String, String> languageMap = new LinkedHashMap<>();

    public static void main(String[] args) {
        new Translate();
    }

    public Translate() {
        super("DICT Client - Dictionary Protocol");
        initializeUI();
        loadLanguagesAsync();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 550);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeUI() {
        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Painel de controle
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Linguagem
        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("Dicionário:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1.0;
        languageComboBox = new JComboBox<>();
        languageComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null && value.toString().equals("Carregando...")) {
                    setEnabled(false);
                }
                return c;
            }
        });
        languageComboBox.addItem("Carregando...");
        controlPanel.add(languageComboBox, gbc);

        // Botão refresh
        gbc.gridx = 2; gbc.gridy = 0;
        gbc.weightx = 0;
        refreshButton = new JButton("↻");
        refreshButton.setToolTipText("Recarregar dicionários");
        refreshButton.addActionListener(e -> loadLanguagesAsync());
        controlPanel.add(refreshButton, gbc);

        // Palavra
        gbc.gridx = 0; gbc.gridy = 1;
        controlPanel.add(new JLabel("Palavra:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.gridwidth = 2;
        wordTextField = new JTextField();
        wordTextField.addActionListener(e -> searchWord());
        controlPanel.add(wordTextField, gbc);

        // Botão de busca
        gbc.gridx = 3; gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.gridwidth = 1;
        searchButton = new JButton("Buscar");
        searchButton.addActionListener(e -> searchWord());
        controlPanel.add(searchButton, gbc);

        // Área de resultados
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultTextArea);

        // Status
        statusLabel = new JLabel("Conectando ao servidor DICT...");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Adicionar componentes ao painel principal
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        add(mainPanel);

        // Inicialmente desabilitar controles
        setControlsEnabled(false);
    }

    private void loadLanguagesAsync() {
        new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Carregando dicionários do servidor...");
                    setControlsEnabled(false);
                });

                Map<String, String> databases = fetchDatabasesFromServer();
                languageMap.clear();
                languageMap.putAll(databases);

                SwingUtilities.invokeLater(() -> {
                    updateLanguageComboBox();
                    statusLabel.setText("Pronto. " + languageMap.size() + " dicionários carregados");
                    setControlsEnabled(true);
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Erro ao carregar dicionários: " + e.getMessage());
                    JOptionPane.showMessageDialog(this,
                            "Erro ao conectar com servidor DICT:\n" + e.getMessage(),
                            "Erro de Conexão",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private Map<String, String> fetchDatabasesFromServer() throws IOException {
        Map<String, String> databases = new LinkedHashMap<>();

        try (Socket socket = new Socket("dict.org", 2628);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {

            socket.setSoTimeout(15000);

            Writer writer = new OutputStreamWriter(out, "UTF-8");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            // Ler banner inicial
            reader.readLine();

            // Enviar comando SHOW DATABASES
            writer.write("SHOW DATABASES\r\n");
            writer.flush();

            // Ler resposta
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("110")) {
                    // Número de databases disponíveis - ignorar
                    continue;
                }
                if (line.startsWith("554")) {
                    throw new IOException("Nenhum database disponível");
                }
                if (line.equals(".")) {
                    break; // Fim da lista
                }
                if (!line.startsWith("250") && !line.isEmpty()) {
                    // Formato: "database Description"
                    String[] parts = line.split("\\s+", 2);
                    if (parts.length >= 2) {
                        String dbCode = parts[0];
                        String dbDescription = parts[1];
                        databases.put(dbDescription, dbCode);
                    }
                }
            }

            // Finalizar conexão
            writer.write("quit\r\n");
            writer.flush();

        } catch (SocketTimeoutException e) {
            throw new IOException("Timeout ao conectar com servidor DICT");
        } catch (ConnectException e) {
            throw new IOException("Não foi possível conectar ao servidor DICT");
        }

        return databases;
    }

    private void updateLanguageComboBox() {
        languageComboBox.removeAllItems();

        // Ordenar por nome do dicionário
        List<String> sortedNames = new ArrayList<>(languageMap.keySet());
        Collections.sort(sortedNames);

        for (String dbName : sortedNames) {
            languageComboBox.addItem(dbName);
        }

        if (languageComboBox.getItemCount() == 0) {
            languageComboBox.addItem("Nenhum dicionário disponível");
        }
    }

    private void setControlsEnabled(boolean enabled) {
        languageComboBox.setEnabled(enabled);
        wordTextField.setEnabled(enabled);
        searchButton.setEnabled(enabled);
        refreshButton.setEnabled(enabled);
    }

    private void searchWord() {
        String word = wordTextField.getText().trim();
        if (word.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite uma palavra para buscar", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (languageComboBox.getSelectedItem() == null || languageMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum dicionário disponível", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedDB = (String) languageComboBox.getSelectedItem();
        String dbCode = languageMap.get(selectedDB);

        // Executar em thread separada
        new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> {
                    searchButton.setEnabled(false);
                    statusLabel.setText("Buscando: " + word + " em " + selectedDB);
                });

                String result = lookupWord(word, dbCode);

                SwingUtilities.invokeLater(() -> {
                    resultTextArea.setText(result);
                    statusLabel.setText("Busca concluída - " + selectedDB);
                    searchButton.setEnabled(true);
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    resultTextArea.setText("Erro na busca: " + e.getMessage() +
                            "\n\nDetalhes técnicos:\n" + Arrays.toString(e.getStackTrace()));
                    statusLabel.setText("Erro na busca");
                    searchButton.setEnabled(true);
                });
            }
        }).start();
    }

    private String lookupWord(String word, String databaseCode) {
        StringBuilder result = new StringBuilder();

        try (Socket socket = new Socket("dict.org", 2628);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {

            socket.setSoTimeout(10000);

            Writer writer = new OutputStreamWriter(out, "UTF-8");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            // Ler banner inicial
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("220")) break;
            }

            // Enviar comando DEFINE
            String command = "DEFINE " + databaseCode + " " + word + "\r\n";
            writer.write(command);
            writer.flush();

            result.append("Busca: ").append(word).append("\n");
            result.append("Dicionário: ").append(databaseCode).append("\n");
            result.append("=".repeat(50)).append("\n\n");

            // Ler resposta
            boolean inDefinition = false;
            while ((line = reader.readLine()) != null) {
                if (line.equals(".")) break;

                if (line.startsWith("150")) {
                    inDefinition = true;
                    result.append("Definições encontradas:\n");
                } else if (line.startsWith("151")) {
                    result.append("\nReferência: ").append(line.substring(4)).append("\n");
                } else if (line.startsWith("250")) {
                    break;
                } else if (line.startsWith("552")) {
                    result.append("Nenhuma definição encontrada para: ").append(word).append("\n");
                    break;
                } else if (inDefinition && !line.isEmpty()) {
                    result.append(line).append("\n");
                }
            }

            writer.write("quit\r\n");
            writer.flush();

        } catch (IOException e) {
            throw new RuntimeException("Erro de conexão: " + e.getMessage(), e);
        }

        return result.toString();
    }


}