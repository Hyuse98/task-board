package com.hyuse.com.board.repository;

import com.hyuse.com.board.model.Columns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColumnRepository extends JpaRepository<Columns, Long> {
}
