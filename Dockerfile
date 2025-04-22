FROM payara/server-full:5.2022.5-jdk11

# Configurar variables de entorno con valores por defecto
ENV DB_SERVER=postgres \
    DB_PORT=5432 \
    DB_NAME=tipicos_tpi135 \
    DB_USER=postgres \
    DB_PASSWORD=postgres \
    SCRIPT_DIR=/opt/payara/scripts \
    DEPLOY_DIR=/opt/payara/deployments

# Copiar el driver de PostgreSQL
COPY postgresql-42.5.1.jar ${PAYARA_DIR}/glassfish/domains/domain1/lib/
# Crear directorio para scripts
RUN mkdir -p /opt/scripts

# Copiar script de configuración
COPY scripts/setup-datasource.sh /opt/scripts/
RUN chmod +x /opt/scripts/setup-datasource.sh

# Asegurarnos que estamos usando commands correctos
RUN which asadmin && ls -la ${PAYARA_HOME}/bin/asadmin

# Crear un script de entrada personalizado
RUN echo '#!/bin/bash' > /opt/docker-entrypoint.sh && \
    echo 'set -e' >> /opt/docker-entrypoint.sh && \
    echo 'echo "Iniciando configuración..."' >> /opt/docker-entrypoint.sh && \
    echo '/opt/scripts/setup-datasource.sh' >> /opt/docker-entrypoint.sh && \
    echo 'echo "Iniciando Payara..."' >> /opt/docker-entrypoint.sh && \
    echo 'exec ${PAYARA_HOME}/bin/startInForeground.sh "$@"' >> /opt/docker-entrypoint.sh && \
    chmod +x /opt/docker-entrypoint.sh

# Asegurarnos que el script de entrada existe
RUN ls -la /opt/docker-entrypoint.sh

# Definir el punto de entrada
ENTRYPOINT ["/opt/docker-entrypoint.sh"]
# Copiar archivos de configuración
COPY src/main/resources/META-INF/persistence.xml ${PAYARA_DIR}/glassfish/domains/domain1/config/
COPY src/main/webapp/WEB-INF/glassfish-resources.xml ${PAYARA_DIR}/glassfish/domains/domain1/config/

# Crear el directorio de despliegue
RUN mkdir -p ${DEPLOY_DIR}

# Copiar script configuración datasource
COPY scripts/setup-datasource.sh ${SCRIPT_DIR}/
RUN chmod +x ${SCRIPT_DIR}/setup-datasource.sh

# Instalar herramientas adicionales para diagnóstico
USER root
RUN apt-get update && apt-get install -y netcat-openbsd curl
USER payara

# Ajustar el comando de inicio
ENTRYPOINT ["sh", "-c", "${SCRIPT_DIR}/setup-datasource.sh"]
