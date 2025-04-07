package com.hyuse.com.board.repository;

import com.hyuse.com.board.model.CardMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardMovementRepository extends JpaRepository<CardMovement, Long> {
}
