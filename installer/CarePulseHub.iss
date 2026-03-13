; CarePulse Hub - Script do Instalador (Inno Setup 6)
; -----------------------------------------------

#define MyAppName "CarePulse Hub"
#define MyAppVersion "0.1.0"
#define MyAppPublisher "CarePulse"
#define MyAppURL "https://github.com/norisilva/care-pulse-hub"
#define MyAppExeName "launch.vbs"

[Setup]
; Identificador unico (Gerado para este projeto)
AppId={{E0B2D405-648A-4A05-AF9B-4D88FE5336E2}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={localappdata}\{#MyAppName}
DisableProgramGroupPage=yes
; Icone do Instalador
SetupIconFile=app_icon.ico
Compression=lzma
SolidCompression=yes
WizardStyle=modern
; Nao requer admin (instala no LocalAppData)
PrivilegesRequired=lowest
OutputDir=output
OutputBaseFilename=CarePulseHub_Setup

[Languages]
Name: "brazilianportuguese"; MessagesFile: "compiler:Languages\BrazilianPortuguese.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: "autostart"; Description: "Iniciar automaticamente com o Windows"; GroupDescription: "Configuracoes:"; Flags: unchecked
Name: "models"; Description: "Baixar modelos de IA (Deep Mode - requer internet)"; GroupDescription: "Opcional:"; Flags: unchecked

[Files]
; Launcher e Arquivos base
Source: "launch.vbs"; DestDir: "{app}"; Flags: ignoreversion
Source: "app_icon.ico"; DestDir: "{app}"; Flags: ignoreversion
; Backend e JAR (Ajuste o caminho se o nome do JAR mudar)
Source: "..\backend\target\backend-0.1.0-SNAPSHOT.jar"; DestDir: "{app}\backend\target"; Flags: ignoreversion
; Frontend
Source: "..\frontend\*"; DestDir: "{app}\frontend"; Flags: ignoreversion recursesubdirs createallsubdirs
; Scripts de utilidade
Source: "..\stop.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\enable-autostart.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\disable-autostart.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\scripts\download-models.bat"; DestDir: "{app}"; Flags: ignoreversion
; Dicionarios e Recursos
Source: "..\backend\src\main\resources\neutralization\*"; DestDir: "{app}\backend\src\main\resources\neutralization"; Flags: ignoreversion recursesubdirs createallsubdirs
; JRE Embutido (Deve ser extraido na pasta 'jre' antes de compilar)
Source: "jre\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\app_icon.ico"
Name: "{autoprograms}\Configuracoes\Parar CarePulse Hub"; Filename: "{app}\stop.bat"; IconFilename: "{app}\app_icon.ico"
Name: "{autoprograms}\Configuracoes\Habilitar Inicio Automatico"; Filename: "{app}\enable-autostart.bat"; IconFilename: "{app}\app_icon.ico"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon; IconFilename: "{app}\app_icon.ico"

[Run]
; Rodar o script de autostart se o usuario selecionou
Filename: "{app}\enable-autostart.bat"; Parameters: "/silent"; Tasks: autostart; Flags: runhidden
; Rodar o script de download de modelos se o usuario selecionou
Filename: "{app}\download-models.bat"; Tasks: models; Flags: runhidden
; Abrir o aplicativo apos instalar
Filename: "wscript.exe"; Parameters: """{app}\{#MyAppExeName}"""; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[UninstallRun]
; Remediar autostart PRIMEIRO
Filename: "{app}\disable-autostart.bat"; Parameters: "/silent"; Flags: runhidden
; Matar o processo ANTES de tentar remover arquivos
Filename: "{app}\stop.bat"; Parameters: "/silent"; Flags: runhidden
