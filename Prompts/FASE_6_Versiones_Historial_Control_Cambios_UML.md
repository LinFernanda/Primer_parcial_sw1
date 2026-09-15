# FASE 6 --- Sistema de versiones, historial y control de cambios del modelo UML

# Proyecto

## Plataforma CASE colaborativa inteligente para diseño UML y generación automática de software

------------------------------------------------------------------------

# Instrucciones para el agente Antigravity

## Objetivo de la fase

Implementar un sistema profesional de control de versiones, historial y
trazabilidad para los modelos UML creados dentro de la plataforma.

Esta fase permitirá que los ingenieros puedan:

-   Consultar la evolución de un diseño UML.
-   Recuperar versiones anteriores.
-   Analizar quién realizó cambios.
-   Mantener trazabilidad completa del modelo.
-   Evitar pérdida de información durante el trabajo colaborativo.

Este módulo será fundamental para proyectos donde múltiples ingenieros
modifican simultáneamente un diseño conceptual.

------------------------------------------------------------------------

# Contexto del módulo

La herramienta CASE desarrollada funciona como un entorno colaborativo
donde varios usuarios pueden modificar diagramas UML.

Debido a esto, el sistema debe registrar cada modificación realizada
sobre:

-   Clases UML.
-   Atributos.
-   Métodos.
-   Relaciones.
-   Cardinalidades.

El sistema debe mantener un historial similar a herramientas
profesionales de control de cambios.

------------------------------------------------------------------------

# Tecnologías obligatorias

## Backend

Utilizar:

-   Java 21.
-   Spring Boot 3.
-   Spring Data JPA.
-   PostgreSQL.
-   Maven.

------------------------------------------------------------------------

## Frontend

Utilizar:

-   React.
-   TypeScript.

------------------------------------------------------------------------

# Arquitectura del módulo

El sistema debe integrarse con la arquitectura existente:

    Frontend React

          |

    Backend Spring Boot

          |

    Servicio de Versionado

          |

    PostgreSQL

------------------------------------------------------------------------

# Objetivo técnico

Crear un mecanismo de snapshots y registro de eventos que permita
recuperar estados anteriores del modelo UML.

------------------------------------------------------------------------

# 1. Diseño del sistema de versiones

Crear un módulo:

    versioning/

Ubicación:

    backend/src/main/java/com/caseplatform/

Estructura:

    versioning/

    ├── controller/
    ├── service/
    ├── repository/
    ├── model/
    └── dto/

------------------------------------------------------------------------

# 2. Creación de entidad VersionModelo

Crear entidad:

    VersionModelo

Representa una versión específica del modelo UML.

------------------------------------------------------------------------

## Atributos obligatorios

    id

    numeroVersion

    nombreVersion

    descripcion

    modeloUML

    usuarioCreador

    fechaCreacion

    estado

------------------------------------------------------------------------

# Reglas

Cada versión debe:

-   Tener un identificador único.
-   Estar asociada a un modelo UML.
-   Registrar quién la creó.
-   Tener fecha de creación.

------------------------------------------------------------------------

# 3. Creación de entidad HistorialCambio

Crear:

    HistorialCambio

Representa una modificación individual realizada sobre el modelo.

------------------------------------------------------------------------

## Atributos

    id

    tipoOperacion

    elementoModificado

    idElemento

    datosAnteriores

    datosNuevos

    usuario

    fechaCambio

    versionModelo

------------------------------------------------------------------------

# Tipos de operaciones

Implementar:

    CREATE

    UPDATE

    DELETE

------------------------------------------------------------------------

# Ejemplo

Usuario:

Juan

Acción:

    Crear clase Cliente

Registro:

    Operacion:
    CREATE

    Elemento:
    Clase UML

    Usuario:
    Juan

    Fecha:
    2026-09-15

------------------------------------------------------------------------

# 4. Captura automática de cambios

Cada operación realizada en el editor UML debe generar un registro.

Ejemplos:

------------------------------------------------------------------------

Crear clase:

    Clase Cliente creada

------------------------------------------------------------------------

Modificar atributo:

    nombre:String

    cambiado a

    nombreCompleto:String

------------------------------------------------------------------------

Eliminar relación:

    Cliente - Venta eliminada

------------------------------------------------------------------------

# Flujo de registro

    Usuario realiza cambio

            |

    Editor UML

            |

    Backend recibe operación

            |

    Guardar cambio

            |

    Actualizar versión

------------------------------------------------------------------------

# 5. Implementación del sistema de snapshots

Crear mecanismo para almacenar estados completos del modelo UML.

------------------------------------------------------------------------

Cada snapshot debe guardar:

-   Clases.
-   Atributos.
-   Métodos.
-   Relaciones.
-   Posiciones visuales.

------------------------------------------------------------------------

Formato recomendado:

JSON estructurado.

Ejemplo conceptual:

    {
     modelo:"Ventas",

     clases:[
       {
        nombre:"Cliente"
       }
     ]
    }

------------------------------------------------------------------------

# 6. Creación de servicio de versionado

Crear:

    VersionService

Responsabilidades:

-   Crear versiones.
-   Consultar historial.
-   Recuperar versiones.
-   Restaurar modelos.

------------------------------------------------------------------------

# Funciones obligatorias

## Crear versión

Debe permitir guardar un punto estable del modelo.

Ejemplo:

    Versión 1.0
    Modelo inicial

------------------------------------------------------------------------

## Consultar historial

Debe mostrar:

    Versión

    Usuario

    Fecha

    Descripción

------------------------------------------------------------------------

## Restaurar versión

Debe permitir volver a un estado anterior.

Ejemplo:

Modelo actual:

    Versión 5

Restaurar:

    Versión 3

------------------------------------------------------------------------

# 7. Creación de Repository

Crear:

    VersionRepository

    HistorialCambioRepository

Responsabilidad:

-   Persistencia PostgreSQL.
-   Consultas de cambios.

------------------------------------------------------------------------

# 8. Creación de DTOs

Crear:

    dto/version/

Implementar:

    VersionDTO

    HistorialCambioDTO

    RestoreVersionDTO

------------------------------------------------------------------------

# Regla

Nunca devolver entidades directamente al frontend.

------------------------------------------------------------------------

# 9. Creación de APIs REST

Crear controlador:

    VersionController

------------------------------------------------------------------------

# Endpoints requeridos

## Crear versión

    POST /api/versiones

------------------------------------------------------------------------

## Obtener versiones

    GET /api/modelos/{id}/versiones

------------------------------------------------------------------------

## Obtener historial

    GET /api/modelos/{id}/historial

------------------------------------------------------------------------

## Restaurar versión

    POST /api/versiones/{id}/restore

------------------------------------------------------------------------

# 10. Integración con editor UML

Actualizar frontend.

Crear:

    features/versioning/

------------------------------------------------------------------------

Implementar interfaz:

## Panel de historial

Debe mostrar:

    Modelo Sistema Ventas

    Versiones:

    v1
    v2
    v3

------------------------------------------------------------------------

## Vista de cambios

Mostrar:

    Juan creó Clase Cliente

    Maria modificó relación Venta

    Carlos eliminó atributo precio

------------------------------------------------------------------------

# 11. Integración con colaboración WebSocket

El sistema debe trabajar junto con la fase colaborativa.

Cuando un usuario realice un cambio:

    Evento WebSocket

            |

    Actualizar modelo

            |

    Registrar historial

            |

    Crear nueva versión o cambio

------------------------------------------------------------------------

# 12. Control de integridad del modelo

Antes de restaurar una versión:

Validar:

-   Usuario autorizado.
-   Modelo existente.
-   Compatibilidad de datos.

------------------------------------------------------------------------

# 13. Pruebas del sistema de versiones

Implementar pruebas:

## Creación de versión

Validar:

-   Se guarda correctamente.

------------------------------------------------------------------------

## Historial

Validar:

-   Registra usuario.
-   Registra fecha.
-   Registra operación.

------------------------------------------------------------------------

## Restauración

Validar:

-   Modelo vuelve al estado anterior.

------------------------------------------------------------------------

## Integración

Validar:

-   Cambios colaborativos generan historial.

------------------------------------------------------------------------

Herramientas:

-   JUnit.
-   Mockito.
-   Spring Boot Test.

------------------------------------------------------------------------

# 14. Documentación del módulo

Crear:

    documentation/architecture/versioning.md

Documentar:

-   Arquitectura.
-   Modelo de datos.
-   Flujo de versiones.
-   Restauración.
-   Historial.

------------------------------------------------------------------------

# Criterios obligatorios para finalizar la fase

La fase está terminada únicamente si:

## Versionado

✅ Se pueden crear versiones del modelo.

✅ Se almacenan snapshots.

✅ Se puede consultar historial.

------------------------------------------------------------------------

## Trazabilidad

✅ Cada cambio registra usuario.

✅ Cada cambio registra fecha.

✅ Cada cambio registra operación realizada.

------------------------------------------------------------------------

## Recuperación

✅ Se puede restaurar una versión anterior.

✅ El modelo recuperado mantiene integridad UML.

------------------------------------------------------------------------

## Integración

✅ Funciona junto al editor UML.

✅ Funciona junto al sistema colaborativo.

------------------------------------------------------------------------

## Calidad

✅ Código organizado.

✅ DTO implementados.

✅ Pruebas ejecutadas.

------------------------------------------------------------------------

# Resultado final esperado

Al finalizar esta fase la plataforma tendrá control profesional de
evolución del diseño:

    Modelo UML

          |

    Versiones

          |

    Historial de cambios

          |

    Restauración

          |

    Trazabilidad completa

Los ingenieros podrán trabajar colaborativamente con seguridad, sabiendo
que cualquier modificación puede ser analizada o revertida.

------------------------------------------------------------------------

# Restricciones de esta fase

No desarrollar todavía:

-   Inteligencia artificial.
-   Conversión imagen a UML.
-   Generador automático Spring Boot.
-   Integración Enterprise Architect.
-   Aplicación Flutter.
-   Funcionamiento offline.

Esta fase únicamente implementa control de evolución, historial y
recuperación del modelo UML.
