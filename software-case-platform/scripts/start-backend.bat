@echo off
echo ===================================================
echo   Iniciando Backend Spring Boot (Java 21)
echo ===================================================
cd /d "%~dp0..\backend"
mvn spring-boot:run
pause
