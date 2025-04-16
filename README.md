# TPI Backend 2025
Sistema de gestión para restaurante de comida típica, como proyecto para la materia tecnicas de programacion en internet 2025, creado por Gerardo Alfonso Avila Marroquin

# Tipicos TPI135 - API REST

Este proyecto implementa una API REST para la gestión de una tienda de productos típicos salvadoreños, desarrollado como parte del curso TPI135 2025.

## Tecnologías utilizadas

- Java 11
- Maven
- Jakarta EE 8
- MicroProfile 3.3
- OpenLiberty
- PostgreSQL
- Docker
- JUnit 5
- Mockito
- JaCoCo

## Estructura del proyecto

El proyecto sigue una arquitectura de N-Capas:

- **Controller**: Expone los endpoints REST
- **Service**: Implementa la lógica de negocio
- **Repository**: Maneja la persistencia de datos
- **Entity**: Define las entidades JPA
- **DTO**: Objetos de transferencia de datos

## Requisitos previos

- JDK 11 o superior
- Maven 3.6 o superior
- Docker y Docker Compose
- PostgreSQL (opcional para desarrollo local)

## Configuración del entorno de desarrollo

1. Clone el repositorio:
   ```bash
   git clone https://github.com/tu-usuario/tipicos-tpi135.git
   cd tipicos-tpi135
   ```

2. Compile el proyecto:
   ```bash
   mvn clean package
   ```

3. Inicie el entorno con Docker Compose:
   ```bash
   docker-compose up -d
   ```

4. Acceda a la aplicación:
   http://localhost:9080/tipicos-tpi135/api/productos

## Endpoints disponibles

### Productos

- `GET /api/productos` - Lista todos los productos
- `GET /api/productos?nombre={nombre}` - Filtra productos por nombre
- `GET /api/productos?activo={true|false}` - Filtra productos por estado
- `GET /api/productos/{id}` - Obtiene un producto por su ID
- `POST /api/productos` - Crea un nuevo producto
- `PUT /api/productos/{id}` - Actualiza un producto existente
- `DELETE /api/productos/{id}` - Elimina un producto

## Ejemplos de uso

### Crear un producto

```bash
curl -X POST http://localhost:9080/tipicos-tpi135/api/productos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Yuca Frita",
    "activo": true,
    "observaciones": "Yuca frita con curtido y salsa de tomate"
  }'
```

### Obtener un producto

```bash
curl http://localhost:9080/tipicos-tpi135/api/productos/1
```

### Actualizar un producto

```bash
curl -X PUT http://localhost:9080/tipicos-tpi135/api/productos/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Yuca Frita con Chicharrón",
    "activo": true,
    "observaciones": "Yuca frita con curtido, salsa de tomate y chicharrón"
  }'
```

### Eliminar un producto

```bash
curl -X DELETE http://localhost:9080/tipicos-tpi135/api/productos/1
```

## Ejecución de pruebas

### Pruebas unitarias

```bash
mvn test
```

### Pruebas de cobertura

```bash
mvn jacoco:report
```

El informe de cobertura estará disponible en `target/site/jacoco/index.html`.

### Pruebas de integración

```bash
mvn failsafe:integration-test
```

### Pruebas de sistema

```bash
docker-compose -f docker-compose.test.yml up -d
mvn failsafe:verify -Dapi.url=http://localhost:9080/tipicos-tpi135/api
docker-compose -f docker-compose.test.yml down
```

## Pipeline de CI/CD

El proyecto incluye un archivo de configuración para GitHub Actions que ejecuta:

1. Compilación del proyecto
2. Pruebas unitarias
3. Análisis de cobertura
4. Pruebas de integración
5. Pruebas de sistema
6. Generación de artefactos Docker

La configuración se encuentra en `.github/workflows/ci-cd.yml`.

## Diagramas

### Diagrama de arquitectura

```
┌───────────────┐    ┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│   Controller  │    │    Service    │    │   Repository  │    │   Database    │
│   (REST API)  │───►│  (Business)   │───►│   (Data)      │───►│  (PostgreSQL) │
└───────────────┘    └───────────────┘    └───────────────┘    └───────────────┘
```

### Diagrama de entidades

```
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│ TipoProducto  │◄────┤ProductoDetalle│────►│   Producto    │
└───────────────┘     └───────────────┘     └───────────────┘
                                                   ▲
                                                   │
                                            ┌──────┴────────┐
                                            │ProductoPrecio │
                                            └───────────────┘
```

## Contribución

1. Fork el repositorio
2. Cree una rama para su característica (`git checkout -b feature/nueva-caracteristica`)
3. Haga commit de sus cambios (`git commit -am 'Agrega nueva característica'`)
4. Haga push a la rama (`git push origin feature/nueva-caracteristica`)
5. Cree un nuevo Pull Request

## Licencia

Este proyecto está licenciado bajo la Licencia MIT - vea el archivo [LICENSE](LICENSE) para más detalles.

## Equipo de desarrollo

- Nombre del Estudiante 1 (Carnet)
- Nombre del Estudiante 2 (Carnet)
- Nombre del Estudiante 3 (Carnet)
