package com.hyuse.com.board.repository;

import com.hyuse.com.board.model.BlockHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockHistoryRepository extends JpaRepository<BlockHistory, Long> {
}
