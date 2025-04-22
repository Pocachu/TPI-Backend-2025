#!/bin/bash
set -e

echo "Iniciando script de configuración del datasource..."

# Verificar la variable PAYARA_HOME
if [ -z "$PAYARA_HOME" ]; then
  echo "PAYARA_HOME no está definido. Intentando detectarlo..."
  # Tratar de encontrar la ruta de instalación de Payara
  if [ -d "/opt/payara" ]; then
    export PAYARA_HOME="/opt/payara"
  elif [ -d "/opt/payara/appserver" ]; then
    export PAYARA_HOME="/opt/payara/appserver"
  else
    echo "No se pudo determinar PAYARA_HOME. Abortando."
    exit 1
  fi
fi

echo "Usando PAYARA_HOME: $PAYARA_HOME"

# Verificar que asadmin existe
if [ ! -f "$PAYARA_HOME/bin/asadmin" ]; then
  echo "ERROR: asadmin no encontrado en $PAYARA_HOME/bin/asadmin"
  echo "Contenido de $PAYARA_HOME/bin:"
  ls -la $PAYARA_HOME/bin
  exit 1
fi

# Esperar a que la base de datos esté disponible
echo "Esperando a que la base de datos esté disponible..."
# Si nc no está disponible, podríamos esperar un tiempo fijo
sleep 10
echo "Continuando con la configuración..."

# Configurar el datasource
echo "Configurando DataSource para PostgreSQL..."

# Ver si el dominio ya está en ejecución
DOMAIN_RUNNING=$($PAYARA_HOME/bin/asadmin list-domains | grep "domain1 running" || echo "no")

if [ "$DOMAIN_RUNNING" = "no" ]; then
  # Iniciar el dominio en modo de administración
  echo "Iniciando dominio..."
  $PAYARA_HOME/bin/asadmin start-domain --verbose domain1
else
  echo "El dominio ya está en ejecución."
fi

# Eliminar recursos existentes si los hay
echo "Eliminando recursos existentes si existen..."
$PAYARA_HOME/bin/asadmin delete-jdbc-resource jdbc/TipicosDS || true
$PAYARA_HOME/bin/asadmin delete-jdbc-connection-pool TipicosDBPool || true

# Crear el pool de conexiones
echo "Creando pool de conexiones..."
$PAYARA_HOME/bin/asadmin create-jdbc-connection-pool \
  --datasourceclassname org.postgresql.ds.PGSimpleDataSource \
  --restype javax.sql.DataSource \
  --property "user=${DB_USER}:password=${DB_PASSWORD}:serverName=${DB_SERVER}:portNumber=${DB_PORT}:databaseName=${DB_NAME}" \
  TipicosDBPool

# Crear el recurso JDBC/DataSource
echo "Creando recurso JDBC..."
$PAYARA_HOME/bin/asadmin create-jdbc-resource \
  --connectionpoolid TipicosDBPool \
  jdbc/TipicosDS

echo "DataSource configurado correctamente."

# No detenemos el dominio aquí, ya que el script principal se encargará de esto
echo "Script de configuración completado."
