# FASE 9 --- Generador automático de Backend Spring Boot desde modelos UML

# Proyecto

## Plataforma CASE colaborativa inteligente para diseño UML y generación automática de software

------------------------------------------------------------------------

# Instrucciones para el agente Antigravity

## Objetivo de la fase

Implementar el motor de generación automática de código fuente que
transforme un modelo conceptual UML en un backend funcional desarrollado
con Spring Boot.

Esta fase implementa una característica central del proyecto:

**Generar software ejecutable a partir del diseño UML.**

El sistema debe recibir como entrada un diagrama de clases UML y
producir un proyecto backend profesional con arquitectura por capas.

------------------------------------------------------------------------

# Contexto del módulo

La plataforma busca aumentar la productividad del ingeniero de software
automatizando la construcción inicial de aplicaciones.

A partir del modelo UML, el sistema debe generar:

-   Entidades.
-   Repositorios.
-   Servicios.
-   Controladores.
-   DTOs.
-   Configuraciones necesarias.

Flujo general:

    Modelo UML

          |

    Analizador del modelo

          |

    Motor generador

          |

    Proyecto Spring Boot

          |

    Aplicación ejecutable

------------------------------------------------------------------------

# Restricción fundamental

El generador debe crear código únicamente basado en el modelo UML
existente.

No debe inventar entidades o lógica de negocio.

La fuente de información será:

-   Clases UML.
-   Atributos UML.
-   Métodos UML.
-   Relaciones UML.
-   Cardinalidades UML.

------------------------------------------------------------------------

# Tecnologías obligatorias

## Código generado

Utilizar:

-   Java 21.
-   Spring Boot 3.

------------------------------------------------------------------------

## Persistencia generada

Utilizar:

-   Spring Data JPA.
-   Hibernate.
-   PostgreSQL.

------------------------------------------------------------------------

## Herramientas de generación

Utilizar una estrategia basada en:

-   JavaPoet.
-   Plantillas FreeMarker.
-   Velocity.

------------------------------------------------------------------------

# Arquitectura del módulo generador

Crear:

    backend/src/main/java/com/caseplatform/generator/

Estructura:

    generator/

    ├── controller/
    ├── service/
    ├── analyzer/
    ├── template/
    ├── builder/
    ├── model/
    └── dto/

------------------------------------------------------------------------

# 1. Analizador del modelo UML

Crear:

    UMLAnalyzerService

Responsabilidad:

Leer el modelo UML almacenado y transformarlo en una estructura
preparada para generación.

------------------------------------------------------------------------

Debe analizar:

## Clases

Obtener:

-   Nombre.
-   Atributos.
-   Métodos.

------------------------------------------------------------------------

## Atributos

Obtener:

-   Nombre.
-   Tipo.
-   Visibilidad.

------------------------------------------------------------------------

## Relaciones

Obtener:

-   Clase origen.
-   Clase destino.
-   Tipo.
-   Cardinalidad.

------------------------------------------------------------------------

# 2. Creación del modelo intermedio de generación

Crear objetos internos:

    GeneratedEntityModel

    GeneratedFieldModel

    GeneratedRelationModel

------------------------------------------------------------------------

Ejemplo:

Entrada UML:

    Cliente

    nombre:String
    edad:Integer

Modelo intermedio:

    Entity:

    Cliente

    Fields:

    nombre String

    edad Integer

------------------------------------------------------------------------

# 3. Generación de entidades JPA

Crear generador:

    EntityGeneratorService

------------------------------------------------------------------------

Debe generar:

Ejemplo:

    Cliente.java

Con:

-   1.  
-   2.  
-   3.  
-   Relaciones JPA.

------------------------------------------------------------------------

Ejemplo conceptual:

``` java
@Entity
public class Cliente {

@Id
private Long id;

private String nombre;

}
```

------------------------------------------------------------------------

# 4. Mapeo de tipos UML a Java

Implementar conversión:

  UML       Java
  --------- -----------
  String    String
  Integer   Integer
  Long      Long
  Double    Double
  Boolean   Boolean
  Date      LocalDate

------------------------------------------------------------------------

# 5. Generación de Repository

Crear:

    RepositoryGeneratorService

------------------------------------------------------------------------

Debe generar:

Ejemplo:

    ClienteRepository.java

Con:

``` java
JpaRepository<Cliente, Long>
```

------------------------------------------------------------------------

# 6. Generación de Service

Crear:

    ServiceGeneratorService

------------------------------------------------------------------------

Debe generar:

Ejemplo:

    ClienteService.java

Debe incluir:

-   CRUD básico.
-   Validaciones básicas.
-   Uso del Repository.

------------------------------------------------------------------------

# 7. Generación de Controller REST

Crear:

    ControllerGeneratorService

------------------------------------------------------------------------

Debe generar:

Ejemplo:

    ClienteController.java

Endpoints:

    GET

    POST

    PUT

    DELETE

------------------------------------------------------------------------

# 8. Generación de DTOs

Crear:

    DTOGeneratorService

------------------------------------------------------------------------

Debe generar:

Ejemplo:

    ClienteDTO.java

------------------------------------------------------------------------

Regla obligatoria:

No exponer directamente entidades JPA.

Flujo:

    Entity

     |

    Service

     |

    DTO

     |

    Controller

------------------------------------------------------------------------

# 9. Generación de relaciones JPA

El generador debe interpretar cardinalidades UML.

------------------------------------------------------------------------

## Uno a muchos

UML:

    Cliente 1 ---- * Venta

Generar:

``` java
@OneToMany
```

------------------------------------------------------------------------

## Muchos a uno

Generar:

``` java
@ManyToOne
```

------------------------------------------------------------------------

## Uno a uno

Generar:

``` java
@OneToOne
```

------------------------------------------------------------------------

# 10. Generación de estructura completa del proyecto

El resultado debe ser un proyecto Spring Boot completo.

Estructura generada:

    generated-project/

    ├── src/main/java/

    │
    ├── entity/
    ├── repository/
    ├── service/
    ├── controller/
    └── dto/

    ├── pom.xml

    └── application.yml

------------------------------------------------------------------------

# 11. Configuración PostgreSQL generada

Generar:

    application.yml

Incluyendo:

-   Driver PostgreSQL.
-   Configuración JPA.
-   Variables de entorno.

------------------------------------------------------------------------

# 12. Endpoint del generador

Crear controlador:

    GeneratorController

------------------------------------------------------------------------

Endpoint:

    POST /api/generator/project/{modeloId}

------------------------------------------------------------------------

Proceso:

    Usuario solicita generación

            |

    Analizar UML

            |

    Generar archivos

            |

    Crear proyecto

            |

    Entregar resultado

------------------------------------------------------------------------

# 13. Descarga del proyecto generado

Implementar generación de paquete:

Formato:

    .zip

Contenido:

Proyecto Spring Boot completo.

------------------------------------------------------------------------

# 14. Validación del código generado

Antes de entregar el proyecto:

El sistema debe verificar:

-   Clases generadas correctamente.
-   Relaciones válidas.
-   Compilación básica.
-   Estructura correcta.

------------------------------------------------------------------------

# 15. Integración con historial UML

Toda generación debe registrar:

    Usuario

    Modelo utilizado

    Fecha

    Versión UML

    Proyecto generado

------------------------------------------------------------------------

# 16. Integración con IA

La IA creada anteriormente puede utilizar el generador.

Ejemplo:

Usuario:

    Generar backend del modelo actual

Flujo:

    IA

    ↓

    GeneratorService

    ↓

    Proyecto Spring Boot

------------------------------------------------------------------------

# 17. Pruebas del generador

Implementar pruebas:

## Generación entidad

Validar:

-   Clase Java creada.
-   Atributos correctos.

------------------------------------------------------------------------

## Generación relaciones

Validar:

-   Anotaciones JPA correctas.

------------------------------------------------------------------------

## Generación completa

Validar:

-   Proyecto generado.
-   Compilación.

------------------------------------------------------------------------

Herramientas:

-   JUnit.
-   Mockito.
-   Spring Boot Test.

------------------------------------------------------------------------

# 18. Documentación del módulo

Crear:

    documentation/architecture/code-generator.md

Documentar:

-   Arquitectura.
-   Flujo UML → Código.
-   Plantillas utilizadas.
-   Reglas de generación.

------------------------------------------------------------------------

# Criterios obligatorios para finalizar la fase

La fase está terminada únicamente si:

## Generación

✅ Un modelo UML puede generar un proyecto Spring Boot.

✅ Se generan entidades.

✅ Se generan repositories.

✅ Se generan services.

✅ Se generan controllers.

✅ Se generan DTOs.

------------------------------------------------------------------------

## Base de datos

✅ Relaciones UML generan relaciones JPA.

✅ PostgreSQL queda configurado.

------------------------------------------------------------------------

## Calidad

✅ Código generado compila.

✅ Arquitectura generada cumple capas requeridas.

✅ Existen pruebas automatizadas.

------------------------------------------------------------------------

# Resultado final esperado

Al finalizar esta fase la plataforma debe transformar diseño en
software:

    Diagrama UML

          |

    Generador automático

          |

    Spring Boot Backend

          |

    Entity
    Repository
    Service
    Controller
    DTO

          |

    Aplicación ejecutable

El ingeniero podrá diseñar un modelo conceptual y obtener una base
funcional del backend automáticamente.

------------------------------------------------------------------------

# Restricciones de esta fase

No desarrollar todavía:

-   Integración Enterprise Architect.
-   Aplicación Flutter.
-   Offline móvil.
-   IA local móvil.
-   Despliegue AWS.

Esta fase únicamente implementa la generación automática de backend
desde UML.
