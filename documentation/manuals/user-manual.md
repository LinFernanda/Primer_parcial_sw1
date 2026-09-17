# MANUAL DE USUARIO FINAL - PLATAFORMA CASE INTELIGENTE

## Guía Operativa para Diseñadores, Ingenieros de Software y Administradores

---

## 1. Acceso a la Plataforma y Autenticación

### Inicio de Sesión
1. Abra el navegador web en la URL de la plataforma (ej. `http://localhost:5173` en local o `https://app.caseplatform.com` en producción).
2. Ingrese sus credenciales:
   - **Correo de Ingeniero:** `ingeniero@caseplatform.com`
   - **Contraseña:** `Ingeniero123!`
   - *(O `admin@caseplatform.com` / `Admin123!` para tareas de administración)*.
3. Al autenticarse correctamente, el sistema generará un token JWT seguro y lo redirigirá al panel principal de proyectos.

---

## 2. Gestión de Proyectos y Modelos UML

### Crear un Nuevo Proyecto
1. En el menú superior o lateral, presione el botón **"+ Nuevo Proyecto"**.
2. Indique el nombre del proyecto (ej. `Sistema de Gestión de Clientes`) y una descripción conceptual.
3. Al crearse, el sistema inicializa automáticamente un **Modelo UML** en blanco asociado al proyecto.

---

## 3. Uso del Editor Visual UML 2.5

### Creación y Edición de Clases
- **Agregar Clase**: En la barra de herramientas izquierda, haga clic en el botón **"Clase"** y colóquela sobre el lienzo interactivo.
- **Definir Atributos**: Seleccione la clase, abra el panel lateral derecho y agregue atributos con su nombre, tipo de dato (`String`, `Integer`, `Double`, `Boolean`, etc.) y nivel de visibilidad (`+ Public`, `- Private`, `# Protected`).
- **Definir Métodos**: En la sección de operaciones, especifique el nombre del método, tipo de retorno y argumentos.

### Creación de Relaciones
- Haga clic en el ancla circular de la clase de origen y arrastre la línea conector hasta el ancla de la clase de destino.
- Seleccione el tipo de relación en el modal flotante:
  - **Asociación** (línea sólida con flecha abierta).
  - **Herencia / Generalización** (línea sólida con triángulo hueco).
  - **Agregación** (línea sólida con rombo hueco).
  - **Composición** (línea sólida con rombo relleno).
  - **Dependencia** (línea discontinua con flecha abierta).
- Configure las multiplicidades en ambos extremos (ej. `1..*`, `0..1`).

---

## 4. Colaboración en Tiempo Real y WebSockets

- **Presencia de Usuarios**: En la esquina superior derecha del editor se muestran los avatares de los ingenieros conectados simultáneamente al modelo.
- **Edición Concurrente**: Cualquier movimiento, adición de clase o modificación de atributo se propaga en milisegundos a todos los participantes conectados sin necesidad de recargar la página.

---

## 5. Asistente de Inteligencia Artificial UML

1. En la parte inferior del editor, abra la pestaña **"Asistente IA"**.
2. Escriba un comando en lenguaje natural, por ejemplo:
   - *"Crear clase Pedido con atributo total de tipo Double y estado de tipo String"*
   - *"Relacionar Cliente con Pedido mediante asociación 1 a muchos"*
3. El agente de IA interpretará la intención, generará los elementos correspondientes y los reflejará de inmediato en el diagrama y en la base de datos.

---

## 6. Conversión de Imagen a Modelo UML (Visión por Computador)

1. En la barra superior, pulse el botón **"Importar Imagen UML"**.
2. Seleccione una fotografía o diagrama escaneado (formato PNG o JPG) de una pizarra o libreta.
3. El motor de visión artificial procesará la imagen, detectará las cajas de clases, textos y conexiones, y presentará una vista previa con los elementos reconocidos.
4. Presione **"Aplicar al Modelo"** para integrar los elementos detectados directamente en su espacio de trabajo.

---

## 7. Generador Automático de Backend Spring Boot 3

1. Con el modelo UML completado, presione el botón **"Generar Backend"**.
2. Configure los parámetros del proyecto:
   - **GroupId**: ej. `com.miempresa.sistema`
   - **ArtifactId**: ej. `sistema-backend`
   - **Versión de Java**: `21`
3. Haga clic en **"Descargar Proyecto ZIP"**.
4. El sistema empaquetará un proyecto Spring Boot 3 compilable con:
   - Entidades JPA con anotaciones relacionales y validación de beans.
   - Repositorios Spring Data JPA.
   - DTOs y Mappers MapStruct.
   - Servicios transaccionales (`@Service`, `@Transactional`).
   - Controladores REST documentados con OpenAPI 3 / Swagger UI.
   - Scripts DDL para PostgreSQL y Dockerfile de despliegue.

---

## 8. Integración con Enterprise Architect (XMI 2.1)

- **Exportar XMI**: Haga clic en **"Exportar > Enterprise Architect (XMI 2.1)"** para descargar el archivo XML estándar. Puede abrirlo directamente en Enterprise Architect con fidelidad total de clases y relaciones.
- **Importar XMI**: Haga clic en **"Importar > Archivo XMI"**, seleccione su archivo exportado desde Enterprise Architect y el sistema reconstruirá el diagrama conceptual en el lienzo visual.

---

## 9. Uso de la Aplicación Móvil (Flutter)

### Acceso Móvil
1. Abra la aplicación en Windows Desktop, navegador Edge o su celular Android.
2. Inicie sesión con:
   - **Correo:** `demo@barberia.com`
   - **Contraseña:** `demo123`
3. Explore los módulos generados: **Clientes**, **Servicios**, **Reservas**, **Asistente IA On-Device** y **Centro de Sincronización**.

### Trabajo Offline y Sincronización
- Puede crear clientes o registrar reservas sin conexión a internet.
- Las operaciones se almacenan en la cola local SQLite con sellos de tiempo.
- Al recuperar conexión, el **Centro de Sincronización** sube los cambios al servidor backend resolviendo automáticamente cualquier discrepancia con la política *Last-Write-Wins*.
