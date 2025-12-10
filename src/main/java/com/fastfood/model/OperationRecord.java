package com.fastfood.model;

import java.time.LocalDateTime;

public class OperationRecord {
    private String tipoOperacion;
    private Pedido pedidoAntes;
    private Pedido pedidoDespues;
    private LocalDateTime timestamp;

    public OperationRecord() {
        this.timestamp = LocalDateTime.now();
    }

    public OperationRecord(String tipoOperacion, Pedido pedidoAntes, Pedido pedidoDespues) {
        this.tipoOperacion = tipoOperacion;
        this.pedidoAntes = pedidoAntes;
        this.pedidoDespues = pedidoDespues;
        this.timestamp = LocalDateTime.now();
    }

    public String getTipoOperacion() {
        return tipoOperacion;
    }

    public void setTipoOperacion(String tipoOperacion) {
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
                "tipoOperacion='" + tipoOperacion + '\'' +
                ", pedidoAntes=" + pedidoAntes +
                ", pedidoDespues=" + pedidoDespues +
                ", timestamp=" + timestamp +
                '}';
    }
}
