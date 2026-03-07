#!/bin/bash

echo "=========================================="
echo "Habilitando inicialização automática"
echo "        do CarePulse Hub (Linux)      "
echo "=========================================="
echo ""

AUTOSTART_DIR="$HOME/.config/autostart"
DESKTOP_FILE="$AUTOSTART_DIR/carepulse-hub.desktop"

# Store the absolute path to the JAR at registration time
JAR_PATH="$(cd "$(dirname "$0")" && pwd)/backend/target/backend-0.0.1-SNAPSHOT.jar"

# Verify the JAR exists before registering
if [ ! -f "$JAR_PATH" ]; then
    echo "[ERRO] Arquivo do motor nao encontrado em:"
    echo "$JAR_PATH"
    echo "Compile o backend antes de ativar o autostart: cd backend && ./mvnw package -DskipTests"
    exit 1
fi

mkdir -p "$AUTOSTART_DIR"

cat <<EOF > "$DESKTOP_FILE"
[Desktop Entry]
Type=Application
Exec=java -jar $JAR_PATH
Hidden=false
NoDisplay=false
X-GNOME-Autostart-enabled=true
Name=CarePulse Hub
Comment=Start CarePulse Hub background engine
EOF

if [ -f "$DESKTOP_FILE" ]; then
    echo "[SUCESSO] O CarePulse Hub iniciará com o sistema."
    echo "O motor ficará rodando em segundo plano na porta 8765."
    echo ""
    echo "Para remover, execute: sh disable-autostart.sh"
    echo "Ou delete manualmente: $DESKTOP_FILE"
else
    echo "[ERRO] Nao foi possivel criar o arquivo de autostart."
fi
echo ""
