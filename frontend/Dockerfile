# Build stage
FROM node:20-alpine AS builder

WORKDIR /app

# Copiar arquivos de configuração do Angular
COPY package*.json ./
COPY angular.json ./
COPY tsconfig*.json ./

# Instalar dependências
RUN npm ci

# Copiar código fonte
COPY src ./src
COPY public ./public

# Build
RUN npm run build -- --configuration production

# Runtime stage - Nginx
FROM nginx:alpine

# Remover config padrão nginx
RUN rm /etc/nginx/conf.d/default.conf

# Copiar arquivos buildados
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html

# Copiar configuração customizada
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
