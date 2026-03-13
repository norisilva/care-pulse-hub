@echo off
setlocal
title CarePulse Hub - Encerrando

echo ==========================================
echo       Encerrando o CarePulse Hub...
echo ==========================================
echo.

:: 1. Tentar matar pelo PID da porta 8765 (preciso)
set KILLED=0
for /f "tokens=5" %%a in ('netstat -aon ^| findstr "LISTENING" ^| findstr :8765') do (
    taskkill /f /pid %%a >nul 2>&1
    set KILLED=1
)

:: 2. Tentar matar qualquer processo javaw.exe rodando da nossa pasta (agressivo/seguro)
:: Isso garante que se o Java travou ou nao abriu a porta, ele ainda seja fechado.
powershell -Command "Get-Process javaw,java -ErrorAction SilentlyContinue | Where-Object { $_.Path -like '*CarePulse Hub*' } | Stop-Process -Force" >nul 2>&1
if %errorlevel% equ 0 set KILLED=1

if %KILLED%==1 (
    echo Feito! O motor do CarePulse foi encerrado com sucesso.
    echo Voce ja pode fechar o navegador.
) else (
    echo O CarePulse Hub nao parecia estar rodando.
)
echo.
if "%1"=="/silent" ( exit /b 0 )
pause
