@echo off
setlocal
title CarePulse Hub - Encerrando

echo ==========================================
echo       Encerrando o CarePulse Hub...
echo ==========================================
echo.

:: Find the PID of the process listening on port 8765 and kill it
set KILLED=0
for /f "tokens=5" %%a in ('netstat -aon ^| findstr "LISTENING" ^| findstr :8765') do (
    taskkill /f /pid %%a >nul 2>&1
    set KILLED=1
)

if %KILLED%==1 (
    echo Feito! O motor do CarePulse foi encerrado com sucesso.
    echo Voce ja pode fechar o navegador.
) else (
    echo O CarePulse Hub nao parecia estar rodando.
)
echo.
pause
