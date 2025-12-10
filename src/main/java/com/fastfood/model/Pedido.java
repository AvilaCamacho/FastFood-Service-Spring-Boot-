package com.fastfood.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Pedido {
    private Long id;
    private Long clienteId;
    private List<Long> productosIds;
    private Double total;
    private String estado;
    private LocalDateTime fecha;

    public Pedido() {
        this.productosIds = new ArrayList<>();
        this.fecha = LocalDateTime.now();
        this.estado = "PENDIENTE";
    }

    public Pedido(Long id, Long clienteId, List<Long> productosIds, Double total, String estado) {
        this.id = id;
        this.clienteId = clienteId;
        this.productosIds = productosIds != null ? productosIds : new ArrayList<>();
        this.total = total;
        this.estado = estado != null ? estado : "PENDIENTE";
        this.fecha = LocalDateTime.now();
    }

    // Constructor de copia para crear snapshots
    public Pedido(Pedido other) {
        if (other != null) {
            this.id = other.id;
            this.clienteId = other.clienteId;
            this.productosIds = other.productosIds != null ? new ArrayList<>(other.productosIds) : new ArrayList<>();
            this.total = other.total;
            this.estado = other.estado;
            this.fecha = other.fecha;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public List<Long> getProductosIds() {
        return productosIds;
    }

    public void setProductosIds(List<Long> productosIds) {
        this.productosIds = productosIds;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return Objects.equals(id, pedido.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", clienteId=" + clienteId +
                ", productosIds=" + productosIds +
                ", total=" + total +
                ", estado='" + estado + '\'' +
                ", fecha=" + fecha +
                '}';
    }
}
