# FASE 4 --- Desarrollo del editor visual UML 2.5 colaborativo base

# Proyecto

## Plataforma CASE colaborativa inteligente para diseño UML y generación automática de software

------------------------------------------------------------------------

# Instrucciones para el agente Antigravity

## Objetivo de la fase

Implementar el editor visual UML de la plataforma CASE.

Esta fase tiene como objetivo transformar el núcleo UML desarrollado en
la fase anterior en una interfaz gráfica donde los ingenieros puedan
diseñar modelos conceptuales mediante una experiencia similar a
herramientas profesionales como Enterprise Architect.

El editor debe permitir crear, modificar y visualizar diagramas de
clases UML 2.5 de manera intuitiva.

------------------------------------------------------------------------

# Contexto del módulo

La plataforma debe funcionar como una pizarra digital de ingeniería de
software.

El usuario debe poder representar visualmente:

-   Clases.
-   Atributos.
-   Métodos.
-   Relaciones.
-   Cardinalidades.

El editor será la interfaz principal utilizada posteriormente por:

-   El sistema colaborativo.
-   El asistente IA.
-   El generador automático de código.
-   La integración con Enterprise Architect.

------------------------------------------------------------------------

# Tecnologías obligatorias

## Frontend Web

Utilizar:

-   React.
-   TypeScript.
-   Vite.

------------------------------------------------------------------------

## Librería gráfica

Utilizar una librería especializada para diagramas.

Tecnología recomendada:

-   React Flow.

Alternativas permitidas:

-   JointJS.
-   mxGraph.

La librería elegida debe permitir:

-   Nodos personalizados.
-   Conexiones.
-   Movimiento de elementos.
-   Eventos de edición.

------------------------------------------------------------------------

## Comunicación con Backend

Utilizar:

-   Axios.
-   API REST.

------------------------------------------------------------------------

# Arquitectura del frontend

Mantener una estructura profesional:

    frontend/src/

    ├── components/
    │
    ├── pages/
    │
    ├── features/
    │
    ├── services/
    │
    ├── hooks/
    │
    ├── models/
    │
    ├── store/
    │
    └── utils/

------------------------------------------------------------------------

# Responsabilidad de carpetas

## components

Componentes reutilizables:

Ejemplo:

-   Botones.
-   Paneles.
-   Formularios.
-   Modales.

------------------------------------------------------------------------

## features

Separar funcionalidades del sistema:

Crear:

    features/

    └── uml-editor/

------------------------------------------------------------------------

Dentro:

    uml-editor/

    ├── components/
    ├── hooks/
    ├── services/
    ├── models/
    └── utils/

------------------------------------------------------------------------

## services

Comunicación con backend:

Ejemplo:

    umlService.ts

------------------------------------------------------------------------

## models

Definir interfaces TypeScript:

Ejemplo:

    ClassUML
    AttributeUML
    RelationUML

------------------------------------------------------------------------

# 1. Creación del espacio de trabajo UML

Crear pantalla principal del editor.

Ruta:

    /projects/:id/editor

------------------------------------------------------------------------

Debe contener:

## Área principal de trabajo

Un canvas donde se dibujen elementos UML.

------------------------------------------------------------------------

## Barra de herramientas

Debe incluir acciones:

-   Crear clase.
-   Crear relación.
-   Eliminar elemento.
-   Guardar modelo.
-   Zoom.
-   Ajustar pantalla.

------------------------------------------------------------------------

## Panel lateral de propiedades

Debe mostrar información del elemento seleccionado.

------------------------------------------------------------------------

Ejemplo:

Seleccionar clase:

    Cliente

Mostrar:

    Nombre:
    Cliente

    Atributos:
    + nombre:String
    + edad:int

------------------------------------------------------------------------

# 2. Implementación de clases UML visuales

Crear componente:

    ClassNode

------------------------------------------------------------------------

Debe representar:

    ---------------------
    Cliente
    ---------------------
    + nombre:String
    + edad:int
    ---------------------
    + comprar()
    ---------------------

------------------------------------------------------------------------

Debe permitir:

-   Mover.
-   Seleccionar.
-   Editar.
-   Eliminar.

------------------------------------------------------------------------

# 3. Creación visual de clases

Implementar acción:

    Crear clase

Flujo:

    Usuario presiona botón

            |

    Sistema crea nodo UML

            |

    Usuario ingresa nombre

            |

    Guardar en backend

------------------------------------------------------------------------

Validaciones:

No permitir:

-   Clase sin nombre.
-   Nombre duplicado dentro del modelo.

------------------------------------------------------------------------

# 4. Gestión visual de atributos

Al seleccionar una clase debe permitir:

Agregar:

    nombre:String

Modificar:

    nombre -> apellido

Eliminar atributos.

------------------------------------------------------------------------

Cada cambio debe sincronizarse con:

Backend.

Entidad:

    AtributoUML

------------------------------------------------------------------------

# 5. Gestión visual de métodos

Permitir agregar:

Ejemplo:

    calcularTotal():Double

Modificar:

-   Nombre.
-   Tipo retorno.
-   Visibilidad.

------------------------------------------------------------------------

# 6. Creación de relaciones UML

Implementar conexiones entre clases.

------------------------------------------------------------------------

Debe permitir:

Seleccionar clase origen.

Seleccionar clase destino.

Crear relación.

------------------------------------------------------------------------

Tipos iniciales:

## Asociación

Ejemplo:

    Cliente -------- Venta

------------------------------------------------------------------------

## Herencia

Ejemplo:

    Persona
       |
    Empleado

------------------------------------------------------------------------

## Dependencia

Ejemplo:

    Factura ---> Impresora

------------------------------------------------------------------------

# 7. Configuración de cardinalidades

Cada relación debe permitir:

Origen:

    1
    0..1
    *
    1..*

Destino:

    1
    0..1
    *
    1..*

------------------------------------------------------------------------

Ejemplo:

    Cliente 1 -------- * Venta

------------------------------------------------------------------------

# 8. Persistencia del diagrama

Cada acción del usuario debe comunicarse con backend.

Operaciones:

## Crear clase

    POST /api/modelos/{id}/clases

------------------------------------------------------------------------

## Actualizar clase

    PUT /api/clases/{id}

------------------------------------------------------------------------

## Eliminar clase

    DELETE /api/clases/{id}

------------------------------------------------------------------------

## Crear relación

    POST /api/relaciones

------------------------------------------------------------------------

# 9. Sistema de guardado del modelo

Implementar:

## Guardado manual

Botón:

    Guardar

------------------------------------------------------------------------

## Guardado automático

Preparar arquitectura para:

-   Guardar cambios periódicamente.
-   Recuperar información.

------------------------------------------------------------------------

# 10. Manejo del estado del editor

Implementar manejo de estado.

Tecnologías permitidas:

-   Zustand.
-   Redux Toolkit.
-   Context API.

------------------------------------------------------------------------

Debe controlar:

-   Elementos actuales.
-   Elemento seleccionado.
-   Cambios pendientes.
-   Estado del canvas.

------------------------------------------------------------------------

# 11. Funcionalidades de experiencia de usuario

Implementar:

## Zoom

Permitir:

-   Acercar.
-   Alejar.

------------------------------------------------------------------------

## Movimiento del canvas

Permitir:

-   Arrastrar.
-   Navegar.

------------------------------------------------------------------------

## Selección múltiple

Preparar soporte para:

-   Seleccionar varios elementos.
-   Operaciones masivas.

------------------------------------------------------------------------

## Atajos básicos

Implementar:

    CTRL + Z
    CTRL + Y
    DELETE

------------------------------------------------------------------------

# 12. Integración con autenticación

El editor debe utilizar la seguridad creada en fases anteriores.

Debe:

-   Obtener usuario autenticado.
-   Validar permisos.
-   Asociar acciones al usuario.

------------------------------------------------------------------------

# 13. Pruebas del editor UML

Implementar pruebas:

## Componentes

Validar:

-   Renderizado de clases.
-   Edición de atributos.
-   Creación de relaciones.

------------------------------------------------------------------------

## Integración

Validar:

-   Comunicación frontend/backend.
-   Persistencia correcta.

------------------------------------------------------------------------

Herramientas:

-   React Testing Library.
-   Jest.

------------------------------------------------------------------------

# 14. Documentación del módulo

Crear:

    documentation/architecture/uml-editor.md

Documentar:

-   Arquitectura frontend.
-   Componentes.
-   Flujo de datos.
-   Integración con backend.

------------------------------------------------------------------------

# Criterios obligatorios para finalizar la fase

La fase está terminada únicamente si:

## Editor visual

✅ Existe un canvas UML funcional.

✅ Se pueden crear clases visualmente.

✅ Se pueden modificar clases.

✅ Se pueden eliminar clases.

------------------------------------------------------------------------

## Elementos UML

✅ Se pueden agregar atributos.

✅ Se pueden agregar métodos.

✅ Se pueden crear relaciones.

✅ Se pueden definir cardinalidades.

------------------------------------------------------------------------

## Integración

✅ Los cambios se guardan en backend.

✅ Los datos persisten en PostgreSQL.

------------------------------------------------------------------------

## Calidad

✅ Código organizado.

✅ Componentes reutilizables.

✅ Pruebas ejecutadas.

------------------------------------------------------------------------

# Resultado final esperado

Al finalizar esta fase debe existir un editor UML funcional:

    Usuario

       |

    Editor Visual UML

       |

    Clase UML
    Atributos
    Métodos
    Relaciones

       |

    Backend UML

El usuario debe poder diseñar un modelo conceptual completo mediante una
interfaz gráfica.

------------------------------------------------------------------------

# Restricciones de esta fase

No desarrollar todavía:

-   Colaboración WebSocket.
-   Sincronización multiusuario.
-   IA asistente.
-   Conversión imagen UML.
-   Generador automático Spring Boot.
-   Enterprise Architect.
-   Aplicación Flutter.

Esta fase únicamente construye el editor UML visual base sobre el núcleo
UML existente.
