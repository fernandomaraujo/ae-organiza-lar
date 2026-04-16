#!/bin/bash

# AE Organiza Lar - Checklist de Validação

echo "🔍 AE Organiza Lar - Checklist de Validação"
echo "============================================"
echo ""

ERRORS=0
WARNINGS=0

# Check Java
echo -n "✓ Verificando Java 17+... "
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | grep -oE '[0-9]+\.[0-9]+' | head -1)
    if [ ! -z "$JAVA_VERSION" ]; then
        echo "✅ Java $JAVA_VERSION encontrado"
    else
        echo "❌ Versão não detectada"
        ((ERRORS++))
    fi
else
    echo "❌ Java não encontrado"
    ((ERRORS++))
fi

# Check Maven
echo -n "✓ Verificando Maven... "
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -v 2>&1 | grep -oE '[0-9]+\.[0-9]+\.[0-9]+' | head -1)
    echo "✅ Maven $MVN_VERSION encontrado"
else
    echo "❌ Maven não encontrado"
    ((ERRORS++))
fi

# Check Docker
echo -n "✓ Verificando Docker... "
if command -v docker &> /dev/null; then
    echo "✅ Docker encontrado"
else
    echo "⚠️  Docker não encontrado (necessário para dev)"
    ((WARNINGS++))
fi

# Check Docker Compose
echo -n "✓ Verificando Docker Compose... "
if command -v docker-compose &> /dev/null; then
    echo "✅ Docker Compose encontrado"
else
    echo "⚠️  Docker Compose não encontrado (necessário para dev)"
    ((WARNINGS++))
fi

echo ""
echo "📂 Verificando estrutura de arquivos..."
echo ""

# Check project structure
FILES=(
    "pom.xml"
    "docker-compose.yml"
    "Dockerfile"
    "README.md"
    "FASE_1_RESUMO.md"
    "INICIANDO.md"
    "test-api.sh"
    "quick-start.sh"
    "src/main/java/com/aelares/AeOrganizaLarApplication.java"
    "src/main/resources/application.yaml"
    "src/main/resources/application-dev.yaml"
    "src/main/resources/db/migration/V1__Initial_Schema.sql"
)

for file in "${FILES[@]}"; do
    echo -n "  ✓ $file... "
    if [ -f "$file" ]; then
        echo "✅"
    else
        echo "❌ FALTANDO"
        ((ERRORS++))
    fi
done

echo ""
echo "📁 Verificando pacotes Java..."
echo ""

PACKAGES=(
    "src/main/java/com/aelares/entity"
    "src/main/java/com/aelares/dto/request"
    "src/main/java/com/aelares/dto/response"
    "src/main/java/com/aelares/service"
    "src/main/java/com/aelares/resource"
    "src/main/java/com/aelares/exception"
)

for package in "${PACKAGES[@]}"; do
    echo -n "  ✓ $package... "
    if [ -d "$package" ] && [ "$(ls -A $package)" ]; then
        COUNT=$(ls -1 "$package"/*.java 2>/dev/null | wc -l)
        echo "✅ ($COUNT files)"
    else
        echo "❌ FALTANDO"
        ((ERRORS++))
    fi
done

echo ""
echo "📝 Verificando classes principais..."
echo ""

CLASSES=(
    "src/main/java/com/aelares/entity/User.java"
    "src/main/java/com/aelares/entity/Home.java"
    "src/main/java/com/aelares/entity/Task.java"
    "src/main/java/com/aelares/entity/TaskSchedule.java"
    "src/main/java/com/aelares/entity/AuthToken.java"
    "src/main/java/com/aelares/service/AuthService.java"
    "src/main/java/com/aelares/service/UserService.java"
    "src/main/java/com/aelares/service/HomeService.java"
    "src/main/java/com/aelares/service/TaskService.java"
    "src/main/java/com/aelares/service/EmailService.java"
    "src/main/java/com/aelares/resource/AuthResource.java"
    "src/main/java/com/aelares/resource/UserResource.java"
    "src/main/java/com/aelares/resource/HomeResource.java"
    "src/main/java/com/aelares/resource/TaskResource.java"
)

for class in "${CLASSES[@]}"; do
    echo -n "  ✓ $(basename $class)... "
    if [ -f "$class" ]; then
        echo "✅"
    else
        echo "❌"
        ((ERRORS++))
    fi
done

echo ""
echo "============================================"
echo ""

# Summary
if [ $ERRORS -eq 0 ]; then
    echo "✅ VALIDAÇÃO COMPLETA - TUDO OK!"
else
    echo "❌ $ERRORS ERRO(S) ENCONTRADO(S)"
fi

if [ $WARNINGS -gt 0 ]; then
    echo "⚠️  $WARNINGS AVISO(S)"
fi

echo ""
echo "📊 Resumo:"
echo "  • Total de arquivos esperados: ${#FILES[@]}"
echo "  • Total de pacotes: ${#PACKAGES[@]}"
echo "  • Total de classes: ${#CLASSES[@]}"
echo ""

if [ $ERRORS -eq 0 ]; then
    echo "🚀 Pronto para começar!"
    echo ""
    echo "Próximos passos:"
    echo "  1. cd backend"
    echo "  2. docker-compose up -d"
    echo "  3. ./mvnw quarkus:dev"
    echo "  4. bash test-api.sh"
    exit 0
else
    echo "❌ Corrigir erros acima antes de continuar"
    exit 1
fi

