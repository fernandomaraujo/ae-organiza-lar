# AE Organiza Lar - Backend

Backend API para gerenciamento de afazeres domésticos desenvolvido com Quarkus.

## Pré-requisitos

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 15+ (ou via Docker)

## Configuração

### 1. Iniciar Banco de Dados e Serviços

```bash
cd backend
docker-compose up -d
```

Esto iniciará:
- PostgreSQL (porta 5432)
- MailHog (SMTP na porta 1025, Web UI na porta 8025)

### 2. Configurar Variáveis de Ambiente (Opcional)

Crie um arquivo `.env` na raiz do projeto backend:

```env
JWT_SECRET=seu-secret-key-super-secreto
SENDGRID_API_KEY=sua-chave-sendgrid-opcional
```

## Desenvolvimento

### Iniciar em Modo Dev

```bash
./mvnw quarkus:dev
```

A aplicação estará disponível em `http://localhost:8080`

O modo dev permite:
- Hot reload (recarregamento automático ao salvar)
- Debug interativo
- Relatório de cobertura de testes

## Build

### Build para Produção

```bash
./mvnw clean package
```

### Build Nativo com GraalVM

```bash
./mvnw clean package -Pnative
```

## API Endpoints

### Autenticação

- **POST** `/api/v1/auth/login` - Envia código OTP por email
- **POST** `/api/v1/auth/verify` - Verifica código e retorna JWT token
- **POST** `/api/v1/auth/validate` - Valida token

### Usuários

- **GET** `/api/v1/users/me` - Dados do usuário autenticado
- **GET** `/api/v1/users/{id}` - Busca usuário por ID

### Residências

- **GET** `/api/v1/homes` - Lista residências do usuário
- **POST** `/api/v1/homes` - Cria nova residência
- **GET** `/api/v1/homes/{id}` - Busca residência
- **PUT** `/api/v1/homes/{id}` - Atualiza residência
- **DELETE** `/api/v1/homes/{id}` - Deleta residência

### Tarefas

- **GET** `/api/v1/homes/{homeId}/tasks` - Lista tarefas da residência
- **GET** `/api/v1/homes/{homeId}/tasks?dayOfWeek=1` - Filtra por dia da semana
- **POST** `/api/v1/homes/{homeId}/tasks` - Cria nova tarefa
- **GET** `/api/v1/homes/{homeId}/tasks/{taskId}` - Busca tarefa
- **PUT** `/api/v1/homes/{homeId}/tasks/{taskId}` - Atualiza tarefa
- **DELETE** `/api/v1/homes/{homeId}/tasks/{taskId}` - Deleta tarefa

## Estrutura do Projeto

```
backend/
├── src/main/
│   ├── java/com/aelares/
│   │   ├── entity/              # Entidades JPA
│   │   ├── dto/
│   │   │   ├── request/         # DTOs de entrada
│   │   │   └── response/        # DTOs de saída
│   │   ├── service/             # Lógica de negócio
│   │   ├── resource/            # REST endpoints
│   │   └── exception/           # Exceções customizadas
│   └── resources/
│       ├── application.yaml     # Configuração padrão
│       ├── application-dev.yaml # Configuração dev
│       └── db/migration/        # Scripts SQL (Flyway)
├── pom.xml                      # Dependências Maven
├── docker-compose.yml           # Serviços locais
└── README.md                    # Este arquivo
```

## Fluxo de Autenticação

1. **Login**: Cliente envia email
2. **OTP**: Backend gera código (6 dígitos) e envia por email
3. **Verificação**: Cliente envia email + código
4. **Token**: Backend retorna JWT válido por 7 dias
5. **Acesso**: Cliente usa token no header `Authorization: Bearer <token>`

## Banco de Dados

Esquema criado automaticamente com Flyway:

- **users**: Usuários da aplicação
- **homes**: Residências cadastradas
- **tasks**: Tarefas domésticas
- **task_schedules**: Agendamento das tarefas (dia da semana)
- **auth_tokens**: Tokens OTP para login
- **reminders**: Configurações de lembretes (Fase 2)
- **email_logs**: Histórico de emails enviados (Fase 2)

## Próximas Fases

- **Fase 2**: Sistema de lembretes automáticos
- **Fase 3**: WebSocket para notificações em tempo real
- **Fase 4**: Frontend Angular

## Troubleshooting

### Erro de conexão com PostgreSQL

```bash
# Verificar se container está rodando
docker ps

# Logs do container
docker logs aelares-postgres

# Reconectar container
docker-compose restart postgres
```

### Erro ao enviar email

O projeto usa MailHog em desenvolvimento. Acesse a Web UI em `http://localhost:8025` para verificar emails enviados.

### Limpar dados do banco

```bash
docker-compose down -v
docker-compose up -d
```

## Licença

Proprietary - AE Organiza Lar

