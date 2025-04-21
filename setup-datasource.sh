#!/bin/bash

# Esperar a que la base de datos esté disponible
function waitForDB() {
  echo "Esperando a que la base de datos esté disponible..."
  until nc -z $DB_SERVER $DB_PORT; do
    echo "PostgreSQL no disponible aún, esperando..."
    sleep 2
  done
  echo "Base de datos disponible!"
}

# Configurar el datasource
function setupDataSource() {
  echo "Configurando DataSource para PostgreSQL..."
  
  # Iniciar el dominio en modo de administración
  $PAYARA_DIR/bin/asadmin start-domain --verbose domain1
  
  # Crear el pool de conexiones
  $PAYARA_DIR/bin/asadmin create-jdbc-connection-pool \
    --datasourceclassname org.postgresql.ds.PGSimpleDataSource \
    --restype javax.sql.DataSource \
    --property user=${DB_USER}:password=${DB_PASSWORD}:serverName=${DB_SERVER}:portNumber=${DB_PORT}:databaseName=${DB_NAME} \
    TipicosDBPool
  
  # Crear el recurso JDBC/DataSource
  $PAYARA_DIR/bin/asadmin create-jdbc-resource \
    --connectionpoolid TipicosDBPool \
    jdbc/TipicosDS
  
  # Detener el dominio
  $PAYARA_DIR/bin/asadmin stop-domain domain1
  
  echo "DataSource configurado correctamente."
}

# Solo configurar si no existe ya
if [ ! -f $PAYARA_DIR/glassfish/domains/domain1/config/datasource_configured ]; then
  waitForDB
  setupDataSource
  touch $PAYARA_DIR/glassfish/domains/domain1/config/datasource_configured
fi

# Continuar con el entrypoint normal
exec $PAYARA_DIR/bin/startInForeground.sh
