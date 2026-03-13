#!/bin/bash
# ============================================================
# CarePulse Hub — Download de Modelos OpenNLP (PT-BR)
# Necessário apenas para habilitar o Deep Mode (Fase 2)
# O sistema funciona normalmente sem estes modelos (Fast Mode)
# ============================================================

MODELS_DIR="backend/src/main/resources/models"

# Modelo oficial Apache OpenNLP — POS Tagger Português (Universal Dependencies)
# Treinado com OpenNLP 2.5.4, corpus UD 2.16 (GSD)
POS_MODEL_FILE="opennlp-pt-ud-gsd-pos-1.3-2.5.4.bin"
POS_MODEL_URL="https://downloads.apache.org/opennlp/models/ud-models-1.3/${POS_MODEL_FILE}"

if [ -f "$MODELS_DIR/$POS_MODEL_FILE" ]; then
    echo "✓ Modelo POS Tagger já existe em $MODELS_DIR/$POS_MODEL_FILE"
    echo "  Para forçar re-download, apague o arquivo e rode novamente."
    exit 0
fi

mkdir -p "$MODELS_DIR"

echo "============================================"
echo "  CarePulse Hub — Download de Modelos NLP"
echo "============================================"
echo ""
echo "Baixando modelo POS Tagger PT-BR do Apache OpenNLP..."
echo "Fonte: $POS_MODEL_URL"
echo ""

curl -L --fail --progress-bar \
    "$POS_MODEL_URL" \
    -o "$MODELS_DIR/$POS_MODEL_FILE"

if [ $? -eq 0 ] && [ -f "$MODELS_DIR/$POS_MODEL_FILE" ]; then
    SIZE=$(du -h "$MODELS_DIR/$POS_MODEL_FILE" | cut -f1)
    echo ""
    echo "✓ Modelo instalado com sucesso!"
    echo "  Arquivo:  $POS_MODEL_FILE"
    echo "  Caminho:  $MODELS_DIR/$POS_MODEL_FILE"
    echo "  Tamanho:  $SIZE"
    echo ""
    echo "  Reinicie o CarePulse Hub para ativar o Deep Mode."
else
    echo ""
    echo "✗ Falha no download."
    echo ""
    echo "  Baixe manualmente o arquivo e coloque em: $MODELS_DIR/"
    echo ""
    echo "  Página de modelos oficiais:"
    echo "  https://opennlp.apache.org/models.html"
    exit 1
fi
