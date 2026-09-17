# FASE 10 — Interoperabilidad con Enterprise Architect mediante Importación y Exportación UML (XMI 2.1)

## 1. Visión General del Módulo

El subsistema de **Interoperabilidad con Enterprise Architect** dota a la **Plataforma CASE Colaborativa** de la capacidad de intercambiar modelos conceptuales con una de las herramientas CASE líderes de la industria del software: **Sparx Systems Enterprise Architect**.

El objetivo no es reemplazar a Enterprise Architect, sino complementarlo ofreciendo:
- Edición visual colaborativa y distribuida en tiempo real (Fase 5).
- Control de versiones y trazabilidad de cambios (Fase 6).
- Asistencia mediante Inteligencia Artificial generativa y NLP (Fase 7).
- Digitalización de diagramas en papel/pizarras mediante Visión Artificial (Fase 8).
- Generación automática de código backend Spring Boot 3 / Java 21 (Fase 9).

---

## 2. Arquitectura del Módulo de Interoperabilidad

El módulo implementa un pipeline bidireccional desacoplado mediante un modelo intermedio agnóstico (`UMLImportModel`):

```
                               ┌───────────────────────────┐
                               │   Enterprise Architect    │
                               └─────────────┬─────────────┘
                                             │
                                     Archivo XMI 2.1 (.xml)
                                             │
                   ┌─────────────────────────┴─────────────────────────┐
                   ▼                                                   ▲
┌───────────────────────────────────────┐   ┌───────────────────────────────────────┐
│           FLUJO DE IMPORTACIÓN        │   │          FLUJO DE EXPORTACIÓN         │
│  1. Lectura segura (Anti-XXE)         │   │  1. Consulta de entidades PostgreSQL  │
│  2. XMIParserImpl (DOM / Namespaces)  │   │  2. XMIExporterImpl                   │
│  3. Extracción de Metamodelo:         │   │  3. Construcción de AST OMG UML 2.1   │
│     - Clases, Visibilidad             │   │  4. Inyección de Extensiones EA:      │
│     - Atributos y Tipos de Datos      │   │     - Geometría de diagramas (left/top)│
│     - Operaciones y Parámetros        │   │     - Tipos de conectores Sparx       │
│     - Herencia, Asociaciones          │   │  5. Serialización XML UTF-8           │
│     - Geometría y Coordenadas         │   └───────────────────┬───────────────────┘
│  4. UMLImportModel (Intermedio)       │                       │
│  5. Validación Semántica              │                       │
│  6. Persistencia en Base de Datos     │                       │
│  7. Snapshot VersionService (Fase 6)  │                       │
│  8. Notificación WebSocket (Fase 5)   │                       │
└───────────────────┬───────────────────┘                       │
                    │                                           │
                    ▼                                           │
       ┌─────────────────────────┐                              │
       │ Base de Datos / Canvas  ├──────────────────────────────┘
       │  Plataforma CASE UML    │
       └─────────────────────────┘
```

---

## 3. Formato de Intercambio XMI (XML Metadata Interchange)

El sistema implementa soporte para **XMI 2.1 (OMG UML 2.1 - 2.5)** con namespaces estándar:
- `xmlns:xmi="http://schema.omg.org/spec/XMI/2.1"`
- `xmlns:uml="http://schema.omg.org/spec/UML/2.1"`

### 3.1 Estructura del Documento XMI Exportado
```xml
<?xml version="1.0" encoding="UTF-8"?>
<xmi:XMI xmi:version="2.1" xmlns:uml="http://schema.omg.org/spec/UML/2.1" xmlns:xmi="http://schema.omg.org/spec/XMI/2.1">
  <xmi:Documentation exporter="Enterprise Architect Compatible CASE Platform" exporterVersion="2.1"/>
  <uml:Model xmi:type="uml:Model" name="ModeloComercial" visibility="public">
    <packagedElement xmi:type="uml:Package" xmi:id="EAPK_1" name="ModeloComercial" visibility="public">
      
      <!-- Clases UML -->
      <packagedElement xmi:type="uml:Class" xmi:id="EAID_1" name="Cliente" visibility="public">
        <ownedAttribute xmi:type="uml:Property" xmi:id="EAID_ATTR_1" name="nombre" visibility="private">
          <type xmi:type="uml:PrimitiveType" href="http://schema.omg.org/spec/UML/2.1/uml.xml#String"/>
        </ownedAttribute>
        <ownedOperation xmi:type="uml:Operation" xmi:id="EAID_METH_1" name="calcularTotal" visibility="public">
          <ownedParameter xmi:id="EAID_RET_1" name="return" direction="return">
            <type xmi:type="uml:PrimitiveType" href="http://schema.omg.org/spec/UML/2.1/uml.xml#Double"/>
          </ownedParameter>
        </ownedOperation>
        <generalization xmi:type="uml:Generalization" xmi:id="EAID_GEN_1" general="EAID_2"/>
      </packagedElement>
      
      <!-- Asociaciones y Cardinalidades -->
      <packagedElement xmi:type="uml:Association" xmi:id="EAID_REL_1" name="Cliente_Pedido">
        <memberEnd xmi:idref="EAID_END_DST_1"/>
        <memberEnd xmi:idref="EAID_END_SRC_1"/>
        <ownedEnd xmi:type="uml:Property" xmi:id="EAID_END_SRC_1" type="EAID_1" association="EAID_REL_1">
          <lowerValue xmi:type="uml:LiteralInteger" value="1"/>
          <upperValue xmi:type="uml:LiteralUnlimitedNatural" value="1"/>
        </ownedEnd>
        <ownedEnd xmi:type="uml:Property" xmi:id="EAID_END_DST_1" type="EAID_3" association="EAID_REL_1" aggregation="none">
          <lowerValue xmi:type="uml:LiteralInteger" value="0"/>
          <upperValue xmi:type="uml:LiteralUnlimitedNatural" value="*"/>
        </ownedEnd>
      </packagedElement>

    </packagedElement>
  </uml:Model>

  <!-- Extensiones Sparx Systems para Preservar Geometría del Diagrama -->
  <xmi:Extension extender="Enterprise Architect" extenderID="6.5">
    <diagrams>
      <diagram xmi:id="EAID_DIAG_1">
        <elements>
          <element subject="EAID_1" seqno="1" style="DUID=D1;left=120;top=100;right=280;bottom=210;"/>
        </elements>
      </diagram>
    </diagrams>
  </xmi:Extension>
</xmi:XMI>
```

---

## 4. Mapeo Semántico de Elementos UML

| Concepto UML | Elemento Plataforma CASE | Representación XMI (Enterprise Architect) |
| :--- | :--- | :--- |
| **Clase** | `ClaseUML` | `<packagedElement xmi:type="uml:Class" name="...">` |
| **Atributo** | `AtributoUML` | `<ownedAttribute xmi:type="uml:Property" name="..." visibility="...">` |
| **Método** | `MetodoUML` | `<ownedOperation xmi:type="uml:Operation" name="...">` |
| **Herencia** | `TipoRelacionUML.HERENCIA` | `<generalization xmi:type="uml:Generalization" general="..."/>` |
| **Asociación** | `TipoRelacionUML.ASOCIACION` | `<packagedElement xmi:type="uml:Association">` con `aggregation="none"` |
| **Agregación** | `TipoRelacionUML.AGREGACION` | `<ownedEnd aggregation="shared">` |
| **Composición** | `TipoRelacionUML.COMPOSICION` | `<ownedEnd aggregation="composite">` |
| **Dependencia** | `TipoRelacionUML.DEPENDENCIA` | `<packagedElement xmi:type="uml:Dependency" client="..." supplier="...">` |
| **Cardinalidad** | `cardinalidadOrigen` / `Destino` | `<lowerValue>` y `<upperValue>` (`LiteralInteger`, `LiteralUnlimitedNatural`) |
| **Posición Canvas** | `posicionX`, `posicionY` | Extensiones `<diagram><elements><element style="left=...;top=...;"/></elements></diagram>` |

---

## 5. Especificación de Endpoints REST

| Método | Endpoint | Descripción | Formato Entrada/Salida |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/integration/ea/validate` | Valida la consistencia estructural del XMI sin persistir. Retorna resumen y métricas. | `multipart/form-data` -> `JSON` |
| `POST` | `/api/integration/ea/models/{id}/import` | Importa el archivo XMI sobre un modelo UML existente (opción de fusionar o reemplazar). | `multipart/form-data` -> `JSON` |
| `POST` | `/api/integration/ea/projects/{id}/import` | Importa el XMI creando un nuevo modelo UML dentro del proyecto. | `multipart/form-data` -> `JSON` |
| `GET` | `/api/integration/ea/models/{id}/export` | Descarga directa del archivo XML compatible con Enterprise Architect. | `application/xml` (Attachment) |
| `GET` | `/api/integration/ea/models/{id}/export/preview`| Previsualización de metadatos y contenido XML del modelo a exportar. | `JSON` |

---

## 6. Integración Transversal

### 6.1 Control de Versiones (Fase 6)
Toda importación XMI dispara la creación automática de una versión formal en `VersionService` con la etiqueta:
`"Versión Importada desde Enterprise Architect (nombre_archivo.xmi)"` e historial de auditoría detallado.

### 6.2 Colaboración en Tiempo Real (Fase 5)
Al finalizar la importación, el backend publica un evento STOMP sobre el canal `/topic/modelos/{id}` notificando a todos los colaboradores en la sala para que sus lienzos ReactFlow se actualicen de inmediato.

### 6.3 Generación de Software Backend (Fase 9)
Los modelos importados desde Enterprise Architect cumplen rigurosamente con el metamodelo UML 2.5 de la plataforma, por lo que pueden ser transformados inmediatamente en una aplicación backend ejecutable en **Spring Boot 3 + PostgreSQL** con un solo clic.

### 6.4 Asistente IA (Fase 7)
El agente de IA reconoce comandos de voz y texto en lenguaje natural:
- *"Exportar a Enterprise Architect"* / *"Exportar XMI"* / *"Descargar XMI"*.
- Notifica al usuario y prepara la descarga del archivo XML interoperable.

---

## 7. Limitaciones y Alcance

1. **Diagramas Soportados**: En esta fase se soporta exclusivamente el **Diagrama de Clases Conceptual (UML Class Diagram)**.
2. **Perfiles Específicos de EA**: Se procesan los estereotipos y extensiones centrales de clases conceptuales (visibilidad, multiplicidades, diagram layout). Extensiones propietarias de EA como diagramas de base de datos físicos (Data Modeling Profile) o diagramas de actividades no aplican al editor conceptual.
3. **Protección de Seguridad**: El parser XML cuenta con protección activa contra inyecciones de entidades externas (**XXE**), denegando DTDs no seguras y entidades parametrizadas externas.
