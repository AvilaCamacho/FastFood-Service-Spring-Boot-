package com.fastfood.controller;

import com.fastfood.model.OperationRecord;
import com.fastfood.model.Pedido;
import com.fastfood.service.HistoryStackService;
import com.fastfood.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final HistoryStackService historyStackService;

    @Autowired
    public PedidoController(PedidoService pedidoService, HistoryStackService historyStackService) {
        this.pedidoService = pedidoService;
        this.historyStackService = historyStackService;
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
    public ResponseEntity<?> rollback() {
        try {
            // Check if history stack is empty
            if (historyStackService.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("mensaje", "No hay operaciones para revertir");
                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
            }
            
            // Pop the most recent operation
            OperationRecord record = historyStackService.pop().get(); // Safe to call get() after isEmpty check
            
            // Attempt to revert the operation
            try {
                Pedido pedidoRevertido = pedidoService.revertOperation(record);
                
                // Build success response
                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("mensaje", "Rollback realizado correctamente");
                successResponse.put("operacionRevertida", record.getTipoOperacion().toString());
                successResponse.put("pedido", pedidoRevertido);
                
                return ResponseEntity.ok(successResponse);
                
            } catch (Exception e) {
                // Re-push the record if revert fails to maintain consistency
                historyStackService.push(record);
                
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("mensaje", "Error al realizar rollback: " + e.getMessage());
                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("mensaje", "Error inesperado: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
