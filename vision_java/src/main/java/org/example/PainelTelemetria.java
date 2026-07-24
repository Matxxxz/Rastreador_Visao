package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class PainelTelemetria extends JFrame {
    private int xCoord = 0;
    private int yCoord = 0;
    private boolean alvoDetectado = false;

    private final JLabel lblStatus = new JLabel("Status: Aguardando Alvo...");
    private final JLabel lblPosicao = new JLabel("Posição: X=0 | Y=0");
    private final RadarPanel radarPanel = new RadarPanel();

    public PainelTelemetria() {
        setTitle("Rastreador de Visão - Telemetria Java");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Painel superior com informações textuais
        JPanel painelInfo = new JPanel(new GridLayout(1, 2));
        lblStatus.setFont(new Font("Arial", Font.BOLD, 14));
        lblStatus.setForeground(Color.RED);
        lblPosicao.setFont(new Font("Arial", Font.PLAIN, 14));

        painelInfo.add(lblStatus);
        painelInfo.add(lblPosicao);
        add(painelInfo, BorderLayout.NORTH);

        // Painel central (Radar 2D)
        add(radarPanel, BorderLayout.CENTER);

        // Inicia a escuta UDP em uma Thread separada (Background) para evitar qualquer lag na UI
        new Thread(this::ouvirRedeUDP).start();
    }

    private void ouvirRedeUDP() {
        int porta = 5005;
        try (DatagramSocket socket = new DatagramSocket(porta)) {
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
                socket.receive(pacote);

                String jsonRecebido = new String(pacote.getData(), 0, pacote.getLength());
                atualizarDados(jsonRecebido);
            }
        } catch (IOException e) {
            System.err.println("[ERRO NO SOCKET] " + e.getMessage());
        }
    }

    private void atualizarDados(String json) {
        // Mostra no console o que o Java realmente recebeu via UDP
        System.out.println("[RECEBIDO DA REDE]: " + json);

        try {
            // Verifica se o alvo foi encontrado de forma mais ampla
            alvoDetectado = json.contains("true") && !json.contains("\"alvo_encontrado\": false");

            if (json.contains("\"x\":") && json.contains("\"y\":")) {
                // Parsing mais robusto aceitando qualquer delimitador de fim de campo (, ou })
                String xStr = json.split("\"x\":")[1].split("[,}]")[0].trim();
                String yStr = json.split("\"y\":")[1].split("[,}]")[0].trim();

                xCoord = Integer.parseInt(xStr);
                yCoord = Integer.parseInt(yStr);
            }
        } catch (Exception e) {
            System.err.println("[FALHA NO PARSER JSON]: " + e.getMessage() + " | JSON bruto: " + json);
        }

        // Atualiza a interface gráfica na Thread segura do Swing
        SwingUtilities.invokeLater(() -> {
            if (alvoDetectado) {
                lblStatus.setText("Status: ALVO DETECTADO");
                lblStatus.setForeground(new Color(0, 150, 0));
            } else {
                lblStatus.setText("Status: Buscando Alvo...");
                lblStatus.setForeground(Color.RED);
            }
            lblPosicao.setText(String.format("Posição: X=%d | Y=%d", xCoord, yCoord));
            radarPanel.repaint(); // Força o radar a redesenhar
        });
    }

    // Componente interno para desenhar o radar 2D
    private class RadarPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());

            // Desenha grades do radar
            g.setColor(new Color(0, 100, 0));
            g.drawRect(20, 20, getWidth() - 40, getHeight() - 40);
            g.drawLine(getWidth() / 2, 20, getWidth() / 2, getHeight() - 20);
            g.drawLine(20, getHeight() / 2, getWidth() - 20, getHeight() / 2);

            // Desenha o alvo se ele for encontrado
            if (alvoDetectado) {
                // Mapeia coordenadas da câmera para a tela do painel
                int telaX = 40 + (xCoord * (getWidth() - 80) / 640);
                int telaY = 40 + (yCoord * (getHeight() - 80) / 480);

                g.setColor(Color.CYAN);
                g.fillOval(telaX - 8, telaY - 8, 16, 16);
                g.setColor(Color.WHITE);
                g.drawOval(telaX - 14, telaY - 14, 28, 28);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PainelTelemetria().setVisible(true));
    }
}
