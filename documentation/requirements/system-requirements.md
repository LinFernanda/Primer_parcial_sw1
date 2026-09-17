# ESPECIFICACIÓN DE REQUERIMIENTOS DEL SISTEMA (SRS)

## Plataforma CASE Colaborativa Inteligente para Diseño UML y Generación de Software

---

## 1. Requerimientos Funcionales (RF)

| ID | Módulo | Descripción del Requerimiento | Fase | Estado |
| :--- | :--- | :--- | :--- | :--- |
| **RF-01** | Autenticación y Seguridad | Registro de usuarios, login con JWT cifrado con HMAC-SHA256, hashing de contraseñas con BCrypt y control de acceso basado en roles (`ADMIN`, `INGENIERO`). | Fase 2 | Implementado ✅ |
| **RF-02** | Núcleo UML 2.5 | Modelado de proyectos y diagramas conceptuales con clases, atributos con tipos de datos y visibilidad, métodos con tipo de retorno y relaciones (Asociación, Agregación, Composición, Herencia, Dependencia). | Fase 3 | Implementado ✅ |
| **RF-03** | Editor Visual Web | Lienzo interactivo en ReactFlow con arrastre de clases, conexión visual de relaciones con anclajes magnéticos, barra de herramientas flotante y panel de propiedades reactivo. | Fase 4 | Implementado ✅ |
| **RF-04** | Colaboración en Tiempo Real | Comunicación bidireccional vía WebSockets sobre protocolo STOMP, presencia de usuarios activos con código de color por avatar y difusión de operaciones atómicas. | Fase 5 | Implementado ✅ |
| **RF-05** | Control de Cambios y Versiones | Creación manual y automática de snapshots inmutables del modelo UML, cálculo de diferencias semánticas (diff) y rollback transaccional con auditoría. | Fase 6 | Implementado ✅ |
| **RF-06** | Agente de IA Asistente | Interpretación de instrucciones en lenguaje natural por texto y voz para crear, editar o eliminar elementos del modelo UML sin intervención manual. | Fase 7 | Implementado ✅ |
| **RF-07** | Visión Artificial (Imagen a UML) | Detección óptica y OCR de diagramas UML dibujados en pizarras o libretas para convertirlos en clases y relaciones estructurales editables. | Fase 8 | Implementado ✅ |
| **RF-08** | Generador de Backend Spring Boot | Generación automática forward de proyectos Spring Boot 3 completos (Entidades JPA, Repositorios Spring Data, DTOs, Mappers, Servicios, Controladores REST, OpenAPI y DDL) empaquetados en `.ZIP`. | Fase 9 | Implementado ✅ |
| **RF-09** | Integración Enterprise Architect | Exportación e importación conforme a la especificación OMG XMI 2.1 estándar, preservando identificadores, paquetes y visibilidades. | Fase 10 | Implementado ✅ |
| **RF-10** | Aplicación Móvil Flutter | App multiplataforma (Android, Windows, Web) con catálogo de barbería generado a partir del modelo UML, consumiendo la API de backend. | Fase 11 | Implementado ✅ |
| **RF-11** | Operación Offline y Sync Móvil | Persistencia local ACID en SQLite, cola de sincronización FIFO, resolución de conflictos *Last-Write-Wins* y Asistente IA On-Device sin internet. | Fase 12 | Implementado ✅ |
| **RF-12** | Despliegue en Producción AWS | Contenedorización multi-stage Docker, perfiles de producción, Terraform IaC, Amazon RDS PostgreSQL, Amazon S3, CI/CD con GitHub Actions y monitoreo CloudWatch. | Fase 13 | Implementado ✅ |

---

## 2. Requerimientos No Funcionales (RNF)

| ID | Atributo de Calidad | Especificación Técnica |
| :--- | :--- | :--- |
| **RNF-01** | **Seguridad** | Cifrado en tránsito HTTPS (TLS 1.3) y en reposo (KMS AES-256 en S3 y RDS). Encabezados HTTP de seguridad (HSTS, CSP, X-Frame-Options SAMEORIGIN). Sesiones sin estado Stateless. |
| **RNF-02** | **Rendimiento** | Respuestas de la API REST por debajo de 200 ms en percentil 95. Compresión gzip de assets web para carga de lienzo en menos de 1.5 segundos. |
| **RNF-03** | **Escalabilidad** | Tareas de backend en AWS ECS Fargate auto-escalables horizontalmente entre 2 y 10 instancias ante incrementos sostenidos de CPU (>80%). |
| **RNF-04** | **Disponibilidad** | Arquitectura Multi-AZ en subredes privadas con conmutación por error en Amazon RDS y Application Load Balancer con SLA del 99.95%. |
| **RNF-05** | **Interoperabilidad** | Cumplimiento de estándares abiertos: OMG UML 2.5, XMI 2.1, OpenAPI 3.0, WebSockets STOMP y contenedores OCI/Docker. |
| **RNF-06** | **Mantenibilidad** | Código fuertemente tipado (TypeScript y Java 21 con records/lambdas), separación rigurosa de responsabilidades (Clean Architecture) y cobertura de pruebas automatizadas (>90%). |
| **RNF-07** | **Resiliencia** | Capacidad de recuperación inmediata ante desastres con copias de seguridad continuas y modo desconectado *offline-first* en clientes móviles. |
