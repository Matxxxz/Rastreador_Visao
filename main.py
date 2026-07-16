import cv2
import numpy as np

def atualizar_valores(valor):
    pass

FONTE_DE_VIDEO = 0
cap = cv2.VideoCapture(FONTE_DE_VIDEO)

if not cap.isOpened():
    print("Erro: Falha ao acessar a fonte de vídeo.")
    exit()

print("Modo de Calibragem iniciado! Ajuste as barras para isolar o objeto. Pressione 'q' para encerrar.")

#Cria a janela do painel antes de colocar as barras nela
cv2.namedWindow('Painel de Calibragem')
cv2.resizeWindow('Painel de Calibragem', 400, 300)

#Cria as 6 barras (H, S, V Mínimo e H, S, V Máximo)
#Sintaxe: cv2.createTrackbar('Nome da Barra', 'Nome da Janela', Valor_Inicial, Valor_Maximo, funcao)
cv2.createTrackbar('H Min', 'Painel de Calibragem', 95, 179, atualizar_valores)
cv2.createTrackbar('H Max', 'Painel de Calibragem', 128, 179, atualizar_valores)

cv2.createTrackbar('S Min', 'Painel de Calibragem', 80, 255, atualizar_valores)
cv2.createTrackbar('S Max', 'Painel de Calibragem', 255, 255, atualizar_valores)

cv2.createTrackbar('V Min', 'Painel de Calibragem', 50, 255, atualizar_valores)
cv2.createTrackbar('V Max', 'Painel de Calibragem', 255, 255, atualizar_valores)

while True:
    sucesso, frame = cap.read()
    if not sucesso:
        break

    hsv_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)

    #Lê o valor atual (em tempo real) de cada barra deslizante
    h_min = cv2.getTrackbarPos('H Min', 'Painel de Calibragem')
    h_max = cv2.getTrackbarPos('H Max', 'Painel de Calibragem')
    s_min = cv2.getTrackbarPos('S Min', 'Painel de Calibragem')
    s_max = cv2.getTrackbarPos('S Max', 'Painel de Calibragem')
    v_min = cv2.getTrackbarPos('V Min', 'Painel de Calibragem')
    v_max = cv2.getTrackbarPos('V Max', 'Painel de Calibragem')

    #Usa os valores lidos para montar as matrizes de limites
    cor_clara = np.array([h_min, s_min, v_min])
    cor_escura = np.array([h_max, s_max, v_max])

    #Cria a máscara usando os valores dinâmicos
    mascara = cv2.inRange(hsv_frame, cor_clara, cor_escura)

    contornos, _ = cv2.findContours(mascara, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    if contornos:
        maior_contorno = max(contornos, key=cv2.contourArea)

        if cv2.contourArea(maior_contorno) > 500:
            x, y, largura, altura = cv2.boundingRect(maior_contorno)
            cv2.rectangle(frame, (x, y), (x + largura, y + altura), (0, 255, 0), 3)
            cv2.putText(frame, "Alvo Detectado", (x, y - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 255, 0), 2)

    #Exibe as 3 janelas (Câmera Original, Máscara e o Painel de Calibragem já aberto)
    cv2.imshow('Sistema de Rastreamento de Cor', frame)
    cv2.imshow('Mascara Binaria', mascara)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()