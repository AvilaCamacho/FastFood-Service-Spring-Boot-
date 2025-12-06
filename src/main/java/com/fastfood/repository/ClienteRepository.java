package com.fastfood.repository;

import com.fastfood.model.Cliente;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ClienteRepository {
    
    private final Map<Long, Cliente> clientes = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Cliente save(Cliente cliente) {
        if (cliente.getId() == null) {
            cliente.setId(idGenerator.getAndIncrement());
        }
        clientes.put(cliente.getId(), cliente);
        return cliente;
    }

    public Optional<Cliente> findById(Long id) {
        return Optional.ofNullable(clientes.get(id));
    }

    public List<Cliente> findAll() {
        return new ArrayList<>(clientes.values());
    }

    public void deleteById(Long id) {
        clientes.remove(id);
    }

    public boolean existsById(Long id) {
        return clientes.containsKey(id);
    }
}
