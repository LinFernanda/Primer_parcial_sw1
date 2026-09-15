# Guía de Configuración y Ejecución del Frontend Web

## 1. Requisitos Previos
- **Node.js:** Versión 18+ o 20+ (probado con v26.8.1).
- **NPM:** 9+ (o Yarn / PNPM).

## 2. Variables de Entorno (.env)
Configurar en `frontend/.env`:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_APP_TITLE=CASE Platform
```

## 3. Instalación de Dependencias
```bash
cd frontend
npm install
```

## 4. Comandos Disponibles
- **Servidor de desarrollo (Vite):**
  ```bash
  npm run dev
  ```
  Acceso en el navegador: `http://localhost:5173`.

- **Compilación para producción:**
  ```bash
  npm run build
  ```

- **Linter de código (ESLint):**
  ```bash
  npm run lint
  ```

- **Formateo de código (Prettier):**
  ```bash
  npm run format
  ```
