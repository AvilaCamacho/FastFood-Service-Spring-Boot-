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
        Pedido nuevoPedido = pedidoRepository.save(pedido);
        
        // Registrar operación CREAR en el historial
        OperationRecord record = new OperationRecord("CREAR", null, new Pedido(nuevoPedido));
        historyStackService.pushOperation(record);
        
        return nuevoPedido;
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
        
        // Guardar snapshot del estado anterior
        Pedido pedidoAntes = new Pedido(pedido);
        
        pedido.setEstado(nuevoEstado.toUpperCase());
        Pedido pedidoActualizado = pedidoRepository.save(pedido);
        
        // Determinar el tipo de operación según el nuevo estado
        String tipoOperacion = switch (nuevoEstado.toUpperCase()) {
            case "CANCELADO" -> "CANCELAR";
            case "ENTREGADO" -> "DESPACHAR";
            default -> "ACTUALIZAR_ESTADO";
        };
        
        // Registrar operación en el historial
        OperationRecord record = new OperationRecord(tipoOperacion, pedidoAntes, new Pedido(pedidoActualizado));
        historyStackService.pushOperation(record);
        
        return pedidoActualizado;
    }

    public void eliminarPedido(Long id) {
        if (!pedidoRepository.existsById(id)) {
            throw new IllegalArgumentException("Pedido no encontrado con id: " + id);
        }
        
        // Guardar snapshot del pedido antes de eliminarlo
        Optional<Pedido> pedidoExistente = pedidoRepository.findById(id);
        Pedido pedidoAntes = new Pedido(pedidoExistente.get());
        
        pedidoRepository.deleteById(id);
        
        // Registrar operación ELIMINAR en el historial
        OperationRecord record = new OperationRecord("ELIMINAR", pedidoAntes, null);
        historyStackService.pushOperation(record);
    }

    /**
     * Realiza un rollback de la última operación registrada en el historial
     * @return OperationRecord de la operación revertida
     */
    public OperationRecord realizarRollback() {
        // Verificar si hay operaciones en el historial
        if (historyStackService.isEmpty()) {
            throw new IllegalStateException("No hay operaciones para revertir");
        }

        OperationRecord record = historyStackService.popOperation();
        
        switch (record.getTipoOperacion()) {
            case "CREAR":
                // Revertir creación: eliminar el pedido creado
                revertirCrear(record);
                break;
            case "CANCELAR":
            case "DESPACHAR":
            case "ACTUALIZAR_ESTADO":
                // Revertir cambio de estado: restaurar estado anterior
                revertirActualizarEstado(record);
                break;
            case "ELIMINAR":
                // Revertir eliminación: restaurar el pedido eliminado
                revertirEliminar(record);
                break;
            default:
                throw new IllegalStateException("Tipo de operación desconocido: " + record.getTipoOperacion());
        }
        
        return record;
    }

    private void revertirCrear(OperationRecord record) {
        Pedido pedidoCreado = record.getPedidoDespues();
        if (pedidoCreado != null && pedidoRepository.existsById(pedidoCreado.getId())) {
            pedidoRepository.deleteById(pedidoCreado.getId());
        }
    }

    private void revertirActualizarEstado(OperationRecord record) {
        Pedido pedidoAntes = record.getPedidoAntes();
        if (pedidoAntes != null && pedidoRepository.existsById(pedidoAntes.getId())) {
            Pedido pedidoActual = pedidoRepository.findById(pedidoAntes.getId()).get();
            pedidoActual.setEstado(pedidoAntes.getEstado());
            pedidoRepository.save(pedidoActual);
        }
    }

    private void revertirEliminar(OperationRecord record) {
        Pedido pedidoEliminado = record.getPedidoAntes();
        if (pedidoEliminado != null) {
            // Restaurar el pedido eliminado
            Pedido pedidoRestaurado = new Pedido(pedidoEliminado);
            pedidoRepository.save(pedidoRestaurado);
        }
    }
}
