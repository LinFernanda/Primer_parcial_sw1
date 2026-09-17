@echo off
REM ==============================================================================
REM FASE 13: SCRIPT DE RESPALDO AUTOMATIZADO DE BASE DE DATOS (AWS RDS / LOCAL)
REM Genera volcado pg_dump con timestamp, compresión y subida opcional a Amazon S3
REM ==============================================================================

setlocal enabledelayedexpansion

set DB_HOST=%DB_HOST%
if "%DB_HOST%"=="" set DB_HOST=localhost

set DB_PORT=%DB_PORT%
if "%DB_PORT%"=="" set DB_PORT=5432

set DB_NAME=%DB_NAME%
if "%DB_NAME%"=="" set DB_NAME=case_platform_db

set DB_USER=%DB_USER%
if "%DB_USER%"=="" set DB_USER=postgres

set S3_BUCKET=%AWS_S3_BUCKET_NAME%
if "%S3_BUCKET%"=="" set S3_BUCKET=case-platform-production-storage

set BACKUP_DIR=%~dp0..\backups
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value') do set datetime=%%I
set TIMESTAMP=%datetime:~0,8%_%datetime:~8,6%
set BACKUP_FILE=%BACKUP_DIR%\backup_%DB_NAME%_%TIMESTAMP%.sql.gz

echo =====================================================================
echo   Iniciando respaldo de base de datos PostgreSQL
echo   Host: %DB_HOST%:%DB_PORT% | BD: %DB_NAME% | Destino: %BACKUP_FILE%
echo =====================================================================

pg_dump -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -F c -b -v -f "%BACKUP_FILE%"

if %ERRORLEVEL% equ 0 (
    echo [OK] Respaldo generado exitosamente: %BACKUP_FILE%
    
    REM Subida opcional a AWS S3 si aws-cli esta disponible
    where aws >nul 2>nul
    if %ERRORLEVEL% equ 0 (
        echo [AWS] Subiendo respaldo cifrado a Amazon S3: s3://%S3_BUCKET%/database-backups/
        aws s3 cp "%BACKUP_FILE%" "s3://%S3_BUCKET%/database-backups/" --sse AES256
        if !ERRORLEVEL! equ 0 (
            echo [AWS OK] Respaldo almacenado en S3 con cifrado AES-256
        )
    )
) else (
    echo [ERROR] Fallo la ejecucion de pg_dump. Verifique credenciales y conectividad.
)

endlocal
