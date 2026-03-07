@echo off
setlocal
title CarePulse Hub - Lançamento Automático

echo ==========================================
echo       CarePulse Hub - Inicializacao
echo ==========================================
echo.

:: 1. Verificar se o Java esta instalado
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERRO] Java nao encontrado no sistema!
    echo O CarePulse Hub precisa do Java 21 ou superior para rodar o motor principal.
    echo.
    echo PASSO A PASSO PARA RESOLVER:
    echo 1. Acesse: https://adoptium.net/
    echo 2. Baixe e instale a versao mais recente LTS.
    echo 3. Reinicie seu computador ou este terminal e tente novamente.
    echo.
    pause
    exit /b
)

:: 2. Verificar se a porta 8765 ja esta em uso
netstat -ano | findstr "LISTENING" | findstr :8765 >nul 2>&1
if %errorlevel% equ 0 (
    echo [INFO] O CarePulse Hub ja esta rodando em segundo plano!
    echo Abrindo o navegador novamente...
    start "" "%~dp0frontend\index.html"
    echo.
    echo DICA: Para fechar o sistema completamente, rode o arquivo "stop.bat"
    echo.
    pause
    exit /b
)

:: 3. Criar pasta de dados se nao existir
if not exist "backend\data" mkdir backend\data

echo [1/3] Iniciando o Servidor Interno (Backend)...
echo O motor vai rodar em uma janela minimizada.
echo Se houver erro, uma nova janela aparecera com a mensagem.
echo.

:: 4. Iniciar o backend. 
:: Usamos CMD /K para que se der erro o usuario consiga ler antes da janela fechar.
start "CarePulse Motor" /min cmd /c "java -jar backend\target\backend-0.0.1-SNAPSHOT.jar || (echo. && echo [ERRO CRITICO] O motor parou inesperadamente! && pause)"

echo [2/3] Aguardando o sistema aquecer (10 segundos)...
timeout /t 10 /nobreak >nul

echo [3/3] Abrindo a interface no seu navegador padrao...
start "" "%~dp0frontend\index.html"

echo.
echo ==========================================
echo Sucesso! O seu navegador deve ter aberto.
echo Se aparecer "Servidor Offline" no canto inferior,
echo aguarde mais alguns segundos e de um F5.
echo.
echo Para fechar o sistema: rode o "stop.bat".
echo ==========================================
echo.
pause
