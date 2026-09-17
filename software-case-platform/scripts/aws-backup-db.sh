#!/usr/bin/env bash
# ==============================================================================
# FASE 13: BACKUP AUTOMATIZADO POSTGRESQL PARA AWS RDS Y LINUX
# ==============================================================================
set -e

DB_HOST="${AWS_RDS_ENDPOINT:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-case_platform_db}"
DB_USER="${DB_USER:-postgres}"
S3_BUCKET="${AWS_S3_BUCKET_NAME:-case-platform-production-storage}"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_DIR="$(dirname "$0")/../backups"
BACKUP_FILE="${BACKUP_DIR}/backup_${DB_NAME}_${TIMESTAMP}.sql.gz"

mkdir -p "${BACKUP_DIR}"

echo "Iniciando respaldo de ${DB_NAME} en ${DB_HOST}:${DB_PORT}..."
pg_dump -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -F c -b -v -f "${BACKUP_FILE}"
echo "[OK] Respaldo generado: ${BACKUP_FILE}"

if command -v aws &> /dev/null; then
    echo "[AWS] Subiendo a s3://${S3_BUCKET}/database-backups/..."
    aws s3 cp "${BACKUP_FILE}" "s3://${S3_BUCKET}/database-backups/" --sse AES256
    echo "[AWS OK] Subida a S3 completada."
fi
