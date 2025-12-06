package com.fastfood.service;

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

    @Autowired
    public PedidoService(PedidoRepository pedidoRepository, 
                        ProductoRepository productoRepository,
                        ClienteRepository clienteRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
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
        return pedidoRepository.save(pedido);
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

        // Validar estados permitidos
        List<String> estadosPermitidos = List.of("PENDIENTE", "EN_PREPARACION", "LISTO", "ENTREGADO", "CANCELADO");
        if (!estadosPermitidos.contains(nuevoEstado.toUpperCase())) {
            throw new IllegalArgumentException("Estado no válido. Estados permitidos: " + estadosPermitidos);
        }

        Pedido pedido = pedidoExistente.get();
        pedido.setEstado(nuevoEstado.toUpperCase());
        return pedidoRepository.save(pedido);
    }

    public void eliminarPedido(Long id) {
        if (!pedidoRepository.existsById(id)) {
            throw new IllegalArgumentException("Pedido no encontrado con id: " + id);
        }
        pedidoRepository.deleteById(id);
    }
}
