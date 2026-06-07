# Deployment — AE Organiza Lar no Railway

## Pré-requisitos
- Conta Railway (railway.app)
- Conta SendGrid (sendgrid.com)
- Domínio customizado (opcional)

## Passo 1: Criar Projeto Railway

1. Acesse https://railway.app
2. Clique "Create New Project"
3. Selecione "Deploy from GitHub"
4. Autorize Railway a acessar seu repositório
5. Selecione o repositório `ae-organiza-lar`

## Passo 2: Configurar PostgreSQL

1. No Railway Dashboard do seu projeto, clique "New"
2. Selecione "Database" → PostgreSQL
3. Railway cria a instância automáticamente
4. Vá em **Connect** e copie as variáveis de conexão
5. Você verá algo como:
   ```
   PGHOST=railway.app
   PGPORT=1234
   DATABASE_URL=postgresql://postgres:...
   ```

## Passo 3: Configurar Backend (Java/Quarkus)

1. No projeto Railway, clique "New" → "GitHub Repo"
2. Configure:
   - **Repository**: seu fork do ae-organiza-lar
   - **Root Directory**: `backend`
   - **Branch**: main
3. Railway detecta `pom.xml` e constrói automaticamente
4. Em **Variables**, clique "Add" e adicione:

```
QUARKUS_PROFILE=prod
DB_HOST=postgres.railway.internal
DB_PORT=5432
DB_USER=postgres
DB_PASSWORD=<senha-postgres-do-seu-banco>
DB_NAME=aelares_db
SENDGRID_API_KEY=SG.sua-api-key
SENDGRID_FROM_EMAIL=noreply@aelares.com
JWT_SECRET=sua-chave-jwt-super-secreta-aqui
CORS_ORIGINS=https://<seu-frontend-domain>
```

5. Clique "Deploy"

## Passo 4: Configurar Frontend (Angular)

1. No projeto Railway, clique "New" → "GitHub Repo"
2. Configure:
   - **Repository**: seu fork do ae-organiza-lar
   - **Root Directory**: `frontend`
   - **Branch**: main
3. Você pode deixar o build automático (Railway detecta `package.json`)
4. Em **Variables**, adicione:
   ```
   API_URL=https://<seu-backend-railway-domain>
   ```
5. Clique "Deploy"

## Passo 5: Configurar SendGrid

1. Acesse https://sendgrid.com
2. Crie conta ou login (se já tiver)
3. Vá para **Settings** → **API Keys**
4. Clique **"Create API Key"**
   - Nome: "AE Organiza Lar Production"
   - Permissões: Full Access (ou somente Mail Send)
5. Copie a chave gerada (ex: `SG.xxxxxxxxxxxxxxxx`)
6. Volte ao Railway, edite o serviço backend
7. Adicione/atualize: `SENDGRID_API_KEY=SG.xxxxxxxxxxxxxxxx`

## Passo 6: Adicionar Domínio Customizado (Opcional)

### Para o Backend:

1. No Railway, selecione o serviço **Backend**
2. Em **Settings**, localize **Domains**
3. Railway oferecerá um domínio public: `seu-projeto-prod.up.railway.app`
4. Se quiser domínio próprio (ex: `api.aelares.com.br`):
   - Copie o CNAME gerado no Railway
   - Vá no seu registrador (Namecheap, Google Domains, etc)
   - Adicione um registro CNAME:
     ```
     api  CNAME  seu-projeto-prod.up.railway.app
     ```
   - Aguarde propagação DNS (5-30 segundos a alguns minutos)

### Para o Frontend:

1. No Railway, selecione o serviço **Frontend**
2. Em **Settings**, localize **Domains**
3. Railway oferecerá: `seu-projeto-prod-fe.up.railway.app`
4. Se usar domínio próprio (ex: `aelares.com.br`):
   - Copie o CNAME
   - Registrador → Adicione CNAME
   - Aguarde propagação

## Passo 7: Deploy

1. Faça um commit em seu repositório
2. Faça push para `main`:
   ```bash
   git add .
   git commit -m "Configuração produção"
   git push origin main
   ```
3. Railway detecta mudanças automaticamente
4. Veja logs em tempo real no Dashboard

## Verificação

1. **Frontend**: Acesse `https://seu-dominio.railway.app`
2. **Backend API Doc**: Acesse `https://seu-backend.railway.app/swagger-ui` (será exibido em dev; em prod está desabilitado)
3. **Testes Funcionais**:
   - Faça login com seu e-mail
   - Verifique se recebe código (check SendGrid Logs no dashboard)
   - Crie um lar
   - Crie uma tarefa
   - Confirme tudo funciona

## Troubleshooting

### ❌ "Não foi possível enviar o código" (Login falha)

**Possíveis causas:**

1. `SENDGRID_API_KEY` incorreta ou expirada
2. E-mail nao configurado corretamente
3. SMTP não alcançavel

**Verificação:**

- Railway Dashboard → Backend → Logs
- Procure por "ERROR" ou "Failed to send"
- Confirme `SENDGRID_API_KEY` está preenchida
- Teste a chave no dashboard SendGrid

### ❌ Frontend não conecta ao backend

**Verificação:**

1. Confirme `CORS_ORIGINS` no backend inclui URL do frontend
2. Confirme `API_URL` no frontend aponta ao backend correto
3. Frontend → Logs → procure por erros CORS

**Solução:**

- Backend: Editar variável `CORS_ORIGINS`, adicionar URL do frontend
- Frontend: Editar variável `API_URL`, apontar para backend correto
- Refazer deploy

### ❌ Banco de dados vazio ou erro de migração

**Verificação:**

- Backend → Logs
- Procure por linhas contendo "Flyway" ou "Migration"

**Possibilidades:**

1. `FLYWAY_MIGRATE_AT_START` não está habilitado (já está por padrão)
2. Senha do banco errada
3. Database não existe

**Solução:**

- Verificar variáveis: `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`
- Manualmente conectar ao banco e criar database se não existir
- Refazer deploy

### ❌ Deploy falha com erro de compilação Maven

**Verificação:**

- Railway Dashboard → Build Logs
- Procure por `[ERROR]`

**Possibilidades:**

1. Dependência faltando em `pom.xml`
2. Código com erro de sintaxe
3. Versão Java incorreta

**Solução:**

- Verificar `pom.xml`
- Compilar localmente: `mvn clean compile`
- Fixar erros
- Fazer push novamente

### ❌ Timeout ou container mata durante build

Railway pode ter recursos limitados. Soluções:

1. Remover dependências não usadas
2. Aumentar plan Railway (pago)
3. Verificar buildpacks/Dockerfile

## Próximos passos (Opcional)

- [ ] Configurar backups automáticos do banco
- [ ] Adicionar SSL/TLS customizado
- [ ] Configurar alertas/health checks
- [ ] Monitorar logs com ferramentas externas
- [ ] Implementar CI/CD mais complexo (GitHub Actions)

