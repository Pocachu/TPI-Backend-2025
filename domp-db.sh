#!/bin/bash
# diagnose-connection.sh

echo "=== Diagnóstico de Conexión a la Base de Datos ==="

# Verificar si el contenedor de la aplicación está en ejecución
echo "Verificando contenedor de la aplicación..."
APP_CONTAINER=$(docker ps -q -f name=tipicos-app)
if [ -z "$APP_CONTAINER" ]; then
  echo "ERROR: El contenedor de la aplicación no está en ejecución."
  exit 1
fi
echo "✓ Contenedor de la aplicación en ejecución."

# Verificar si el contenedor de la base de datos está en ejecución
echo "Verificando contenedor de la base de datos..."
DB_CONTAINER=$(docker ps -q -f name=tipicos-db)
if [ -z "$DB_CONTAINER" ]; then
  echo "ERROR: El contenedor de la base de datos no está en ejecución."
  exit 1
fi
echo "✓ Contenedor de la base de datos en ejecución."

# Verificar la conectividad entre contenedores
echo "Verificando conectividad de red entre contenedores..."
docker exec $APP_CONTAINER ping -c 2 postgres
if [ $? -ne 0 ]; then
  echo "ERROR: No hay conectividad de red entre la aplicación y la base de datos."
  exit 1
fi
echo "✓ Conectividad de red OK."

# Verificar que el puerto de PostgreSQL está abierto
echo "Verificando que el puerto de PostgreSQL está abierto..."
docker exec $APP_CONTAINER nc -zv postgres 5432
if [ $? -ne 0 ]; then
  echo "ERROR: El puerto de PostgreSQL no está accesible."
  exit 1
fi
echo "✓ Puerto de PostgreSQL accesible."

# Verificar la conexión JDBC con el pool de conexiones
echo "Verificando conexión JDBC..."
docker exec $APP_CONTAINER $PAYARA_DIR/bin/asadmin ping-connection-pool TipicosDBPool
if [ $? -ne 0 ]; then
  echo "ERROR: No se pudo establecer la conexión JDBC con la base de datos."
  echo "Mostrando logs del servidor..."
  docker logs --tail 50 $APP_CONTAINER
  exit 1
fi
echo "✓ Conexión JDBC establecida correctamente."

# Mostrar las primeras filas de las tablas principales
echo "Verificando acceso a datos..."
docker exec $DB_CONTAINER psql -U postgres -d tipicos_tpi135 -c "SELECT count(*) FROM tipo_producto;"
docker exec $DB_CONTAINER psql -U postgres -d tipicos_tpi135 -c "SELECT count(*) FROM producto;"

echo "=== Diagnóstico completado exitosamente ==="
