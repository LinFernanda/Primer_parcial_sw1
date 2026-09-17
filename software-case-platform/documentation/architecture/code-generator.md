# FASE 9 — Generador Automático de Backend Spring Boot desde Modelos UML

## 1. Visión General del Módulo

El subsistema de **Generación Automática de Backend Spring Boot** transforma de forma determinista y sin intervención manual diagramas de clases UML 2.5 (diseñados visualmente o recuperados mediante visión artificial en la Fase 8) en una solución backend completa, compilable, desacoplada y lista para producción en **Spring Boot 3 (Java 21)** con base de datos **PostgreSQL**.

### 1.1 Objetivos Fundamentales
- **Cero Boilerplate**: Generación automática de capas de persistencia, negocio, transferencia y presentación REST.
- **Calidad de Código de Grado Empresarial**: Cumplimiento estricto de Clean Architecture, patrones DTO y buenas prácticas de Spring Data JPA.
- **Empaquetado Estándar y Despliegue Inmediato**: Entrega en archivo comprimido `.zip` con manifiesto Maven (`pom.xml`), configuración externa (`application.yml`), contenedorización (`Dockerfile`, `docker-compose.yml`) y documentación (`README.md`).
- **Trazabilidad y Colaboración**: Auditoría de cada generación en el historial de versiones (Fase 6) y difusión de eventos a colaboradores conectados vía WebSocket (Fase 5).
- **Integración con Inteligencia Artificial**: Activación por comandos en lenguaje natural mediante el asistente IA (Fase 7).

---

## 2. Arquitectura del Generador

El generador implementa un **Pipeline de Transformación por Fases (Multi-Stage Transformation Pipeline)**:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            FRONTEND (React + TS)                            │
│  ┌──────────────────────────────┐     ┌──────────────────────────────────┐  │
│  │     CodeGeneratorModal       │     │          useGenerator            │  │
│  │  - Árbol de archivos (capas) │     │  - Configuración del proyecto    │  │
│  │  - Visor de código interactivo│    │  - Descarga de Blob ZIP          │  │
│  │  - Selector de opciones      │     │  - Notificaciones toast          │  │
│  └──────────────┬───────────────┘     └─────────────────┬────────────────┘  │
└─────────────────┼───────────────────────────────────────┼───────────────────┘
                  │ REST (Preview / Generate / ZIP)        │
                  ▼                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         BACKEND (Spring Boot 3 / Java 21)                   │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                         GeneratorController                           │  │
│  │  POST /api/generator/project/{id}        GET /preview                 │  │
│  │  GET  /api/generator/project/{id}/zip    (Descarga directa)           │  │
│  └──────────────────────────────────┬────────────────────────────────────┘  │
│                                     │                                       │
│                                     ▼                                       │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                      BackendGeneratorService                          │  │
│  │  - Orquestación del pipeline, ZIP in-memory, auditoría y WebSockets   │  │
│  └──────┬───────────────────────────┬─────────────────────────────┬──────┘  │
│         │                           │                             │         │
│         ▼                           ▼                             ▼         │
│  ┌──────────────┐            ┌──────────────┐             ┌──────────────┐  │
│  │ UMLAnalyzer  │            │ Code Builders│             │ProjectBuilder│  │
│  │   Service    │            │(Entity, Repo,│             │ (pom, yml,   │  │
│  │(Normalización│            │ DTO, Service,│             │ Dockerfile,  │  │
│  │ y Metamodelo)│            │ Controller)  │             │ README)      │  │
│  └──────────────┘            └──────────────┘             └──────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Pipeline de Generación por Capas

El proceso de generación se compone de cinco etapas secuenciales y deterministas:

### Etapa 1: Análisis y Normalización del Metamodelo (`UMLAnalyzerService`)
1. **Extracción y Validación**: Inspecciona el `ModeloUML`, sus `ClaseUML` y `RelacionUML`.
2. **Normalización de Nomenclatura**:
   - Nombres de clases transformados a `PascalCase` válido en Java (eliminando caracteres especiales).
   - Nombres de atributos y métodos transformados a `camelCase`.
   - Paquetes base y nombres de artefactos normalizados a identificadores Maven estándar.
3. **Inferencia de Claves Primarias (PK)**:
   - Si una clase carece de campo identificador (`id` o similar), el analizador inyecta sintéticamente un campo `id` de tipo `Long` con generación automática `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
4. **Mapeo de Tipos de Datos UML a Java/JPA**:
   - `String`, `Integer`, `Long`, `Double`, `Float`, `Boolean`, `BigDecimal`, `LocalDate`, `LocalDateTime`, `byte[]`.
5. **Resolución de Cardinalidades y Relaciones**:
   - Clasificación en `ONE_TO_MANY`, `MANY_TO_ONE`, `ONE_TO_ONE` y `MANY_TO_MANY`.
   - Inyección de relaciones bidireccionales y campos foráneos con colecciones apropiadas (`List<T>`).

### Etapa 2: Generación de Capas de Código (`Code Builders`)

#### 1. Capa de Entidades JPA (`EntityGeneratorService`)
- Anotaciones: `@Entity`, `@Table(name = "...")`, `@Id`, `@GeneratedValue`.
- Relaciones mapeadas con configuración de cascada (`CascadeType.ALL`) y lazy loading (`FetchType.LAZY` / `EAGER` según convenga).
- Claves foráneas anotadas con `@JoinColumn` y tablas intermedias para ManyToMany con `@JoinTable`.
- Métodos constructores (sin argumentos y completo) y métodos accesores Getters/Setters.

#### 2. Capa de Repositorios (`RepositoryGeneratorService`)
- Interfaces que extienden `JpaRepository<Entity, Long>`.
- Métodos de consulta derivados por atributos clave (ej. `findByNombreContainingIgnoreCase`, `findByEstado`).

#### 3. Capa de Objetos de Transferencia DTO (`DTOGeneratorService`)
- Separación de responsabilidades: **las entidades JPA nunca se exponen en la API pública**.
- **RequestDTO**: Objeto de entrada con anotaciones de validación (`@NotBlank`, `@NotNull`, `@Size`, `@Min`, `@Max`).
- **ResponseDTO**: Objeto de salida desacoplado que evita ciclos de serialización infinita en relaciones bidireccionales.
- **Mapper**: Clase utilitaria con métodos estáticos `toEntity(RequestDTO)` y `toResponseDTO(Entity)`.

#### 4. Capa de Lógica de Negocio (`ServiceGeneratorService`)
- **Interfaz de Servicio (`IEntityService`)**: Contrato CRUD con métodos paginados y de búsqueda:
  - `findAll()`, `findById(Long id)`, `save(RequestDTO dto)`, `update(Long id, RequestDTO dto)`, `deleteById(Long id)`.
- **Implementación (`EntityServiceImpl`)**:
  - Inyección de dependencias por constructor.
  - Anotación `@Service` y gestión transaccional `@Transactional(readOnly = true)` en lecturas y `@Transactional` en modificaciones.
  - Manejo de excepciones de negocio (`EntityNotFoundException`).

#### 5. Capa de Controladores REST (`ControllerGeneratorService`)
- Anotaciones: `@RestController`, `@RequestMapping("/api/v1/{entities}")`.
- Endpoints REST estándar:
  - `GET /api/v1/{entities}`: Listar todos los registros.
  - `GET /api/v1/{entities}/{id}`: Obtener entidad por identificador.
  - `POST /api/v1/{entities}`: Crear nueva entidad con validación `@Valid`.
  - `PUT /api/v1/{entities}/{id}`: Actualizar entidad existente.
  - `DELETE /api/v1/{entities}/{id}`: Eliminar entidad por ID.
- Respuestas HTTP semánticas (`200 OK`, `201 Created`, `204 No Content`, `404 Not Found`).
- Documentación Swagger / OpenAPI 3 (`@Tag`, `@Operation`, `@ApiResponse`).

### Etapa 3: Ensamblado del Proyecto Maven (`ProjectStructureBuilder`)
- **`pom.xml`**:
  - Spring Boot Starter Web, Data JPA, Validation, PostgreSQL Driver, Lombok (opcional), SpringDoc OpenAPI.
  - Configuración de plugins `spring-boot-maven-plugin` y Java 21 compiler plugin.
- **`application.yml`**:
  - Configuración de DataSource PostgreSQL con variables de entorno (`SPRING_DATASOURCE_URL`, etc.).
  - Hibernate DDL-Auto (`update`), formateo SQL e inicialización de esquemas.
  - Puerto de servidor configurable (`server.port: 8080`).
- **`Application.java`**: Clase principal con `@SpringBootApplication`.
- **Infraestructura de Despliegue**:
  - `Dockerfile` multi-stage (eclipse-temurin:21-jdk para compilación y jre para ejecución ligera).
  - `docker-compose.yml` orquestando la base de datos PostgreSQL y la aplicación Spring Boot.
  - `README.md` exhaustivo con instrucciones de compilación (`./mvnw spring-boot:run`), endpoints y variables.

### Etapa 4: Compresión In-Memory y Empaquetado ZIP
- El servicio genera el flujo de bytes del archivo `.zip` en memoria a través de `ByteArrayOutputStream` y `ZipOutputStream`.
- Sin dependencia de archivos temporales en disco, garantizando alto rendimiento y concurrencia.

### Etapa 5: Versionado y Notificación en Tiempo Real
- Registro automático en `VersionService`: Crea una versión del modelo etiquetada con la generación del backend y notas de cambios.
- Emisión de evento WebSocket a los colaboradores en la sala de edición (`/topic/modelos/{id}`).

---

## 4. Mapeo de Tipos y Cardinalidades UML a JPA

| Tipo UML | Tipo Java | Anotación JPA / Validación |
| :--- | :--- | :--- |
| `String` / `Texto` | `java.lang.String` | `@Column(nullable = false, length = 255)` / `@NotBlank` |
| `Integer` / `int` | `java.lang.Integer` | `@Column(nullable = false)` / `@NotNull` |
| `Long` / `id` | `java.lang.Long` | `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)` |
| `Double` / `float` | `java.lang.Double` | `@Column(nullable = false)` |
| `Boolean` / `bool` | `java.lang.Boolean` | `@Column(nullable = false)` |
| `Date` / `Fecha` | `java.time.LocalDate` | `@Column` |
| `DateTime` | `java.time.LocalDateTime` | `@Column` |
| `BigDecimal` / `Moneda` | `java.math.BigDecimal` | `@Column(precision = 10, scale = 2)` |

| Cardinalidad UML | Relación JPA | Atributo / Anotación Generada |
| :--- | :--- | :--- |
| `1` a `0..*` / `*` | `@OneToMany` / `@ManyToOne` | `@OneToMany(mappedBy = "padre", cascade = CascadeType.ALL)` en el extremo "1", `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "padre_id")` en el extremo "*". |
| `1` a `1` | `@OneToOne` | `@OneToOne(cascade = CascadeType.ALL) @JoinColumn(name = "relacion_id")` con `mappedBy` inverso. |
| `*` a `*` | `@ManyToMany` | `@ManyToMany @JoinTable(name = "tabla_intermedia", joinColumns = ..., inverseJoinColumns = ...)` |

---

## 5. Especificación de Endpoints REST del Generador

| Método | Endpoint | Descripción | Autenticación |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/generator/project/{id}` | Genera el proyecto backend completo a partir del modelo UML. Retorna árbol de archivos y métricas. | Requerida (JWT) |
| `GET` | `/api/generator/project/{id}/preview` | Obtiene la vista previa de todos los archivos generados con estructura arbórea sin descargar el ZIP. | Requerida (JWT) |
| `GET` | `/api/generator/project/{id}/zip` | Descarga directa del archivo ZIP (`application/zip`) con encabezado `Content-Disposition`. | Requerida (JWT) |

---

## 6. Interfaz de Usuario (Frontend)

El módulo cliente en React (`src/features/generator/`) provee una experiencia intuitiva para el desarrollador:

1. **Botón de Acción en Toolbar**: Botón con icono de código en la barra de herramientas principal del editor UML.
2. **Modal de Generación (`CodeGeneratorModal.tsx`)**:
   - **Barra de Configuración**: Campos para editar `groupId`, `artifactId`, `version` y base de datos (PostgreSQL por defecto).
   - **Panel Izquierdo (Explorador de Archivos)**: Árbol interactivo clasificado por capas funcionales (`Entities`, `Repositories`, `DTOs`, `Services`, `Controllers`, `Config & Build`).
   - **Panel Derecho (Visor de Código)**: Resaltador de sintaxis Java/XML/YAML con contador de líneas y botón para copiar código al portapapeles.
   - **Descarga Inmediata**: Botón de descarga de archivo ZIP con estado visual de progreso.
3. **Integración con Comandos de Voz y Texto (IA)**:
   - "Generar backend del modelo" ejecuta automáticamente el generador mediante `AICommandParser`.

---

## 7. Pruebas y Validación de Calidad

- **Pruebas Unitarias Backend**:
  - `UMLAnalyzerServiceTest`: Verificación de normalización de clases, atributos, inyección de IDs y relaciones bidireccionales.
  - `EntityGeneratorServiceTest`: Verificación de código fuente Java generado para entidades JPA.
  - `SpringProjectGeneratorServiceTest`: Validación de generación completa de proyectos y estructura Maven/Docker.
- **Pruebas de Integración Backend**:
  - `GeneratorControllerIntegrationTest`: Endpoints REST `POST /api/generator/project/{id}`, preview y descarga de ZIP binario.
- **Pruebas Frontend (Vitest / React Testing Library)**:
  - `generator.test.tsx`: Renderizado del modal, navegación por el árbol de archivos, descarga de archivo ZIP y visualización de código.
