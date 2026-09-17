# Sistema de Versiones, Historial y Control de Cambios del Modelo UML (Fase 6)

## 1. Visión General del Módulo

El módulo de **Control de Versiones, Historial y Trazabilidad** proporciona un mecanismo para la gestión evolutiva de diagramas conceptuales UML 2.5 en la **Plataforma CASE Colaborativa Inteligente**.

Permite a los equipos de ingeniería de software:
- **Congelar hitos y snapshots**: Crear versiones inmutables del modelo UML en puntos clave del diseño (ej. `v1.0`, `v2.0`).
- **Trazabilidad atómica completa**: Registrar de manera automática y transparente cada mutación (creación, edición, eliminación) realizada sobre clases, atributos, métodos y relaciones, asociando el autor, fecha y detalle del cambio.
- **Auditoría colaborativa**: Visualizar cronologías de modificaciones producidas tanto por peticiones REST como por eventos distribuidos vía WebSockets en tiempo real.
- **Restauración con integridad referencial**: Revertir el modelo activo a cualquier versión anterior sin corromper la consistencia de clases, cardinalidades ni conectores UML.

---

## 2. Arquitectura del Módulo

```
    ┌─────────────────────────────────────────────────────────────┐
    │                       Frontend React                        │
    │  ┌─────────────────────────┐   ┌─────────────────────────┐  │
    │  │ Panel de Versiones      │   │ Historial de Cambios    │  │
    │  │ (Snapshots, Restaurar)  │   │ (Trazabilidad en Vivo)  │  │
    │  └────────────┬────────────┘   └────────────┬────────────┘  │
    └───────────────┼─────────────────────────────┼───────────────┘
                    │ REST API                    │ WebSocket STOMP
                    ▼                             ▼
    ┌─────────────────────────────────────────────────────────────┐
    │                 Backend Spring Boot 3                       │
    │  ┌───────────────────────┐       ┌───────────────────────┐  │
    │  │ VersionController     │       │ UMLSocketController   │  │
    │  └───────────┬───────────┘       └───────────┬───────────┘  │
    │              │                               │              │
    │              ▼                               ▼              │
    │  ┌───────────────────────────────────────────────────────┐  │
    │  │                   VersionService                      │  │
    │  │  - crearVersion()        - restaurarVersion()         │  │
    │  │  - listarVersiones()     - registrarCambio()          │  │
    │  │  - generarSnapshot()     - obtenerHistorial()         │  │
    │  └──────────────────────────┬────────────────────────────┘  │
    └─────────────────────────────┼───────────────────────────────┘
                                  ▼
    ┌─────────────────────────────────────────────────────────────┐
    │                      PostgreSQL 16                          │
    │  ┌───────────────────────┐       ┌───────────────────────┐  │
    │  │ versiones_modelo      │       │ historial_cambios     │  │
    │  │ (JSON Snapshots)      │       │ (Eventos atómicos)    │  │
    │  └───────────────────────┘       └───────────────────────┘  │
    └─────────────────────────────────────────────────────────────┘
```

---

## 3. Modelo de Datos y Entidades

### 3.1 Entidad `VersionModelo` (Snapshots Congelados)

Representa una versión formalizada y persistente del modelo conceptual UML.

| Campo | Tipo | Restricción | Descripción |
|---|---|---|---|
| `id` | BIGSERIAL | PK | Identificador único de la versión |
| `numero_version` | VARCHAR(50) | NOT NULL | Código de versión (ej. `v1.0`, `v2.0`) |
| `nombre_version` | VARCHAR(150) | NOT NULL | Título del hito (ej. `Modelo Inicial MVP`) |
| `descripcion` | VARCHAR(1000) | Nullable | Notas de la versión o justificación del diseño |
| `snapshot_json` | TEXT | NOT NULL | Representación serializada completa del modelo |
| `modelo_id` | BIGINT | FK NOT NULL | Referencia al `ModeloUML` |
| `usuario_creador_id` | BIGINT | FK Nullable | Ingeniero que congeló la versión |
| `fecha_creacion` | TIMESTAMP | NOT NULL | Fecha y hora UTC del congelamiento |
| `estado` | VARCHAR(50) | NOT NULL | Estado operativo (`ACTIVA`, `RESTAURADA`) |

### 3.2 Entidad `HistorialCambio` (Modificaciones Atómicas)

Registra cada cambio individual ocurrido en el editor visual o colaborativo.

| Campo | Tipo | Restricción | Descripción |
|---|---|---|---|
| `id` | BIGSERIAL | PK | Identificador del evento de cambio |
| `tipo_operacion` | VARCHAR(50) | NOT NULL | `CREATE`, `UPDATE`, `DELETE`, `RESTORE` |
| `elemento_modificado` | VARCHAR(100) | NOT NULL | `Clase UML`, `Atributo`, `Método`, `Relación`, `Modelo UML` |
| `id_elemento` | VARCHAR(100) | Nullable | ID del elemento afectado |
| `datos_anteriores` | TEXT | Nullable | Estado previo del elemento antes de la modificación |
| `datos_nuevos` | TEXT | Nullable | Estado resultante tras la modificación |
| `usuario_id` | BIGINT | FK Nullable | Referencia al usuario ejecutor |
| `usuario_email` | VARCHAR(150) | NOT NULL | Email del usuario para trazabilidad garantizada |
| `fecha_cambio` | TIMESTAMP | NOT NULL | Timestamp del evento |
| `version_modelo_id` | BIGINT | FK Nullable | Versión asociada (si aplica) |
| `modelo_id` | BIGINT | FK NOT NULL | Modelo UML contenedor |

---

## 4. Estructura del Snapshot JSON

El snapshot almacena una captura profunda y autocontenida de todas las entidades del diagrama:

```json
{
  "modeloId": 100,
  "nombreModelo": "Sistema Ventas",
  "version": "v1.0",
  "clases": [
    {
      "id": 1001,
      "nombre": "Cliente",
      "visibilidad": "PUBLIC",
      "descripcion": "Entidad que realiza compras",
      "posicionX": 120.0,
      "posicionY": 150.0,
      "atributos": [
        {
          "id": 201,
          "nombre": "email",
          "tipoDato": "String",
          "visibilidad": "PRIVATE",
          "valorInicial": null
        }
      ],
      "metodos": [
        {
          "id": 301,
          "nombre": "registrar",
          "tipoRetorno": "void",
          "visibilidad": "PUBLIC",
          "parametros": "email:String, password:String"
        }
      ]
    }
  ],
  "relaciones": [
    {
      "id": 501,
      "tipoRelacion": "ASOCIACION",
      "claseOrigenNombre": "Cliente",
      "claseDestinoNombre": "Pedido",
      "cardinalidadOrigen": "1",
      "cardinalidadDestino": "*",
      "descripcion": "Un cliente genera pedidos"
    }
  ]
}
```

---

## 5. Algoritmo de Restauración y Control de Integridad UML

Para restaurar una versión previa sin riesgo de inconsistencias en la base de datos o fallos de claves foráneas:

1. **Validación de Integridad**: Se deserializa el `snapshot_json` verificando sintaxis JSON y coherencia estructural.
2. **Desconexión Segura de Relaciones**: Se eliminan las relaciones UML actuales asociadas al modelo (`relacionUMLRepository.deleteAll`).
3. **Limpieza de Clases**: Se eliminan las clases actuales junto con sus atributos y métodos por cascada (`claseUMLRepository.deleteAll`).
4. **Flush de Persistencia**: Se sincroniza el contexto de persistencia JPA para liberar restricciones únicas de nombre (`uk_clase_modelo_nombre`).
5. **Reconstrucción de Clases**: Se instancian y guardan las clases del snapshot, reconstruyendo sus atributos y métodos hijos. Se mantiene un mapa en memoria `Map<String, ClaseUML>`.
6. **Reconexión de Relaciones**: Se reconstruyen las relaciones asociando las nuevas instancias de clase por nombre o identificador previo.
7. **Actualización de Versión Activa**: Se actualiza el campo `version` del `ModeloUML` al número de versión restaurado y se actualiza `fecha_actualizacion`.
8. **Registro en Auditoría**: Se genera un registro `TipoOperacionHistorial.RESTORE` en `historial_cambios`.
9. **Transaccionalidad Atómica**: Toda la operación se ejecuta bajo `@Transactional`. En caso de cualquier error imprevisto, se realiza rollback completo.

---

## 6. Endpoints de la API REST (`VersionController`)

| Método | Ruta | Descripción | Código Éxito |
|---|---|---|---|
| `POST` | `/api/versiones` | Congelar un nuevo snapshot y crear versión | `201 Created` |
| `GET` | `/api/modelos/{id}/versiones` | Listar versiones del modelo ordenadas desc | `200 OK` |
| `GET` | `/api/versiones/{id}` | Obtener detalle y snapshot de una versión | `200 OK` |
| `GET` | `/api/modelos/{id}/historial` | Consultar cronología de cambios atómicos | `200 OK` |
| `POST` | `/api/versiones/{id}/restore` | Restaurar el modelo UML al snapshot | `200 OK` |

---

## 7. Integración con el Frontend y Edición Visual

1. **Toolbar de la Pizarra**: Se incluye el botón **Versiones** con acceso directo al modal interactivo de gestión.
2. **Pestaña Versiones**:
   - Visualización de tarjetas de versiones con insignia de versión (`v1.0`, `v2.0`), fecha, creador y estado.
   - Botón **Restaurar** con modal de confirmación preventiva.
   - Botón **Nueva Versión** para congelar el estado actual.
3. **Pestaña Registro de Cambios**:
   - Cronología de cambios con insignias de color (`CREACIÓN`, `EDICIÓN`, `ELIMINACIÓN`, `RESTAURACIÓN`).
   - Resúmenes naturales (ej. *"Juan creó Clase Cliente"*, *"Carlos eliminó Atributo precio"*).
   - Buscador en tiempo real por usuario, elemento o descripción.
4. **Sincronización React Flow**: Al confirmar una restauración, el `useUMLStore` ejecuta `restoreModeloState()`, repintando inmediatamente los nodos de clase y aristas en el lienzo interactivo.
