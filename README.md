# TPI Backend 2025
Sistema de gestión para restaurante de comida típica, como proyecto para la materia tecnicas de programacion en internet 2025, creado por Gerardo Alfonso Avila Marroquin

# Tipicos TPI135 - API REST Backend

Este proyecto implementa una API REST para la gestión de un restaurante de comida típica salvadoreña, desarrollado como parte del curso TPI135 2025. El sistema permite la gestión de productos, tipos de productos, combos y sus detalles, órdenes y pagos.

## Autor
Gerardo Alfonso Avila Marroquin

## Tecnologías utilizadas

- Java 11
- Jakarta EE 8
- MicroProfile 3.3
- OpenLiberty/GlassFish
- PostgreSQL 14
- Docker y Docker Compose
- Maven
- JUnit 5 + Mockito
- JaCoCo (para cobertura de código)
- Jenkins (para pipeline CI/CD)

## Estructura del proyecto

El proyecto sigue una arquitectura de N-Capas:

- **Controller**: Expone los endpoints REST
- **Service**: Implementa la lógica de negocio
- **Repository**: Maneja la persistencia de datos
- **Entity**: Define las entidades JPA
- **DTO**: Objetos de transferencia de datos

## Características implementadas

- CRUD completo para Productos
- CRUD completo para Tipos de Productos
- CRUD completo para Combos
- Búsqueda por diferentes criterios (nombre, estado)
- Validaciones de datos
- Pipeline CI/CD con pruebas automatizadas

## Requisitos previos

- JDK 11 o superior
- Maven 3.6 o superior
- Docker y Docker Compose
- PostgreSQL (opcional para desarrollo local)

## Configuración del entorno de desarrollo

1. Clone el repositorio:
   ```bash
   git clone https://github.com/pocachu/tipicos-tpi135.git
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
   ```
   http://localhost:8080/tipicos-api/api/productos
   ```

## Endpoints disponibles

### Productos

- `GET /api/productos` - Lista todos los productos
- `GET /api/productos?nombre={nombre}` - Filtra productos por nombre
- `GET /api/productos?activo={true|false}` - Filtra productos por estado
- `GET /api/productos/{id}` - Obtiene un producto por su ID
- `POST /api/productos` - Crea un nuevo producto
- `PUT /api/productos/{id}` - Actualiza un producto existente
- `DELETE /api/productos/{id}` - Elimina un producto

### Tipos de Productos

- `GET /api/tipos-productos` - Lista todos los tipos de productos
- `GET /api/tipos-productos?nombre={nombre}` - Filtra tipos de productos por nombre
- `GET /api/tipos-productos?activo={true|false}` - Filtra tipos de productos por estado
- `GET /api/tipos-productos/{id}` - Obtiene un tipo de producto por su ID
- `POST /api/tipos-productos` - Crea un nuevo tipo de producto
- `PUT /api/tipos-productos/{id}` - Actualiza un tipo de producto existente
- `DELETE /api/tipos-productos/{id}` - Elimina un tipo de producto

### Combos

- `GET /api/combos` - Lista todos los combos
- `GET /api/combos?nombre={nombre}` - Filtra combos por nombre
- `GET /api/combos?activo={true|false}` - Filtra combos por estado
- `GET /api/combos/{id}` - Obtiene un combo por su ID
- `POST /api/combos` - Crea un nuevo combo
- `PUT /api/combos/{id}` - Actualiza un combo existente
- `DELETE /api/combos/{id}` - Elimina un combo

## Ejemplos de uso

### Crear un producto

```bash
curl -X POST http://localhost:8080/tipicos-api/api/productos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Yuca Frita",
    "activo": true,
    "observaciones": "Yuca frita con curtido y salsa de tomate"
  }'
```

### Obtener un producto

```bash
curl http://localhost:8080/tipicos-api/api/productos/1
```

### Actualizar un producto

```bash
curl -X PUT http://localhost:8080/tipicos-api/api/productos/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Yuca Frita con Chicharrón",
    "activo": true,
    "observaciones": "Yuca frita con curtido, salsa de tomate y chicharrón"
  }'
```

### Eliminar un producto

```bash
curl -X DELETE http://localhost:8080/tipicos-api/api/productos/1
```

## Pruebas

El proyecto incluye cuatro tipos de pruebas:

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

## Pipeline CI/CD

El proyecto incluye configuración para CI/CD tanto en GitHub Actions como en Jenkins:

### Jenkins

El archivo `Jenkinsfile` en la raíz del proyecto configura un pipeline que ejecuta:
1. Compilación del proyecto
2. Pruebas unitarias
3. Análisis de cobertura
4. Pruebas de integración
5. Pruebas de sistema
6. Generación del artefacto final

### GitHub Actions

El archivo `.github/workflows/ci-cd.yml` configura un workflow similar al de Jenkins.

## Base de datos

La estructura de la base de datos sigue el siguiente esquema:

```
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│ TipoProducto  │◄────┤ProductoDetalle│────►│   Producto    │
└───────────────┘     └───────────────┘     └───────────────┘
                                                   ▲
                                                   │
                                            ┌──────┴────────┐
                                            │ProductoPrecio │
                                            └───────────────┘
                                                   ▲
                                                   │
                      ┌────────┐           ┌──────┴────────┐
                      │  Pago  │◄─────────►│OrdenDetalle   │
                      └────────┘           └───────────────┘
                          ▲                       ▲
                          │                       │
                      ┌───┴────┐           ┌─────┴───────┐
                      │PagoDetail│         │   Orden     │
                      └────────┘           └─────────────┘

┌─────────┐     ┌──────────────┐
│  Combo  │◄────┤ComboDetalle  │
└─────────┘     └──────────────┘
```

## Dockerización

El proyecto incluye archivos Docker y Docker Compose para facilitar el despliegue:

- `Dockerfile`: Para construir la imagen de la aplicación
- `docker-compose.yml`: Para ejecutar la aplicación junto con PostgreSQL
- `docker-compose.test.yml`: Configuración específica para pruebas


## Licencia

Este proyecto está licenciado bajo la Licencia MIT - vea el archivo [LICENSE](LICENSE) para más detalles.
