package com.fastfood.service;

import com.fastfood.model.OperationRecord;
import org.springframework.stereotype.Service;

import java.util.Stack;

@Service
public class HistoryStackService {

    private final Stack<OperationRecord> historyStack = new Stack<>();

    /**
     * Añade un registro de operación a la pila de historial
     */
    public synchronized void pushOperation(OperationRecord record) {
        historyStack.push(record);
    }

    /**
     * Obtiene y elimina el último registro de operación de la pila
     */
    public synchronized OperationRecord popOperation() {
        if (historyStack.isEmpty()) {
            return null;
        }
        return historyStack.pop();
    }

    /**
     * Verifica si la pila de historial está vacía
     */
    public synchronized boolean isEmpty() {
        return historyStack.isEmpty();
    }

    /**
     * Obtiene el tamaño de la pila de historial
     */
    public synchronized int size() {
        return historyStack.size();
    }

    /**
     * Limpia toda la pila de historial
     */
    public synchronized void clear() {
        historyStack.clear();
    }
}
