FastFood Service - Spring Boot 🍔🍟
Sistema backend de pedidos de comida rápida desarrollado con Spring Boot. Este proyecto incluye modelos de Cliente, Producto y Pedido; controladores REST para crear y consultar datos; y servicios con la lógica del negocio. Los datos se almacenan temporalmente en memoria con estructuras thread-safe (ConcurrentHashMap).
📋 Características
- **Gestión de Clientes**: Crear, consultar, actualizar y eliminar clientes
- **Gestión de Productos**: CRUD completo de productos con filtrado por categoría
- **Gestión de Pedidos**: Crear pedidos, consultar por cliente o estado, actualizar estado
- **Almacenamiento en Memoria**: Uso de ConcurrentHashMap para almacenamiento thread-safe
- **API REST**: Endpoints RESTful completos para todas las operaciones
- **Validaciones**: Validación de datos en la capa de servicios
Tecnologías
- Java 17
- Spring Boot 3.1.5
- Maven
- Spring Web
Estructura del Proyecto

```
src/main/java/com/fastfood/
├── FastFoodApplication.java      # Clase principal
├── model/                         # Modelos de dominio
│   ├── Cliente.java
│   ├── Producto.java
│   └── Pedido.java
├── repository/                    # Capa de persistencia (en memoria)
│   ├── ClienteRepository.java
│   ├── ProductoRepository.java
│   └── PedidoRepository.java
├── service/                       # Lógica de negocio
│   ├── ClienteService.java
│   ├── ProductoService.java
│   └── PedidoService.java
└── controller/                    # Controladores REST
    ├── ClienteController.java
    ├── ProductoController.java
    └── PedidoController.java
```
- Java 17 o superior
- Maven 3.6 o superior
1. **Clonar el repositorio:**
```bash
git clone https://github.com/AvilaCamacho/FastFood-Service-Spring-Boot-.git
cd FastFood-Service-Spring-Boot-
```
2. **Compilar el proyecto:**
```bash
mvn clean install
```
3. **Ejecutar la aplicación:**
```bash
mvn spring-boot:run
```
O alternativamente:
```bash
java -jar target/fastfood-service-1.0.0.jar
```
La aplicación estará disponible en: `http://localhost:8080`
## 📡 API Endpoints FLOW PARA LA PRUEBA DE RESPONSE 
## Clientes
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/clientes` | Crear un nuevo cliente |
| GET | `/api/clientes` | Obtener todos los clientes |
| GET | `/api/clientes/{id}` | Obtener un cliente por ID |
| PUT | `/api/clientes/{id}` | Actualizar un cliente |
| DELETE | `/api/clientes/{id}` | Eliminar un cliente |

### Productos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/productos` | Crear un nuevo producto |
| GET | `/api/productos` | Obtener todos los productos |
| GET | `/api/productos?categoria={categoria}` | Filtrar productos por categoría |
| GET | `/api/productos/{id}` | Obtener un producto por ID |
| PUT | `/api/productos/{id}` | Actualizar un producto |
| DELETE | `/api/productos/{id}` | Eliminar un producto |

### Pedidos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/pedidos` | Crear un nuevo pedido |
| GET | `/api/pedidos` | Obtener todos los pedidos |
| GET | `/api/pedidos?clienteId={id}` | Filtrar pedidos por cliente |
| GET | `/api/pedidos?estado={estado}` | Filtrar pedidos por estado |
| GET | `/api/pedidos/{id}` | Obtener un pedido por ID |
| PATCH | `/api/pedidos/{id}/estado` | Actualizar estado de un pedido |
| DELETE | `/api/pedidos/{id}` | Eliminar un pedido |

## 🧪 Ejemplos de Uso con Postman

### 1. Crear un Cliente

**POST** `http://localhost:8080/api/clientes`

```json
{
  "nombre": "Juan Pérez",
  "email": "juan.perez@example.com",
  "telefono": "555-1234"
}
```

**Respuesta:**
```json
{
  "id": 1,
  "nombre": "Juan Pérez",
  "email": "juan.perez@example.com",
  "telefono": "555-1234"
}
```

### 2. Crear Productos

**POST** `http://localhost:8080/api/productos`

```json
{
  "nombre": "Hamburguesa Clásica",
  "descripcion": "Hamburguesa con carne, lechuga, tomate y queso",
  "precio": 8.99,
  "categoria": "HAMBURGUESAS"
}
```

```json
{
  "nombre": "Papas Fritas",
  "descripcion": "Papas fritas crujientes",
  "precio": 3.50,
  "categoria": "ACOMPAÑAMIENTOS"
}
```

```json
{
  "nombre": "Coca Cola",
  "descripcion": "Bebida gaseosa 500ml",
  "precio": 2.50,
  "categoria": "BEBIDAS"
}
```

### 3. Crear un Pedido

**POST** `http://localhost:8080/api/pedidos`

```json
{
  "clienteId": 1,
  "productosIds": [1, 2, 3]
}
```

**Respuesta:**
```json
{
  "id": 1,
  "clienteId": 1,
  "productosIds": [1, 2, 3],
  "total": 14.99,
  "estado": "PENDIENTE",
  "fecha": "2024-01-15T10:30:00"
}
```

### 4. Consultar Todos los Clientes

**GET** `http://localhost:8080/api/clientes`

**Respuesta:**
```json
[
  {
    "id": 1,
    "nombre": "Juan Pérez",
    "email": "juan.perez@example.com",
    "telefono": "555-1234"
  }
]
```

### 5. Consultar Productos por Categoría

**GET** `http://localhost:8080/api/productos?categoria=HAMBURGUESAS`

### 6. Actualizar Estado de un Pedido

**PATCH** `http://localhost:8080/api/pedidos/1/estado`

```json
{
  "estado": "EN_PREPARACION"
}
```

Estados permitidos:
- `PENDIENTE`
- `EN_PREPARACION`
- `LISTO`
- `ENTREGADO`
- `CANCELADO`

### 7. Consultar Pedidos por Cliente

**GET** `http://localhost:8080/api/pedidos?clienteId=1`

### 8. Consultar Pedidos por Estado

**GET** `http://localhost:8080/api/pedidos?estado=PENDIENTE`

## 📚 Modelos de Datos

### Cliente
```java
{
  "id": Long,
  "nombre": String,
  "email": String,
  "telefono": String
}
```

### Producto
```java
{
  "id": Long,
  "nombre": String,
  "descripcion": String,
  "precio": Double,
  "categoria": String
}
```

### Pedido
```java
{
  "id": Long,
  "clienteId": Long,
  "productosIds": List<Long>,
  "total": Double,
  "estado": String,
  "fecha": LocalDateTime
}
```
- Uso de `ConcurrentHashMap` para almacenamiento de datos
- `AtomicLong` para generación de IDs únicos
- Operaciones atómicas garantizadas en entornos multi-hilo
