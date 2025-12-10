package com.fastfood.service;

import com.fastfood.model.OperationRecord;
import com.fastfood.model.Pedido;
import com.fastfood.model.Producto;
import com.fastfood.repository.ClienteRepository;
import com.fastfood.repository.PedidoRepository;
import com.fastfood.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final HistoryStackService historyStackService;

    @Autowired
    public PedidoService(PedidoRepository pedidoRepository, 
                        ProductoRepository productoRepository,
                        ClienteRepository clienteRepository,
                        HistoryStackService historyStackService) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
        this.historyStackService = historyStackService;
    }

    public Pedido crearPedido(Pedido pedido) {
        // Validar que el cliente existe
        if (pedido.getClienteId() == null || !clienteRepository.existsById(pedido.getClienteId())) {
            throw new IllegalArgumentException("Cliente no válido o no existe");
        }

        // Validar que los productos existen y calcular el total
        if (pedido.getProductosIds() == null || pedido.getProductosIds().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe contener al menos un producto");
        }

        double total = 0.0;
        for (Long productoId : pedido.getProductosIds()) {
            Optional<Producto> producto = productoRepository.findById(productoId);
            if (producto.isEmpty()) {
                throw new IllegalArgumentException("Producto no encontrado con id: " + productoId);
            }
            total += producto.get().getPrecio();
        }

        pedido.setTotal(total);
        Pedido savedPedido = pedidoRepository.save(pedido);
        
        // Register operation in history stack
        OperationRecord record = new OperationRecord(
            OperationRecord.TipoOperacion.CREAR,
            null,
            OperationRecord.copyPedido(savedPedido)
        );
        historyStackService.push(record);
        
        return savedPedido;
    }

    public Optional<Pedido> obtenerPedidoPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    public List<Pedido> obtenerTodosLosPedidos() {
        return pedidoRepository.findAll();
    }

    public List<Pedido> obtenerPedidosPorCliente(Long clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new IllegalArgumentException("Cliente no encontrado con id: " + clienteId);
        }
        return pedidoRepository.findByClienteId(clienteId);
    }

    public List<Pedido> obtenerPedidosPorEstado(String estado) {
        return pedidoRepository.findByEstado(estado);
    }

    public Pedido actualizarEstadoPedido(Long id, String nuevoEstado) {
        Optional<Pedido> pedidoExistente = pedidoRepository.findById(id);
        if (pedidoExistente.isEmpty()) {
            throw new IllegalArgumentException("Pedido no encontrado con id: " + id);
        }

        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }

        // Validar estados permitidos
        List<String> estadosPermitidos = List.of("PENDIENTE", "EN_PREPARACION", "LISTO", "ENTREGADO", "CANCELADO");
        if (!estadosPermitidos.contains(nuevoEstado.toUpperCase())) {
            throw new IllegalArgumentException("Estado no válido. Estados permitidos: " + estadosPermitidos);
        }

        Pedido pedido = pedidoExistente.get();
        Pedido pedidoAnterior = OperationRecord.copyPedido(pedido);
        
        pedido.setEstado(nuevoEstado.toUpperCase());
        Pedido pedidoActualizado = pedidoRepository.save(pedido);
        
        // Determine operation type based on new state
        OperationRecord.TipoOperacion tipoOperacion;
        if ("CANCELADO".equals(nuevoEstado.toUpperCase())) {
            tipoOperacion = OperationRecord.TipoOperacion.CANCELAR;
        } else if ("ENTREGADO".equals(nuevoEstado.toUpperCase())) {
            tipoOperacion = OperationRecord.TipoOperacion.DESPACHAR;
        } else {
            tipoOperacion = OperationRecord.TipoOperacion.ACTUALIZAR_ESTADO;
        }
        
        // Register operation in history stack
        OperationRecord record = new OperationRecord(
            tipoOperacion,
            pedidoAnterior,
            OperationRecord.copyPedido(pedidoActualizado)
        );
        historyStackService.push(record);
        
        return pedidoActualizado;
    }

    public void eliminarPedido(Long id) {
        if (!pedidoRepository.existsById(id)) {
            throw new IllegalArgumentException("Pedido no encontrado con id: " + id);
        }
        
        // Get pedido before deleting
        Optional<Pedido> pedidoExistente = pedidoRepository.findById(id);
        Pedido pedidoEliminado = pedidoExistente.orElse(null);
        
        pedidoRepository.deleteById(id);
        
        // Register operation in history stack
        OperationRecord record = new OperationRecord(
            OperationRecord.TipoOperacion.ELIMINAR,
            OperationRecord.copyPedido(pedidoEliminado),
            null
        );
        historyStackService.push(record);
    }
    
    /**
     * Revert an operation from the history stack
     */
    public Pedido revertOperation(OperationRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Operation record cannot be null");
        }
        
        switch (record.getTipoOperacion()) {
            case CREAR:
                // Revert CREAR: delete the created pedido
                if (record.getPedidoDespues() != null) {
                    Long pedidoId = record.getPedidoDespues().getId();
                    if (pedidoRepository.existsById(pedidoId)) {
                        pedidoRepository.deleteById(pedidoId);
                    }
                }
                return null;
                
            case CANCELAR:
            case DESPACHAR:
            case ACTUALIZAR_ESTADO:
                // Revert state change: restore previous state
                if (record.getPedidoAntes() != null) {
                    Pedido pedidoRestaurado = OperationRecord.copyPedido(record.getPedidoAntes());
                    return pedidoRepository.save(pedidoRestaurado);
                }
                return null;
                
            case ELIMINAR:
                // Revert ELIMINAR: re-insert the deleted pedido
                if (record.getPedidoAntes() != null) {
                    Pedido pedidoRestaurado = OperationRecord.copyPedido(record.getPedidoAntes());
                    return pedidoRepository.save(pedidoRestaurado);
                }
                return null;
                
            default:
                throw new IllegalArgumentException("Unknown operation type: " + record.getTipoOperacion());
        }
    }
}
