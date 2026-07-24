package org.example;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReceptorTelemetria {

    private static final int PORTA = 5005;
    private static final int TAMANHO_BUFFER = 1024;

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   INICIANDO RECEPTOR JAVA TELEMETRIA   ");
        System.out.println("=========================================");

        try (DatagramSocket socket = new DatagramSocket(PORTA)) {
            System.out.println("[JAVA REDE] Servidor UDP escutando na porta " + PORTA + "...");
            byte[] buffer = new byte[TAMANHO_BUFFER];

            while (true) {
                DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);

                // Bloqueia e aguarda a chegada do próximo pacote do Python
                socket.receive(pacote);

                String mensagemRecebida = new String(pacote.getData(), 0, pacote.getLength());

                // Converte a String JSON para o objeto Java
                DadosTelemetria telemetria = JsonParserUtil.parse(mensagemRecebida);

                // Exibe os dados no console para confirmação
                System.out.println("[RECEBIDO] " + telemetria);
            }

        } catch (Exception e) {
            System.err.println("[ERRO CRÍTICO] Falha na comunicação UDP: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
