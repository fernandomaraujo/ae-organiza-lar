#!/bin/bash

# AE Organiza Lar - API Test Commands
# Execute os comandos curl abaixo para testar a API

API_URL="http://localhost:8080/api/v1"
EMAIL="user@example.com"

echo "=========================================="
echo "AE Organiza Lar - API Test Commands"
echo "=========================================="

# 1. LOGIN - Enviar código OTP por email
echo -e "\n1. Enviando código OTP para $EMAIL"
curl -X POST "$API_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\"}" \
  | jq .

echo -e "\n⚠️ Verifique seu email ou MailHog (http://localhost:8025) para o código"

# 2. VERIFY - Substituir XXXXXX pelo código recebido
read -p "Cole o código recebido: " OTP_CODE

echo -e "\n2. Verificando código OTP"
TOKEN_RESPONSE=$(curl -s -X POST "$API_URL/auth/verify" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"code\":\"$OTP_CODE\"}")

echo $TOKEN_RESPONSE | jq .

# Extrair token
TOKEN=$(echo $TOKEN_RESPONSE | jq -r '.token')
echo -e "\n✅ Token obtido: ${TOKEN:0:50}..."

# 3. GET Current User
echo -e "\n3. Buscando dados do usuário autenticado"
curl -s -X GET "$API_URL/users/me" \
  -H "Authorization: Bearer $TOKEN" \
  | jq .

# 4. CREATE HOME
echo -e "\n4. Criando residência"
HOME_RESPONSE=$(curl -s -X POST "$API_URL/homes" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Minha Casa",
    "description": "Casa principal onde moro"
  }')

echo $HOME_RESPONSE | jq .
HOME_ID=$(echo $HOME_RESPONSE | jq -r '.id')
echo -e "Home ID: $HOME_ID"

# 5. LIST HOMES
echo -e "\n5. Listando minhas residências"
curl -s -X GET "$API_URL/homes" \
  -H "Authorization: Bearer $TOKEN" \
  | jq .

# 6. CREATE TASK (Segunda-feira)
echo -e "\n6. Criando tarefa para segunda-feira"
TASK_RESPONSE=$(curl -s -X POST "$API_URL/homes/$HOME_ID/tasks" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Lavar roupas",
    "description": "Lavar e estender roupas no varal",
    "dayOfWeek": 1,
    "priority": "HIGH",
    "notes": "Não esquecer de usar água morna"
  }')

echo $TASK_RESPONSE | jq .
TASK_ID=$(echo $TASK_RESPONSE | jq -r '.id')

# 7. CREATE ANOTHER TASK (Terça-feira)
echo -e "\n7. Criando tarefa para terça-feira"
curl -s -X POST "$API_URL/homes/$HOME_ID/tasks" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Varrer o quarto",
    "description": "Varrer o piso do quarto",
    "dayOfWeek": 2,
    "priority": "MEDIUM"
  }' | jq .

# 8. LIST TASKS
echo -e "\n8. Listando todas as tarefas"
curl -s -X GET "$API_URL/homes/$HOME_ID/tasks" \
  -H "Authorization: Bearer $TOKEN" \
  | jq .

# 9. GET TASKS BY DAY OF WEEK
echo -e "\n9. Tarefas de segunda-feira (dayOfWeek=1)"
curl -s -X GET "$API_URL/homes/$HOME_ID/tasks?dayOfWeek=1" \
  -H "Authorization: Bearer $TOKEN" \
  | jq .

# 10. GET SPECIFIC TASK
echo -e "\n10. Buscando tarefa específica"
curl -s -X GET "$API_URL/homes/$HOME_ID/tasks/$TASK_ID" \
  -H "Authorization: Bearer $TOKEN" \
  | jq .

# 11. UPDATE TASK
echo -e "\n11. Atualizando tarefa"
curl -s -X PUT "$API_URL/homes/$HOME_ID/tasks/$TASK_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Lavar roupas - ATUALIZADO",
    "description": "Lavar, secar e pendurar",
    "dayOfWeek": 1,
    "priority": "MEDIUM",
    "notes": "Tarefa atualizada"
  }' | jq .

# 12. DELETE TASK
echo -e "\n12. Deletando tarefa"
curl -s -X DELETE "$API_URL/homes/$HOME_ID/tasks/$TASK_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nStatus: %{http_code}\n"

# 13. UPDATE HOME
echo -e "\n13. Atualizando residência"
curl -s -X PUT "$API_URL/homes/$HOME_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Minha Casa - ATUALIZADO",
    "description": "Casa onde mora a família"
  }' | jq .

# 14. DELETE HOME
echo -e "\n14. Deletando residência"
curl -s -X DELETE "$API_URL/homes/$HOME_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nStatus: %{http_code}\n"

echo -e "\n=========================================="
echo "✅ Teste concluído!"
echo "=========================================="

