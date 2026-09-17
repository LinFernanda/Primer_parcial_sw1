# Agente de Inteligencia Artificial para Edición UML Inteligente (Fase 7)

## 1. Visión General del Módulo

El subsistema del **Agente de Inteligencia Artificial (IA)** proporciona asistencia interactiva en lenguaje natural y por voz para la manipulación y evolución ágil de diagramas de clases conceptuales UML 2.5 en la **Plataforma CASE Colaborativa**.

El asistente permite a los ingenieros de software:
- **Interactuar en lenguaje natural (texto y voz)**: Dictar u ordenar creaciones, modificaciones, renombrados y eliminaciones de clases, atributos, cardinalidades y relaciones UML sin necesidad de arrastrar elementos manualmente.
- **Detección inteligente de ambigüedad**: Cuando el usuario emite instrucciones incompletas (por ejemplo: *"Crear cliente"*), el agente no toma acciones destructivas ni asume conjeturas; en su lugar, activa un flujo de confirmación en dos pasos (*"¿Desea crear una clase llamada 'Cliente'?"*).
- **Integración colaborativa en tiempo real**: Cualquier cambio orquestado por el agente se propaga inmediatamente a los demás ingenieros conectados mediante WebSockets (`EventPublisher`), permitiendo trabajo en equipo concurrente sin desincronizaciones.
- **Trazabilidad y auditoría completa**: Cada acción del agente se registra en la base de datos relacional (`ai_comandos_historial`) y se audita en el historial de versiones (`HistorialCambio`).

> [!IMPORTANT]
> **RESTRICCIÓN ESTRICTA DE ALCANCE**:
> El Agente IA **NO genera sistemas completos desde cero** ni arquitecturas completas monolíticas de forma autónoma. Su diseño y propósito operacional es actuar **exclusivamente como asistente interactivo de edición y manipulación del modelo conceptual UML existente**. Solicitudes de generación de sistemas completos son interceptadas y rechazadas formalmente.

---

## 2. Arquitectura General del Subsistema IA

```
  ┌─────────────────────────────────────────────────────────────────────────────┐
  │                              Frontend React                                 │
  │  ┌─────────────────────────────────┐   ┌─────────────────────────────────┐  │
  │  │        AIChatPanel              │   │           VoiceButton           │  │
  │  │  - Entrada en lenguaje natural  │   │  - Web Speech API (Dictado)     │  │
  │  │  - Confirmación interactiva     │   │  - Whisper API (Audio WAV/MP3)  │  │
  │  │  - Actualización store canvas   │   │  - Indicadores visuales en vivo │  │
  │  └────────────────┬────────────────┘   └────────────────┬────────────────┘  │
  └───────────────────┼─────────────────────────────────────┼───────────────────┘
                      │ POST /ai/command                    │ POST /ai/voice
                      ▼                                     ▼
  ┌─────────────────────────────────────────────────────────────────────────────┐
  │                            AIController                                     │
  │            /api/modelos/{id}/ai/command  |  /api/modelos/{id}/ai/voice       │
  └───────────────────────────────────┬─────────────────────────────────────────┘
                                      │
                                      ▼
  ┌─────────────────────────────────────────────────────────────────────────────┐
  │                           AIAgentService                                    │
  │       (Orquestador transaccional de comandos de manipulación UML)           │
  └───────────────┬───────────────────────────────────────────┬─────────────────┘
                  │                                           │
                  ▼                                           ▼
  ┌───────────────────────────────┐           ┌───────────────────────────────┐
  │       AICommandParser         │           │   Validación de Ambigüedad    │
  │  ┌─────────────────────────┐  │           │   y Flujo de Confirmación     │
  │  │ OpenAI LLM (GPT-4o)     │  │           │   - requiresConfirmation: true│
  │  ├─────────────────────────┤  │           │   - 2-Step interactive prompt │
  │  │ Motor NLP Semántico     │  │           └───────────────────────────────┘
  │  │ Determinístico Local    │  │
  │  └─────────────────────────┘  │
  └───────────────┬───────────────┘
                  │ ParsedAIAction
                  ▼
  ┌─────────────────────────────────────────────────────────────────────────────┐
  │                  Servicios Nucleares de Modelado UML                        │
  │   - ClaseUMLService       - AtributoUMLService    - RelacionUMLService      │
  └───────┬───────────────────────────┬───────────────────────────────┬─────────┘
          │                           │                               │
          ▼                           ▼                               ▼
  ┌───────────────┐           ┌───────────────┐               ┌───────────────┐
  │ EventPublisher│           │VersionService │               │ AICommand-    │
  │ (WebSocket    │           │ (Historial de │               │ HistoryRepo   │
  │  Colaborativo)│           │  Cambios F6)  │               │ (Auditoría)   │
  └───────────────┘           └───────────────┘               └───────────────┘
```

---

## 3. Estrategia Híbrida de Procesamiento NLP

El componente `AICommandParser` utiliza una estrategia **híbrida de alta resiliencia**:

```
                              ┌────────────────────────┐
                              │ Prompt en Lenguaje     │
                              │ Natural del Usuario    │
                              └───────────┬────────────┘
                                          │
                                          ▼
                       ¿ai.openai.api-key configurada?
                                     /        \
                                   SÍ          NO
                                  /              \
                                 ▼                \
              ┌────────────────────────┐           \
              │ Llamada a OpenAI LLM   │            \
              │ (chat/completions)     │             \
              └───────────┬────────────┘              \
                          │                            \
                    ¿Llamada exitosa?                   \
                       /        \                        \
                     SÍ          NO (timeout / cuota)     \
                    /              \                       \
                   ▼                └───────────────────┐   ▼
       ┌────────────────────────┐            ┌───────────────────────────────┐
       │ Acción JSON generada   │            │ Motor NLP de Reglas Semánticas│
       │ por modelo de lenguaje │            │ Determinísticas Local         │
       └────────────────────────┘            └───────────────┬───────────────┘
                                                             │
                                                             ▼
                                             ┌───────────────────────────────┐
                                             │ ParsedAIAction estructurada   │
                                             └───────────────────────────────┘
```

### 3.1 Motor Determinístico Local
Garantiza funcionamiento fuera de línea o sin credenciales comerciales. Emplea patrones semánticos compilados para:
- Creación de clases simples y compuestas (`crear clase <Nombre> con atributos <campo> <tipo>`).
- Renombrado de clases (`renombrar clase <Viejo> a <Nuevo>`).
- Eliminación de clases (`eliminar clase <Nombre>`).
- Manipulación de atributos (`agregar atributo <nombre> <tipo> a <Clase>`, `eliminar atributo <nombre> de <Clase>`).
- Creación de relaciones (`crear relacion entre <Origen> y <Destino> con cardinalidad <C1> a <C2>`).
- Relaciones de Herencia (`<Hijo> hereda de <Padre>`).
- Modificación de cardinalidades (`cambiar relacion <Origen>-<Destino> a 1:N`).

### 3.2 Detección de Ambigüedad y Confirmación en Dos Pasos
Si una instrucción es incompleta o puede interpretarse de múltiples maneras (ej. *"Crear factura"*), el parser asigna:
- `tipoOperacion`: `CONFIRMATION_REQUIRED`
- `requiereConfirmacion`: `true`
- `preguntaConfirmacion`: *"¿Desea crear una clase llamada 'Factura'?"*

El backend guarda el intento en el historial y responde al frontend sin mutar el modelo. El panel de chat renderiza botones directos:
- **Sí, crear clase** (ejecuta el comando con `confirmado: true` y `accionConfirmada`).
- **Cancelar** (descarta la operación).

---

## 4. Integración con el Motor UML y Colaboración Concurrente

Cada operación interpretada se ejecuta mediante llamadas directas a los servicios del núcleo UML:
1. **Creación/Edición**:
   - `ClaseUMLService.crearClase(modeloId, dto)` con cálculo inteligente de posición en la cuadrícula del lienzo (distribución matemática por filas de 4 columnas para evitar superposiciones).
   - `AtributoUMLService.agregarAtributo(claseId, dto)` con visibilidad e inferencia de tipos (`String`, `Integer`, `Double`, `Boolean`, `LocalDate`).
   - `RelacionUMLService.crearRelacion(dto)` verificando la existencia previa de las clases origen y destino.
2. **Difusión en Tiempo Real**:
   - Invocación de `EventPublisher.publishEvent("/topic/modelo/{id}", evento)` con el tipo de operación (`CREATE`, `UPDATE`, `DELETE`) y el payload modificado.
   - Todos los usuarios concurrentes ven aparecer instantáneamente el nodo o la arista en su canvas sin recargar.
3. **Trazabilidad y Control de Versiones**:
   - Invocación de `VersionService.registrarCambio(...)` (Fase 6), permitiendo que las acciones realizadas por la IA queden completamente documentadas en el historial de cambios del modelo.

---

## 5. Entrada y Procesamiento por Voz

La plataforma soporta comandos dictados por voz a través de dos mecanismos:

### 5.1 Web Speech API (Frontend Nativo)
- Implementado en el componente `VoiceButton`.
- Emplea `SpeechRecognition` / `webkitSpeechRecognition` para transcripción directa en el navegador en tiempo real.
- Permite reconocimiento continuo con retroalimentación visual animada (onda de pulso roja).
- Transfiere el texto reconocido automáticamente al campo de entrada o lo despacha directamente al Agente IA.

### 5.2 Endpoint de Audio y Whisper (Backend)
- Endpoint `POST /api/modelos/{id}/ai/voice`.
- Acepta `textoTranscrito` directo o audio codificado en Base64 para transcripción con modelos Whisper configurados en `application.yml` (`ai.whisper.model`).

---

## 6. Persistencia y Auditoría de Comandos IA

### 6.1 Tabla `ai_comandos_historial`

```sql
CREATE TABLE ai_comandos_historial (
    id BIGSERIAL PRIMARY KEY,
    tipo_operacion VARCHAR(50) NOT NULL,
    elemento_objetivo VARCHAR(100),
    parametros TEXT,
    prompt_original TEXT NOT NULL,
    respuesta_generada TEXT,
    exitoso BOOLEAN NOT NULL DEFAULT TRUE,
    requiere_confirmacion BOOLEAN NOT NULL DEFAULT FALSE,
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    usuario_email VARCHAR(150) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modelo_id BIGINT NOT NULL REFERENCES modelos_uml(id) ON DELETE CASCADE
);
```

---

## 7. Endpoints REST del Subsistema IA

| Método | Endpoint | Roles | Descripción |
|---|---|---|---|
| `POST` | `/api/modelos/{id}/ai/command` | Autenticado | Procesa una instrucción en lenguaje natural o ejecuta una acción confirmada. |
| `POST` | `/api/modelos/{id}/ai/voice` | Autenticado | Procesa comandos dictados por voz (transcripción o audio). |
| `GET` | `/api/modelos/{id}/ai/history` | Autenticado | Retorna el historial cronológico de comandos ejecutados sobre el modelo. |

---

## 8. Verificación y Cobertura de Pruebas

El subsistema cuenta con una suite completa de pruebas automatizadas:

| Componente | Tipo de Prueba | Archivo | Casos Verificados |
|---|---|---|---|
| `AICommandParser` | Unitaria | `AICommandParserTest.java` | Clases simples, clases con atributos múltiples, renombrado, eliminación, atributos, relaciones de asociación, herencia directa, detección de ambigüedad y rechazo de generación de sistemas completos. |
| `AIAgentService` | Unitaria (Mocks) | `AIAgentServiceTest.java` | Orquestación completa, delegación a servicios UML, manejo de ambigüedad, ejecución de confirmaciones, comandos por voz y auditoría. |
| `AIController` | Integración MockMvc | `AIControllerIntegrationTest.java` | Peticiones HTTP seguras con JWT, validación de endpoints `/command`, `/voice` e `/history`, verificación de respuestas JSON y persistencia en H2/PostgreSQL. |
| `aiService` & Store | Frontend (Vitest) | `ai.test.ts` | Peticiones API cliente, flujo de confirmación, actualización inmediata de nodos y aristas en `useUMLStore`, restricción de alcance. |
