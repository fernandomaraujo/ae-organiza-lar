# 🔄 ALTERNATIVA: Executar Backend SEM Docker

Se não quiser usar Docker, você pode executar PostgreSQL e MailHog localmente.

---

## ✅ OPÇÃO A: PostgreSQL Local + MailHog Docker

### Pré-requisitos
- PostgreSQL 15 instalado no seu PC
- Docker (apenas para MailHog)

### Passos

1. **Verificar PostgreSQL está instalado e rodando**
```bash
psql --version
```

2. **Criar banco de dados**
```bash
psql -U postgres -c "CREATE DATABASE aelares_db;"
psql -U postgres -c "CREATE USER aelares_user WITH PASSWORD 'aelares_password';"
psql -U postgres -c "ALTER DATABASE aelares_db OWNER TO aelares_user;"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE aelares_db TO aelares_user;"
```

3. **Modificar application.yaml**
```yaml
quarkus:
  datasource:
    db-kind: postgresql
    username: aelares_user
    password: aelares_password
    jdbc:
      url: jdbc:postgresql://localhost:5432/aelares_db
```

4. **Executar backend**
```bash
./mvnw quarkus:dev
```

---

## ✅ OPÇÃO B: Executar TUDO Localmente (SEM Docker)

### Pré-requisitos
- PostgreSQL 15 instalado
- Java 17+
- Maven 3.8+
- Nenhum Docker necessário

### Passos

1. **Iniciar PostgreSQL** (Windows)
```bash
# Se PostgreSQL for serviço Windows
net start postgresql-x64-15

# Ou se usar executável
"C:\Program Files\PostgreSQL\15\bin\pg_ctl" -D "C:\Program Files\PostgreSQL\15\data" start
```

2. **Criar banco**
```bash
psql -U postgres -h localhost -c "CREATE DATABASE aelares_db;"
psql -U postgres -h localhost -c "CREATE USER aelares_user WITH PASSWORD 'aelares_password';"
psql -U postgres -h localhost -c "ALTER DATABASE aelares_db OWNER TO aelares_user;"
psql -U postgres -h localhost -c "GRANT ALL PRIVILEGES ON DATABASE aelares_db TO aelares_user;"
```

3. **Modificar application-dev.yaml**
```yaml
quarkus:
  datasource:
    db-kind: postgresql
    username: aelares_user
    password: aelares_password
    jdbc:
      url: jdbc:postgresql://localhost:5432/aelares_db

  mailer:
    host: smtp.gmail.com  # Usar Gmail ou outro SMTP
    port: 587
    ssl: false
    username: seu-email@gmail.com
    password: sua-senha-app
    from: seu-email@gmail.com
```

4. **Executar backend**
```bash
./mvnw quarkus:dev
```

---

## 🚀 COMPARAÇÃO DE OPÇÕES

| Opção | Docker | PostgreSQL | MailHog | Setup | Fácil |
|-------|--------|-----------|---------|-------|-------|
| **A** | ✅ Apenas MailHog | Local | Docker | Médio | ⭐⭐ |
| **B** | ❌ Nenhum | Local | Email Real | Simples | ⭐⭐⭐ |
| **Original** | ✅ Completo | Docker | Docker | Simples | ⭐⭐⭐ |

---

## 📝 MODIFICAR CONFIGURAÇÃO

### Para Opção A (PostgreSQL Local + MailHog Docker)

**Arquivo:** `src/main/resources/application-dev.yaml`

```yaml
quarkus:
  datasource:
    db-kind: postgresql
    username: aelares_user
    password: aelares_password
    jdbc:
      url: jdbc:postgresql://localhost:5432/aelares_db
  
  mailer:
    host: localhost
    port: 1025
    ssl: false
    trust-all: true
```

### Para Opção B (Tudo Local)

**Arquivo:** `src/main/resources/application-dev.yaml`

```yaml
quarkus:
  datasource:
    db-kind: postgresql
    username: aelares_user
    password: aelares_password
    jdbc:
      url: jdbc:postgresql://localhost:5432/aelares_db
  
  mailer:
    # Usando Gmail (precisa de App Password)
    host: smtp.gmail.com
    port: 587
    ssl: false
    username: seu-email@gmail.com
    password: sua-senha-app-google
    from: seu-email@gmail.com
```

---

## ✨ RECOMENDAÇÃO

**Para Desenvolvimento:**
- ✅ Use **Opção Original** (Docker) = Mais limpo
- ✅ Use **Opção B** se não tiver Docker instalado

**Para Produção:**
- ✅ Use Docker sempre
- ✅ Use Kubernetes para escalar

---

## 🎯 PRÓXIMOS PASSOS

1. Escolha uma opção acima
2. Faça as modificações necessárias
3. Execute: `./mvnw quarkus:dev`
4. Teste: `bash test-api.sh`

---

**Status:** Guia de Alternativas
**Data:** 17/02/2026

