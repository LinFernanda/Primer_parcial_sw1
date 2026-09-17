@echo off
REM ==============================================================================
REM FASE 13: SCRIPT DE RESTAURACIÓN DE BASE DE DATOS (AWS RDS / POSTGRESQL LOCAL)
REM Restaura un archivo dump con pg_restore hacia la base de datos de producción
REM ==============================================================================

setlocal enabledelayedexpansion

set BACKUP_FILE=%~1
if "%BACKUP_FILE%"=="" (
    echo [ERROR] Debe especificar la ruta del archivo de respaldo como primer parametro.
    echo Ejemplo: aws-restore-db.bat ..\backups\backup_case_platform_db_20260917.sql.gz
    exit /b 1
)

if not exist "%BACKUP_FILE%" (
    echo [ERROR] El archivo especificado no existe: %BACKUP_FILE%
    exit /b 1
)

set DB_HOST=%DB_HOST%
if "%DB_HOST%"=="" set DB_HOST=localhost

set DB_PORT=%DB_PORT%
if "%DB_PORT%"=="" set DB_PORT=5432

set DB_NAME=%DB_NAME%
if "%DB_NAME%"=="" set DB_NAME=case_platform_db

set DB_USER=%DB_USER%
if "%DB_USER%"=="" set DB_USER=postgres

echo =====================================================================
echo   Iniciando restauracion en PostgreSQL
echo   Host: %DB_HOST%:%DB_PORT% | BD: %DB_NAME% | Archivo: %BACKUP_FILE%
echo =====================================================================

pg_restore -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% --clean --if-exists -v "%BACKUP_FILE%"

if %ERRORLEVEL% equ 0 (
    echo [OK] Base de datos restaurada con exito en %DB_NAME%.
) else (
    echo [AVISO] pg_restore finalizo con advertencias o errores menores. Revise los registros.
)

endlocal
