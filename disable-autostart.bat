@echo off
setlocal
title CarePulse Hub - Remover Autostart

set STARTUP_DIR=%APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup
set VBS_FILE=%STARTUP_DIR%\CarePulseHub.vbs

if exist "%VBS_FILE%" (
    del /f "%VBS_FILE%"
    echo [SUCESSO] Inicializacao automatica removida com sucesso.
    echo O CarePulse Hub nao vai mais iniciar com o Windows.
) else (
    echo O CarePulse Hub nao estava configurado para inicializacao automatica.
)
echo.
pause
