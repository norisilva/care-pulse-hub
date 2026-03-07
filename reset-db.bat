@echo off
setlocal
title CarePulse Hub - Reset Database

echo ==========================================
echo        AVISO: LIMPEZA DE DADOS
echo ==========================================
echo.
echo Este script vai apagar TODAS as configuracoes, 
echo grupos e relatorios salvos localmente.
echo.
set /p CONFIRM=Tem certeza que deseja continuar? (S/N): 

if /i "%CONFIRM%" neq "S" (
    echo Operacao cancelada.
    pause
    exit /b
)

:: Stop the engine if it is running
echo Parando o motor...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr "LISTENING" ^| findstr :8765') do (
    taskkill /f /pid %%a >nul 2>&1
)

:: Delete the data folder
if exist "backend\data" (
    rmdir /s /q "backend\data"
    echo [SUCESSO] Base de dados limpa com sucesso.
) else (
    echo A base de dados ja estava limpa ou nao foi encontrada.
)

echo.
echo Agora voce pode iniciar o sistema do zero com "start.bat"
echo.
pause
