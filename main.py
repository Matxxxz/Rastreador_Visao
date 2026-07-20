import cv2
import numpy as np
from collections import deque
import socket
import json

try:
    from config import IP_CAM
    FONTE_DE_VIDEO = IP_CAM
    print("Ambiente de Dev: Conectando à câmera externa via IP...")
except ImportError:
    FONTE_DE_VIDEO = 0
    print("Ambiente de Produção: Usando a webcam padrão (0)...")

cap = cv2.VideoCapture(FONTE_DE_VIDEO)

if not cap.isOpened():
    print("Erro: Falha ao acessar a fonte de vídeo.")
    exit()

# CONFIGURAÇÃO DA REDE
ENDERECO_JAVA = ('127.0.0.1', 5005)
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

# Memória para o rastro da trajetória (últimos 20 pontos)
historico_pontos = deque(maxlen=20)

print("Motor de Visão Iniciado. Transmitindo telemetria na porta 5005...")
print("Pressione 'q' na janela de vídeo para encerrar.")

while True:
    sucesso, frame = cap.read()
    if not sucesso:
        break

    # 1. Suavização (Filtro Passa-Baixa) para estabilizar o tremor
    frame_suavizado = cv2.GaussianBlur(frame, (11, 11), 0)
    hsv_frame = cv2.cvtColor(frame_suavizado, cv2.COLOR_BGR2HSV)

    # 2. Definição da Cor (Azul Balanceado)
    azul_claro = np.array([95, 80, 50])
    azul_escuro = np.array([128, 255, 255])

    # 3. Criação da Máscara e Extração de Contornos
    mascara = cv2.inRange(hsv_frame, azul_claro, azul_escuro)
    contornos, _ = cv2.findContours(mascara, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    # 4. Lógica de Rastreamento e Telemetria
    if contornos:
        maior_contorno = max(contornos, key=cv2.contourArea)

        if cv2.contourArea(maior_contorno) > 500:
            # Encontra o Centroide
            M = cv2.moments(maior_contorno)
            if M["m00"] != 0:
                cx = int(M["m10"] / M["m00"])
                cy = int(M["m01"] / M["m00"])

                # --- LÓGICA VISUAL ---
                # Adiciona o ponto atual na memória e desenha o centro
                historico_pontos.appendleft((cx, cy))
                cv2.circle(frame, (cx, cy), 5, (0, 0, 255), -1)

                # Desenha o rastro (linha conectando pontos antigos)
                for i in range(1, len(historico_pontos)):
                    if historico_pontos[i - 1] is None or historico_pontos[i] is None:
                        continue
                    cv2.line(frame, historico_pontos[i - 1], historico_pontos[i], (255, 0, 0), 2)

                # Desenha a caixa delimitadora
                x, y, largura, altura = cv2.boundingRect(maior_contorno)
                cv2.rectangle(frame, (x, y), (x + largura, y + altura), (0, 255, 0), 3)

                # --- LÓGICA DE REDE (ENVIO PARA O JAVA) ---
                dados_telemetria = {
                    "alvo_encontrado": True,
                    "x": cx,
                    "y": cy
                }
                mensagem_json = json.dumps(dados_telemetria)
                sock.sendto(mensagem_json.encode(), ENDERECO_JAVA)

                # Imprime no terminal para sabermos que está funcionando
                print(f"ENVIANDO: {mensagem_json}")

    else:
        # Se nenhum contorno azul for encontrado, envia coordenadas 0 para o Java
        dados_vazios = {"alvo_encontrado": False, "x": 0, "y": 0}
        sock.sendto(json.dumps(dados_vazios).encode(), ENDERECO_JAVA)

    # Mostra a tela final
    cv2.imshow('Motor de Visao (Backend)', frame)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()