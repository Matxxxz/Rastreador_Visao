Sistema de Visão e Telemetria (Arquitetura Poliglota)

Este repositório contém o desenvolvimento de um sistema de Visão Computacional e Telemetria construído como portfólio técnico e iniciação para o núcleo de robótica Pequi Mecânico (UFG).

🏗️ Arquitetura do Projeto

O projeto utiliza uma arquitetura de separação de responsabilidades (Mono-repo):

/vision_python: Motor de visão computacional (Backend). Escrito em Python com OpenCV, responsável por capturar o vídeo, isolar o objeto por processamento de cor (HSV), calcular o centroide e transmitir as coordenadas em tempo real via rede local (Socket UDP).

/control_java: [Em Desenvolvimento] Interface e regras de negócio (Frontend). Escrito em Java, responsável por escutar a porta de rede, tratar o JSON recebido e exibir o painel de telemetria.

🚀 Como rodar o Motor de Visão (Python)

Navegue até a pasta /vision_python.

Instale as dependências executando: pip install -r requirements.txt

Crie um arquivo config.py e adicione a URL da sua câmera IP na variável IP_CAM. Caso o arquivo não exista, o sistema usará a webcam padrão (0) como fallback de segurança.

Execute o arquivo main.py.

🛠️ Tecnologias Utilizadas

Python 3.10

OpenCV (Filtro Passa-Baixa, Thresholding, Momentos de Hu)

NumPy (Manipulação de Matrizes)

Sockets UDP (Comunicação Interprocessos)