@echo off
REM ============================================================
REM CarePulse Hub — Download de Modelos OpenNLP (PT-BR)
REM Necessario apenas para habilitar o Deep Mode (Fase 2)
REM O sistema funciona normalmente sem estes modelos (Fast Mode)
REM ============================================================

set MODELS_DIR=backend\src\main\resources\models

REM Modelo oficial Apache OpenNLP — POS Tagger Portugues (Universal Dependencies)
REM Treinado com OpenNLP 2.5.4, corpus UD 2.16 (GSD)
set POS_MODEL_FILE=opennlp-pt-ud-gsd-pos-1.3-2.5.4.bin
set POS_MODEL_URL=https://downloads.apache.org/opennlp/models/ud-models-1.3/%POS_MODEL_FILE%

if exist "%MODELS_DIR%\%POS_MODEL_FILE%" (
    echo [OK] Modelo POS Tagger ja existe em %MODELS_DIR%\%POS_MODEL_FILE%
    echo      Para forcar re-download, apague o arquivo e rode novamente.
    exit /b 0
)

if not exist "%MODELS_DIR%" mkdir "%MODELS_DIR%"

echo ============================================
echo   CarePulse Hub — Download de Modelos NLP
echo ============================================
echo.
echo Baixando modelo POS Tagger PT-BR do Apache OpenNLP...
echo Fonte: %POS_MODEL_URL%
echo.

curl -L --fail "%POS_MODEL_URL%" -o "%MODELS_DIR%\%POS_MODEL_FILE%"

if %ERRORLEVEL% EQU 0 (
    if exist "%MODELS_DIR%\%POS_MODEL_FILE%" (
        echo.
        echo [OK] Modelo instalado com sucesso!
        echo      Arquivo:  %POS_MODEL_FILE%
        echo      Caminho:  %MODELS_DIR%\%POS_MODEL_FILE%
        echo.
        echo      Reinicie o CarePulse Hub para ativar o Deep Mode.
    ) else (
        goto :download_failed
    )
) else (
    goto :download_failed
)
exit /b 0

:download_failed
echo.
echo [ERRO] Falha no download.
echo.
echo   Baixe manualmente o arquivo e coloque em: %MODELS_DIR%\
echo.
echo   Pagina de modelos oficiais:
echo   https://opennlp.apache.org/models.html
exit /b 1
