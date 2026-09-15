# Requisitos Funcionales y No Funcionales - CASE Platform

## 1. Requisitos Funcionales por Módulo

### Módulo 1: Gestión de Identidad y Seguridad (Fase 2)
- RF-01: Registro e inicio de sesión con JWT y BCrypt.
- RF-02: Control de acceso basado en roles (RBAC: Admin, Architect, Developer, Viewer).
- RF-03: Renovación de tokens (Refresh Tokens).

### Módulo 2: Metamodelo UML 2.5 (Fase 3 & 4)
- RF-04: Definición de paquetes, clases, atributos, operaciones, visibilidades y estereotipos.
- RF-05: Relaciones UML: Asociación, Agregación, Composición, Generalización/Herencia y Dependencia.
- RF-06: Editor visual con canvas interactivo, zoom, pan, snap-to-grid y exportación en formatos gráficos (SVG, PNG).

### Módulo 3: Colaboración en Tiempo Real (Fase 5 & 6)
- RF-07: Sincronización multiusuario mediante WebSockets / STOMP.
- RF-08: Presencia y cursores de usuarios concurrentes.
- RF-09: Control de versiones, historial de cambios y rollback de diagramas.

### Módulo 4: Asistente Inteligente con IA (Fase 7 & 8)
- RF-10: Asistente conversacional con LLM para generación de modelos a partir de descripciones textuales.
- RF-11: Visión artificial para conversión de diagramas dibujados a mano a modelos UML digitales.

### Módulo 5: Generador de Backend & Interoperabilidad (Fase 9 & 10)
- RF-12: Generación automática de proyectos Spring Boot completos desde diagramas de clases.
- RF-13: Importación y exportación de archivos XMI compatibles con Enterprise Architect.

### Módulo 6: Movilidad y Despliegue (Fase 11, 12 & 13)
- RF-14: Aplicación móvil Flutter para visualización y edición ligera.
- RF-15: Modo offline con sincronización diferencial.
- RF-16: Infraestructura en la nube sobre AWS con Docker y CI/CD.

## 2. Requisitos No Funcionales
- **RNF-01 (Rendimiento):** Tiempo de respuesta menor a 200 ms en endpoints estándar de backend.
- **RNF-02 (Escalabilidad):** Capacidad de escalar horizontalmente el backend al ser 100% sin estado.
- **RNF-03 (Mantenibilidad):** Cumplimiento estricto de principios SOLID, cobertura de pruebas unitarias y linters.
- **RNF-04 (Portabilidad):** Despliegue en contenedores Docker y compatibilidad multiplataforma (Windows, Linux, macOS).
