package com.example;


import javax.swing.*;
import java.io.*;
import java.util.zip.GZIPOutputStream;


public class Main {

    public static void main(String[] args) {
        // Cria e exibe a janela de seleção de arquivo
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione o arquivo para compactar");

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File arquivoSelecionado = fileChooser.getSelectedFile();
            
            // Pergunta onde salvar o arquivo compactado
            fileChooser.setDialogTitle("Salvar arquivo compactado como...");
            fileChooser.setSelectedFile(new File(arquivoSelecionado.getAbsolutePath() + ".gz"));
            int saveSelection = fileChooser.showSaveDialog(null);
            
            if (saveSelection == JFileChooser.APPROVE_OPTION) {
                File arquivoDestino = fileChooser.getSelectedFile();
                
                try {
                    compactarArquivo(arquivoSelecionado, arquivoDestino);
                    JOptionPane.showMessageDialog(null, 
                        "Arquivo compactado com sucesso!\n" +
                        "Origem: " + arquivoSelecionado.getAbsolutePath() + "\n" +
                        "Destino: " + arquivoDestino.getAbsolutePath(),
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, 
                        "Erro ao compactar arquivo: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, 
                "Nenhum arquivo selecionado.", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static void compactarArquivo(File arquivoOrigem, File arquivoDestino) throws IOException {
        try (FileInputStream fis = new FileInputStream(arquivoOrigem);
             FileOutputStream fos = new FileOutputStream(arquivoDestino);
             GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {
            
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                gzipOS.write(buffer, 0, len);
            }
        }
    }
}