# FASE 8 --- Conversión de imagen a modelo UML mediante visión artificial

# Proyecto

## Plataforma CASE colaborativa inteligente para diseño UML y generación automática de software

------------------------------------------------------------------------

# Instrucciones para el agente Antigravity

## Objetivo de la fase

Implementar un módulo de inteligencia artificial y visión computacional
capaz de recibir una imagen de un diagrama conceptual UML y
transformarla en un modelo UML editable dentro de la plataforma.

Esta funcionalidad permitirá que los ingenieros puedan reutilizar
diseños existentes realizados en:

-   Papel.
-   Pizarra.
-   Capturas de pantalla.
-   Documentos.
-   Herramientas externas.

El objetivo es convertir una representación visual del diseño en una
estructura digital compatible con el núcleo UML desarrollado
anteriormente.

------------------------------------------------------------------------

# Contexto del módulo

La herramienta CASE debe permitir tres formas principales de creación de
modelos durante la demostración:

1.  Creación manual mediante editor visual UML.
2.  Creación mediante comandos de inteligencia artificial.
3.  Creación mediante una imagen del diagrama.

Esta fase implementa la tercera modalidad.

------------------------------------------------------------------------

# Restricción fundamental del módulo

El sistema NO debe simplemente mostrar una imagen dentro del editor.

Debe realizar una conversión:

    Imagen del diagrama

            |

    Procesamiento visual

            |

    Interpretación UML

            |

    Modelo UML estructurado

            |

    Diagrama editable

El resultado debe ser un conjunto de elementos UML reales:

-   Clases.
-   Atributos.
-   Relaciones.
-   Cardinalidades.

------------------------------------------------------------------------

# Tecnologías obligatorias

## Backend

Utilizar:

-   Java 21.
-   Spring Boot 3.
-   Spring Web.
-   Spring Data JPA.

------------------------------------------------------------------------

## Procesamiento de imágenes

Utilizar tecnologías de visión artificial:

Opciones recomendadas:

-   OpenCV.
-   OCR.
-   Modelos de visión artificial.

------------------------------------------------------------------------

## Inteligencia artificial

Utilizar:

-   Modelo de visión compatible.
-   Servicio IA.
-   Modelo multimodal si está disponible.

------------------------------------------------------------------------

# Arquitectura del módulo

La arquitectura debe seguir:

    Usuario

       |

    Carga imagen

       |

    Servicio de procesamiento visual

       |

    Reconocimiento OCR

       |

    Interpretación UML

       |

    Modelo UML

       |

    Editor visual

------------------------------------------------------------------------

# 1. Creación del módulo Image To UML

Crear:

    backend/src/main/java/com/caseplatform/imageuml/

Estructura:

    imageuml/

    ├── controller/
    ├── service/
    ├── processor/
    ├── detector/
    ├── parser/
    ├── model/
    └── dto/

------------------------------------------------------------------------

# 2. Servicio de carga de imágenes

Crear:

    ImageUploadController

Responsabilidad:

Recibir imágenes enviadas por el usuario.

------------------------------------------------------------------------

Endpoint:

    POST /api/imageuml/upload

------------------------------------------------------------------------

Debe aceptar:

Formatos:

    PNG

    JPG

    JPEG

------------------------------------------------------------------------

Validaciones:

-   Tamaño máximo permitido.
-   Formato válido.
-   Imagen no vacía.

------------------------------------------------------------------------

# 3. Almacenamiento temporal de imágenes

Implementar almacenamiento temporal.

Opciones:

-   Sistema de archivos.
-   Amazon S3 preparado para futuras fases.

------------------------------------------------------------------------

Registrar:

    idImagen

    nombreArchivo

    usuario

    fechaCarga

    estadoProcesamiento

------------------------------------------------------------------------

# 4. Procesamiento inicial de imagen

Crear:

    ImageProcessorService

Responsabilidades:

Preparar imagen para análisis.

Procesos:

-   Redimensionamiento.
-   Eliminación de ruido.
-   Mejora de contraste.
-   Detección de bordes.

------------------------------------------------------------------------

Objetivo:

Mejorar la calidad antes del reconocimiento.

------------------------------------------------------------------------

# 5. Reconocimiento de elementos UML

Crear:

    UMLDetectorService

Debe detectar:

## Clases

Ejemplo visual:

    ----------------
    Cliente
    ----------------
    nombre:String
    edad:int
    ----------------

------------------------------------------------------------------------

Debe extraer:

-   Nombre de clase.
-   Atributos.
-   Tipos de datos.

------------------------------------------------------------------------

# 6. Reconocimiento OCR

Implementar reconocimiento de texto.

Debe detectar:

## Nombre de clases

Ejemplo:

    Cliente

------------------------------------------------------------------------

## Atributos

Ejemplo:

    nombre:String

------------------------------------------------------------------------

## Métodos

Ejemplo:

    calcularTotal()

------------------------------------------------------------------------

El resultado debe convertirse en texto estructurado.

------------------------------------------------------------------------

# 7. Reconocimiento de relaciones UML

El sistema debe detectar conexiones entre clases.

Debe identificar:

-   Líneas.
-   Flechas.
-   Conectores.

------------------------------------------------------------------------

Ejemplo:

Imagen:

    Cliente -------- Venta

Resultado:

    RelacionUML

    origen:
    Cliente

    destino:
    Venta

------------------------------------------------------------------------

# 8. Reconocimiento de cardinalidades

Debe intentar identificar:

    1

    0..1

    *

    1..*

------------------------------------------------------------------------

Ejemplo:

Imagen:

    Cliente 1 -------- * Venta

Resultado:

    Cliente

    cardinalidad:
    1

    Venta

    cardinalidad:
    *

------------------------------------------------------------------------

# 9. Conversión a modelo UML interno

Una vez interpretada la imagen, utilizar las entidades existentes:

    ClaseUML

    AtributoUML

    MetodoUML

    RelacionUML

------------------------------------------------------------------------

Flujo:

    Imagen

    ↓

    Interpretación

    ↓

    Crear entidades UML

    ↓

    Guardar PostgreSQL

    ↓

    Mostrar en editor

------------------------------------------------------------------------

# 10. Integración con editor visual UML

Después de procesar la imagen:

El usuario debe poder:

-   Visualizar el modelo generado.
-   Modificar elementos.
-   Corregir errores.
-   Continuar trabajando normalmente.

------------------------------------------------------------------------

Ejemplo:

Imagen inicial:

    Diagrama dibujado

Resultado:

    Editor UML editable

------------------------------------------------------------------------

# 11. Interfaz frontend

Crear módulo:

    frontend/src/features/imageuml/

Estructura:

    imageuml/

    ├── components/
    ├── services/
    └── hooks/

------------------------------------------------------------------------

Componentes:

## ImageUploader

Permitir:

-   Seleccionar imagen.
-   Enviar procesamiento.

------------------------------------------------------------------------

## ProcessingStatus

Mostrar:

    Procesando imagen...

    Detectando clases...

    Generando modelo UML...

------------------------------------------------------------------------

## ResultPreview

Mostrar:

-   Modelo generado.
-   Errores detectados.

------------------------------------------------------------------------

# 12. Corrección manual del modelo generado

Debido a posibles errores de reconocimiento, el usuario debe poder
modificar:

-   Nombres.
-   Atributos.
-   Relaciones.
-   Cardinalidades.

La corrección debe utilizar el editor UML existente.

------------------------------------------------------------------------

# 13. Integración con historial y versiones

Toda conversión de imagen debe generar una versión del modelo.

Ejemplo:

    Versión 1

    Generada desde imagen

Debe registrar:

-   Usuario.
-   Fecha.
-   Imagen origen.
-   Cambios realizados.

------------------------------------------------------------------------

# 14. Integración con colaboración

Cuando un usuario importe una imagen y genere UML:

Los usuarios conectados deben recibir:

    Nuevo modelo generado desde imagen

mediante WebSocket.

------------------------------------------------------------------------

# 15. Seguridad

Validar:

-   Usuario autenticado.
-   Permisos del proyecto.
-   Archivos permitidos.

------------------------------------------------------------------------

# 16. Pruebas del módulo

Implementar pruebas:

## Carga de imagen

Validar:

-   Formatos permitidos.
-   Archivos inválidos.

------------------------------------------------------------------------

## Reconocimiento

Validar:

-   Clases detectadas.
-   Atributos detectados.
-   Relaciones detectadas.

------------------------------------------------------------------------

## Conversión

Validar:

-   Imagen genera modelo UML válido.

------------------------------------------------------------------------

## Integración

Validar:

-   Modelo aparece en editor.
-   Historial actualizado.
-   Usuarios reciben cambios.

------------------------------------------------------------------------

Herramientas:

-   JUnit.
-   Mockito.
-   Spring Boot Test.

------------------------------------------------------------------------

# 17. Documentación del módulo

Crear:

    documentation/architecture/image-to-uml.md

Documentar:

-   Arquitectura.
-   Flujo de procesamiento.
-   Tecnologías utilizadas.
-   Limitaciones.
-   Casos de uso.

------------------------------------------------------------------------

# Criterios obligatorios para finalizar la fase

La fase está terminada únicamente si:

## Procesamiento

✅ El usuario puede cargar una imagen.

✅ El sistema procesa la imagen.

✅ El sistema reconoce elementos UML.

------------------------------------------------------------------------

## Conversión

✅ Se generan clases UML.

✅ Se generan atributos UML.

✅ Se generan relaciones UML.

✅ El resultado es editable.

------------------------------------------------------------------------

## Integración

✅ El modelo generado aparece en el editor visual.

✅ Se registra una versión del modelo.

✅ Se puede trabajar colaborativamente.

------------------------------------------------------------------------

## Calidad

✅ Código organizado.

✅ Errores controlados.

✅ Pruebas ejecutadas.

------------------------------------------------------------------------

# Resultado final esperado

Al finalizar esta fase la plataforma tendrá capacidad de convertir
diseños visuales en modelos digitales:

    Imagen UML

          |

    Visión Artificial

          |

    Modelo UML

          |

    Editor Visual

          |

    Sistema CASE

El ingeniero podrá tomar un diseño existente y continuar trabajando
sobre él dentro de la plataforma.

------------------------------------------------------------------------

# Restricciones de esta fase

No desarrollar todavía:

-   Generador automático Spring Boot.
-   Integración Enterprise Architect.
-   Aplicación Flutter.
-   Funcionamiento offline.
-   IA local móvil.

Esta fase únicamente implementa la transformación de imagen a modelo UML
editable.
