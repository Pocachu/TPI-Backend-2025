#!/bin/bash
set -e # Termina inmediatamente si un comando sale con un estado distinto de cero.

echo "Esperando a que la base de datos esté lista..."
# Puedes añadir aquí una espera activa a la DB si depends_on no es suficiente o quieres más seguridad
# Ejemplo simple (requiere wait-for-it.sh o similar, o usar un bucle con pg_isready):
# /opt/payara/scripts/wait-for-it.sh postgres-test:5432 --timeout=30

echo "Configurando el datasource..."
# Ejecuta tu script de configuración con la ruta correcta
/opt/payara/scripts/setup-test-datasource.sh

echo "Iniciando el servidor Payara en primer plano..."
# Ejecuta el comando principal del servidor en primer plano.
# Usa 'exec' para reemplazar el proceso actual del script por el del servidor.
# Esto asegura que las señales (como SIGTERM para detener el contenedor) se envíen al servidor Payara directamente.
exec /opt/payara/bin/startInForeground.sh "$@" # Pasa cualquier argumento extra al script de inicio de Payara
