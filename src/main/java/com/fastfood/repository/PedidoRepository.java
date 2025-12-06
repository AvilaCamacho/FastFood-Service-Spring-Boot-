package com.fastfood.repository;

import com.fastfood.model.Pedido;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PedidoRepository {
    
    private final Map<Long, Pedido> pedidos = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Pedido save(Pedido pedido) {
        if (pedido.getId() == null) {
            pedido.setId(idGenerator.getAndIncrement());
        }
        pedidos.put(pedido.getId(), pedido);
        return pedido;
    }

    public Optional<Pedido> findById(Long id) {
        return Optional.ofNullable(pedidos.get(id));
    }

    public List<Pedido> findAll() {
        return new ArrayList<>(pedidos.values());
    }

    public List<Pedido> findByClienteId(Long clienteId) {
        return pedidos.values().stream()
                .filter(p -> p.getClienteId().equals(clienteId))
                .toList();
    }

    public List<Pedido> findByEstado(String estado) {
        return pedidos.values().stream()
                .filter(p -> p.getEstado().equalsIgnoreCase(estado))
                .toList();
    }

    public void deleteById(Long id) {
        pedidos.remove(id);
    }

    public boolean existsById(Long id) {
        return pedidos.containsKey(id);
    }
}
