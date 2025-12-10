package com.fastfood.service;

import com.fastfood.model.OperationRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HistoryStackServiceTest {
    
    private HistoryStackService historyStackService;
    
    @BeforeEach
    void setUp() {
        historyStackService = new HistoryStackService();
    }
    
    @Test
    void testPushAndPop() {
        OperationRecord record = new OperationRecord(
            OperationRecord.TipoOperacion.CREAR,
            null,
            null
        );
        
        historyStackService.push(record);
        
        assertFalse(historyStackService.isEmpty());
        assertEquals(1, historyStackService.size());
        
        var popped = historyStackService.pop();
        assertTrue(popped.isPresent());
        assertEquals(record, popped.get());
        assertTrue(historyStackService.isEmpty());
    }
    
    @Test
    void testPeek() {
        OperationRecord record = new OperationRecord(
            OperationRecord.TipoOperacion.CREAR,
            null,
            null
        );
        
        historyStackService.push(record);
        
        var peeked = historyStackService.peek();
        assertTrue(peeked.isPresent());
        assertEquals(record, peeked.get());
        assertFalse(historyStackService.isEmpty());
    }
    
    @Test
    void testEmptyPop() {
        assertTrue(historyStackService.isEmpty());
        var popped = historyStackService.pop();
        assertFalse(popped.isPresent());
    }
    
    @Test
    void testClear() {
        historyStackService.push(new OperationRecord());
        historyStackService.push(new OperationRecord());
        historyStackService.push(new OperationRecord());
        
        assertEquals(3, historyStackService.size());
        
        historyStackService.clear();
        
        assertTrue(historyStackService.isEmpty());
        assertEquals(0, historyStackService.size());
    }
    
    @Test
    void testPushNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            historyStackService.push(null);
        });
    }
}
