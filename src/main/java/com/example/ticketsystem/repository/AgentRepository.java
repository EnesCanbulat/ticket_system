package com.example.ticketsystem.repository;

import com.example.ticketsystem.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, Long> {
}