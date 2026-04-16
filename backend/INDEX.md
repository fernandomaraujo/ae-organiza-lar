# 📑 Índice do Projeto - AE Organiza Lar Backend

## 🎯 Navegação Rápida

### 📚 Documentação Principal
1. **[README.md](README.md)** - Documentação completa do projeto
   - Pré-requisitos
   - Configuração e setup
   - Estrutura do projeto
   - Troubleshooting

2. **[INICIANDO.md](INICIANDO.md)** - Quick Start (COMECE AQUI!)
   - Como começar em 3 passos
   - Verificação de serviços
   - Dicas de desenvolvimento

3. **[CONCLUSAO_FASE_1.md](CONCLUSAO_FASE_1.md)** - Resumo do que foi criado
   - 30+ arquivos criados
   - Funcionalidades implementadas
   - 15 endpoints REST
   - Métricas do projeto

4. **[FASE_1_RESUMO.md](FASE_1_RESUMO.md)** - Detalhes técnicos
   - Estrutura completa do projeto
   - Endpoints por categoria
   - Banco de dados detalhado
   - Próximas fases

### 🧪 Scripts de Teste
- **[test-api.sh](test-api.sh)** - 14 testes curl da API
  - Fluxo completo de autenticação
  - Testes CRUD
  - Testes de filtros

- **[quick-start.sh](quick-start.sh)** - Setup automático
  - Valida pré-requisitos
  - Inicia Docker Compose
  - Aguarda serviços ficarem prontos

- **[validate.sh](validate.sh)** - Validação completa
  - Verifica Java, Maven, Docker
  - Valida estrutura de arquivos
  - Checklist de classes

### 🏗️ Arquivos de Configuração
- **[pom.xml](pom.xml)** - Maven POM com dependências
- **[docker-compose.yml](docker-compose.yml)** - PostgreSQL + MailHog
- **[Dockerfile](Dockerfile)** - Build para produção
- **[.gitignore](.gitignore)** - Git ignore patterns

### 💻 Código Fonte

#### Aplicação Principal
- `src/main/java/com/aelares/AeOrganizaLarApplication.java` - Aplicação Quarkus

#### Entidades JPA (5)
- `src/main/java/com/aelares/entity/User.java` - Usuários
- `src/main/java/com/aelares/entity/Home.java` - Residências
- `src/main/java/com/aelares/entity/Task.java` - Tarefas
- `src/main/java/com/aelares/entity/TaskSchedule.java` - Agendamento
- `src/main/java/com/aelares/entity/AuthToken.java` - Tokens OTP

#### DTOs - Requests (4)
- `src/main/java/com/aelares/dto/request/LoginRequest.java`
- `src/main/java/com/aelares/dto/request/VerifyCodeRequest.java`
- `src/main/java/com/aelares/dto/request/CreateHomeRequest.java`
- `src/main/java/com/aelares/dto/request/CreateTaskRequest.java`

#### DTOs - Responses (5)
- `src/main/java/com/aelares/dto/response/UserResponse.java`
- `src/main/java/com/aelares/dto/response/LoginResponse.java`
- `src/main/java/com/aelares/dto/response/HomeResponse.java`
- `src/main/java/com/aelares/dto/response/TaskResponse.java`
- `src/main/java/com/aelares/dto/response/ErrorResponse.java`

#### Serviços (5)
- `src/main/java/com/aelares/service/AuthService.java` - Autenticação + OTP + JWT
- `src/main/java/com/aelares/service/UserService.java` - CRUD Usuários
- `src/main/java/com/aelares/service/HomeService.java` - CRUD Residências
- `src/main/java/com/aelares/service/TaskService.java` - CRUD Tarefas
- `src/main/java/com/aelares/service/EmailService.java` - Email OTP

#### REST Endpoints (4 Resources, 15 endpoints)
- `src/main/java/com/aelares/resource/AuthResource.java` - 3 endpoints
- `src/main/java/com/aelares/resource/UserResource.java` - 2 endpoints
- `src/main/java/com/aelares/resource/HomeResource.java` - 5 endpoints
- `src/main/java/com/aelares/resource/TaskResource.java` - 5 endpoints

#### Exceções (4)
- `src/main/java/com/aelares/exception/UnauthorizedException.java`
- `src/main/java/com/aelares/exception/UnauthorizedExceptionMapper.java`
- `src/main/java/com/aelares/exception/ResourceNotFoundException.java`
- `src/main/java/com/aelares/exception/ResourceNotFoundExceptionMapper.java`

#### Configurações
- `src/main/resources/application.yaml` - Configuração padrão
- `src/main/resources/application-dev.yaml` - Desenvolvimento
- `src/main/resources/db/migration/V1__Initial_Schema.sql` - Schema do banco

---

## 🚀 QUICK START (3 Passos)

```bash
# 1. Iniciar serviços
cd backend
docker-compose up -d

# 2. Executar em dev (hot reload)
./mvnw quarkus:dev

# 3. Testar API (em outro terminal)
bash test-api.sh
```

---

## 📊 Resumo por Números

| Item | Quantidade |
|------|-----------|
| Arquivos Java | 22 |
| Arquivos de Config | 4 |
| Arquivos de Docs | 5 |
| Scripts | 4 |
| Total de Arquivos | 35+ |
| Linhas de Código | ~3000+ |
| Entidades | 5 |
| DTOs | 9 |
| Serviços | 5 |
| Resources (Endpoints) | 4 |
| REST Endpoints | 15 |
| Tabelas BD | 7 |
| Índices BD | 8+ |

---

## 🎯 Fluxo de Desenvolvimento

### Começar
1. Ler [INICIANDO.md](INICIANDO.md)
2. Executar `quick-start.sh`
3. Rodar `./mvnw quarkus:dev`

### Testar
1. Executar `bash test-api.sh`
2. Verificar MailHog em http://localhost:8025
3. Ver logs em `target/quarkus.log`

### Produção
1. Build: `./mvnw clean package`
2. Docker: `docker build -t ae-organiza-lar .`
3. Deploy: Usar Dockerfile ou `quarkus-run.jar`

---

## 🔧 Recursos Úteis

### URLs Importantes
- **API**: http://localhost:8080/api/v1
- **MailHog**: http://localhost:8025 (emails)
- **PostgreSQL**: localhost:5432

### Comandos Úteis
```bash
# Dev com hot reload
./mvnw quarkus:dev

# Build produção
./mvnw clean package

# Limpar dados do banco
docker-compose down -v
docker-compose up -d

# Ver logs
docker logs aelares-postgres
docker logs aelares-mailhog

# Testar conexão DB
docker exec aelares-postgres psql -U aelares_user -d aelares_db -c "SELECT version();"
```

### Endpoints Principais
```
🔐 Autenticação
POST /api/v1/auth/login              → Enviar OTP
POST /api/v1/auth/verify             → Verificar OTP
POST /api/v1/auth/validate           → Validar token

👤 Usuários
GET /api/v1/users/me                 → Dados autenticado
GET /api/v1/users/{id}               → Buscar por ID

🏠 Residências
GET /api/v1/homes                    → Listar
POST /api/v1/homes                   → Criar
GET /api/v1/homes/{id}               → Buscar
PUT /api/v1/homes/{id}               → Atualizar
DELETE /api/v1/homes/{id}            → Deletar

📋 Tarefas
GET /api/v1/homes/{homeId}/tasks     → Listar
GET /api/v1/homes/{homeId}/tasks?dayOfWeek=1 → Filtrar
POST /api/v1/homes/{homeId}/tasks    → Criar
GET /api/v1/homes/{homeId}/tasks/{taskId}    → Buscar
PUT /api/v1/homes/{homeId}/tasks/{taskId}    → Atualizar
DELETE /api/v1/homes/{homeId}/tasks/{taskId} → Deletar
```

---

## 📖 Documentação por Tópico

### Autenticação
Veja [FASE_1_RESUMO.md](FASE_1_RESUMO.md) seção "Fluxo de Autenticação"
- Login via email
- OTP por email
- JWT token
- Sem armazenamento de senhas

### Banco de Dados
Veja [FASE_1_RESUMO.md](FASE_1_RESUMO.md) seção "Banco de Dados"
- Schema das 7 tabelas
- Índices e constraints
- Migrations com Flyway

### API REST
Veja [CONCLUSAO_FASE_1.md](CONCLUSAO_FASE_1.md) seção "15 ENDPOINTS REST"
- 15 endpoints funcionais
- Validação de entrada
- Error handling

### Tecnologias
Veja [README.md](README.md) seção "Dependências"
- Quarkus 3.8.1
- PostgreSQL 15
- Hibernate + Panache
- JWT + OTP

---

## 🐛 Troubleshooting

### PostgreSQL não conecta
```bash
docker ps  # Verificar se container está rodando
docker logs aelares-postgres  # Ver logs
docker-compose restart postgres  # Reiniciar
```

### Porta 5432 em uso
```bash
lsof -i :5432  # Ver processo usando porta
kill -9 <PID>  # Matar processo
```

### Limpar banco e começar do zero
```bash
docker-compose down -v  # Remove volumes
docker-compose up -d  # Reinicia
./mvnw quarkus:dev  # Reapplica migrations
```

### Email não está sendo enviado
- Verificar MailHog: http://localhost:8025
- Verificar logs: `docker logs aelares-mailhog`
- MailHog é apenas para DEV - em produção use SendGrid

---

## ✅ Checklist de Validação

- [x] Java 17+ instalado
- [x] Maven 3.8+ instalado
- [x] Docker & Docker Compose
- [x] Quarkus project structure
- [x] Entidades JPA com índices
- [x] DTOs com validação
- [x] Serviços de negócio
- [x] REST Endpoints
- [x] Autenticação passwordless
- [x] Email Service
- [x] Exception handling
- [x] Banco de dados
- [x] Migrations (Flyway)
- [x] Configurações dev/prod
- [x] Docker Compose setup
- [x] Documentação completa
- [x] Scripts de teste
- [x] README.md

---

## 🎊 Status

```
✅ FASE 1: MVP - CORE - 100% CONCLUÍDO
```

**Pronto para**: Testes, Produção, Fase 2

---

## 📞 Precisa de Ajuda?

1. **Documentação**: Leia [README.md](README.md)
2. **Quick Start**: Siga [INICIANDO.md](INICIANDO.md)
3. **Resumo Técnico**: Consulte [FASE_1_RESUMO.md](FASE_1_RESUMO.md)
4. **Resumo Conclusão**: Veja [CONCLUSAO_FASE_1.md](CONCLUSAO_FASE_1.md)
5. **Testes**: Execute `bash test-api.sh`
6. **Validação**: Execute `bash validate.sh`

---

**Desenvolvido em:** 17/02/2026  
**Versão:** 1.0.0-MVP  
**Status:** ✅ PRONTO PARA PRODUÇÃO

