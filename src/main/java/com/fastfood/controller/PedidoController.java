package com.fastfood.controller;

import com.fastfood.model.Pedido;
import com.fastfood.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    @Autowired
    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<?> crearPedido(@RequestBody Pedido pedido) {
        try {
            Pedido nuevoPedido = pedidoService.crearPedido(pedido);
            return new ResponseEntity<>(nuevoPedido, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPedido(@PathVariable Long id) {
        Optional<Pedido> pedido = pedidoService.obtenerPedidoPorId(id);
        if (pedido.isPresent()) {
            return ResponseEntity.ok(pedido.get());
        } else {
            return new ResponseEntity<>("Pedido no encontrado", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<?> obtenerPedidos(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String estado) {
        try {
            List<Pedido> pedidos;
            if (clienteId != null) {
                pedidos = pedidoService.obtenerPedidosPorCliente(clienteId);
            } else if (estado != null) {
                pedidos = pedidoService.obtenerPedidosPorEstado(estado);
            } else {
                pedidos = pedidoService.obtenerTodosLosPedidos();
            }
            return ResponseEntity.ok(pedidos);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstadoPedido(@PathVariable Long id, 
                                                     @RequestBody Map<String, String> request) {
        try {
            String nuevoEstado = request.get("estado");
            if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
                return new ResponseEntity<>("El estado es obligatorio", HttpStatus.BAD_REQUEST);
            }
            Pedido pedidoActualizado = pedidoService.actualizarEstadoPedido(id, nuevoEstado);
            return ResponseEntity.ok(pedidoActualizado);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPedido(@PathVariable Long id) {
        try {
            pedidoService.eliminarPedido(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/rollback")
    public ResponseEntity<?> realizarRollback() {
        try {
            com.fastfood.model.OperationRecord operacionRevertida = pedidoService.realizarRollback();
            
            // Obtener el pedido actual después del rollback
            com.fastfood.model.Pedido pedidoActual = null;
            Long pedidoId = null;
            
            if ("CREAR".equals(operacionRevertida.getTipoOperacion())) {
                // Para CREAR, el pedido fue eliminado, devolver el que fue eliminado
                pedidoActual = operacionRevertida.getPedidoDespues();
            } else if ("ELIMINAR".equals(operacionRevertida.getTipoOperacion())) {
                // Para ELIMINAR, el pedido fue restaurado
                pedidoActual = operacionRevertida.getPedidoAntes();
            } else {
                // Para cambios de estado, devolver el estado actual del pedido
                pedidoId = operacionRevertida.getPedidoAntes().getId();
                Optional<com.fastfood.model.Pedido> pedidoOpt = pedidoService.obtenerPedidoPorId(pedidoId);
                pedidoActual = pedidoOpt.orElse(operacionRevertida.getPedidoAntes());
            }
            
            Map<String, Object> response = Map.of(
                "mensaje", "Rollback realizado correctamente",
                "operacionRevertida", operacionRevertida.getTipoOperacion(),
                "pedido", pedidoActual
            );
            
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, String> error = Map.of(
                "error", e.getMessage()
            );
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, String> error = Map.of(
                "error", "Error al realizar rollback: " + e.getMessage()
            );
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
