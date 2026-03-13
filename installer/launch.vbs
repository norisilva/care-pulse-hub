' CarePulse Hub - Launcher Silencioso
' Objetivo: Rodar o backend em segundo plano (sem janela de console) e abrir o navegador.

Set WshShell = CreateObject("WScript.Shell")
Set FSO = CreateObject("Scripting.FileSystemObject")

' Caminho base do aplicativo
AppPath = FSO.GetParentFolderName(WScript.ScriptFullName)

' Configuracoes
JAR_FILE = AppPath & "\backend\target\backend-0.1.0-SNAPSHOT.jar"
JRE_JAVA = AppPath & "\jre\bin\javaw.exe"
FRONTEND_INDEX = AppPath & "\frontend\index.html"
PORT = "8765"

' 1. Verificar se ja esta rodando na porta 8765
' Usamos um comando auxiliar para checar a porta
CheckCmd = "cmd /c netstat -aon | findstr LISTENING | findstr :" & PORT
IsRunning = WshShell.Run(CheckCmd, 0, True)

If IsRunning = 0 Then
    ' 2. Se ja estiver rodando, apenas abre o navegador
    WshShell.Run "cmd /c start """" """ & FRONTEND_INDEX & """", 0, False
Else
    ' 3. Se nao estiver rodando, inicia o backend e aguarda
    ' Comando para rodar o javaw (sem console) com o JAR
    ' Importante: Se o JRE embutido nao existir por algum motivo de erro na build, tentamos o sistema
    If FSO.FileExists(JRE_JAVA) Then
        JavaCmd = """" & JRE_JAVA & """ -jar """ & JAR_FILE & """"
    Else
        JavaCmd = "javaw -jar """ & JAR_FILE & """"
    End If

    ' Inicia o motor em segundo plano (0 = oculto, False = nao espera terminar)
    WshShell.Run JavaCmd, 0, False

    ' Aguarda o servidor subir antes de abrir o navegador
    WScript.Sleep 8000

    ' 4. Abre o navegador no frontend local
    WshShell.Run "cmd /c start """" """ & FRONTEND_INDEX & """", 0, False
End If
