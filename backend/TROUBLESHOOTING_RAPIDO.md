# ⚡ TROUBLESHOOTING RÁPIDO

## ❌ Erro: "unable to get image 'mailhog/mailhog:latest'"

### Causa
Docker Desktop não está ativo

### ✅ Solução Rápida

**1. Abra Docker Desktop**
   - Menu Iniciar → Procure "Docker Desktop" → Clique
   - Aguarde 60 segundos para inicializar

**2. Verifique se está rodando**
```bash
docker ps
```

**3. Tente novamente**
```bash
cd C:\Desenvolvimento\ae-organiza-lar\backend
docker-compose up -d
```

---

## ❌ Erro: "Port 5432 already in use"

### Causa
PostgreSQL já está rodando em outra instância

### ✅ Solução

```bash
# Limpar tudo e reiniciar
docker-compose down -v
docker-compose up -d
```

---

## ❌ Erro: "connection refused"

### Causa
Banco de dados não iniciou ainda

### ✅ Solução

```bash
# Aguarde 15 segundos
# Verifique se containers estão rodando
docker ps

# Se sim, aguarde mais um pouco
# Se não, veja SOLUCAO_DOCKER.md
```

---

## ❌ Maven/Java não encontrado

### Causa
Java 17+ ou Maven não instalados

### ✅ Solução

Verificar:
```bash
java -version
mvn -version
```

Se falhar, instale em:
- Java: https://www.oracle.com/java/technologies/downloads/
- Maven: https://maven.apache.org/download.cgi

---

## ✅ Se nada funcionar

### Opção 1: Usar PostgreSQL Local
Veja: `ALTERNATIVAS_SEM_DOCKER.md`

### Opção 2: Limpar tudo e começar do zero
```bash
# Parar containers
docker-compose down -v

# Remover imagens
docker rmi postgres:15-alpine mailhog/mailhog:latest

# Reiniciar Docker Desktop

# Tentar novamente
docker-compose up -d
```

### Opção 3: Reinstalar Docker Desktop
https://www.docker.com/products/docker-desktop

---

## 🎯 CHECKLIST

- [ ] Docker Desktop instalado e rodando
- [ ] `docker ps` retorna lista
- [ ] `docker-compose up -d` sem erros
- [ ] PostgreSQL container rodando
- [ ] MailHog container rodando
- [ ] `./mvnw quarkus:dev` inicia sem erros
- [ ] Backend em http://localhost:8080
- [ ] MailHog em http://localhost:8025

---

## 📞 DOCUMENTAÇÃO

- **SOLUCAO_DOCKER.md** - Guia detalhado de Docker
- **ALTERNATIVAS_SEM_DOCKER.md** - Sem Docker
- **README.md** - Documentação geral
- **test-api.sh** - Testes automáticos

---

Desenvolvido: 17/02/2026

