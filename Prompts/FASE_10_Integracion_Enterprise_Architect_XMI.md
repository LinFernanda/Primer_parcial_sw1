# FASE 10 --- Integración con Enterprise Architect mediante importación y exportación UML

# Proyecto

## Plataforma CASE colaborativa inteligente para diseño UML y generación automática de software

------------------------------------------------------------------------

# Instrucciones para el agente Antigravity

## Objetivo de la fase

Implementar un módulo de interoperabilidad entre la plataforma CASE
colaborativa y Enterprise Architect, permitiendo importar y exportar
modelos UML.

Esta fase tiene como objetivo integrar la herramienta desarrollada con
una herramienta CASE profesional existente, permitiendo que los
ingenieros puedan continuar trabajando sus modelos en diferentes
ambientes.

La integración debe permitir conservar la estructura del modelo
conceptual:

-   Clases.
-   Atributos.
-   Métodos.
-   Relaciones.
-   Cardinalidades.
-   Información UML.

------------------------------------------------------------------------

# Contexto del módulo

La plataforma desarrollada no busca reemplazar completamente
herramientas profesionales como Enterprise Architect.

El objetivo es complementar sus capacidades agregando:

-   Trabajo colaborativo.
-   Inteligencia artificial.
-   Generación automática de código.
-   Edición distribuida.

Por esta razón, debe existir interoperabilidad entre ambas herramientas.

------------------------------------------------------------------------

# Escenarios requeridos

## Escenario 1 --- Importación

Un ingeniero ya posee un modelo UML en Enterprise Architect.

Flujo:

    Enterprise Architect

            |

    Archivo UML

            |

    Plataforma CASE

            |

    Modelo UML editable

Resultado:

El usuario puede continuar trabajando el modelo dentro de la plataforma
colaborativa.

------------------------------------------------------------------------

## Escenario 2 --- Exportación

Un equipo desarrolla un modelo dentro de la plataforma.

Flujo:

    Plataforma CASE

            |

    Exportación UML

            |

    Enterprise Architect

            |

    Continuar diseño avanzado

------------------------------------------------------------------------

# Tecnologías obligatorias

## Backend

Utilizar:

-   Java 21.
-   Spring Boot 3.
-   Spring Web.
-   Spring Data JPA.

------------------------------------------------------------------------

## Formato de intercambio

Implementar soporte para:

-   XMI (XML Metadata Interchange).

XMI es utilizado para representar modelos UML intercambiables entre
herramientas CASE.

------------------------------------------------------------------------

## Procesamiento XML

Utilizar:

-   Java XML Parser.
-   JAXB.
-   Jackson XML.
-   DOM/SAX Parser.

------------------------------------------------------------------------

# Arquitectura del módulo

La arquitectura debe ser:

    Archivo XMI

          |

    Parser UML

          |

    Modelo intermedio

          |

    Entidades UML internas

          |

    Base de datos PostgreSQL

------------------------------------------------------------------------

# 1. Creación del módulo Enterprise Architect

Crear:

    backend/src/main/java/com/caseplatform/integration/

Estructura:

    integration/

    ├── enterprisearchitect/

    │   ├── controller/
    │   ├── service/
    │   ├── parser/
    │   ├── exporter/
    │   ├── dto/
    │   └── model/

------------------------------------------------------------------------

# 2. Implementación del importador UML

Crear:

    EnterpriseArchitectImportService

Responsabilidad:

Recibir un archivo XMI y convertirlo al modelo interno UML.

------------------------------------------------------------------------

# Flujo de importación

    Archivo XMI

            |

    Lectura XML

            |

    Identificación elementos UML

            |

    Conversión

            |

    Guardar Modelo UML

------------------------------------------------------------------------

# 3. Procesamiento del archivo XMI

El sistema debe interpretar:

## Clases UML

Extraer:

-   Nombre.
-   Identificador.
-   Tipo.

Ejemplo:

    Cliente

------------------------------------------------------------------------

## Atributos

Extraer:

-   Nombre.
-   Tipo.
-   Visibilidad.

Ejemplo:

    nombre:String

------------------------------------------------------------------------

## Métodos

Extraer:

-   Nombre.
-   Tipo retorno.
-   Parámetros.

------------------------------------------------------------------------

## Relaciones

Extraer:

-   Clase origen.
-   Clase destino.
-   Tipo relación.

------------------------------------------------------------------------

## Cardinalidades

Extraer:

-   Multiplicidad.
-   Asociación.

------------------------------------------------------------------------

# 4. Modelo intermedio de conversión

Crear una estructura temporal:

    UMLImportModel

Responsabilidad:

Representar el contenido del archivo antes de almacenarlo.

Ejemplo:

    UMLImportModel

        |
        ├── Classes
        |
        ├── Attributes
        |
        └── Relations

------------------------------------------------------------------------

# 5. Conversión a entidades internas

Después del análisis:

Transformar:

    UMLImportModel

            |

    ClaseUML

    AtributoUML

    RelacionUML

Utilizar los servicios existentes:

-   ClaseUMLService.
-   AtributoUMLService.
-   RelacionUMLService.

------------------------------------------------------------------------

# 6. Implementación del exportador UML

Crear:

    EnterpriseArchitectExportService

Responsabilidad:

Convertir modelos internos en archivos XMI compatibles.

------------------------------------------------------------------------

# Flujo de exportación

    Modelo UML PostgreSQL

            |

    Generador XMI

            |

    Archivo XML

            |

    Enterprise Architect

------------------------------------------------------------------------

# 7. Generación del archivo XMI

Debe generar:

## Paquetes UML

Representar proyectos.

------------------------------------------------------------------------

## Clases UML

Representar entidades.

------------------------------------------------------------------------

## Propiedades

Representar atributos.

------------------------------------------------------------------------

## Relaciones

Representar asociaciones.

------------------------------------------------------------------------

# 8. Integración con el frontend

Crear módulo:

    frontend/src/features/enterprisearchitect/

Estructura:

    enterprisearchitect/

    ├── components/
    ├── services/
    └── hooks/

------------------------------------------------------------------------

# Componentes necesarios

## Importar modelo

Permitir:

-   Seleccionar archivo XMI.
-   Subir archivo.
-   Procesar modelo.

------------------------------------------------------------------------

## Exportar modelo

Permitir:

-   Seleccionar modelo.
-   Descargar archivo XMI.

------------------------------------------------------------------------

# 9. Validación del modelo importado

Antes de guardar:

Validar:

-   Clases correctas.
-   Relaciones válidas.
-   Cardinalidades permitidas.
-   Integridad del modelo.

------------------------------------------------------------------------

Si existen errores:

Mostrar:

    Error de importación

    Elemento inválido:
    Clase Cliente

------------------------------------------------------------------------

# 10. Integración con versiones e historial

Toda importación debe generar una versión.

Ejemplo:

    Versión 3

    Importada desde Enterprise Architect

Registrar:

-   Usuario.
-   Fecha.
-   Archivo origen.

------------------------------------------------------------------------

# 11. Integración con colaboración

Cuando un usuario importe un modelo:

Los usuarios conectados deben recibir:

    Nuevo modelo importado

mediante WebSocket.

------------------------------------------------------------------------

# 12. Integración con generador de código

El modelo importado debe ser compatible con:

-   Generador Spring Boot.

Flujo:

    Enterprise Architect

            |

    Importación XMI

            |

    Modelo UML

            |

    Generador Backend

------------------------------------------------------------------------

# 13. Pruebas del módulo

Implementar pruebas:

## Importación

Validar:

-   Archivo válido.
-   Archivo inválido.
-   Clases detectadas.

------------------------------------------------------------------------

## Exportación

Validar:

-   XMI generado correctamente.
-   Modelo recuperable.

------------------------------------------------------------------------

## Compatibilidad

Validar:

-   Importar modelo exportado.
-   Mantener información UML.

------------------------------------------------------------------------

Herramientas:

-   JUnit.
-   Mockito.
-   Spring Boot Test.

------------------------------------------------------------------------

# 14. Documentación del módulo

Crear:

    documentation/architecture/enterprise-architect-integration.md

Documentar:

-   Arquitectura.
-   Formato XMI.
-   Flujo importación/exportación.
-   Limitaciones.

------------------------------------------------------------------------

# Criterios obligatorios para finalizar la fase

La fase está terminada únicamente si:

## Importación

✅ El sistema recibe archivos XMI.

✅ Detecta clases UML.

✅ Detecta atributos.

✅ Detecta relaciones.

✅ Genera modelo editable.

------------------------------------------------------------------------

## Exportación

✅ El sistema genera archivos XMI.

✅ Enterprise Architect puede interpretar el modelo exportado.

------------------------------------------------------------------------

## Integración

✅ Funciona con el editor UML.

✅ Funciona con versiones.

✅ Funciona con generación backend.

------------------------------------------------------------------------

## Calidad

✅ Código organizado.

✅ Errores controlados.

✅ Pruebas ejecutadas.

------------------------------------------------------------------------

# Resultado final esperado

Al finalizar esta fase la plataforma tendrá interoperabilidad con
Enterprise Architect:

    Enterprise Architect

            ⇅

    Archivo XMI

            ⇅

    Plataforma CASE

            |

    Modelo UML

            |

    Generación de software

Los ingenieros podrán utilizar ambas herramientas dentro del mismo flujo
de desarrollo.

------------------------------------------------------------------------

# Restricciones de esta fase

No desarrollar todavía:

-   Aplicación móvil Flutter.
-   Funcionamiento offline.
-   IA local móvil.
-   Despliegue AWS.

Esta fase únicamente implementa la integración UML con Enterprise
Architect.
