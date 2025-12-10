package com.fastfood.model;

import java.time.LocalDateTime;

public class OperationRecord {
    
    public enum TipoOperacion {
        CREAR,
        CANCELAR,
        DESPACHAR,
        ELIMINAR,
        ACTUALIZAR_ESTADO
    }
    
    private TipoOperacion tipoOperacion;
    private Pedido pedidoAntes;
    private Pedido pedidoDespues;
    private LocalDateTime timestamp;
    
    public OperationRecord() {
        this.timestamp = LocalDateTime.now();
    }
    
    public OperationRecord(TipoOperacion tipoOperacion, Pedido pedidoAntes, Pedido pedidoDespues) {
        this.tipoOperacion = tipoOperacion;
        this.pedidoAntes = pedidoAntes;
        this.pedidoDespues = pedidoDespues;
        this.timestamp = LocalDateTime.now();
    }
    
    // Helper method to create a deep copy of a Pedido
    public static Pedido copyPedido(Pedido original) {
        if (original == null) {
            return null;
        }
        Pedido copy = new Pedido();
        copy.setId(original.getId());
        copy.setClienteId(original.getClienteId());
        copy.setProductosIds(original.getProductosIds() != null ? 
            new java.util.ArrayList<>(original.getProductosIds()) : null);
        copy.setTotal(original.getTotal());
        copy.setEstado(original.getEstado());
        copy.setFecha(original.getFecha());
        return copy;
    }
    
    public TipoOperacion getTipoOperacion() {
        return tipoOperacion;
    }
    
    public void setTipoOperacion(TipoOperacion tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
    }
    
    public Pedido getPedidoAntes() {
        return pedidoAntes;
    }
    
    public void setPedidoAntes(Pedido pedidoAntes) {
        this.pedidoAntes = pedidoAntes;
    }
    
    public Pedido getPedidoDespues() {
        return pedidoDespues;
    }
    
    public void setPedidoDespues(Pedido pedidoDespues) {
        this.pedidoDespues = pedidoDespues;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "OperationRecord{" +
                "tipoOperacion=" + tipoOperacion +
                ", pedidoAntes=" + (pedidoAntes != null ? pedidoAntes.getId() : "null") +
                ", pedidoDespues=" + (pedidoDespues != null ? pedidoDespues.getId() : "null") +
                ", timestamp=" + timestamp +
                '}';
    }
}
