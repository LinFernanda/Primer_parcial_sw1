@echo off
echo ===================================================
echo   Iniciando Backend Spring Boot (Java 21)
echo ===================================================
if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot" (
    set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)
cd /d "%~dp0..\backend"
mvn spring-boot:run
pause
