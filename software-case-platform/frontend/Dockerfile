# ==============================================================================
# FASE 13: DOCKERFILE MULTI-STAGE - FRONTEND REACT 18 + VITE + TYPESCRIPT
# Servido a través de Nginx Alpine de alta eficiencia para AWS
# ==============================================================================

# STAGE 1: Construcción de Assets con Node.js 20 Alpine
FROM node:20-alpine AS build

WORKDIR /app

# Copiar archivos de definición de dependencias
COPY package*.json ./

# Instalar dependencias limpias
RUN npm ci

# Copiar el código fuente completo
COPY . .

# Argumento para URL de backend en tiempo de compilación (si se requiere)
ARG VITE_API_URL=/api/v1
ARG VITE_WS_URL=/ws
ENV VITE_API_URL=$VITE_API_URL
ENV VITE_WS_URL=$VITE_WS_URL

# Compilar proyecto optimizado para producción
RUN npm run build

# ==============================================================================
# STAGE 2: Servidor Web Nginx para Despliegue de Producción
FROM nginx:1.27-alpine AS runner

LABEL maintainer="Equipo CASE Platform <dev@caseplatform.com>"
LABEL version="1.0.0"
LABEL description="Frontend Web React en Nginx para Plataforma CASE en AWS"

# Eliminar configuración por defecto de Nginx
RUN rm -rf /etc/nginx/conf.d/*

# Copiar configuración personalizada optimizada para SPA y proxies
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copiar el build estático generado
COPY --from=build /app/dist /usr/share/nginx/html

# Exponer el puerto HTTP estándar
EXPOSE 80

# Verificación de salud del servidor web
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
