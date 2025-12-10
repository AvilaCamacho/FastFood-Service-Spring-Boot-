# Mecanismo de Rollback - Documentación

## Descripción General

Este sistema implementa un mecanismo de rollback (pila de historial) para operaciones de pedidos en FastFood Service. Permite revertir las últimas operaciones realizadas sobre pedidos, incluyendo creación, actualización de estado y eliminación.

## Arquitectura

### Componentes Principales

1. **OperationRecord** (`com.fastfood.model.OperationRecord`)
   - Modelo que representa un registro de operación en el historial
   - Contiene: tipo de operación, estado antes, estado después, timestamp
   - Tipos de operación: CREAR, CANCELAR, DESPACHAR, ELIMINAR, ACTUALIZAR_ESTADO

2. **HistoryStackService** (`com.fastfood.service.HistoryStackService`)
   - Servicio singleton que mantiene la pila de historial en memoria
   - Thread-safe mediante sincronización
   - Operaciones: push, pop, peek, isEmpty, clear, size

3. **PedidoService** (modificado)
   - Registra automáticamente todas las operaciones en la pila
   - Implementa método `revertOperation()` para aplicar rollbacks
   - Integrado con operaciones: crear, actualizar estado, eliminar

4. **PedidoController** (modificado)
   - Nuevo endpoint POST /api/pedidos/rollback
   - Manejo de errores y respuestas en formato JSON

## API Endpoints

### POST /api/pedidos/rollback

Revierte la última operación realizada sobre pedidos.

**Request:**
```http
POST /api/pedidos/rollback
```

**Respuestas:**

**Éxito (200 OK):**
```json
{
  "mensaje": "Rollback realizado correctamente",
  "operacionRevertida": "CANCELAR",
  "pedido": {
    "id": 1,
    "clienteId": 1,
    "productosIds": [1],
    "total": 15.5,
    "estado": "PENDIENTE",
    "fecha": "2025-12-10T13:00:00"
  }
}
```

**Pila vacía (409 Conflict):**
```json
{
  "mensaje": "No hay operaciones para revertir"
}
```

**Error (500 Internal Server Error):**
```json
{
  "mensaje": "Error al realizar rollback: [detalle del error]"
}
```

### GET /scalar

Endpoint de verificación rápida para probar conectividad.

**Request:**
```http
GET /scalar
```

**Respuesta (200 OK):**
```json
{
  "status": "scalar",
  "time": "2025-12-10T13:00:50.467994126"
}
```

## Swagger UI

La documentación interactiva de la API está disponible en:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Docs**: http://localhost:8080/api-docs

## Lógica de Rollback

### Por Tipo de Operación

1. **CREAR**: Elimina el pedido que fue creado
2. **CANCELAR**: Restaura el estado anterior del pedido
3. **DESPACHAR**: Restaura el estado anterior del pedido
4. **ACTUALIZAR_ESTADO**: Restaura el estado anterior del pedido
5. **ELIMINAR**: Reinserta el pedido eliminado con su estado anterior

## Ejemplos de Uso

### Ejemplo 1: Crear y revertir un pedido

```bash
# 1. Crear un pedido
curl -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"clienteId": 1, "productosIds": [1]}'

# Respuesta:
# { "id": 1, "estado": "PENDIENTE", ... }

# 2. Revertir la creación
curl -X POST http://localhost:8080/api/pedidos/rollback

# Respuesta:
# {
#   "mensaje": "Rollback realizado correctamente",
#   "operacionRevertida": "CREAR",
#   "pedido": null
# }

# El pedido ha sido eliminado
```

### Ejemplo 2: Cancelar y revertir cancelación

```bash
# 1. Cancelar un pedido existente (ID: 1)
curl -X PATCH http://localhost:8080/api/pedidos/1/estado \
  -H "Content-Type: application/json" \
  -d '{"estado": "CANCELADO"}'

# 2. Revertir la cancelación
curl -X POST http://localhost:8080/api/pedidos/rollback

# El pedido vuelve a estado "PENDIENTE"
```

### Ejemplo 3: Múltiples rollbacks en secuencia

```bash
# Supongamos las siguientes operaciones:
# 1. Crear pedido -> estado PENDIENTE
# 2. Actualizar a EN_PREPARACION
# 3. Actualizar a LISTO
# 4. Actualizar a ENTREGADO

# Primer rollback: ENTREGADO -> LISTO
curl -X POST http://localhost:8080/api/pedidos/rollback

# Segundo rollback: LISTO -> EN_PREPARACION
curl -X POST http://localhost:8080/api/pedidos/rollback

# Tercer rollback: EN_PREPARACION -> PENDIENTE
curl -X POST http://localhost:8080/api/pedidos/rollback

# Cuarto rollback: Elimina el pedido (revierte CREAR)
curl -X POST http://localhost:8080/api/pedidos/rollback
```

## Consideraciones Técnicas

### Thread Safety
- `HistoryStackService` utiliza sincronización para garantizar operaciones thread-safe
- Todas las operaciones de la pila están sincronizadas

### Almacenamiento
- La pila de historial es **en memoria** (no persistente)
- Los datos se pierden al reiniciar la aplicación
- Para producción, considerar implementar persistencia en base de datos

### Copias Profundas
- Los pedidos se copian profundamente antes de almacenar en el historial
- Evita problemas de mutación accidental de objetos compartidos

### Manejo de Errores
- Si un rollback falla, el registro se vuelve a insertar en la pila (re-push)
- Mantiene la consistencia del estado del historial

## Tests

El sistema incluye tests completos:

```bash
# Ejecutar todos los tests
mvn test

# Tests incluidos:
# - HistoryStackServiceTest (5 tests)
# - PedidoServiceRollbackTest (5 tests)
# - PedidoControllerRollbackTest (4 tests)
# - ScalarControllerTest (1 test)
```

## Seguridad

- **CodeQL**: 0 vulnerabilidades detectadas
- Thread-safe mediante sincronización
- Validaciones de entrada en todos los endpoints
- Manejo apropiado de excepciones

## Limitaciones Conocidas

1. **No persistente**: La pila de historial se pierde al reiniciar
2. **Sin límite de tamaño**: La pila puede crecer indefinidamente
3. **Sin autenticación**: No hay control de acceso al endpoint de rollback
4. **Operaciones atómicas**: No hay soporte para transacciones distribuidas

## Mejoras Futuras Sugeridas

1. Implementar persistencia en base de datos
2. Agregar límite máximo de tamaño de pila
3. Implementar autenticación y autorización
4. Agregar endpoint para ver historial sin modificarlo
5. Implementar rollback selectivo (por ID de operación)
6. Agregar soporte para rollback de múltiples operaciones a la vez
7. Implementar evento de auditoría para cada rollback
