package com.example.orderflow.controller;

import com.example.orderflow.domain.Client;
import com.example.orderflow.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<Client> create(@Valid @RequestBody Client client) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(clientService.createClient(client));
    }

    @GetMapping
    public ResponseEntity<List<Client>> getAll() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> update(@PathVariable Long id, @Valid @RequestBody Client client) {
        return ResponseEntity.ok(clientService.updateClient(id, client));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}