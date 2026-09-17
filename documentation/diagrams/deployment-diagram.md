# DIAGRAMAS DE DESPLIEGUE Y ARQUITECTURA CLOUD (AWS)

## Plataforma CASE Inteligente Colaborativa

---

## 1. Diagrama de Despliegue Físico / Cloud (Mermaid)

El siguiente diagrama modela la topología de red, zonas de disponibilidad, servicios gestionados y flujos de tráfico en **Amazon Web Services**:

```mermaid
flowchart TB
    subgraph Internet ["Zona Externa (Internet)"]
        Browser["Navegador Web (React 18 SPA)\nhttps://app.caseplatform.com"]
        MobileApp["App Móvil Flutter\n(Android / Windows / Web)"]
    end

    subgraph EdgeServices ["Servicios Perimetrales Globales de AWS"]
        Route53["Amazon Route 53\nDNS Routing & Health Checks"]
        CloudFront["Amazon CloudFront CDN\nEdge Locations Globales"]
        ACM["AWS Certificate Manager (ACM)\nCertificados SSL/TLS Gestionados"]
        WAF["AWS WAF\nWeb Application Firewall"]
    end

    subgraph AWS_VPC ["Amazon VPC: 10.0.0.0/16 (us-east-1)"]
        subgraph PublicSubnets ["Subredes Públicas (Multi-AZ)"]
            ALB["AWS Application Load Balancer (ALB)\nHTTP/HTTPS & WebSockets STOMP\npuertos 80/443"]
            NAT_GW["NAT Gateway\nSalida segura para tareas privadas"]
        end

        subgraph PrivateSubnetApp ["Subred Privada - Aplicación"]
            subgraph ECS_Cluster ["Amazon ECS Cluster (AWS Fargate)"]
                Task1["Contenedor Docker 1\nSpring Boot 3 / Java 21\nPuerto 8080"]
                Task2["Contenedor Docker 2\nSpring Boot 3 / Java 21\nPuerto 8080"]
            end
        end

        subgraph PrivateSubnetData ["Subred Privada - Persistencia"]
            RDS["Amazon RDS PostgreSQL 16\nInstancia Multi-AZ\nPuerto 5432 (Cifrado KMS)"]
        end
    end

    subgraph ManagedStorage ["Almacenamiento Cloud y Telemetría"]
        S3_Frontend["Amazon S3 Bucket\nAssets Estáticos Web (HTML5/CSS/JS)"]
        S3_Assets["Amazon S3 Bucket Privado\nImágenes UML, Archivos XMI, ZIPs"]
        CloudWatch["Amazon CloudWatch\nLogs de Contenedores y Métricas"]
    end

    %% Conexiones
    Browser --> Route53
    MobileApp --> Route53
    Route53 --> CloudFront
    CloudFront --> WAF
    WAF --> S3_Frontend
    WAF --> ALB
    ACM -.-> CloudFront
    ACM -.-> ALB
    ALB --> Task1
    ALB --> Task2
    Task1 --> RDS
    Task2 --> RDS
    Task1 --> S3_Assets
    Task2 --> S3_Assets
    Task1 -.-> CloudWatch
    Task2 -.-> CloudWatch
    Task1 -.-> NAT_GW
    Task2 -.-> NAT_GW
```

---

## 2. Diagrama de Secuencia: Despliegue e Interacción en Producción

```mermaid
sequenceDiagram
    autonumber
    actor Dev as Ingeniero DevOps
    participant Git as GitHub Repository
    participant GHA as GitHub Actions CI/CD
    participant ECR as Amazon ECR
    participant ECS as Amazon ECS Fargate
    participant ALB as Application Load Balancer
    actor Client as Usuario / Móvil

    Dev->>Git: Push a rama main
    Git->>GHA: Dispara Pipeline CI/CD
    GHA->>GHA: Ejecuta 115 Tests Backend (Java 21)
    GHA->>GHA: Ejecuta 48 Tests Frontend (Vitest)
    GHA->>GHA: Ejecuta 38 Tests Mobile (Flutter)
    GHA->>ECR: Build y Push de Imágenes Docker
    GHA->>ECS: Actualiza Task Definition & Service
    ECS->>ALB: Registro en Target Group & Healthcheck /actuator/health
    ALB-->>ECS: Estado Healthy (HTTP 200)
    Client->>ALB: Petición HTTPS / REST / WebSockets
    ALB->>ECS: Enruta tráfico a contenedor saludable
```
