package com.fastfood.controller;

import com.fastfood.model.Cliente;
import com.fastfood.model.Pedido;
import com.fastfood.model.Producto;
import com.fastfood.repository.ClienteRepository;
import com.fastfood.repository.PedidoRepository;
import com.fastfood.repository.ProductoRepository;
import com.fastfood.service.HistoryStackService;
import com.fastfood.service.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PedidoController.class)
class PedidoControllerRollbackTest {
    
    @TestConfiguration
    static class TestConfig {
        @Bean
        public PedidoRepository pedidoRepository() {
            return new PedidoRepository();
        }
        
        @Bean
        public ProductoRepository productoRepository() {
            return new ProductoRepository();
        }
        
        @Bean
        public ClienteRepository clienteRepository() {
            return new ClienteRepository();
        }
        
        @Bean
        public HistoryStackService historyStackService() {
            return new HistoryStackService();
        }
        
        @Bean
        public PedidoService pedidoService(
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository,
            ClienteRepository clienteRepository,
            HistoryStackService historyStackService
        ) {
            return new PedidoService(
                pedidoRepository,
                productoRepository,
                clienteRepository,
                historyStackService
            );
        }
    }
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private PedidoService pedidoService;
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private HistoryStackService historyStackService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        // Clear history
        historyStackService.clear();
        
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
    void testRollbackWithEmptyStack() throws Exception {
        mockMvc.perform(post("/api/pedidos/rollback"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.mensaje").value("No hay operaciones para revertir"));
    }
    
    @Test
    void testRollbackCrearPedido() throws Exception {
        // Create a pedido
        Pedido pedido = new Pedido();
        pedido.setClienteId(1L);
        pedido.setProductosIds(List.of(1L));
        
        String pedidoJson = objectMapper.writeValueAsString(pedido);
        
        mockMvc.perform(post("/api/pedidos")
            .contentType(MediaType.APPLICATION_JSON)
            .content(pedidoJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists());
        
        // Rollback
        mockMvc.perform(post("/api/pedidos/rollback"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensaje").value("Rollback realizado correctamente"))
            .andExpect(jsonPath("$.operacionRevertida").value("CREAR"))
            .andExpect(jsonPath("$.pedido").doesNotExist());
    }
    
    @Test
    void testRollbackCancelarPedido() throws Exception {
        // Create a pedido
        Pedido pedido = new Pedido();
        pedido.setClienteId(1L);
        pedido.setProductosIds(List.of(1L));
        
        Pedido createdPedido = pedidoService.crearPedido(pedido);
        historyStackService.clear(); // Clear create operation
        
        // Cancel pedido
        String estadoJson = "{\"estado\": \"CANCELADO\"}";
        
        mockMvc.perform(patch("/api/pedidos/" + createdPedido.getId() + "/estado")
            .contentType(MediaType.APPLICATION_JSON)
            .content(estadoJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("CANCELADO"));
        
        // Rollback
        mockMvc.perform(post("/api/pedidos/rollback"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensaje").value("Rollback realizado correctamente"))
            .andExpect(jsonPath("$.operacionRevertida").value("CANCELAR"))
            .andExpect(jsonPath("$.pedido.estado").value("PENDIENTE"));
    }
    
    @Test
    void testMultipleRollbacks() throws Exception {
        // Create a pedido
        Pedido pedido = new Pedido();
        pedido.setClienteId(1L);
        pedido.setProductosIds(List.of(1L));
        
        Pedido createdPedido = pedidoService.crearPedido(pedido);
        
        // Update estado to EN_PREPARACION
        pedidoService.actualizarEstadoPedido(createdPedido.getId(), "EN_PREPARACION");
        
        // Update estado to LISTO
        pedidoService.actualizarEstadoPedido(createdPedido.getId(), "LISTO");
        
        // First rollback: LISTO -> EN_PREPARACION
        mockMvc.perform(post("/api/pedidos/rollback"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pedido.estado").value("EN_PREPARACION"));
        
        // Second rollback: EN_PREPARACION -> PENDIENTE
        mockMvc.perform(post("/api/pedidos/rollback"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pedido.estado").value("PENDIENTE"));
        
        // Third rollback: Delete pedido
        mockMvc.perform(post("/api/pedidos/rollback"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.operacionRevertida").value("CREAR"));
        
        // Fourth rollback: Empty stack
        mockMvc.perform(post("/api/pedidos/rollback"))
            .andExpect(status().isConflict());
    }
}
