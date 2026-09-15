# FASE 5 --- Implementación del sistema colaborativo en tiempo real para modelos UML

# Proyecto

## Plataforma CASE colaborativa inteligente para diseño UML y generación automática de software

------------------------------------------------------------------------

# Instrucciones para el agente Antigravity

## Objetivo de la fase

Transformar el editor UML individual desarrollado en la fase anterior en
una plataforma colaborativa donde múltiples ingenieros de software
puedan trabajar simultáneamente sobre un mismo modelo conceptual.

Esta fase implementa la característica principal que diferencia a la
herramienta de soluciones tradicionales como Enterprise Architect:

**El diseño colaborativo en tiempo real.**

El sistema debe permitir que varios usuarios conectados puedan
visualizar y modificar un mismo diagrama UML, manteniendo la
consistencia del modelo.

------------------------------------------------------------------------

# Contexto del módulo

La problemática principal del proyecto surge porque equipos grandes de
ingenieros pueden encontrarse distribuidos en diferentes ubicaciones
geográficas.

La herramienta debe funcionar como una pizarra digital colaborativa
donde:

-   Un ingeniero puede crear una clase.
-   Otro ingeniero puede modificar una relación.
-   Todos los participantes pueden observar los cambios inmediatamente.

El sistema debe controlar la edición simultánea para evitar
inconsistencias en el modelo UML.

------------------------------------------------------------------------

# Tecnologías obligatorias

## Backend

Utilizar:

-   Java 21.
-   Spring Boot 3.
-   Spring WebSocket.
-   Spring Data JPA.
-   PostgreSQL.

------------------------------------------------------------------------

## Comunicación en tiempo real

Implementar:

-   WebSocket.

Tecnologías recomendadas:

Backend:

-   Spring WebSocket.
-   STOMP.
-   SockJS.

Frontend:

-   WebSocket Client.
-   STOMP Client.

------------------------------------------------------------------------

# Arquitectura del módulo colaborativo

La arquitectura debe funcionar de la siguiente manera:

    Usuario A
        |
        |
    Frontend React
        |
     WebSocket
        |
    Servidor Colaborativo
        |
        |
    Modelo UML actualizado
        |
        |
    Usuarios B, C, D

------------------------------------------------------------------------

# Objetivo técnico

Implementar un sistema basado en eventos donde cada modificación
realizada sobre un modelo UML sea enviada a todos los usuarios
conectados.

------------------------------------------------------------------------

# 1. Diseño del servicio WebSocket

Crear módulo:

    backend/

    └── websocket/

Estructura:

    websocket/

    ├── WebSocketConfig
    ├── UMLSocketController
    ├── EventPublisher
    └── SessionManager

------------------------------------------------------------------------

# 2. Configuración WebSocket en Spring Boot

Crear configuración:

    WebSocketConfig

Debe configurar:

-   Endpoint de conexión.
-   Broker de mensajes.
-   Canales de comunicación.

Ejemplo conceptual:

    /ws

------------------------------------------------------------------------

# 3. Creación del canal colaborativo

Cada modelo UML debe tener un canal independiente.

Ejemplo:

Proyecto:

    Sistema Ventas

Modelo:

    Modelo UML Principal

Canal:

    /topic/modelo/1

------------------------------------------------------------------------

Los usuarios conectados al mismo modelo reciben los mismos eventos.

------------------------------------------------------------------------

# 4. Sistema de eventos UML

Crear estructura:

    UMLEvent

Debe representar acciones realizadas por usuarios.

------------------------------------------------------------------------

Tipos de eventos:

## CREATE

Crear elemento UML.

Ejemplo:

    Crear clase Cliente

------------------------------------------------------------------------

## UPDATE

Modificar elemento UML.

Ejemplo:

    Cambiar nombre de clase

------------------------------------------------------------------------

## DELETE

Eliminar elemento UML.

Ejemplo:

    Eliminar relación

------------------------------------------------------------------------

# Datos del evento

Cada evento debe contener:

    idEvento

    usuario

    modeloUML

    tipoOperacion

    elementoAfectado

    fecha

    datosCambio

------------------------------------------------------------------------

# 5. Actualización del frontend en tiempo real

Modificar el editor UML creado en la fase 4.

Agregar:

    services/

    └── websocketService.ts

------------------------------------------------------------------------

Responsabilidades:

-   Conectar al servidor WebSocket.
-   Escuchar eventos.
-   Enviar cambios.
-   Actualizar canvas UML.

------------------------------------------------------------------------

# 6. Sincronización de elementos UML

Cuando un usuario realiza una acción:

Ejemplo:

Usuario A:

    Crear clase Cliente

Flujo:

    Frontend Usuario A

            |

    Enviar evento WebSocket

            |

    Servidor colaborativo

            |

    Actualizar modelo

            |

    Enviar evento

            |

    Usuarios conectados

------------------------------------------------------------------------

Todos los usuarios deben visualizar:

    Nueva clase Cliente

sin recargar la aplicación.

------------------------------------------------------------------------

# 7. Gestión de usuarios conectados

Implementar presencia de usuarios.

El sistema debe mostrar:

-   Usuarios conectados.
-   Usuario activo.
-   Estado de conexión.

Ejemplo:

    Proyecto: Sistema Ventas

    Usuarios conectados:

    ● Juan
    ● Maria
    ● Carlos

------------------------------------------------------------------------

# 8. Indicador de edición

Implementar visualización de actividad.

Ejemplo:

Usuario:

    Juan está editando Cliente

Debe permitir saber quién está modificando un elemento.

------------------------------------------------------------------------

# 9. Control de conflictos de edición

Implementar mecanismos para evitar modificaciones incompatibles.

Problema:

Usuario A modifica:

    Clase Cliente

Usuario B elimina:

    Clase Cliente

al mismo tiempo.

------------------------------------------------------------------------

Implementar estrategia inicial:

## Bloqueo temporal del elemento

Cuando un usuario edita un elemento:

    Clase Cliente

    Estado:
    EN_EDICION
    Usuario:
    Juan

------------------------------------------------------------------------

Otros usuarios reciben aviso.

------------------------------------------------------------------------

# 10. Control de concurrencia del modelo UML

Implementar validaciones:

-   No perder cambios.
-   Mantener integridad del modelo.
-   Ordenar eventos correctamente.

------------------------------------------------------------------------

Cada modificación debe tener:

    timestamp

    usuario

    versionModelo

------------------------------------------------------------------------

# 11. Sistema de guardado colaborativo

Las modificaciones recibidas deben persistirse.

Flujo:

    Evento WebSocket

          |

    Validación

          |

    Actualizar modelo UML

          |

    Guardar PostgreSQL

------------------------------------------------------------------------

# 12. Integración con autenticación

Utilizar el sistema creado en la fase 2.

Cada conexión WebSocket debe identificar:

-   Usuario.
-   Rol.
-   Proyecto.
-   Modelo UML.

------------------------------------------------------------------------

No permitir acceso a modelos sin permisos.

------------------------------------------------------------------------

# 13. Manejo de desconexiones

Implementar:

Cuando un usuario pierde conexión:

-   Informar al equipo.
-   Liberar elementos bloqueados.
-   Mantener modelo consistente.

------------------------------------------------------------------------

Ejemplo:

    Juan desconectado

    Clase Cliente desbloqueada

------------------------------------------------------------------------

# 14. Pruebas del sistema colaborativo

Implementar pruebas:

## Conexiones simultáneas

Validar:

-   Múltiples usuarios conectados.

------------------------------------------------------------------------

## Eventos

Validar:

-   Crear elemento.
-   Modificar elemento.
-   Eliminar elemento.

------------------------------------------------------------------------

## Conflictos

Validar:

-   Ediciones simultáneas.
-   Desconexiones.

------------------------------------------------------------------------

Herramientas:

Backend:

-   JUnit.
-   Spring Boot Test.

Frontend:

-   Jest.
-   React Testing Library.

------------------------------------------------------------------------

# 15. Documentación del módulo

Crear:

    documentation/architecture/collaboration.md

Debe documentar:

-   Arquitectura WebSocket.
-   Flujo de eventos.
-   Sincronización.
-   Control de conflictos.

------------------------------------------------------------------------

# Criterios obligatorios para finalizar la fase

La fase está terminada únicamente si:

## Colaboración

✅ Dos o más usuarios pueden ingresar al mismo modelo UML.

✅ Los cambios aparecen en tiempo real.

✅ No es necesario actualizar manualmente.

------------------------------------------------------------------------

## Eventos

✅ Crear elementos sincroniza correctamente.

✅ Modificar elementos sincroniza correctamente.

✅ Eliminar elementos sincroniza correctamente.

------------------------------------------------------------------------

## Seguridad

✅ Solo usuarios autorizados pueden acceder.

------------------------------------------------------------------------

## Consistencia

✅ El modelo no pierde información.

✅ Los conflictos tienen tratamiento.

------------------------------------------------------------------------

## Calidad

✅ Código organizado.

✅ Arquitectura desacoplada.

✅ Pruebas ejecutadas.

------------------------------------------------------------------------

# Resultado final esperado

Al finalizar esta fase debe existir una pizarra UML colaborativa
funcional:

    Usuario A
          |
          |
          ↓
     Editor UML
          |
     WebSocket
          |
    Servidor colaborativo
          |
          |
     -------------------
     |        |        |
    Usuario B Usuario C Usuario D

Los ingenieros deben poder trabajar sobre un mismo diseño UML como si
estuvieran físicamente frente a una misma pizarra.

------------------------------------------------------------------------

# Restricciones de esta fase

No desarrollar todavía:

-   Inteligencia artificial.
-   Conversión imagen a UML.
-   Generador automático Spring Boot.
-   Integración Enterprise Architect.
-   Aplicación Flutter.
-   Sincronización offline.

Esta fase únicamente convierte el editor UML en una plataforma
colaborativa multiusuario.
