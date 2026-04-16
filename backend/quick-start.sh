#!/bin/bash

# ============================================
# AE Organiza Lar - QUICK START
# ============================================

echo "🚀 AE Organiza Lar - Inicialização Rápida"
echo "==========================================="

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java não está instalado. Por favor, instale Java 17+."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1)
echo "✅ Java encontrado: $JAVA_VERSION"

# Check Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker não está instalado. Por favor, instale Docker."
    exit 1
fi

echo "✅ Docker encontrado"

# Check Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose não está instalado."
    exit 1
fi

echo "✅ Docker Compose encontrado"

echo ""
echo "📦 Iniciando serviços (PostgreSQL + MailHog)..."
docker-compose up -d

# Wait for PostgreSQL
echo ""
echo "⏳ Aguardando PostgreSQL ficar pronto..."
for i in {1..30}; do
    if docker exec aelares-postgres pg_isready -U aelares_user > /dev/null 2>&1; then
        echo "✅ PostgreSQL pronto!"
        break
    fi
    echo -n "."
    sleep 1
done

echo ""
echo "==========================================="
echo "✅ SETUP COMPLETO!"
echo "==========================================="
echo ""
echo "📊 Serviços iniciados:"
echo "  • PostgreSQL: localhost:5432"
echo "  • MailHog Web UI: http://localhost:8025"
echo ""
echo "🚀 Para iniciar o backend em modo dev:"
echo "   ./mvnw quarkus:dev"
echo ""
echo "🧪 Para testar a API:"
echo "   bash test-api.sh"
echo ""
echo "📖 Documentação: Veja README.md"
echo "==========================================="

