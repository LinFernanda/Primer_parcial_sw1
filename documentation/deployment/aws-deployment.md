# GUÍA DE DESPLIEGUE EN PRODUCCIÓN - AMAZON WEB SERVICES (AWS)

## Plataforma CASE Colaborativa Inteligente para Diseño UML y Generación de Software

---

## 1. Visión General de la Infraestructura Cloud

La plataforma CASE está diseñada para operar como una solución cloud-native distribuida, resiliente, de alta disponibilidad y segura en **Amazon Web Services (AWS)**. El sistema desacopla el procesamiento de API, la persistencia relacional, la colaboración en tiempo real y el almacenamiento masivo de diagramas y binarios generados.

```mermaid
flowchart TD
    Users["Clientes Web / Móviles"] --> Route53["Amazon Route 53 (DNS / SSL)"]
    Route53 --> CloudFront["Amazon CloudFront (CDN)"]
    
    subgraph VPC ["Amazon VPC (10.0.0.0/16) - Multi-AZ"]
        subgraph PublicSubnets ["Subredes Públicas"]
            ALB["Application Load Balancer (ALB) :80 / :443"]
            NAT["NAT Gateway"]
        end
        
        subgraph PrivateSubnetsApp ["Subredes Privadas (Aplicación)"]
            ECS1["ECS Fargate Task 1\nBackend Spring Boot 3 (:8080)"]
            ECS2["ECS Fargate Task 2\nBackend Spring Boot 3 (:8080)"]
        end
        
        subgraph PrivateSubnetsData ["Subredes Privadas (Datos)"]
            RDS_Master["Amazon RDS PostgreSQL 16\nMulti-AZ (db.t4g.micro / gp3)"]
        end
    end
    
    CloudFront -->|Assets Estáticos| S3_Frontend["Amazon S3\nFrontend React SPA"]
    CloudFront -->|/api/* y /ws/*| ALB
    ALB --> ECS1
    ALB --> ECS2
    ECS1 --> RDS_Master
    ECS2 --> RDS_Master
    ECS1 --> S3_Assets["Amazon S3\nDiagramas, XMI y ZIPs"]
    ECS2 --> S3_Assets
    ECS1 -.-> CloudWatch["Amazon CloudWatch\nLogs y Alarmas"]
    ECS2 -.-> CloudWatch
```

---

## 2. Servicios de AWS Utilizados

| Servicio AWS | Propósito en la Plataforma CASE | Nivel de Red / Seguridad |
| :--- | :--- | :--- |
| **Amazon VPC** | Aislamiento de red con subredes públicas y privadas en dos Zonas de Disponibilidad (*us-east-1a*, *us-east-1b*). | CIDR `10.0.0.0/16`. Tráfico interno cifrado. |
| **AWS Application Load Balancer (ALB)** | Balanceo de carga HTTP/HTTPS, terminación SSL/TLS (certificado ACM), soporte para WebSockets STOMP y health checks. | Subredes públicas. Security Group restringido a puertos 80/443. |
| **Amazon ECS (Fargate)** | Ejecución de contenedores Docker del Backend Spring Boot 3 (Java 21) sin gestión de servidores físicos. Auto-scaling horizontal. | Subredes privadas sin IP pública. |
| **Amazon RDS (PostgreSQL 16)** | Base de datos relacional para usuarios, proyectos, modelos UML, entidades, relaciones, historial y auditoría. | Subred privada exclusiva para base de datos. Cifrado en reposo con KMS. Backups automáticos de 7 días. |
| **Amazon S3 (Simple Storage Service)** | Almacenamiento seguro de imágenes escaneadas de diagramas, exportaciones XML/XMI y archivos ZIP con código fuente generado. | Bucket privado con bloqueo de acceso público, versionado habilitado y cifrado del lado del servidor SSE-AES256. |
| **Amazon CloudFront** | Red de distribución de contenido (CDN) global para servir el Frontend React SPA con latencia ultra baja y compresión gzip/brotli. | Integración con AWS Certificate Manager (ACM) y encabezados de seguridad HSTS. |
| **Amazon CloudWatch** | Centralización de logs de aplicación en formato JSON estructurado, métricas de CPU/RAM y alarmas de salud del sistema. | Retención de logs configurable (30 días en producción). |
| **Amazon Route 53** | Gestión de DNS y enrutamiento con conmutación por error ante desastres. | Alias directo hacia CloudFront y ALB. |

---

## 3. Matriz de Variables de Entorno de Producción

Para el despliegue del Backend Spring Boot en Amazon ECS / Fargate, se configuran las siguientes variables de entorno seguras (gestionadas mediante **AWS Systems Manager Parameter Store** o **AWS Secrets Manager**):

```bash
# Perfil y Servidor
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# Base de datos Amazon RDS PostgreSQL
AWS_RDS_ENDPOINT=case-platform-postgres-production.xxxxxx.us-east-1.rds.amazonaws.com
DB_PORT=5432
DB_NAME=case_platform_db
DB_USER=case_admin
DB_PASSWORD=SuperSecureProductionPassword2026!
DB_SSL_MODE=require

# Seguridad y Autenticación JWT
JWT_SECRET=ProdSuperSecretKeyCasePlatform2026SecureHex64BytesLongRequiredBySecurityGuidelines
JWT_EXPIRATION_MS=28800000

# CORS Restrictivo
CORS_ALLOWED_ORIGINS=https://caseplatform.com,https://app.caseplatform.com

# Almacenamiento AWS S3
AWS_REGION=us-east-1
AWS_S3_BUCKET_NAME=case-platform-production-storage
AWS_ACCESS_KEY_ID=AKIAIOSFODNN7EXAMPLE
AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY

# Inteligencia Artificial
AI_OPENAI_API_KEY=sk-proj-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
AI_OPENAI_MODEL=gpt-4o-mini
```

---

## 4. Proceso de Despliegue Paso a Paso

### Paso 1: Aprovisionamiento de Infraestructura con Terraform
1. Ubicarse en el directorio de Terraform:
   ```bash
   cd infrastructure/aws/terraform
   ```
2. Inicializar los proveedores de Terraform:
   ```bash
   terraform init
   ```
3. Validar y planificar la infraestructura:
   ```bash
   terraform plan -out=tfplan.binary
   ```
4. Aplicar los cambios en la cuenta de AWS:
   ```bash
   terraform apply tfplan.binary
   ```

### Paso 2: Construcción y Publicación de Contenedores en Amazon ECR
1. Autenticar Docker con el registro ECR:
   ```bash
   aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com
   ```
2. Compilar y publicar la imagen del Backend:
   ```bash
   cd software-case-platform/backend
   docker build -t <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/case-platform-backend:latest .
   docker push <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/case-platform-backend:latest
   ```
3. Compilar y publicar la imagen del Frontend:
   ```bash
   cd ../frontend
   docker build -t <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/case-platform-frontend:latest .
   docker push <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/case-platform-frontend:latest
   ```

### Paso 3: Inicialización de la Base de Datos RDS
1. Conectar mediante túnel seguro (Bastion Host o AWS SSM Session Manager):
   ```bash
   psql -h <RDS_ENDPOINT> -U case_admin -d case_platform_db -f software-case-platform/scripts/init-db.sql
   ```

### Paso 4: Despliegue Continuo con GitHub Actions (CI/CD)
El flujo en [`.github/workflows/ci-cd.yml`](file:///C:/Users/Lin%20Acosta/Documents/SOFTWARE%201/Primer%20parcial%20S/.github/workflows/ci-cd.yml) automatiza:
1. Ejecución de pruebas unitarias e integración en las 3 plataformas:
   - Backend Maven (Java 21): 115 tests aprobados.
   - Frontend Vitest (React 18): 48 tests aprobados.
   - Mobile Flutter (Dart 3.5): 38 tests aprobados.
2. Construcción de imágenes Docker multi-stage.
3. Despliegue automático a Amazon ECS Fargate y sincronización de assets estáticos con invalidación de caché en CloudFront.

---

## 5. Monitoreo, Métricas y Alarmas CloudWatch

1. **Endpoints de Salud y Diagnóstico**:
   - Healthcheck para ALB: `GET /actuator/health` (Respuesta `{"status":"UP"}`).
   - Métricas de Sistema: `GET /actuator/metrics` y `GET /actuator/prometheus`.
2. **Grupo de Logs**:
   - `/ecs/case-platform-backend-production` con retención de 30 días y formato JSON estructurado.
3. **Alarmas Automáticas**:
   - **Alarma de CPU**: Se activa si el uso de CPU supera el 80% durante 5 minutos consecutivos, escalando instancias Fargate automáticamente.
   - **Alarma de Tasa de Errores 5XX**: Notificación SNS inmediata al equipo de ingeniería ante degradación del servicio.

---

## 6. Procedimientos de Respaldo y Recuperación ante Desastres (Disaster Recovery)

- **Amazon RDS PostgreSQL**: Instantáneas automáticas diarias a las 03:00 UTC retenidas durante 7 días, con capacidad de recuperación punto en el tiempo (*Point-In-Time-Recovery - PITR*).
- **Scripts de Respaldo Manual/Programado**:
  - Respaldo: [`software-case-platform/scripts/aws-backup-db.bat`](file:///C:/Users/Lin%20Acosta/Documents/SOFTWARE%201/Primer%20parcial%20S/software-case-platform/scripts/aws-backup-db.bat) o `aws-backup-db.sh`.
  - Restauración: [`software-case-platform/scripts/aws-restore-db.bat`](file:///C:/Users/Lin%20Acosta/Documents/SOFTWARE%201/Primer%20parcial%20S/software-case-platform/scripts/aws-restore-db.bat) o `aws-restore-db.sh`.
