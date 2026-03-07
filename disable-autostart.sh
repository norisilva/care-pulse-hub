#!/bin/bash
DESKTOP_FILE="$HOME/.config/autostart/carepulse-hub.desktop"

if [ -f "$DESKTOP_FILE" ]; then
    rm "$DESKTOP_FILE"
    echo "[SUCESSO] Inicialização automática removida com sucesso."
    echo "O CarePulse Hub não vai mais iniciar com o sistema."
else
    echo "O CarePulse Hub não estava configurado para inicialização automática."
fi
