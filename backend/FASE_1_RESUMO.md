# Fase 1: MVP - Core | Resumo Estruturado

## ✅ Projeto Backend Quarkus Criado

A estrutura completa do backend para **AE Organiza Lar** foi criada conforme planejado.

---

## 📂 Estrutura do Projeto

```
backend/
├── src/main/
│   ├── java/com/aelares/
│   │   ├── entity/
│   │   │   ├── User.java              ✅ Usuários
│   │   │   ├── Home.java              ✅ Residências
│   │   │   ├── Task.java              ✅ Tarefas
│   │   │   ├── TaskSchedule.java      ✅ Agendamento por dia da semana
│   │   │   └── AuthToken.java         ✅ Tokens OTP
│   │   │
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── VerifyCodeRequest.java
│   │   │   │   ├── CreateHomeRequest.java
│   │   │   │   └── CreateTaskRequest.java
│   │   │   └── response/
│   │   │       ├── UserResponse.java
│   │   │       ├── LoginResponse.java
│   │   │       ├── HomeResponse.java
│   │   │       ├── TaskResponse.java
│   │   │       └── ErrorResponse.java
│   │   │
│   │   ├── service/
│   │   │   ├── AuthService.java       ✅ Autenticação + OTP + JWT
│   │   │   ├── UserService.java       ✅ Gerenciamento de usuários
│   │   │   ├── HomeService.java       ✅ CRUD de residências
│   │   │   ├── TaskService.java       ✅ CRUD de tarefas
│   │   │   └── EmailService.java      ✅ Envio de emails
│   │   │
│   │   ├── resource/
│   │   │   ├── AuthResource.java      ✅ Endpoints de autenticação
│   │   │   ├── UserResource.java      ✅ Endpoints de usuários
│   │   │   ├── HomeResource.java      ✅ Endpoints de residências
│   │   │   └── TaskResource.java      ✅ Endpoints de tarefas
│   │   │
│   │   ├── exception/
│   │   │   ├── UnauthorizedException.java
│   │   │   ├── ResourceNotFoundException.java
│   │   │   ├── UnauthorizedExceptionMapper.java
│   │   │   └── ResourceNotFoundExceptionMapper.java
│   │   │
│   │   └── AeOrganizaLarApplication.java  ✅ Aplicação principal
│   │
│   └── resources/
│       ├── application.yaml           ✅ Config padrão
│       ├── application-dev.yaml       ✅ Config desenvolvimento
│       └── db/migration/
│           └── V1__Initial_Schema.sql ✅ Schema do banco
│
├── pom.xml                            ✅ Maven dependencies
├── docker-compose.yml                 ✅ PostgreSQL + MailHog
├── Dockerfile                         ✅ Build para produção
├── README.md                          ✅ Documentação
├── test-api.sh                        ✅ Testes curl
└── .gitignore                         ✅ Git ignore

```

---

## 🔐 Autenticação (Passwordless)

### Fluxo:
1. **POST** `/api/v1/auth/login` (email)
   - Gera código OTP de 6 dígitos
   - Envia por email
   
2. **POST** `/api/v1/auth/verify` (email + código)
   - Valida código
   - Cria/atualiza usuário
   - Retorna JWT token
   
3. **Uso**: Header `Authorization: Bearer <token>`
   - Token válido por 7 dias
   - Código OTP válido por 15 minutos

---

## 📊 Endpoints API

### Autenticação
```
POST   /api/v1/auth/login        → Enviar OTP
POST   /api/v1/auth/verify       → Verificar OTP e obter token
POST   /api/v1/auth/validate     → Validar token
```

### Usuários
```
GET    /api/v1/users/me          → Dados do usuário autenticado
GET    /api/v1/users/{id}        → Buscar usuário por ID
```

### Residências
```
GET    /api/v1/homes             → Listar todas
POST   /api/v1/homes             → Criar nova
GET    /api/v1/homes/{id}        → Buscar por ID
PUT    /api/v1/homes/{id}        → Atualizar
DELETE /api/v1/homes/{id}        → Deletar
```

### Tarefas
```
GET    /api/v1/homes/{homeId}/tasks              → Listar todas
GET    /api/v1/homes/{homeId}/tasks?dayOfWeek=1 → Filtrar por dia
POST   /api/v1/homes/{homeId}/tasks              → Criar nova
GET    /api/v1/homes/{homeId}/tasks/{taskId}    → Buscar
PUT    /api/v1/homes/{homeId}/tasks/{taskId}    → Atualizar
DELETE /api/v1/homes/{homeId}/tasks/{taskId}    → Deletar
```

---

## 🗄️ Banco de Dados

### Tabelas Criadas (Flyway):

**users**
- id (UUID)
- email (unique)
- name
- created_at
- last_login
- preferences (JSONB)

**homes**
- id (UUID)
- user_id (foreign key)
- name
- description
- created_at, updated_at

**tasks**
- id (UUID)
- home_id (foreign key)
- title
- description
- created_at, updated_at

**task_schedules**
- id (UUID)
- task_id (unique foreign key)
- day_of_week (1-7)
- priority (LOW/MEDIUM/HIGH)
- notes
- created_at

**auth_tokens**
- id (UUID)
- email
- code (6 dígitos)
- created_at, expires_at
- used_at (null enquanto não usado)
- ip_address

**reminders** (Fase 2)
**email_logs** (Fase 2)

---

## 🚀 Iniciando o Projeto

### Pré-requisitos
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### Passos:

1. **Iniciar serviços (PostgreSQL + MailHog)**
```bash
cd backend
docker-compose up -d
```

2. **Executar em modo desenvolvimento**
```bash
./mvnw quarkus:dev
```

3. **Testar API**
```bash
bash test-api.sh
```

---

## 📧 Email (Desenvolvimento)

- **Serviço**: MailHog (mock SMTP)
- **Web UI**: http://localhost:8025
- **Todos os emails são salvos** sem realmente enviar

---

## 🛠️ Tecnologias Utilizadas

| Componente | Tecnologia |
|-----------|-----------|
| Framework | Quarkus 3.8.1 |
| Java | 17+ |
| Banco | PostgreSQL 15 |
| ORM | Hibernate + Panache |
| Migrations | Flyway |
| Auth | JWT + OTP |
| Email | Quarkus Mailer |
| Validação | Hibernate Validator |
| Build | Maven 3.8+ |
| Container | Docker |

---

## 📝 Próximas Fases

### Fase 2: Sistema de Lembretes
- [ ] Scheduler automático
- [ ] Envio de emails semanais
- [ ] Histórico de lembretes

### Fase 3: Notificações em Tempo Real
- [ ] WebSocket
- [ ] Notificações do navegador

### Fase 4: Frontend Angular
- [ ] Interface web responsiva
- [ ] Login com OTP
- [ ] Dashboard com tarefas semanais
- [ ] Gerenciamento de residências

---

## ✨ Status

✅ **Fase 1 (MVP Core) - COMPLETA**

- [x] Entidades e banco de dados
- [x] Autenticação passwordless
- [x] CRUD de residências
- [x] CRUD de tarefas com agendamento semanal
- [x] Serviço de email
- [x] API REST completa
- [x] Documentação
- [x] Docker setup
- [x] Configurações dev/prod

---

## 🎯 Próximos Passos

1. ✅ **Backend criado e pronto para testes**
2. 🔄 Testar endpoints (usar `test-api.sh`)
3. ⏳ Aguardar aprovação para Fase 2
4. 🎨 Desenvolver Frontend Angular (após aprovação)

---

## 📚 Referências

- [Quarkus Docs](https://quarkus.io/)
- [Hibernate Panache](https://quarkus.io/guides/hibernate-orm-panache)
- [Flyway](https://flywaydb.org/)
- [JWT](https://tools.ietf.org/html/rfc7519)

---

**Desenvolvido para: AE Organiza Lar**
**Data**: 17/02/2026
**Versão**: 1.0.0-MVP

