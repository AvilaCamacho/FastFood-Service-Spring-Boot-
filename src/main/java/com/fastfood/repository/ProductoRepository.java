package com.fastfood.repository;

import com.fastfood.model.Producto;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ProductoRepository {
    
    private final Map<Long, Producto> productos = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Producto save(Producto producto) {
        if (producto.getId() == null) {
            producto.setId(idGenerator.getAndIncrement());
        }
        productos.put(producto.getId(), producto);
        return producto;
    }

    public Optional<Producto> findById(Long id) {
        return Optional.ofNullable(productos.get(id));
    }

    public List<Producto> findAll() {
        return new ArrayList<>(productos.values());
    }

    public List<Producto> findByCategoria(String categoria) {
        return productos.values().stream()
                .filter(p -> p.getCategoria() != null && p.getCategoria().equalsIgnoreCase(categoria))
                .toList();
    }

    public void deleteById(Long id) {
        productos.remove(id);
    }

    public boolean existsById(Long id) {
        return productos.containsKey(id);
    }
}
