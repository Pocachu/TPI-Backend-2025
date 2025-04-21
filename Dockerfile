FROM payara/server-full:5.2022.5-jdk11

# Configurar variables de entorno
ENV DB_SERVER=postgres \
    DB_PORT=5432 \
    DB_NAME=tipicos_tpi135 \
    DB_USER=postgres \
    DB_PASSWORD=postgres \
    SCRIPT_DIR=/opt/payara/scripts

# Copiar el driver de PostgreSQL al directorio de librerías
COPY postgresql-42.5.1.jar ${PAYARA_DIR}/glassfish/domains/domain1/lib/

# Copiar configuraciones personalizadas si es necesario
COPY src/main/resources/META-INF/persistence.xml ${PAYARA_DIR}/glassfish/domains/domain1/config/

# Crear el directorio de despliegue si no existe
RUN mkdir -p ${DEPLOY_DIR}

# Copiar la aplicación WAR al directorio de despliegue automático
# (Este archivo será montado como volumen desde docker-compose)
# COPY target/tipicos-api.war ${DEPLOY_DIR}/

# Configurar datasource por si es necesario
COPY setup-datasource.sh ${SCRIPT_DIR}/
RUN chmod +x ${SCRIPT_DIR}/setup-datasource.sh

# Ajustar el comando de inicio para configurar DataSource antes de iniciar el servidor
CMD ["bin/entrypoint.sh"]
