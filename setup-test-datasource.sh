#!/bin/bash

# Esperar a que la base de datos de prueba esté disponible
function waitForTestDB() {
  echo "Esperando a que la base de datos de prueba esté disponible..."
  until nc -z $DB_SERVER $DB_PORT; do
    echo "PostgreSQL no disponible aún, esperando..."
    sleep 2
  done
  echo "Base de datos de prueba disponible!"
}

# Configurar el datasource para pruebas
function setupTestDataSource() {
  echo "Configurando DataSource para entorno de pruebas..."
  
  # Iniciar el dominio en modo de administración
  $PAYARA_DIR/bin/asadmin start-domain --verbose domain1
  
  # Crear el pool de conexiones para pruebas
  $PAYARA_DIR/bin/asadmin create-jdbc-connection-pool \
    --datasourceclassname org.postgresql.ds.PGSimpleDataSource \
    --restype javax.sql.DataSource \
    --property user=${DB_USER}:password=${DB_PASSWORD}:serverName=${DB_SERVER}:portNumber=${DB_PORT}:databaseName=${DB_NAME} \
    TipicosTestDBPool
  
  # Crear el recurso JDBC/DataSource para pruebas
  $PAYARA_DIR/bin/asadmin create-jdbc-resource \
    --connectionpoolid TipicosTestDBPool \
    jdbc/TipicosTestDS
  
  # Configurar algunas propiedades adicionales para pruebas
  $PAYARA_DIR/bin/asadmin set server.monitoring-service.module-monitoring-levels.jdbc=HIGH
  
  # Detener el dominio
  $PAYARA_DIR/bin/asadmin stop-domain domain1
  
  echo "DataSource para pruebas configurado correctamente."
}

# Solo configurar si no existe ya
if [ ! -f $PAYARA_DIR/glassfish/domains/domain1/config/test_datasource_configured ]; then
  waitForTestDB
  setupTestDataSource
  
  # Marcar como configurado
  touch $PAYARA_DIR/glassfish/domains/domain1/config/test_datasource_configured
fi

# Continuar con el entrypoint normal
exec $PAYARA_DIR/bin/startInForeground.sh
