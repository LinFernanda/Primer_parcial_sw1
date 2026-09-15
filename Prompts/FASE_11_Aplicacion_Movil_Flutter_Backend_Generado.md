# FASE 11 --- Desarrollo de aplicación móvil Flutter utilizando backend generado

# Proyecto

## Plataforma CASE colaborativa inteligente para diseño UML y generación automática de software

------------------------------------------------------------------------

# Instrucciones para el agente Antigravity

## Objetivo de la fase

Desarrollar una aplicación móvil utilizando Flutter que permita
demostrar el funcionamiento del backend generado automáticamente por la
plataforma CASE.

Esta fase tiene como propósito validar que el proceso completo de
ingeniería funciona correctamente:

    Modelo UML

          ↓

    Generador automático

          ↓

    Backend Spring Boot

          ↓

    Aplicación móvil Flutter

          ↓

    Sistema funcionando

La aplicación móvil será utilizada como demostración práctica del
producto generado por la herramienta.

------------------------------------------------------------------------

# Contexto del módulo

Uno de los objetivos principales del proyecto es demostrar que a partir
de un diseño conceptual UML se puede generar software funcional.

La plataforma debe ser capaz de generar un backend y posteriormente
consumirlo desde una aplicación móvil real.

La aplicación móvil puede representar cualquier sistema de gestión, por
ejemplo:

-   Barbería.
-   Restaurante.
-   Hospital.
-   Ventas.
-   Inventario.
-   Reservas.

El objetivo no es construir un sistema empresarial completo, sino
demostrar la integración entre:

-   Backend generado.
-   Frontend móvil.
-   Inteligencia artificial.
-   Funcionamiento offline.

------------------------------------------------------------------------

# Tecnologías obligatorias

## Aplicación móvil

Utilizar:

-   Flutter.
-   Dart.

------------------------------------------------------------------------

## Comunicación con backend

Utilizar:

-   API REST.
-   JSON.
-   HTTP Client.

------------------------------------------------------------------------

## Backend consumido

Debe ser:

-   Spring Boot generado por la plataforma.
-   Arquitectura por capas.
-   PostgreSQL.

------------------------------------------------------------------------

# Arquitectura general del módulo móvil

La arquitectura debe seguir:

    Aplicación Flutter

            |

    Servicios API

            |

    Backend Spring Boot

            |

    PostgreSQL

------------------------------------------------------------------------

# Arquitectura interna Flutter

Organizar el proyecto:

    mobile/lib/

    ├── core/
    │
    ├── models/
    │
    ├── services/
    │
    ├── repositories/
    │
    ├── providers/
    │
    ├── screens/
    │
    ├── widgets/
    │
    └── main.dart

------------------------------------------------------------------------

# Responsabilidad de carpetas

## models

Contendrá:

-   Modelos de datos.
-   Objetos JSON.

------------------------------------------------------------------------

## services

Responsable de:

-   Comunicación HTTP.
-   Consumo de APIs.

------------------------------------------------------------------------

## repositories

Responsable de:

-   Manejo de datos.
-   Separación entre interfaz y fuente de información.

------------------------------------------------------------------------

## providers

Responsable de:

-   Estado global.
-   Gestión de información compartida.

------------------------------------------------------------------------

## screens

Contendrá:

-   Pantallas principales.

------------------------------------------------------------------------

## widgets

Componentes reutilizables.

------------------------------------------------------------------------

# 1. Selección del sistema de demostración

Crear una aplicación móvil de ejemplo.

El agente debe seleccionar un dominio sencillo.

Ejemplo recomendado:

## Sistema de barbería

Entidades:

    Cliente

    Barbero

    Servicio

    Reserva

    Pago

------------------------------------------------------------------------

El sistema debe permitir demostrar:

-   Creación de registros.
-   Consulta de información.
-   Actualización de datos.

------------------------------------------------------------------------

# 2. Integración con backend generado

La aplicación debe consumir el backend generado en la FASE 9.

No crear APIs manualmente si ya fueron generadas.

------------------------------------------------------------------------

Implementar:

-   Cliente HTTP.
-   Manejo de respuestas.
-   Manejo de errores.

------------------------------------------------------------------------

# 3. Configuración de comunicación REST

Crear:

    services/api_service.dart

Responsabilidades:

-   Configurar URL base.
-   Ejecutar solicitudes HTTP.
-   Manejar respuestas.

------------------------------------------------------------------------

Operaciones necesarias:

## GET

Consultar información.

------------------------------------------------------------------------

## POST

Crear registros.

------------------------------------------------------------------------

## PUT

Actualizar registros.

------------------------------------------------------------------------

## DELETE

Eliminar registros.

------------------------------------------------------------------------

# 4. Implementación de autenticación móvil

Integrar con el sistema de seguridad creado.

Debe permitir:

-   Inicio de sesión.
-   Almacenamiento de token JWT.
-   Envío del token en solicitudes.

------------------------------------------------------------------------

Flujo:

    Usuario

     |

    Login

     |

    Backend

     |

    JWT

     |

    Aplicación Flutter

------------------------------------------------------------------------

# 5. Desarrollo de interfaz móvil

Crear pantallas:

## Login

Debe permitir:

-   Email.
-   Password.

------------------------------------------------------------------------

## Inicio

Mostrar:

-   Información principal.
-   Acciones disponibles.

------------------------------------------------------------------------

## Gestión principal

Implementar CRUD del dominio elegido.

Ejemplo:

Barbería:

    Clientes

    Reservas

    Servicios

------------------------------------------------------------------------

# 6. Integración de inteligencia artificial en móvil

La aplicación debe incorporar IA en alguna funcionalidad.

La interacción puede ser:

-   Texto.
-   Voz.
-   Asistente.

------------------------------------------------------------------------

Ejemplo:

Usuario:

    Registrar una reserva para Juan mañana

La aplicación interpreta:

    Crear Reserva

    Cliente:
    Juan

------------------------------------------------------------------------

# 7. Preparación para IA local

La arquitectura debe permitir incorporar posteriormente IA offline.

Crear separación:

    ai/

    ├── AIService

    ├── LocalAIService

    └── CloudAIService

------------------------------------------------------------------------

No depender directamente de un proveedor externo.

------------------------------------------------------------------------

# 8. Manejo de estado

Implementar una solución profesional:

Opciones:

-   Riverpod.
-   Bloc.
-   Provider.

------------------------------------------------------------------------

Debe controlar:

-   Usuario autenticado.
-   Datos cargados.
-   Estados de conexión.
-   Errores.

------------------------------------------------------------------------

# 9. Manejo de errores

Implementar:

-   Errores de conexión.
-   Errores del servidor.
-   Datos inválidos.

------------------------------------------------------------------------

Mostrar mensajes claros al usuario.

Ejemplo:

    No existe conexión con el servidor.
    Los datos serán sincronizados posteriormente.

------------------------------------------------------------------------

# 10. Diseño preparado para funcionamiento offline

Aunque la implementación completa offline pertenece a la siguiente fase,
esta aplicación debe quedar preparada.

Debe separar:

    Fuente remota

            |

    Fuente local

            |

    Repositorio

------------------------------------------------------------------------

Arquitectura esperada:

    UI Flutter

          |

    Repository

          |

    ----------------

    |              |

    API          Base local

------------------------------------------------------------------------

# 11. Pruebas de la aplicación móvil

Implementar:

## Pruebas unitarias

Validar:

-   Servicios.
-   Modelos.
-   Lógica.

------------------------------------------------------------------------

## Pruebas de widgets

Validar:

-   Pantallas.
-   Componentes.

------------------------------------------------------------------------

## Pruebas integración

Validar:

-   Comunicación con backend.

------------------------------------------------------------------------

Herramientas:

-   Flutter Test.

------------------------------------------------------------------------

# 12. Documentación del módulo

Crear:

    documentation/mobile/flutter-application.md

Debe documentar:

-   Arquitectura Flutter.
-   Comunicación backend.
-   Pantallas.
-   Funcionalidades.
-   Integración IA.

------------------------------------------------------------------------

# Criterios obligatorios para finalizar la fase

La fase está terminada únicamente si:

## Aplicación móvil

✅ La aplicación Flutter compila correctamente.

✅ Puede ejecutarse en dispositivo o emulador.

------------------------------------------------------------------------

## Integración backend

✅ Consume el backend generado.

✅ Puede realizar operaciones CRUD.

✅ Maneja autenticación.

------------------------------------------------------------------------

## IA

✅ Existe una funcionalidad con inteligencia artificial.

------------------------------------------------------------------------

## Arquitectura

✅ Código organizado.

✅ Separación por capas.

✅ Preparación para offline.

------------------------------------------------------------------------

## Calidad

✅ Pruebas ejecutadas.

✅ Errores controlados.

------------------------------------------------------------------------

# Resultado final esperado

Al finalizar esta fase debe existir una aplicación móvil funcional
conectada al software generado:

    Modelo UML

          ↓

    Backend Spring Boot

          ↓

    API REST

          ↓

    Flutter Mobile

          ↓

    Usuario final

La demostración debe evidenciar que la herramienta CASE puede generar
software que posteriormente es utilizado por una aplicación real.

------------------------------------------------------------------------

# Restricciones de esta fase

No desarrollar todavía:

-   Sincronización offline completa.
-   Base de datos local.
-   IA local en dispositivo.
-   Despliegue AWS.

Esta fase únicamente construye la aplicación Flutter funcional
consumiendo el backend generado.
