#!/bin/bash

echo "=========================================="
echo "      CarePulse Hub - Inicialização       "
echo "=========================================="
echo ""

# 1. Verificar se o Java está instalado
if ! command -v java &> /dev/null; then
    echo "[ERRO] Java não encontrado no sistema!"
    echo "O CarePulse Hub precisa do Java 21 ou superior para rodar o motor principal."
    echo ""
    echo "COMO RESOLVER (Ubuntu/Debian):"
    echo "sudo apt update && sudo apt install openjdk-21-jre"
    echo ""
    echo "COMO RESOLVER (Fedora):"
    echo "sudo dnf install java-21-openjdk"
    echo ""
    read -p "Pressione Enter para sair..."
    exit 1
fi

FILE_PATH="$(pwd)/frontend/index.html"

# 2. Verificar porta 8765
if command -v lsof &> /dev/null; then
    if lsof -Pi :8765 -sTCP:LISTEN -t >/dev/null ; then
        echo "[INFO] O CarePulse Hub já está rodando em background!"
        echo "Abrindo o navegador novamente..."
        if command -v xdg-open &> /dev/null; then
            xdg-open "$FILE_PATH"
        elif command -v open &> /dev/null; then
            open "$FILE_PATH"
        fi
        echo "Para fechar o sistema completamente, rode: sh stop.sh"
        exit 0
    fi
fi

# 3. Criar pasta de dados
mkdir -p backend/data

echo "[1/3] Iniciando o Servidor Interno (Backend)..."
# Roda em background
nohup java -jar backend/target/backend-0.0.1-SNAPSHOT.jar > backend_log.txt 2>&1 &
BACKEND_PID=$!

echo "[2/3] Aguardando o sistema aquecer (10 segundos)..."
sleep 10

echo "[3/3] Abrindo a interface no seu navegador padrão..."

if command -v xdg-open &> /dev/null; then
    xdg-open "$FILE_PATH"
elif command -v open &> /dev/null; then
    # Para macOS
    open "$FILE_PATH"
else
    echo "Não foi possível abrir o navegador automaticamente."
    echo "Por favor, abra o arquivo manualmente:"
    echo "$FILE_PATH"
fi

echo ""
echo "=========================================="
echo "Sistema rodando em background (PID: $BACKEND_PID)."
echo "Para fechar o sistema completamente, rode: sh stop.sh"
echo "=========================================="
echo ""
