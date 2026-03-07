#!/bin/bash
echo "Encerrando o CarePulse Hub..."
if command -v lsof &> /dev/null; then
    PID=$(lsof -Pi :8765 -sTCP:LISTEN -t)
    if [ -n "$PID" ]; then
        # SIGTERM first for graceful shutdown, Spring Boot needs time to flush
        kill -15 $PID
        sleep 2
        # If still running, force kill
        kill -9 $PID 2>/dev/null
        echo "Processo interno (PID $PID) encerrado com sucesso."
    else
        echo "O CarePulse Hub não parece estar rodando."
    fi
else
    echo "lsof não encontrado. Tentando método alternativo..."
    pkill -f "backend-0.0.1-SNAPSHOT.jar"
    echo "Comando enviado."
fi
