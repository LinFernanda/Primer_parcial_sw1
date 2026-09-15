@echo off
echo ===================================================================
echo   CASE Platform - Lanzador Integrado de Servicios
echo ===================================================================
echo Iniciando Backend Spring Boot en nueva ventana...
start "CASE Platform - Backend" cmd /k "cd /d %~dp0..\backend && mvn spring-boot:run"

echo Esperando 5 segundos para que la infraestructura inicialice...
timeout /t 5 /nobreak >nul

echo Iniciando Frontend React en nueva ventana...
start "CASE Platform - Frontend" cmd /k "cd /d %~dp0..\frontend && npm run dev"

echo.
echo Todos los servicios han sido lanzados.
echo - Backend: http://localhost:8080/api/v1/health
echo - Frontend: http://localhost:5173
echo.
pause
