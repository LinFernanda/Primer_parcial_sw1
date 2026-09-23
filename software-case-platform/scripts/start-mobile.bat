@echo off
echo ===================================================
echo   Iniciando Aplicacion Movil en Celular (Flutter)
echo ===================================================
set JAVA_TOOL_OPTIONS=
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
set "PATH=%JAVA_HOME%\bin;C:\Users\Lin Acosta\AppData\Local\Android\sdk\platform-tools;%PATH%"

cd /d "%~dp0..\mobile"
flutter run
pause
