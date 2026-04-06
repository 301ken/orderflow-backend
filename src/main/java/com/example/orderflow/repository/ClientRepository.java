package com.example.orderflow.repository;

import com.example.orderflow.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {

}