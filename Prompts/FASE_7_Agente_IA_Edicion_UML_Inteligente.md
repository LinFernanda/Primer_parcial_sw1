# FASE 7 --- Implementación del agente de Inteligencia Artificial para edición inteligente de modelos UML

# Proyecto

## Plataforma CASE colaborativa inteligente para diseño UML y generación automática de software

------------------------------------------------------------------------

# Instrucciones para el agente Antigravity

## Objetivo de la fase

Implementar un agente de inteligencia artificial integrado dentro de la
plataforma CASE que permita a los ingenieros modificar modelos UML
mediante instrucciones en lenguaje natural.

La inteligencia artificial debe funcionar como un asistente de edición
del modelo conceptual.

Su responsabilidad principal será interpretar comandos del usuario y
ejecutar acciones sobre el diagrama UML existente.

------------------------------------------------------------------------

# Contexto del módulo

La herramienta desarrollada busca mejorar la productividad de los
ingenieros de software.

Actualmente, para modificar un diagrama UML un diseñador debe:

-   Buscar el elemento.
-   Seleccionarlo.
-   Abrir propiedades.
-   Modificar manualmente.
-   Guardar cambios.

Con este módulo el usuario podrá expresar acciones mediante:

-   Texto.
-   Voz.

Ejemplo:

Usuario:

    Crear una clase llamada Cliente

El agente debe interpretar la intención y ejecutar:

    CREATE_CLASS
    nombre = Cliente

------------------------------------------------------------------------

# Restricción fundamental de la IA

La IA NO debe generar automáticamente un sistema completo desde un
requerimiento.

La IA NO debe recibir una descripción del problema y crear todo el
diagrama.

La IA debe funcionar únicamente como:

**Agente inteligente de manipulación del modelo UML.**

------------------------------------------------------------------------

# Ejemplos de acciones permitidas

Crear clase:

    Crear clase Cliente

------------------------------------------------------------------------

Agregar atributo:

    Agregar atributo nombre de tipo String a Cliente

------------------------------------------------------------------------

Modificar clase:

    Cambiar el nombre de Cliente a Usuario

------------------------------------------------------------------------

Eliminar elemento:

    Eliminar la clase Producto

------------------------------------------------------------------------

Crear relación:

    Relacionar Cliente con Venta con cardinalidad uno a muchos

------------------------------------------------------------------------

Cambiar cardinalidad:

    Cambiar relación Cliente-Venta a 1:N

------------------------------------------------------------------------

# Tecnologías obligatorias

## Backend

Utilizar:

-   Java 21.
-   Spring Boot 3.
-   Spring Web.
-   Spring Data JPA.

------------------------------------------------------------------------

## Inteligencia Artificial

Implementar una capa de integración IA utilizando:

Opciones permitidas:

-   OpenAI API.
-   Modelo local.
-   Servicio compatible con modelos LLM.

------------------------------------------------------------------------

## Orquestación IA

Tecnología recomendada:

-   LangChain.

Responsabilidad:

-   Interpretar intención.
-   Generar acciones estructuradas.
-   Ejecutar operaciones UML.

------------------------------------------------------------------------

## Voz

Para interacción por voz utilizar:

-   Speech To Text.
-   Whisper u otra tecnología equivalente.

------------------------------------------------------------------------

# Arquitectura del módulo IA

La arquitectura debe ser:

    Usuario

       |

    Texto / Voz

       |

    Agente IA

       |

    Interpretación de intención

       |

    Acciones UML estructuradas

       |

    Servicio UML

       |

    Modelo UML actualizado

------------------------------------------------------------------------

# 1. Creación del módulo IA

Crear:

    backend/src/main/java/com/caseplatform/ai/

Estructura:

    ai/

    ├── controller/
    ├── service/
    ├── model/
    ├── parser/
    ├── command/
    └── config/

------------------------------------------------------------------------

# 2. Diseño del modelo de comandos IA

Crear una estructura intermedia entre lenguaje natural y acciones UML.

Entidad:

    AICommand

------------------------------------------------------------------------

Debe contener:

    id

    tipoOperacion

    elementoObjetivo

    parametros

    usuario

    fecha

------------------------------------------------------------------------

# Tipos de operaciones soportadas

Crear enum:

    CREATE_CLASS

    UPDATE_CLASS

    DELETE_CLASS

    CREATE_ATTRIBUTE

    UPDATE_ATTRIBUTE

    DELETE_ATTRIBUTE

    CREATE_RELATION

    UPDATE_RELATION

    DELETE_RELATION

------------------------------------------------------------------------

# 3. Implementación del interprete de comandos

Crear:

    AICommandParser

Responsabilidad:

Convertir lenguaje natural en acciones estructuradas.

Ejemplo:

Entrada:

    Crear clase Cliente con atributo nombre String

Salida:

``` json
{
 "accion":"CREATE_CLASS",
 "clase":"Cliente",
 "atributos":[
   {
    "nombre":"nombre",
    "tipo":"String"
   }
 ]
}
```

------------------------------------------------------------------------

# 4. Implementación del agente IA

Crear:

    AIAgentService

Responsabilidades:

-   Recibir instrucciones.
-   Analizar intención.
-   Validar comandos.
-   Ejecutar acciones UML.

------------------------------------------------------------------------

Flujo:

    Usuario

     |

    Comando

     |

    IA

     |

    Validación

     |

    Servicio UML

     |

    Actualizar modelo

------------------------------------------------------------------------

# 5. Integración con el núcleo UML existente

El agente debe utilizar los servicios creados en fases anteriores.

No debe modificar directamente la base de datos.

Debe utilizar:

    ClaseUMLService

    AtributoUMLService

    RelacionUMLService

------------------------------------------------------------------------

Ejemplo:

La IA recibe:

    Crear clase Cliente

Proceso:

    IA

    ↓

    ClaseUMLService

    ↓

    Guardar ClaseUML

    ↓

    Actualizar editor

------------------------------------------------------------------------

# 6. Integración con el editor visual UML

El usuario debe poder utilizar la IA desde el editor.

Crear componente:

    frontend/src/features/ai/

------------------------------------------------------------------------

Componentes:

    AIChatPanel

    VoiceButton

    CommandHistory

------------------------------------------------------------------------

# Funcionalidades frontend

Debe permitir:

-   Escribir comandos.
-   Enviar instrucciones.
-   Mostrar respuesta.
-   Mostrar acciones realizadas.

------------------------------------------------------------------------

Ejemplo:

Usuario:

    Crear clase Cliente

Sistema:

    Clase Cliente creada correctamente

------------------------------------------------------------------------

# 7. Historial de comandos IA

Registrar:

-   Usuario.
-   Comando enviado.
-   Acción ejecutada.
-   Fecha.
-   Resultado.

------------------------------------------------------------------------

Crear:

    AICommandHistory

------------------------------------------------------------------------

Debe integrarse con:

Sistema de versiones UML.

------------------------------------------------------------------------

# 8. Integración con sistema colaborativo

Cuando la IA modifique el modelo:

Debe generar eventos WebSocket.

Ejemplo:

IA crea clase Cliente.

Flujo:

    IA

    ↓

    Servicio UML

    ↓

    Evento WebSocket

    ↓

    Usuarios conectados

------------------------------------------------------------------------

Todos los usuarios deben visualizar el cambio inmediatamente.

------------------------------------------------------------------------

# 9. Manejo de errores de interpretación

La IA debe manejar comandos ambiguos.

Ejemplo:

Usuario:

    Crear cliente

Respuesta esperada:

    ¿Desea crear una clase llamada Cliente?

------------------------------------------------------------------------

No ejecutar acciones inseguras sin confirmación.

------------------------------------------------------------------------

# 10. Seguridad del agente IA

Implementar validaciones:

-   Verificar usuario autenticado.
-   Verificar permisos.
-   Validar existencia de elementos.
-   Evitar modificaciones inválidas.

------------------------------------------------------------------------

# 11. Implementación de interacción por voz

Agregar soporte:

    Voz

    ↓

    Speech To Text

    ↓

    Agente IA

    ↓

    Acción UML

------------------------------------------------------------------------

Debe permitir comandos como:

    Agregar atributo precio tipo Double

------------------------------------------------------------------------

# 12. Pruebas del módulo IA

Implementar pruebas:

## Interpretación

Validar:

-   Comandos simples.
-   Comandos complejos.

------------------------------------------------------------------------

## Ejecución

Validar:

-   Creación de clases.
-   Modificación de atributos.
-   Creación de relaciones.

------------------------------------------------------------------------

## Seguridad

Validar:

-   Usuarios sin permisos.
-   Acciones inválidas.

------------------------------------------------------------------------

Herramientas:

-   JUnit.
-   Mockito.
-   Spring Boot Test.

------------------------------------------------------------------------

# 13. Documentación del módulo

Crear:

    documentation/architecture/artificial-intelligence.md

Documentar:

-   Arquitectura IA.
-   Flujo de comandos.
-   Integración UML.
-   Seguridad.
-   Casos de uso.

------------------------------------------------------------------------

# Criterios obligatorios para finalizar la fase

La fase está terminada únicamente si:

## Inteligencia artificial

✅ El usuario puede enviar comandos en lenguaje natural.

✅ La IA interpreta acciones UML.

✅ La IA modifica el modelo correctamente.

------------------------------------------------------------------------

## Funcionalidades UML

✅ Crear clases mediante IA.

✅ Crear atributos mediante IA.

✅ Modificar clases mediante IA.

✅ Crear relaciones mediante IA.

✅ Eliminar elementos mediante IA.

------------------------------------------------------------------------

## Integración

✅ Los cambios realizados por IA aparecen en el editor UML.

✅ Los cambios se sincronizan con usuarios conectados.

✅ Los cambios quedan registrados en historial.

------------------------------------------------------------------------

## Seguridad

✅ Solo usuarios autorizados pueden utilizar el agente.

------------------------------------------------------------------------

# Resultado final esperado

Al finalizar esta fase la plataforma tendrá un asistente inteligente:

    Usuario

       |

    Texto / Voz

       |

    Agente IA

       |

    Interpretación

       |

    Operaciones UML

       |

    Modelo actualizado

El ingeniero podrá modificar diagramas UML sin utilizar exclusivamente
herramientas gráficas tradicionales.

------------------------------------------------------------------------

# Restricciones de esta fase

No desarrollar todavía:

-   Conversión imagen a UML.
-   Generador automático Spring Boot.
-   Integración Enterprise Architect.
-   Aplicación Flutter.
-   IA local móvil offline.

Esta fase únicamente implementa el agente IA para edición inteligente de
modelos UML.
