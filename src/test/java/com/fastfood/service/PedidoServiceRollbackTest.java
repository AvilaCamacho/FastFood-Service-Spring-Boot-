package com.fastfood.service;

import com.fastfood.model.OperationRecord;
import com.fastfood.model.Pedido;
import com.fastfood.model.Cliente;
import com.fastfood.model.Producto;
import com.fastfood.repository.ClienteRepository;
import com.fastfood.repository.PedidoRepository;
import com.fastfood.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PedidoServiceRollbackTest {
    
    private PedidoService pedidoService;
    private PedidoRepository pedidoRepository;
    private ProductoRepository productoRepository;
    private ClienteRepository clienteRepository;
    private HistoryStackService historyStackService;
    
    @BeforeEach
    void setUp() {
        pedidoRepository = new PedidoRepository();
        productoRepository = new ProductoRepository();
        clienteRepository = new ClienteRepository();
        historyStackService = new HistoryStackService();
        
        pedidoService = new PedidoService(
            pedidoRepository,
            productoRepository,
            clienteRepository,
            historyStackService
        );
        
        // Setup test data
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Test Cliente");
        clienteRepository.save(cliente);
        
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Hamburguesa");
        producto.setPrecio(10.0);
        productoRepository.save(producto);
    }
    
    @Test
    void testRollbackCrearPedido() {
        // Create a pedido
        Pedido pedido = new Pedido();
        pedido.setClienteId(1L);
        pedido.setProductosIds(List.of(1L));
        
        Pedido createdPedido = pedidoService.crearPedido(pedido);
        assertNotNull(createdPedido.getId());
        
        // Verify it was added to history
        assertFalse(historyStackService.isEmpty());
        
        // Get the operation record
        var recordOpt = historyStackService.pop();
        assertTrue(recordOpt.isPresent());
        
        OperationRecord record = recordOpt.get();
        assertEquals(OperationRecord.TipoOperacion.CREAR, record.getTipoOperacion());
        
        // Revert the operation
        pedidoService.revertOperation(record);
        
        // Verify pedido was deleted
        assertFalse(pedidoRepository.existsById(createdPedido.getId()));
    }
    
    @Test
    void testRollbackCancelarPedido() {
        // Create a pedido
        Pedido pedido = new Pedido();
        pedido.setClienteId(1L);
        pedido.setProductosIds(List.of(1L));
        
        Pedido createdPedido = pedidoService.crearPedido(pedido);
        historyStackService.clear(); // Clear the CREATE operation
        
        String originalEstado = createdPedido.getEstado();
        
        // Cancel the pedido
        Pedido canceledPedido = pedidoService.actualizarEstadoPedido(createdPedido.getId(), "CANCELADO");
        assertEquals("CANCELADO", canceledPedido.getEstado());
        
        // Get the operation record
        var recordOpt = historyStackService.pop();
        assertTrue(recordOpt.isPresent());
        
        OperationRecord record = recordOpt.get();
        assertEquals(OperationRecord.TipoOperacion.CANCELAR, record.getTipoOperacion());
        
        // Revert the operation
        Pedido revertedPedido = pedidoService.revertOperation(record);
        
        // Verify estado was reverted
        assertNotNull(revertedPedido);
        assertEquals(originalEstado, revertedPedido.getEstado());
        
        // Verify in repository
        var pedidoFromRepo = pedidoRepository.findById(createdPedido.getId());
        assertTrue(pedidoFromRepo.isPresent());
        assertEquals(originalEstado, pedidoFromRepo.get().getEstado());
    }
    
    @Test
    void testRollbackDespacharPedido() {
        // Create a pedido
        Pedido pedido = new Pedido();
        pedido.setClienteId(1L);
        pedido.setProductosIds(List.of(1L));
        
        Pedido createdPedido = pedidoService.crearPedido(pedido);
        historyStackService.clear();
        
        String originalEstado = createdPedido.getEstado();
        
        // Update to ENTREGADO
        Pedido despachadoPedido = pedidoService.actualizarEstadoPedido(createdPedido.getId(), "ENTREGADO");
        assertEquals("ENTREGADO", despachadoPedido.getEstado());
        
        // Get the operation record
        var recordOpt = historyStackService.pop();
        assertTrue(recordOpt.isPresent());
        
        OperationRecord record = recordOpt.get();
        assertEquals(OperationRecord.TipoOperacion.DESPACHAR, record.getTipoOperacion());
        
        // Revert the operation
        Pedido revertedPedido = pedidoService.revertOperation(record);
        
        // Verify estado was reverted
        assertNotNull(revertedPedido);
        assertEquals(originalEstado, revertedPedido.getEstado());
    }
    
    @Test
    void testRollbackEliminarPedido() {
        // Create a pedido
        Pedido pedido = new Pedido();
        pedido.setClienteId(1L);
        pedido.setProductosIds(List.of(1L));
        
        Pedido createdPedido = pedidoService.crearPedido(pedido);
        Long pedidoId = createdPedido.getId();
        historyStackService.clear();
        
        // Delete the pedido
        pedidoService.eliminarPedido(pedidoId);
        assertFalse(pedidoRepository.existsById(pedidoId));
        
        // Get the operation record
        var recordOpt = historyStackService.pop();
        assertTrue(recordOpt.isPresent());
        
        OperationRecord record = recordOpt.get();
        assertEquals(OperationRecord.TipoOperacion.ELIMINAR, record.getTipoOperacion());
        
        // Revert the operation
        Pedido revertedPedido = pedidoService.revertOperation(record);
        
        // Verify pedido was restored
        assertNotNull(revertedPedido);
        assertTrue(pedidoRepository.existsById(pedidoId));
        
        var restoredPedido = pedidoRepository.findById(pedidoId);
        assertTrue(restoredPedido.isPresent());
        assertEquals(createdPedido.getEstado(), restoredPedido.get().getEstado());
    }
    
    @Test
    void testMultipleOperationsAndRollback() {
        // Create a pedido
        Pedido pedido = new Pedido();
        pedido.setClienteId(1L);
        pedido.setProductosIds(List.of(1L));
        
        Pedido createdPedido = pedidoService.crearPedido(pedido);
        assertEquals(1, historyStackService.size());
        
        // Update estado
        pedidoService.actualizarEstadoPedido(createdPedido.getId(), "EN_PREPARACION");
        assertEquals(2, historyStackService.size());
        
        // Update estado again
        pedidoService.actualizarEstadoPedido(createdPedido.getId(), "LISTO");
        assertEquals(3, historyStackService.size());
        
        // Rollback last operation (LISTO -> EN_PREPARACION)
        var record1 = historyStackService.pop().get();
        pedidoService.revertOperation(record1);
        
        var pedido1 = pedidoRepository.findById(createdPedido.getId()).get();
        assertEquals("EN_PREPARACION", pedido1.getEstado());
        
        // Rollback second operation (EN_PREPARACION -> PENDIENTE)
        var record2 = historyStackService.pop().get();
        pedidoService.revertOperation(record2);
        
        var pedido2 = pedidoRepository.findById(createdPedido.getId()).get();
        assertEquals("PENDIENTE", pedido2.getEstado());
    }
}
