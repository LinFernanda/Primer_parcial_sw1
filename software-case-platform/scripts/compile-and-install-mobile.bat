@echo off
echo ================================================================
echo   Compilando e Instalando APK en tu Celular Android (Samsung)
echo ================================================================

:: Limpiar opciones y configurar Java 21 LTS
set JAVA_TOOL_OPTIONS=
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
set "PATH=%JAVA_HOME%\bin;C:\Users\Lin Acosta\AppData\Local\Android\sdk\platform-tools;%PATH%"

cd /d "%~dp0..\mobile"

echo.
echo [1/3] Compilando APK de depuracion...
call flutter build apk --debug

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] La compilacion ha fallado.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [2/3] Instalando en el celular conectado...
adb install -r "build\app\outputs\flutter-apk\app-debug.apk"

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] No se pudo instalar. Verifica que el celular este conectado y desbloqueado.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [3/3] Abriendo la aplicacion en el celular...
adb shell am start -n com.caseplatform.mobile/.MainActivity

echo.
echo ================================================================
echo   PROCESO COMPLETADO CON EXITO
echo ================================================================
pause
