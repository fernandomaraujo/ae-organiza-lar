# 🔧 SOLUÇÃO: Docker Desktop Não Está Rodando

## ❌ Erro Encontrado

```
unable to get image 'mailhog/mailhog:latest': error during connect: 
Get "http://%2F%2F.%2Fpipe%2FdockerDesktopLinuxEngine/v1.51/images/mailhog/mailhog:latest/json": 
open //./pipe/dockerDesktopLinuxEngine: O sistema não pode encontrar o arquivo especificado.
```

**Causa:** Docker Desktop não está ativo/rodando

---

## ✅ SOLUÇÃO

### Opção 1: Iniciar Docker Desktop (RECOMENDADO)

#### Windows 11/10 com Docker Desktop instalado:

1. **Abra o Menu Iniciar**
2. **Procure por "Docker Desktop"**
3. **Clique para abrir**
4. **Aguarde 30-60 segundos** para inicializar completamente
5. **Você verá um ícone de baleia** na bandeja do sistema

#### Verificar se está rodando:
```powershell
docker ps
```

Se ver a resposta abaixo = Docker está ativo:
```
CONTAINER ID   IMAGE     COMMAND   CREATED   STATUS    PORTS     NAMES
```

---

### Opção 2: Se Docker Desktop não estiver instalado

Você precisa instalar:
1. Acesse: https://www.docker.com/products/docker-desktop
2. Faça download do Docker Desktop para Windows
3. Execute o instalador
4. Reinicie o computador
5. Abra o Docker Desktop após reiniciar

---

### Opção 3: Se tiver WSL 2 instalado

```powershell
# Verificar se WSL 2 está rodando
wsl --list --verbose

# Se não estiver, ativar
wsl --install
```

---

## 🔍 DIAGNÓSTICO

### Teste a Conexão com Docker:

```powershell
# Teste básico
docker --version

# Teste se daemon está rodando
docker ps

# Se ambos falharem = Docker não está rodando
```

### Logs do Docker:
```powershell
# Se tiver instalado, ver logs
Get-Content "$env:APPDATA\Docker\*"
```

---

## 🔄 PRÓXIMOS PASSOS (Após Docker estar rodando)

### 1. Verificar Docker (no PowerShell)
```powershell
docker ps
```

Você deve ver uma tabela (vazia ou com containers):
```
CONTAINER ID   IMAGE     COMMAND   CREATED   STATUS    PORTS     NAMES
```

### 2. Tentar Docker Compose novamente
```bash
cd C:\Desenvolvimento\ae-organiza-lar\backend
docker-compose up -d
```

Você deve ver:
```
[+] Running 2/2
 ✔ Container aelares-postgres     Running
 ✔ Container aelares-mailhog      Running
```

### 3. Verificar Containers
```bash
docker ps
```

Você deve ver PostgreSQL e MailHog:
```
CONTAINER ID   IMAGE                    STATUS              NAMES
abc123...      postgres:15-alpine       Up 2 minutes        aelares-postgres
def456...      mailhog/mailhog:latest   Up 2 minutes        aelares-mailhog
```

---

## 🐛 PROBLEMAS COMUNS

### "Docker daemon is not running"
- Abra o Docker Desktop novamente
- Aguarde 60 segundos

### "Cannot connect to Docker daemon"
- Reinicie o Docker Desktop
- Se persistir, reinicie o Windows

### "Port 5432 already in use"
```bash
# Parar containers antigos
docker-compose down

# Limpar volumes
docker-compose down -v

# Reiniciar
docker-compose up -d
```

---

## ✅ CHECKLIST

- [ ] Docker Desktop instalado
- [ ] Docker Desktop está rodando (ícone baleia visível)
- [ ] `docker ps` retorna lista (vazia ou com containers)
- [ ] `docker-compose up -d` executa sem erros
- [ ] PostgreSQL e MailHog estão rodando
- [ ] Porta 5432 está respondendo

---

## 📞 SE AINDA NÃO FUNCIONAR

### Teste conexão com PostgreSQL:
```bash
docker exec aelares-postgres psql -U aelares_user -d aelares_db -c "SELECT version();"
```

### Teste MailHog:
```
http://localhost:8025
```
Você deve ver a interface web do MailHog.

### Limpar tudo e começar do zero:
```bash
# Parar e remover tudo
docker-compose down -v

# Remover imagens
docker rmi postgres:15-alpine mailhog/mailhog:latest

# Reiniciar
docker-compose up -d
```

---

## 🎯 RESUMO

| Problema | Solução |
|----------|---------|
| Docker não inicia | Abra Docker Desktop |
| Porta em uso | `docker-compose down -v` |
| Containers não criam | Verifique internet/Docker Hub |
| PostgreSQL não conecta | Aguarde 10-15 segundos |
| MailHog não abre | Verifique http://localhost:8025 |

---

## ✨ Após Docker estar funcionando

Você pode:
```bash
# Ver logs
docker logs aelares-postgres
docker logs aelares-mailhog

# Parar containers
docker-compose stop

# Reiniciar
docker-compose start

# Remover tudo
docker-compose down -v
```

---

**Desenvolvido:** 17/02/2026
**Status:** Guia de Troubleshooting
**Próximo:** Após Docker rodar → `./mvnw quarkus:dev`

