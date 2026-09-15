# FASE 3 --- Desarrollo del núcleo UML 2.5 y modelo conceptual del sistema

# Proyecto

## Plataforma CASE colaborativa inteligente para diseño UML y generación automática de software

------------------------------------------------------------------------

# Instrucciones para el agente Antigravity

## Objetivo de la fase

Implementar el núcleo principal de modelado conceptual de la plataforma
CASE.

En esta fase se debe construir el motor interno que permitirá
representar modelos UML 2.5, específicamente orientado al diseño de
diagramas de clases.

Este módulo será la base para futuras funcionalidades:

-   Editor visual UML.
-   Colaboración en tiempo real.
-   Asistente IA para modificación de diagramas.
-   Conversión de imagen a UML.
-   Generación automática de backend Spring Boot.
-   Integración con Enterprise Architect.

------------------------------------------------------------------------

# Contexto del módulo

La herramienta desarrollada debe permitir que ingenieros de software
puedan diseñar modelos conceptuales de sistemas mediante diagramas de
clases UML.

El sistema debe representar digitalmente una pizarra de diseño
colaborativa donde los usuarios puedan crear:

-   Clases.
-   Atributos.
-   Métodos.
-   Relaciones.
-   Cardinalidades.

El modelo UML creado será posteriormente utilizado como entrada para
generar código backend.

------------------------------------------------------------------------

# Tecnologías obligatorias

## Backend

Utilizar:

-   Java 21.
-   Spring Boot 3.
-   Spring Data JPA.
-   Hibernate.
-   Maven.

------------------------------------------------------------------------

## Base de datos

Utilizar:

-   PostgreSQL.

------------------------------------------------------------------------

## Arquitectura

Mantener separación por capas:

    Controller
          |
    Service
          |
    Repository
          |
    Entity

Utilizar DTO para comunicación externa.

------------------------------------------------------------------------

# Objetivo arquitectónico del módulo

Crear una representación persistente del modelo UML.

La arquitectura debe permitir:

    Modelo UML

         |

    Base de datos PostgreSQL

         |

    Servicios Backend

         |

    Editor visual / IA / Generador código

------------------------------------------------------------------------

# 1. Diseño del dominio UML

Crear las entidades principales del modelo conceptual.

Ubicación:

    backend/src/main/java/com/caseplatform/model/

------------------------------------------------------------------------

# Entidad Proyecto UML

Crear:

    ProyectoUML

Representa un espacio de trabajo donde los ingenieros diseñan un
sistema.

------------------------------------------------------------------------

## Atributos obligatorios

    id
    nombre
    descripcion
    fechaCreacion
    fechaActualizacion
    usuarioPropietario
    estado

------------------------------------------------------------------------

## Funcionalidad

Debe permitir:

-   Crear proyectos.
-   Identificar propietarios.
-   Asociar modelos UML.

------------------------------------------------------------------------

# Entidad Modelo UML

Crear:

    ModeloUML

Representa el diagrama conceptual completo.

------------------------------------------------------------------------

## Atributos

    id
    nombre
    version
    fechaCreacion
    fechaActualizacion
    proyecto

------------------------------------------------------------------------

## Relación

Un proyecto puede tener uno o varios modelos.

Ejemplo:

    Proyecto Sistema Ventas

            |

            |

    Modelo UML versión 1
    Modelo UML versión 2

------------------------------------------------------------------------

# Entidad Clase UML

Crear:

    ClaseUML

Representa una clase del diagrama.

------------------------------------------------------------------------

## Atributos

    id
    nombre
    visibilidad
    descripcion
    posicionX
    posicionY
    modeloUML

------------------------------------------------------------------------

## Ejemplo

Representación:

    -----------------
    Cliente
    -----------------
    + nombre:String
    + edad:int
    -----------------

------------------------------------------------------------------------

# Entidad Atributo UML

Crear:

    AtributoUML

Representa propiedades internas de una clase.

------------------------------------------------------------------------

## Atributos

    id
    nombre
    tipoDato
    visibilidad
    valorInicial
    claseUML

------------------------------------------------------------------------

## Tipos de datos soportados inicialmente

Debe permitir:

    String
    Integer
    Long
    Double
    Boolean
    Date

------------------------------------------------------------------------

# Entidad Método UML

Crear:

    MetodoUML

Representa operaciones de una clase.

------------------------------------------------------------------------

## Atributos

    id
    nombre
    tipoRetorno
    visibilidad
    claseUML

------------------------------------------------------------------------

Ejemplo:

    + calcularTotal():Double

------------------------------------------------------------------------

# Entidad Relación UML

Crear:

    RelacionUML

Representa conexiones entre clases.

------------------------------------------------------------------------

## Atributos

    id
    tipoRelacion
    claseOrigen
    claseDestino
    cardinalidadOrigen
    cardinalidadDestino
    descripcion

------------------------------------------------------------------------

# Tipos de relaciones soportadas

Implementar inicialmente:

## Asociación

Ejemplo:

    Cliente -------- Venta

------------------------------------------------------------------------

## Herencia

Ejemplo:

    Empleado
        |
        |
    Gerente

------------------------------------------------------------------------

## Dependencia

Ejemplo:

    Factura ---> Impresora

------------------------------------------------------------------------

# Cardinalidades UML obligatorias

El sistema debe soportar:

    1

    0..1

    *

    1..*

    0..*

------------------------------------------------------------------------

Ejemplo:

    Cliente 1 -------- * Venta

------------------------------------------------------------------------

# 2. Diseño de base de datos PostgreSQL

Crear las tablas necesarias.

------------------------------------------------------------------------

## Tabla proyecto_uml

Debe almacenar:

-   Información del proyecto.

------------------------------------------------------------------------

## Tabla modelo_uml

Debe almacenar:

-   Versiones del modelo.

------------------------------------------------------------------------

## Tabla clase_uml

Debe almacenar:

-   Clases creadas.

------------------------------------------------------------------------

## Tabla atributo_uml

Debe almacenar:

-   Propiedades de las clases.

------------------------------------------------------------------------

## Tabla metodo_uml

Debe almacenar:

-   Operaciones.

------------------------------------------------------------------------

## Tabla relacion_uml

Debe almacenar:

-   Relaciones entre clases.

------------------------------------------------------------------------

# Reglas de integridad

Implementar:

-   Claves primarias.
-   Claves foráneas.
-   Restricciones de datos.
-   Eliminación controlada.

------------------------------------------------------------------------

# 3. Implementación Repository

Crear repositories:

    repository/

    ├── ProyectoUMLRepository
    ├── ModeloUMLRepository
    ├── ClaseUMLRepository
    ├── AtributoUMLRepository
    ├── MetodoUMLRepository
    └── RelacionUMLRepository

------------------------------------------------------------------------

Responsabilidad:

-   Comunicación con PostgreSQL.
-   Consultas del modelo UML.

------------------------------------------------------------------------

# 4. Implementación Service

Crear servicios:

    service/

    ├── ProyectoUMLService
    ├── ModeloUMLService
    ├── ClaseUMLService
    ├── AtributoUMLService
    ├── MetodoUMLService
    └── RelacionUMLService

------------------------------------------------------------------------

Responsabilidades:

-   Validar operaciones.
-   Aplicar reglas de negocio.
-   Coordinar persistencia.

------------------------------------------------------------------------

# 5. Implementación DTO

Crear:

    dto/uml/

Debe contener:

    ProyectoUMLDTO

    ModeloUMLDTO

    ClaseUMLDTO

    AtributoUMLDTO

    MetodoUMLDTO

    RelacionUMLDTO

------------------------------------------------------------------------

Regla:

Nunca devolver directamente entidades JPA al frontend.

------------------------------------------------------------------------

# 6. Creación de APIs REST UML

Crear controladores:

    controller/

    UMLController

------------------------------------------------------------------------

# Endpoints requeridos

## Proyectos UML

Crear:

    POST /api/proyectos

Consultar:

    GET /api/proyectos

------------------------------------------------------------------------

## Clases UML

Crear:

    POST /api/modelos/{id}/clases

Consultar:

    GET /api/modelos/{id}/clases

Actualizar:

    PUT /api/clases/{id}

Eliminar:

    DELETE /api/clases/{id}

------------------------------------------------------------------------

## Relaciones

Crear:

    POST /api/relaciones

Eliminar:

    DELETE /api/relaciones/{id}

------------------------------------------------------------------------

# 7. Validaciones del modelo UML

Implementar reglas:

------------------------------------------------------------------------

## Clases

No permitir:

-   Clases sin nombre.
-   Clases duplicadas dentro del mismo modelo.

------------------------------------------------------------------------

## Atributos

No permitir:

-   Atributos sin nombre.
-   Tipos inválidos.

------------------------------------------------------------------------

## Relaciones

No permitir:

-   Relaciones sin origen.
-   Relaciones sin destino.

------------------------------------------------------------------------

# 8. Pruebas del núcleo UML

Implementar pruebas:

## Proyecto UML

Validar:

-   Creación.
-   Consulta.

------------------------------------------------------------------------

## Clase UML

Validar:

-   Creación.
-   Modificación.
-   Eliminación.

------------------------------------------------------------------------

## Relaciones

Validar:

-   Asociación correcta.
-   Cardinalidades.

------------------------------------------------------------------------

Utilizar:

-   JUnit.
-   Mockito.
-   Spring Boot Test.

------------------------------------------------------------------------

# 9. Documentación del módulo

Crear:

    documentation/architecture/uml-core.md

Debe documentar:

-   Modelo de entidades.
-   Relaciones.
-   Arquitectura.
-   Reglas del dominio.

------------------------------------------------------------------------

# Criterios obligatorios para finalizar la fase

La fase está terminada únicamente si:

## Modelo UML

✅ Se pueden crear proyectos UML.

✅ Se pueden crear modelos.

✅ Se pueden crear clases.

✅ Se pueden agregar atributos y métodos.

✅ Se pueden crear relaciones.

✅ Se soportan cardinalidades.

------------------------------------------------------------------------

## Backend

✅ Entidades persistentes funcionando.

✅ PostgreSQL conectado.

✅ APIs REST funcionando.

✅ DTO implementados.

------------------------------------------------------------------------

## Calidad

✅ Código separado por capas.

✅ Validaciones implementadas.

✅ Pruebas ejecutadas.

------------------------------------------------------------------------

# Resultado final esperado

Al finalizar esta fase debe existir el núcleo de representación UML:

    Proyecto UML

            |

    Modelo UML

            |

    Clase UML

            |

    Atributos + Métodos

            |

    Relaciones + Cardinalidades

Este modelo será utilizado posteriormente por:

-   Editor visual UML.
-   Motor colaborativo.
-   Inteligencia artificial.
-   Generador automático de código.

------------------------------------------------------------------------

# Restricciones de esta fase

No desarrollar todavía:

-   Editor gráfico UML.
-   WebSocket.
-   IA.
-   Reconocimiento de imágenes.
-   Generador Spring Boot.
-   Enterprise Architect.
-   Aplicación Flutter.

Esta fase únicamente construye el motor de datos y lógica del modelo
UML.
