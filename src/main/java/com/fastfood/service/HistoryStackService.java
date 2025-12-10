package com.fastfood.service;

import com.fastfood.model.OperationRecord;
import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

@Service
public class HistoryStackService {
    
    private final Deque<OperationRecord> historyStack = new LinkedList<>();
    
    /**
     * Push an operation record to the history stack
     */
    public synchronized void push(OperationRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Operation record cannot be null");
        }
        historyStack.push(record);
    }
    
    /**
     * Pop the most recent operation record from the stack
     */
    public synchronized Optional<OperationRecord> pop() {
        if (historyStack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(historyStack.pop());
    }
    
    /**
     * Peek at the most recent operation without removing it
     */
    public synchronized Optional<OperationRecord> peek() {
        if (historyStack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(historyStack.peek());
    }
    
    /**
     * Check if the history stack is empty
     */
    public synchronized boolean isEmpty() {
        return historyStack.isEmpty();
    }
    
    /**
     * Clear all history
     */
    public synchronized void clear() {
        historyStack.clear();
    }
    
    /**
     * Get the current size of the history stack
     */
    public synchronized int size() {
        return historyStack.size();
    }
}
