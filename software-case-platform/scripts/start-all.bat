@echo off
echo ===================================================================
echo   CASE Platform - Lanzador Integrado de Servicios
echo ===================================================================
echo Iniciando Backend Spring Boot en nueva ventana...
start "CASE Platform - Backend" cmd /k "call "%~dp0start-backend.bat""

echo Esperando 5 segundos para que la infraestructura inicialice...
timeout /t 5 /nobreak >nul

echo Iniciando Frontend React en nueva ventana...
start "CASE Platform - Frontend" cmd /k "cd /d %~dp0..\frontend && npm run dev"

echo Esperando 3 segundos y abriendo navegador...
timeout /t 3 /nobreak >nul
start http://localhost:5173

echo.
echo ===================================================================
echo   Todos los servicios han sido lanzados con exito!
echo ===================================================================
echo   Frontend Web:  http://localhost:5173
echo   Backend API:   http://localhost:8080/api/v1/health
echo   Swagger UI:    http://localhost:8080/swagger-ui.html
echo.
echo   Credenciales:
echo   - Correo:      admin@caseplatform.com  (o ingeniero@caseplatform.com)
echo   - Password:    Admin123!               (o Ingeniero123!)
echo ===================================================================
echo.
pause
