#!/bin/bash
set -e

# Colores para la salida
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}=== Iniciando pruebas de sistema para Tipicos API ===${NC}"

# Asegurarse de que tenemos el jar del driver de PostgreSQL
if [ ! -f "postgresql-42.5.1.jar" ]; then
  echo -e "${YELLOW}Descargando driver de PostgreSQL...${NC}"
  curl -s -o postgresql-42.5.1.jar https://jdbc.postgresql.org/download/postgresql-42.5.1.jar
fi

# Compilar el proyecto
echo -e "${YELLOW}Compilando el proyecto...${NC}"
mvn clean package -DskipTests

# Verificar si el empaquetado fue exitoso
if [ ! -f "target/tipicos-api.war" ]; then
  echo -e "${RED}Error: No se pudo generar el archivo WAR.${NC}"
  exit 1
fi

# Detener y eliminar contenedores existentes si los hay
echo -e "${YELLOW}Limpiando entorno de pruebas anterior...${NC}"
docker compose -f docker-compose.test.yml down -v 2>/dev/null || true

# Iniciar los contenedores de prueba
echo -e "${YELLOW}Iniciando entorno de pruebas...${NC}"
docker compose -f docker-compose.test.yml up -d

# Esperar a que la aplicación esté lista
echo -e "${YELLOW}Esperando a que la aplicación esté disponible...${NC}"
MAX_RETRIES=30
count=0
while [ $count -lt $MAX_RETRIES ]; do
  if curl -s http://localhost:9080/tipicos-api/api/productos > /dev/null; then
    echo -e "${GREEN}¡Aplicación disponible!${NC}"
    break
  fi
  echo "Esperando... ($count/$MAX_RETRIES)"
  sleep 5
  count=$((count + 1))
done

if [ $count -eq $MAX_RETRIES ]; then
  echo -e "${RED}Error: La aplicación no está disponible después de $MAX_RETRIES intentos.${NC}"
  docker compose -f docker-compose.test.yml logs app-test
  docker compose -f docker-compose.test.yml down -v
  exit 1
fi

# Ejecutar pruebas de sistema
echo -e "${YELLOW}Ejecutando pruebas de sistema...${NC}"
mvn -e verify -P system-tests -Dapi.url=http://localhost:9080/tipicos-api/api

# Capturar el resultado
test_result=$?

# Mostrar logs si hay fallos
if [ $test_result -ne 0 ]; then
  echo -e "${RED}Las pruebas de sistema han fallado. Mostrando logs:${NC}"
  docker compose -f docker-compose.test.yml logs
fi

# Detener los contenedores
echo -e "${YELLOW}Limpiando entorno de pruebas...${NC}"
docker compose -f docker-compose.test.yml down -v

# Mostrar resultado final
if [ $test_result -eq 0 ]; then
  echo -e "${GREEN}¡Las pruebas de sistema se completaron con éxito!${NC}"
  exit 0
else
  echo -e "${RED}Las pruebas de sistema fallaron.${NC}"
  exit 1
fi
