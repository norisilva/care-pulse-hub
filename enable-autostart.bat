@echo off
setlocal
title CarePulse Hub - Autostart Setup

echo ==========================================
echo Habilitando inicializacao automatica 
echo      do CarePulse Hub no Windows
echo ==========================================
echo.

set STARTUP_DIR=%APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup
set VBS_FILE=%STARTUP_DIR%\CarePulseHub.vbs
set JAR_PATH=%~dp0backend\target\backend-0.1.0-SNAPSHOT.jar

:: Remove old version if exists
if exist "%VBS_FILE%" del /f "%VBS_FILE%"

:: Create VBS script to run Java silently in the background
(
    echo Set WshShell = CreateObject^("WScript.Shell"^)
    echo WshShell.Run "javaw -jar ""%JAR_PATH%""", 0, False
) > "%VBS_FILE%"

if exist "%VBS_FILE%" (
    echo [SUCESSO] O CarePulse Hub vai iniciar automaticamente com o Windows.
    echo O motor ficara rodando invisivel em segundo plano na porta 8765.
    echo.
    echo Para remover essa configuracao, rode o arquivo "disable-autostart.bat"
    echo ou delete manualmente:
    echo %VBS_FILE%
) else (
    echo [ERRO] Nao foi possivel criar o arquivo de inicializacao automatica.
    echo Verifique suas permissoes de usuario.
)
echo.
if "%1"=="/silent" ( exit /b 0 )
pause
