#!/bin/bash
set -e

echo "==== Iniciando servidor de pruebas ===="

# Esperar a que la base de datos esté disponible
echo "Esperando a la base de datos en ${DB_SERVER}:${DB_PORT}..."
until nc -z ${DB_SERVER} ${DB_PORT}; do
  echo "PostgreSQL no disponible aún, esperando..."
  sleep 2
done
echo "¡Base de datos disponible!"

# Configurar el DataSource
echo "Configurando DataSource..."
${PAYARA_DIR}/bin/asadmin start-domain --verbose domain1
${PAYARA_DIR}/bin/asadmin create-jdbc-connection-pool \
  --datasourceclassname org.postgresql.ds.PGSimpleDataSource \
  --restype javax.sql.DataSource \
  --property "user=${DB_USER}:password=${DB_PASSWORD}:serverName=${DB_SERVER}:portNumber=${DB_PORT}:databaseName=${DB_NAME}" \
  TipicosTestPool

${PAYARA_DIR}/bin/asadmin create-jdbc-resource \
  --connectionpoolid TipicosTestPool \
  jdbc/TipicosDS

# Desplegar aplicación
echo "Desplegando aplicación..."
${PAYARA_DIR}/bin/asadmin deploy --force=true --contextroot tipicos-api --name tipicos-api ${DEPLOY_DIR}/tipicos-api.war

# Verificar despliegue
echo "Verificando despliegue..."
${PAYARA_DIR}/bin/asadmin list-applications

# Reiniciar dominio para asegurar configuración correcta
${PAYARA_DIR}/bin/asadmin stop-domain domain1
echo "==== Inicializando servidor en modo definitivo ===="

# Iniciar en modo de primer plano para mantener el contenedor corriendo
exec ${PAYARA_DIR}/bin/startInForeground.sh
