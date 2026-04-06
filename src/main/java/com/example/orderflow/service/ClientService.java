package com.example.orderflow.service;
import com.example.orderflow.domain.Client;
import com.example.orderflow.repository.ClientRepository;
import com.example.orderflow.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final OrderRepository orderRepository;

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));
    }

    public Client createClient(Client client) {
        return clientRepository.save(client);
    }

    @Transactional
    public Client updateClient(Long id, Client updatedClient) {
        Client existing = getClientById(id);
        existing.setName(updatedClient.getName());
        existing.setEmail(updatedClient.getEmail());
        return existing;
    }

    public void deleteClient(Long id) {

        boolean hasOrders = orderRepository.existsByClientId(id);

        if (hasOrders) {
            throw new RuntimeException("Cannot delete client with orders");
        }

        clientRepository.deleteById(id);


    }
}
