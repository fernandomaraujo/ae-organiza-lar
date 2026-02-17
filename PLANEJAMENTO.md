# Planejamento - AE Organiza Lar
## Aplicação Web de Gerenciamento de Afazeres Domésticos

**Data**: Fevereiro 2026  
**Status**: Planejamento Inicial  
**Objetivo**: Criar uma aplicação web responsiva para gerenciamento centralizado de tarefas domésticas em múltiplas residências com sistema de lembretes por email

---

## 📋 ÍNDICE
1. [Visão Geral do Projeto](#visão-geral)
2. [Arquitetura Técnica](#arquitetura)
3. [Banco de Dados](#banco-de-dados)
4. [Backend - API REST](#backend)
5. [Frontend](#frontend)
6. [Fluxo de Autenticação](#autenticação)
7. [Sistema de Lembretes](#lembretes)
8. [Fases de Desenvolvimento](#fases)
9. [Tecnologias Recomendadas](#tecnologias)
10. [Considerações de Segurança](#segurança)

---

## <a name="visão-geral"></a>🎯 1. VISÃO GERAL DO PROJETO

### Objetivo Principal
Permitir que usuários gerenciem suas responsabilidades domésticas em múltiplas residências (casa própria, casa de conjugue, pais, etc.) através de um calendário semanal, com sistema de lembretes automáticos via email.

### Problemas a Resolver
- "Acho que lembrar o que preciso fazer" → Sistema de lembretes automáticos
- Organização centralizada de múltiplas residências
- Acesso fácil via web (desktop e mobile)
- Sem necessidade de lembrar senhas

### Personas
- **Usuário Primário**: Pessoa com responsabilidades em 2+ residências (cônjuge, pais, propriedade aluguel, etc.)
- **Requisitos**: Acesso via navegador (web responsiva), lembretes por email

---

## <a name="arquitetura"></a>🏗️ 2. ARQUITETURA TÉCNICA

### Diagrama de Arquitetura
```
┌─────────────────────────────────────────────────────┐
│                    Frontend Web                      │
│  (React/Vue + Vite + TailwindCSS - Responsivo)      │
│  - Desktop (Windows/Mac/Linux)                       │
│  - Mobile (iOS Safari, Android Chrome)              │
└─────────────────┬───────────────────────────────────┘
                  │ HTTPS REST API
                  ▼
┌─────────────────────────────────────────────────────┐
│              Backend - Quarkus API                   │
│  - Autenticação (Email OTP)                         │
│  - CRUD Residências                                 │
│  - CRUD Afazeres                                    │
│  - Serviço de Lembretes (agendado)                  │
│  - Integração com Email Service                     │
└─────────────────┬───────────────────────────────────┘
                  │ JDBC/Hibernate
                  ▼
┌─────────────────────────────────────────────────────┐
│            PostgreSQL Database                       │
│  - Users, Homes, Tasks, TaskSchedules               │
│  - Reminders, EmailLogs, AuditLog                   │
└─────────────────────────────────────────────────────┘
                  │
                  ▼
         ┌──────────────────┐
         │ Email Service    │
         │ (SendGrid/       │
         │  Mailgun/SMTP)   │
         └──────────────────┘
```

### Stack Técnico Recomendado
- **Backend**: Quarkus 3.x + Java 17+
- **Frontend**: Angular 17+ + TypeScript + TailwindCSS
- **Banco de Dados**: PostgreSQL 14+
- **Email Service**: SendGrid API ou SMTP self-hosted
- **Containerização**: Docker + Docker Compose
- **Deploy**: AWS ECS, Heroku, ou VPS com Docker

---

## <a name="banco-de-dados"></a>🗄️ 3. BANCO DE DADOS

### Modelo de Entidades (ER Diagram)

```sql
-- USUÁRIOS
users
├── id (UUID) PRIMARY KEY
├── email (VARCHAR UNIQUE)
├── name (VARCHAR)
├── created_at (TIMESTAMP)
├── last_login (TIMESTAMP)
├── preferences (JSON)
│   ├── reminder_day (1-7, sendo 1=segunda até 7=domingo)
│   ├── reminder_time (HH:MM)
│   ├── reminder_type (email | web | both)
│   └── timezone (string)

-- RESIDÊNCIAS
homes
├── id (UUID) PRIMARY KEY
├── user_id (UUID) FOREIGN KEY → users
├── name (VARCHAR) - "Casa", "Apartamento Pais", etc.
├── description (TEXT)
├── created_at (TIMESTAMP)
├── updated_at (TIMESTAMP)

-- AFAZERES DOMÉSTICOS
tasks
├── id (UUID) PRIMARY KEY
├── home_id (UUID) FOREIGN KEY → homes
├── title (VARCHAR) - "Lavar roupa", "Varrer quarto"
├── description (TEXT)
├── created_at (TIMESTAMP)
├── updated_at (TIMESTAMP)

-- AGENDAMENTO SEMANAL
task_schedules
├── id (UUID) PRIMARY KEY
├── task_id (UUID) FOREIGN KEY → tasks
├── day_of_week (1-7) - 1=segunda, 7=domingo
├── notes (TEXT)
├── priority (LOW | MEDIUM | HIGH)
├── created_at (TIMESTAMP)

-- LEMBRETES
reminders
├── id (UUID) PRIMARY KEY
├── user_id (UUID) FOREIGN KEY → users
├── last_sent (TIMESTAMP)
├── next_scheduled (TIMESTAMP)
├── status (active | paused | disabled)

-- LOGS DE EMAIL ENVIADO
email_logs
├── id (UUID) PRIMARY KEY
├── user_id (UUID) FOREIGN KEY → users
├── email (VARCHAR)
├── subject (VARCHAR)
├── sent_at (TIMESTAMP)
├── status (sent | failed | bounced)
├── error_message (TEXT)

-- AUTENTICAÇÃO (Tokens OTP)
auth_tokens
├── id (UUID) PRIMARY KEY
├── email (VARCHAR)
├── code (VARCHAR UNIQUE) - 6 dígitos
├── created_at (TIMESTAMP)
├── expires_at (TIMESTAMP) - 15 minutos
├── used_at (TIMESTAMP)
├── ip_address (VARCHAR)
```

### Relacionamentos
- 1 Usuário → N Residências
- 1 Residência → N Afazeres
- 1 Afazer → 1 Agendamento Semanal (ou múltiplos se repetir)
- 1 Usuário → 1 Preferência de Lembretes
- N Email Logs → 1 Usuário

### Índices para Performance
```sql
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_homes_user_id ON homes(user_id);
CREATE INDEX idx_tasks_home_id ON tasks(home_id);
CREATE INDEX idx_task_schedules_task_id ON task_schedules(task_id);
CREATE INDEX idx_task_schedules_day ON task_schedules(day_of_week);
CREATE INDEX idx_auth_tokens_email ON auth_tokens(email);
CREATE INDEX idx_auth_tokens_expires ON auth_tokens(expires_at);
CREATE INDEX idx_email_logs_user_id ON email_logs(user_id);
```

---

## <a name="backend"></a>⚙️ 4. BACKEND - API REST (Quarkus)

### Estrutura de Diretórios
```
src/main/java/
├── com.aelares
│   ├── AeLaresApplication.java
│   ├── config/
│   │   ├── DatabaseConfig.java
│   │   ├── CorsConfig.java
│   │   └── EmailConfig.java
│   ├── entities/
│   │   ├── User.java
│   │   ├── Home.java
│   │   ├── Task.java
│   │   ├── TaskSchedule.java
│   │   ├── Reminder.java
│   │   ├── EmailLog.java
│   │   └── AuthToken.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── HomeRepository.java
│   │   ├── TaskRepository.java
│   │   ├── TaskScheduleRepository.java
│   │   ├── ReminderRepository.java
│   │   ├── EmailLogRepository.java
│   │   └── AuthTokenRepository.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   ├── HomeService.java
│   │   ├── TaskService.java
│   │   ├── ReminderService.java
│   │   ├── EmailService.java
│   │   └── ScheduledTasksService.java
│   ├── resource/
│   │   ├── AuthResource.java
│   │   ├── UserResource.java
│   │   ├── HomeResource.java
│   │   ├── TaskResource.java
│   │   ├── ReminderResource.java
│   │   └── HealthResource.java
│   ├── dto/
│   │   ├── requests/
│   │   │   ├── LoginRequest.java
│   │   │   ├── VerifyCodeRequest.java
│   │   │   ├── CreateHomeRequest.java
│   │   │   ├── CreateTaskRequest.java
│   │   │   └── UpdatePreferencesRequest.java
│   │   └── responses/
│   │       ├── LoginResponse.java
│   │       ├── UserResponse.java
│   │       ├── HomeResponse.java
│   │       ├── TaskResponse.java
│   │       └── ErrorResponse.java
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtSecurityFilter.java
│   │   └── SecurityContext.java
│   └── exception/
│       ├── ResourceNotFoundException.java
│       ├── UnauthorizedException.java
│       └── GlobalExceptionHandler.java
src/main/resources/
├── application.yaml (Configurações principais)
├── application-dev.yaml
├── application-prod.yaml
├── db/migration/
│   ├── V1__Initial_Schema.sql
│   └── V2__Add_Indexes.sql
└── templates/
    ├── email-weekly-reminder.html
    └── email-verification-code.html
```

### Endpoints da API

#### 1. **Autenticação (Auth Resource)**

```
POST /api/auth/login
├── Request: { "email": "user@example.com" }
├── Response: { "message": "Código enviado para seu email", "verificationId": "xxx" }
├── Status: 200/400

POST /api/auth/verify
├── Request: { "email": "user@example.com", "code": "123456" }
├── Response: { "token": "jwt_token", "user": {...} }
├── Status: 200/401

POST /api/auth/logout
├── Headers: Authorization: Bearer {token}
├── Response: { "message": "Logout realizado" }
├── Status: 200
```

#### 2. **Usuário (User Resource)**

```
GET /api/users/me
├── Headers: Authorization: Bearer {token}
├── Response: { "id": "uuid", "email": "...", "name": "...", "preferences": {...} }
├── Status: 200/401

PUT /api/users/me
├── Headers: Authorization: Bearer {token}
├── Request: { "name": "João Silva", "email": "joao@example.com" }
├── Response: { "id": "uuid", "email": "...", "name": "..." }
├── Status: 200/400

PUT /api/users/me/preferences
├── Headers: Authorization: Bearer {token}
├── Request: { 
│   "reminderDay": 6, 
│   "reminderTime": "09:00",
│   "reminderType": "email",
│   "timezone": "America/Sao_Paulo"
│ }
├── Response: { "preferences": {...} }
├── Status: 200/400
```

#### 3. **Residências (Home Resource)**

```
GET /api/homes
├── Headers: Authorization: Bearer {token}
├── Response: [ { "id": "uuid", "name": "Casa", "description": "..." }, ... ]
├── Status: 200

POST /api/homes
├── Headers: Authorization: Bearer {token}
├── Request: { "name": "Casa", "description": "Minha residência principal" }
├── Response: { "id": "uuid", "name": "Casa", "description": "..." }
├── Status: 201/400

GET /api/homes/{homeId}
├── Headers: Authorization: Bearer {token}
├── Response: { "id": "uuid", "name": "Casa", "tasks": [...] }
├── Status: 200/404

PUT /api/homes/{homeId}
├── Headers: Authorization: Bearer {token}
├── Request: { "name": "Casa Updated", "description": "..." }
├── Response: { "id": "uuid", "name": "Casa Updated", "description": "..." }
├── Status: 200/400

DELETE /api/homes/{homeId}
├── Headers: Authorization: Bearer {token}
├── Response: { "message": "Residência deletada" }
├── Status: 204
```

#### 4. **Afazeres (Task Resource)**

```
GET /api/homes/{homeId}/tasks
├── Headers: Authorization: Bearer {token}
├── Response: [ 
│   { 
│     "id": "uuid", 
│     "title": "Lavar roupa",
│     "schedule": { "dayOfWeek": 2, "priority": "HIGH" }
│   },
│   ...
│ ]
├── Status: 200

POST /api/homes/{homeId}/tasks
├── Headers: Authorization: Bearer {token}
├── Request: {
│   "title": "Lavar roupa",
│   "description": "Lavar e estender no varal",
│   "schedule": {
│     "dayOfWeek": 2,
│     "priority": "HIGH",
│     "notes": "Usar detergente neutro"
│   }
│ }
├── Response: { "id": "uuid", "title": "Lavar roupa", "schedule": {...} }
├── Status: 201/400

PUT /api/tasks/{taskId}
├── Headers: Authorization: Bearer {token}
├── Request: { "title": "...", "description": "...", "schedule": {...} }
├── Response: { "id": "uuid", "title": "..." }
├── Status: 200/400

DELETE /api/tasks/{taskId}
├── Headers: Authorization: Bearer {token}
├── Response: { "message": "Afazer deletado" }
├── Status: 204

GET /api/tasks/week
├── Headers: Authorization: Bearer {token}
├── Query: ?homeId=uuid&startDate=YYYY-MM-DD (opcional)
├── Response: {
│   "week": "2026-02-17",
│   "tasks": [
│     { "day": 2, "tasks": [{ "id": "uuid", "title": "Lavar roupa", ... }] },
│     ...
│   ]
│ }
├── Status: 200
```

#### 5. **Lembretes (Reminder Resource)**

```
GET /api/reminders/schedule
├── Headers: Authorization: Bearer {token}
├── Response: { "nextScheduled": "2026-02-22T09:00:00", "status": "active" }
├── Status: 200

PATCH /api/reminders/pause
├── Headers: Authorization: Bearer {token}
├── Response: { "status": "paused" }
├── Status: 200

PATCH /api/reminders/resume
├── Headers: Authorization: Bearer {token}
├── Response: { "status": "active" }
├── Status: 200

GET /api/reminders/history
├── Headers: Authorization: Bearer {token}
├── Response: [ { "sentAt": "2026-02-15T09:00:00", "taskCount": 5 }, ... ]
├── Status: 200
```

#### 6. **Health Check**

```
GET /api/health
├── Response: { "status": "UP", "database": "UP", "timestamp": "..." }
├── Status: 200
```

### Fluxos de API

#### Login Flow
```
1. Cliente POST /api/auth/login com email
2. Backend valida email
3. Backend gera código OTP 6 dígitos
4. Backend envia email com código
5. Cliente POST /api/auth/verify com email + código
6. Backend valida código (expira em 15 min)
7. Backend gera JWT token (validade 7 dias)
8. Cliente armazena JWT no localStorage/sessionStorage
9. Cliente usa JWT em todas as requisições subsequentes
```

---

## <a name="frontend"></a>🎨 5. FRONTEND

### Tecnologias
- **Framework**: Angular 17+ com TypeScript
- **Build Tool**: Angular CLI (Webpack integrado)
- **Styling**: TailwindCSS + Angular Material (opcional)
- **State Management**: RxJS + NgRx (ou Akita)
- **HTTP Client**: HttpClient (built-in)
- **Routing**: Angular Router
- **Icons**: Material Icons ou Tabler Icons
- **Forms**: Reactive Forms + Validators

### Estrutura de Diretórios
```
frontend/
├── src/
│   ├── app/
│   │   ├── app.component.ts
│   │   ├── app.component.html
│   │   ├── app.component.css
│   │   ├── app-routing.module.ts
│   │   ├── app.module.ts
│   │   ├── core/
│   │   │   ├── services/
│   │   │   │   ├── auth.service.ts
│   │   │   │   ├── user.service.ts
│   │   │   │   ├── home.service.ts
│   │   │   │   ├── task.service.ts
│   │   │   │   ├── reminder.service.ts
│   │   │   │   ├── api.service.ts (Configuração HttpClient)
│   │   │   │   └── notification.service.ts
│   │   │   ├── guards/
│   │   │   │   ├── auth.guard.ts
│   │   │   │   └── unsaved-changes.guard.ts
│   │   │   ├── interceptors/
│   │   │   │   ├── auth.interceptor.ts
│   │   │   │   ├── error.interceptor.ts
│   │   │   │   └── loading.interceptor.ts
│   │   │   └── models/
│   │   │       ├── auth.model.ts
│   │   │       ├── user.model.ts
│   │   │       ├── home.model.ts
│   │   │       ├── task.model.ts
│   │   │       └── reminder.model.ts
│   │   ├── shared/
│   │   │   ├── components/
│   │   │   │   ├── header/
│   │   │   │   │   ├── header.component.ts
│   │   │   │   │   ├── header.component.html
│   │   │   │   │   └── header.component.css
│   │   │   │   ├── sidebar/
│   │   │   │   ├── footer/
│   │   │   │   ├── button/
│   │   │   │   ├── modal/
│   │   │   │   ├── input/
│   │   │   │   ├── select/
│   │   │   │   ├── loading/
│   │   │   │   └── toast/
│   │   │   ├── pipes/
│   │   │   │   ├── date-format.pipe.ts
│   │   │   │   └── title-case.pipe.ts
│   │   │   └── shared.module.ts
│   │   ├── features/
│   │   │   ├── auth/
│   │   │   │   ├── login/
│   │   │   │   │   ├── login.component.ts
│   │   │   │   │   ├── login.component.html
│   │   │   │   │   └── login.component.css
│   │   │   │   ├── verification-code/
│   │   │   │   └── auth-routing.module.ts
│   │   │   ├── dashboard/
│   │   │   │   ├── dashboard.component.ts
│   │   │   │   ├── dashboard.component.html
│   │   │   │   ├── dashboard.component.css
│   │   │   │   ├── home-selector/
│   │   │   │   ├── weekly-view/
│   │   │   │   └── dashboard-routing.module.ts
│   │   │   ├── homes/
│   │   │   │   ├── home-list/
│   │   │   │   ├── home-detail/
│   │   │   │   ├── home-form/
│   │   │   │   └── homes-routing.module.ts
│   │   │   ├── tasks/
│   │   │   │   ├── task-list/
│   │   │   │   ├── task-detail/
│   │   │   │   ├── task-form/
│   │   │   │   ├── weekly-calendar/
│   │   │   │   └── tasks-routing.module.ts
│   │   │   ├── preferences/
│   │   │   │   ├── preferences.component.ts
│   │   │   │   ├── preferences.component.html
│   │   │   │   ├── reminder-preferences/
│   │   │   │   ├── reminder-history/
│   │   │   │   └── preferences-routing.module.ts
│   │   │   └── not-found/
│   │   │       └── not-found.component.ts
│   ├── assets/
│   │   ├── images/
│   │   ├── icons/
│   │   └── styles/
│   ├── environments/
│   │   ├── environment.ts
│   │   └── environment.prod.ts
│   ├── styles.css (Global styles)
│   ├── main.ts
│   └── index.html
├── angular.json
├── tsconfig.json
├── tsconfig.app.json
├── package.json
├── tailwind.config.js
└── postcss.config.js
```

### Páginas Principais

#### 1. **Login Component** (`login.component.ts`)
- Campo para email
- Botão "Enviar Código"
- Campo para código OTP (apareça após envio)
- Botão "Verificar e Entrar"
- Mensagens de erro/sucesso
- Reactive Forms com validação
- Responsive para mobile

#### 2. **Dashboard Component** (`dashboard.component.ts`)
- Header com username e logout
- Seletor de residência (dropdown ou cards)
- Visualização semanal dos afazeres via component `weekly-view`
  - Segunda-feira: [Lavar roupa, Estender roupa]
  - Terça-feira: [Varrer quarto]
  - ...etc
- Botão "+ Novo Afazer"
- Botão "Gerenciar Residências"
- Botão "Preferências de Lembretes"
- Sub-componentes: `home-selector`, `weekly-view`

#### 3. **Home Detail Component** (`home-detail.component.ts`)
- Nome da residência
- Descrição
- Lista de afazeres cadastrados via `task-list`
- Opção editar/deletar
- Botão "+ Adicionar Afazer"
- Voltar ao dashboard

#### 4. **Task Create/Edit Component** (`task-form.component.ts`)
- Reactive Form com validadores
- Campo título
- Campo descrição
- Seletor dia da semana
- Seletor prioridade (Baixa/Média/Alta)
- Campo notas adicionais
- Botão salvar/atualizar
- Botão cancelar
- Tratamento de erros de formulário

#### 5. **Preferences Component** (`preferences.component.ts`)
- Seletor dia da semana para lembrete (padrão: sexta-feira)
- Seletor hora do lembrete (padrão: 09:00)
- Seletor tipo (Email / Web / Ambos)
- Seletor timezone
- Toggle ativar/desativar lembretes
- Sub-componente `reminder-history` para histórico
- Botão salvar

### Fluxo de Navegação

```
Login Route
    ↓
Dashboard Route
    ├─ home/:id → Home Detail
    │  ├─ tasks/create → Task Form (Create) → Home Detail
    │  ├─ tasks/:taskId/edit → Task Form (Edit) → Home Detail
    │  └─ Back → Dashboard
    ├─ homes/manage → Home Management (Modal/Page)
    │  ├─ homes/create
    │  ├─ homes/:id/edit
    │  └─ homes/:id/delete (com confirmação)
    └─ preferences → Preferences
       └─ Save → Dashboard
```

### Responsividade
- **Mobile First Approach**: Começar com mobile (< 640px) depois desktop
- **Breakpoints**: sm: 640px, md: 768px, lg: 1024px, xl: 1280px
- **Touch-friendly**: Botões com tamanho mínimo 48x48px
- **Viewport**: Meta tag viewport configurada
- **Ajustes**: Sidebars colapsam em mobile, modais occupam tela cheia

---

## <a name="autenticação"></a>🔐 6. FLUXO DE AUTENTICAÇÃO

### Sistema de Autenticação Passwordless

#### Processo Login
```
PASSO 1: Cliente entra no site
├─ Vê página de login com campo email
└─ Clica "Enviar Código"

PASSO 2: Backend - Envio do Código
├─ Recebe POST /api/auth/login com email
├─ Valida formato email
├─ Gera código OTP (6 dígitos aleatório)
├─ Salva em auth_tokens com expiration (15 min)
├─ Envia email com código
├─ Retorna success message
└─ Se email não existe, cria novo usuário automaticamente

PASSO 3: Cliente - Recebimento do Código
├─ Recebe email com código
├─ Insere código nos campos de verificação
├─ Clica "Verificar e Entrar"
└─ Envia POST /api/auth/verify

PASSO 4: Backend - Validação
├─ Recebe email + código
├─ Busca em auth_tokens
├─ Valida se código ainda é válido (não expirou)
├─ Marca código como usado
├─ Gera JWT token (validade 7 dias)
├─ Atualiza last_login do usuário
└─ Retorna JWT + User data

PASSO 5: Cliente - Armazenamento
├─ Armazena JWT (localStorage)
├─ Armazena user data (estado React)
├─ Redireciona para Dashboard
└─ Envia JWT em Authorization header (Bearer token)
```

### JWT Token
```
Header: {
  "alg": "HS256",
  "typ": "JWT"
}

Payload: {
  "sub": "user_uuid",
  "email": "user@example.com",
  "name": "João Silva",
  "iat": 1645000000,
  "exp": 1645604800  // 7 dias
}

Signature: HMAC-SHA256(base64(header) + base64(payload), secret_key)
```

### Segurança
- JWT secret armazenado em variável de ambiente
- Código OTP válido por apenas 15 minutos
- Código OTP pode ser usado apenas 1 vez
- Email enviado com SSL/TLS
- HTTPS obrigatório em produção
- Rate limiting: máx 5 tentativas de login por email a cada 1 hora
- Rate limiting: máx 3 tentativas de verificação por email a cada 15 minutos
- Audit log de todos os logins (email, IP, timestamp)

### Refresh Token (Futuro)
Para melhor UX, implementar refresh tokens:
```
POST /api/auth/refresh
├── Request: { "refreshToken": "..." }
├── Response: { "token": "new_jwt_token" }
└── Refresh token válido por 30 dias
```

---

## <a name="lembretes"></a>🔔 7. SISTEMA DE LEMBRETES

### Estratégia de Lembretes

#### Tipo 1: Lembrete por Email (Recomendado para MVP)
```
CONFIGURAÇÃO:
├─ Dia da semana: Sexta-feira (padrão) ou customizável (seg-dom)
├─ Hora: 09:00 AM (padrão) ou customizável
├─ Conteúdo: Resumo das tarefas da próxima semana

EXECUÇÃO (Agendado via Quartz Scheduler):
├─ Job roda diariamente às 00:00 (ou em intervalo curto)
├─ Verifica qual horário é o preferido do usuário
├─ Verifica a hora atual na timezone do usuário
├─ Se for o horário, gera email
├─ Busca tarefas da próxima semana
├─ Renderiza template HTML com tarefas
├─ Envia via SendGrid/SMTP
├─ Registra em email_logs
└─ Atualiza last_sent em reminders

TEMPLATE DO EMAIL:
Assunto: Seu planejamento semanal - AE Organiza Lar

Olá {{userName}},

Aqui está seu planejamento para a semana de {{weekStart}} até {{weekEnd}}:

{{#homes}}
📍 {{homeName}}
{{#tasks}}
  ✓ {{dayName}}: {{taskTitle}} ({{priority}})
{{/tasks}}
{{/homes}}

Se preferir, você pode pausar os lembretes em sua conta.

Atenciosamente,
AE Organiza Lar
```

#### Tipo 2: Notificação Web (Futuro - Fase 2)
```
Usar Web Push Notifications (Service Worker + Push API)
- Notificação no navegador
- Funciona mesmo com abas fechadas
- Requer consentimento do usuário
- Implementação em segunda fase
```

### Preferências de Lembretes

```
Usuário pode configurar:
1. Dia da semana (1-7)
2. Hora (HH:MM)
3. Tipo (Email | Web | Ambos)
4. Timezone (importante para cálculo correto)
5. Status (Ativo | Pausado | Desabilitado)

Padrões:
├─ Dia: Sexta-feira (dia 5)
├─ Hora: 09:00
├─ Tipo: Email
├─ Timezone: America/Sao_Paulo
└─ Status: Ativo (novo usuário)
```

### Implementação Técnica

#### Backend - Scheduler
```java
// ReminderScheduler.java (Quarkus)
@ApplicationScoped
public class ReminderScheduler {
  
  @Scheduled(every="1m") // Verifica a cada 1 minuto
  void checkAndSendReminders() {
    // 1. Busca todos os usuários com reminders ativos
    // 2. Para cada usuário:
    //    a. Converte hora preferida para timezone do usuário
    //    b. Verifica se é hora de enviar
    //    c. Se sim, busca tarefas da próxima semana
    //    d. Renderiza template
    //    e. Envia email
    //    f. Registra em email_logs
    //    g. Atualiza reminder.last_sent
  }
}
```

#### Email Service
```java
// EmailService.java
public class EmailService {
  
  public void sendWeeklyReminder(User user, List<Task> tasks) {
    String html = renderTemplate("email-weekly-reminder.html", 
      Map.of(
        "userName", user.getName(),
        "weekStart", weekStart,
        "weekEnd", weekEnd,
        "homes", groupedTasks
      )
    );
    
    sendEmail(
      to: user.getEmail(),
      subject: "Seu planejamento semanal",
      html: html,
      from: "noreply@aelares.com"
    );
  }
}
```

---

## <a name="fases"></a>📅 8. FASES DE DESENVOLVIMENTO

### Fase 1: MVP - Core (2-3 semanas)
**Objetivo**: Funcionalidade básica funcionando end-to-end

#### Backend
- [ ] Setup Quarkus project com PostgreSQL
- [ ] Entidades: User, Home, Task, TaskSchedule, AuthToken
- [ ] Autenticação: Login/Verify endpoints
- [ ] CRUD: Homes e Tasks
- [ ] Migrations de banco
- [ ] Validações básicas
- [ ] Exception handling global

#### Frontend
- [ ] Setup Angular CLI (ng new)
- [ ] Criar módulos por feature (auth, homes, tasks, preferences, shared, core)
- [ ] Login component com OTP
- [ ] Dashboard component com seletor de residências
- [ ] Home detail component
- [ ] Task CRUD components (create, edit, list)
- [ ] Preferences component
- [ ] Services (auth, user, home, task, reminder, api)
- [ ] Guards de autenticação (AuthGuard)
- [ ] Interceptors (JwtInterceptor, ErrorInterceptor)
- [ ] Models/Interfaces TypeScript
- [ ] Reactive Forms com validação
- [ ] Routing configurado
- [ ] Integração com backend API
- [ ] TailwindCSS configurado

#### Infrastructure
- [ ] Docker Compose com PostgreSQL
- [ ] Configuração de CORS
- [ ] HTTPS em dev (self-signed cert)

#### Email Service (Básico)
- [ ] SendGrid API integration
- [ ] Template de código OTP
- [ ] Teste de envio

### Fase 2: Lembretes (1-2 semanas)
**Objetivo**: Sistema de lembretes por email funcionando

#### Backend
- [ ] Entidade Reminder e EmailLog
- [ ] Quartz Scheduler para verificação
- [ ] ReminderService com lógica
- [ ] Endpoints: GET schedule, PATCH pause, PATCH resume
- [ ] Template renderizado com tarefas
- [ ] Rate limiting em auth
- [ ] Audit log

#### Frontend
- [ ] Preferences page
- [ ] Configuração de lembretes
- [ ] Visualização de histórico
- [ ] Pause/Resume reminders

#### Testing
- [ ] Testes unitários do scheduler
- [ ] Testes de email rendering
- [ ] Testes de timezone handling

### Fase 3: UX & Polimento (1 semana)
**Objetivo**: Aplicação pronta para uso

#### Frontend
- [ ] Responsividade mobile completa com TailwindCSS
- [ ] Testes em navegadores (Chrome, Firefox, Safari, Edge)
- [ ] Testes em dispositivos móveis reais
- [ ] Otimização de performance (bundle size, lazy loading)
- [ ] Accessibility (WCAG 2.1 AA)
- [ ] Loading states com spinners
- [ ] Toast notifications melhoradas (toastr ou ngx-toastr)
- [ ] Testes unitários (Jasmine/Karma)
- [ ] Testes E2E (Cypress ou Protractor)
- [ ] Dark mode (opcional)

#### Backend
- [ ] Otimização de queries
- [ ] Caching com Redis (opcional)
- [ ] Compressão de respostas
- [ ] Logging estruturado

#### DevOps
- [ ] CI/CD pipeline (GitHub Actions/GitLab)
- [ ] Testes automatizados
- [ ] Code coverage

### Fase 4: Deploy & Monitoramento (1-2 semanas)
**Objetivo**: Aplicação em produção

#### Deployment
- [ ] Build Docker image
- [ ] Configuração em cloud (AWS/Heroku/DigitalOcean)
- [ ] SSL/TLS certificate
- [ ] CDN para frontend assets
- [ ] Database backups

#### Monitoring
- [ ] Logs centralizados (ELK Stack)
- [ ] Alertas para erros
- [ ] Métricas de performance
- [ ] Uptime monitoring

#### Documentação
- [ ] README com setup
- [ ] API documentation (Swagger)
- [ ] Runbook para ops
- [ ] Manual do usuário

---

## <a name="tecnologias"></a>💻 9. TECNOLOGIAS RECOMENDADAS

### Backend
| Tecnologia | Versão | Motivo |
|------------|--------|--------|
| Quarkus | 3.x | Framework Java moderno, rápido, otimizado para containers |
| Java | 17+ | Suporte LTS, performance melhorada |
| PostgreSQL | 14+ | Banco relacional robusto, JSONB support |
| Hibernate ORM | 6.x | Mapeamento objeto-relacional integrado ao Quarkus |
| Quartz | 2.x | Job scheduling para lembretes |
| SendGrid SDK | 4.x | Serviço de email confiável ou usar SMTP |
| JWT | io.jsonwebtoken:jjwt | Geração/validação de tokens |
| JUnit 5 + Mockito | - | Testes unitários |
| Flyway/Liquibase | - | Migrations de banco (Quarkus usa Flyway) |

### Frontend
| Tecnologia | Versão | Motivo |
|------------|--------|--------|
| Angular | 17.x | Framework completo, escalável, ideal para SPAs |
| TypeScript | 5.x | Type safety, melhor DX |
| Angular CLI | 17.x | Ferramentas integradas para scaffolding e build |
| TailwindCSS | 3.x | Utility-first CSS, rápido para prototipagem |
| RxJS | 7.x | Reactive programming, essencial para Angular |
| Angular Material | 17.x | Componentes prontos e acessíveis (opcional) |
| ngx-toastr | - | Notifications/Toast |
| Angular Forms | - | Reactive Forms integrado |
| Jasmine + Karma | - | Testes unitários |
| Cypress | - | Testes E2E |

### DevOps & Infrastructure
| Tecnologia | Propósito |
|------------|----------|
| Docker | Containerização backend e frontend |
| Docker Compose | Orquestração local (dev) |
| GitHub Actions | CI/CD automation |
| PostgreSQL Docker | Banco em container |
| Nginx | Proxy reverso (em prod) |
| AWS ECS/Fargate ou Heroku | Hosting cloud |

### Email Service
| Opção | Custo | Volumes |
|-------|-------|---------|
| SendGrid | Freemium (100/dia) | Ideal para MVP |
| Mailgun | Freemium (1000/mês) | Alternativa |
| SMTP Self-Hosted | Gratuito | Complexo, requer maintenance |

**Recomendação**: SendGrid para MVP (confiável, fácil integração)

### Banco de Dados
| Opção | Motivo |
|-------|--------|
| PostgreSQL | Recomendado (relacional, JSONB, performance) |
| MySQL 8.x | Alternativa válida |
| MongoDB | Não recomendado (modelo relacional é melhor aqui) |

**Recomendação**: PostgreSQL 14+ (melhor para modelo relacional)

---

## <a name="segurança"></a>🔒 10. CONSIDERAÇÕES DE SEGURANÇA

### Autenticação & Autorização
- ✅ Login passwordless com OTP
- ✅ JWT com expiration de 7 dias
- ✅ Rate limiting em login (5 tentativas/hora)
- ✅ Rate limiting em verificação (3 tentativas/15 min)
- ✅ Código OTP válido por 15 minutos
- ✅ HTTPS obrigatório em produção
- ✅ Secure cookies (HttpOnly, Secure, SameSite)
- ✅ CSRF protection via tokens
- ⚠️ Implementar refresh tokens (Fase 2)

### Dados Sensíveis
- ✅ Emails nunca expostos em logs
- ✅ Senhas não armazenadas (passwordless)
- ✅ Código OTP hasheado no banco
- ✅ JWT secret em variável de ambiente
- ✅ SendGrid API key em variável de ambiente
- ✅ Database credentials em variável de ambiente

### Validações
- ✅ Validação de email format
- ✅ Validação de input (XSS prevention)
- ✅ SQL injection prevention (via ORM)
- ✅ CORS configurado (frontend domain apenas)
- ✅ Content-Type validation
- ✅ Request size limits

### Banco de Dados
- ✅ Foreign keys com constraints
- ✅ NOT NULL constraints
- ✅ Unique constraints em email
- ✅ Índices para performance
- ✅ Backups automatizados (em prod)
- ✅ Encryption at rest (em prod)

### Logging & Audit
- ✅ Audit log de logins (email, IP, timestamp)
- ✅ Email logs para tracking
- ✅ Error logs sem dados sensíveis
- ✅ Logs centralizados em produção

### Deploys & CI/CD
- ✅ Secrets gerenciados via environment variables
- ✅ No hardcoded credentials
- ✅ Teste de segurança via OWASP ZAP (futuro)
- ✅ Dependency scanning via Snyk (futuro)

---

## 📊 RESUMO DO CRONOGRAMA

| Fase | Duração | Entrega Principal | Status |
|------|---------|------------------|--------|
| 1 - MVP Core | 2-3 sem | Login, CRUD Homes/Tasks | 📋 Planejado |
| 2 - Lembretes | 1-2 sem | Email reminders, Preferences | 📋 Planejado |
| 3 - UX Polished | 1 sem | Responsividade, Performance | 📋 Planejado |
| 4 - Deploy | 1-2 sem | Production ready, Monitoring | 📋 Planejado |
| **TOTAL** | **5-8 semanas** | MVP + Features | - |

---

## ✅ CHECKLIST PRÉ-DESENVOLVIMENTO

- [ ] Aprovação deste planejamento
- [ ] Definição de email service (SendGrid/Mailgun/SMTP)
- [ ] Definição de cloud provider (AWS/Heroku/DigitalOcean)
- [ ] Setup de repositório Git (GitHub/GitLab)
- [ ] Setup de IDE (IntelliJ/VS Code com plugins)
- [ ] Preparação de máquina local (Docker, Node, Java 17+, PostgreSQL)
- [ ] Criação de estrutura de pasta inicial
- [ ] Setup de CI/CD básico
- [ ] Configuração de email de notificação (noreply@domain)
- [ ] Domain/DNS configurado (se aplicável)

---

## 📞 PRÓXIMOS PASSOS

1. **Aprovação**: Revisar e aprovar planejamento
2. **Setup Inicial**: Preparar ambiente de desenvolvimento
3. **Fase 1**: Iniciar backend Quarkus + banco dados
4. **Fase 1**: Iniciar frontend React em paralelo
5. **Testing**: Integração e testes end-to-end
6. **Deploy**: Publicar MVP

---

**Documento versão**: 1.0  
**Data de criação**: Fevereiro 2026  
**Autor**: Planejamento técnico  
**Status**: Aguardando aprovação

