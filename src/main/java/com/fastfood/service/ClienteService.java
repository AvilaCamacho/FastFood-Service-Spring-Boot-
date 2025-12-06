package com.fastfood.service;

import com.fastfood.model.Cliente;
import com.fastfood.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Autowired
    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente crearCliente(Cliente cliente) {
        if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }
        if (cliente.getEmail() == null || cliente.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("El email del cliente es obligatorio");
        }
        return clienteRepository.save(cliente);
    }

    public Optional<Cliente> obtenerClientePorId(Long id) {
        return clienteRepository.findById(id);
    }

    public List<Cliente> obtenerTodosLosClientes() {
        return clienteRepository.findAll();
    }

    public Cliente actualizarCliente(Long id, Cliente clienteActualizado) {
        Optional<Cliente> clienteExistente = clienteRepository.findById(id);
        if (clienteExistente.isEmpty()) {
            throw new IllegalArgumentException("Cliente no encontrado con id: " + id);
        }
        
        Cliente cliente = clienteExistente.get();
        if (clienteActualizado.getNombre() != null) {
            cliente.setNombre(clienteActualizado.getNombre());
        }
        if (clienteActualizado.getEmail() != null) {
            cliente.setEmail(clienteActualizado.getEmail());
        }
        if (clienteActualizado.getTelefono() != null) {
            cliente.setTelefono(clienteActualizado.getTelefono());
        }
        
        return clienteRepository.save(cliente);
    }

    public void eliminarCliente(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new IllegalArgumentException("Cliente no encontrado con id: " + id);
        }
        clienteRepository.deleteById(id);
    }
}
