# AE Organiza Lar - Frontend (Angular)

MVP inicial do frontend para integrar com o backend Quarkus ja existente.

## Escopo implementado

- Login por e-mail com envio de codigo OTP
- Verificacao de codigo OTP para obter token JWT
- Persistencia de token em `localStorage`
- Interceptor HTTP para enviar `Authorization: Bearer <token>`
- Rotas privadas com guarda de autenticacao
- Layout base autenticado com navegacao
- CRUD completo de `homes` (listar, criar, editar e excluir)
- CRUD de `tasks` por lar com filtro por dia da semana
- Visao semanal de tarefas em cards agrupados por dia
- Marcacao e desmarcacao de conclusao de tarefas por semana/data de referencia
- Pagina inicial de `reminders`
- Integracao real de lembretes (`/api/v1/reminders/info` e `/api/v1/reminders/send-now`)

## Estrutura principal

- `src/app/core`: auth, guardas, interceptor, modelos e servicos base
- `src/app/features`: funcionalidades por area de negocio
- `src/app/layouts`: casca autenticada da aplicacao
- `src/environments/environment.ts`: URL da API

## Pre-requisitos

- Node.js 22+
- npm 11+
- Backend Quarkus em execucao na URL `http://localhost:8080`

## Como rodar

```bash
cd frontend
npm install
npm start
```

A aplicacao sobe em `http://localhost:4200`.

## Build

```bash
cd frontend
npm run build
```

## Fluxo rapido para validar

1. Acesse `http://localhost:4200/login`.
2. Informe um e-mail valido e envie o codigo.
3. Pegue o OTP (MailHog ou e-mail configurado no backend).
4. Valide em `/verify-code`.
5. Acesse as telas protegidas (`/home`, `/homes`, `/tasks`, `/reminders`).

## Proximos passos

- Evoluir a visao semanal para modo calendario
- Permitir notas de conclusao diretamente na interface
