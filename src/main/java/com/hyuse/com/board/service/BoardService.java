package com.hyuse.com.board.service;

import com.hyuse.com.board.enums.ColumnType;
import com.hyuse.com.board.model.Board;
import com.hyuse.com.board.model.Columns;
import com.hyuse.com.board.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    public Optional<Board> getBoardById(Long id) {
        return boardRepository.findById(id);
    }

    public Board createBoard(String name, List<Columns> columns) {
        Board board = new Board(name);

        // Validate columns
        boolean hasInitial = false;
        boolean hasFinal = false;
        boolean hasCancelled = false;

        for (Columns column : columns) {
            if (column.getType() == ColumnType.INITIAL) {
                if (hasInitial) throw new IllegalArgumentException("Board can only have one INITIAL column");
                if (column.getPosition() != 0) throw new IllegalArgumentException("INITIAL column must be the first column");
                hasInitial = true;
            } else if (column.getType() == ColumnType.FINAL) {
                if (hasFinal) throw new IllegalArgumentException("Board can only have one FINAL column");
                hasFinal = true;
            } else if (column.getType() == ColumnType.CANCELLED) {
                if (hasCancelled) throw new IllegalArgumentException("Board can only have one CANCELLED column");
                hasCancelled = true;
            }
            board.addColumn(column);
        }

        // Check required columns
        if (!hasInitial) throw new IllegalArgumentException("Board must have an INITIAL column");
        if (!hasFinal) throw new IllegalArgumentException("Board must have a FINAL column");
        if (!hasCancelled) throw new IllegalArgumentException("Board must have a CANCELLED column");

        // Validate final column is penultimate
        boolean finalColumnIsPenultimate = true;
        boolean cancelledColumnIsLast = true;

        for (Columns column : columns) {
            if (column.getType() == ColumnType.FINAL) {
                finalColumnIsPenultimate = column.getPosition() == columns.size() - 2;
            }
            if (column.getType() == ColumnType.CANCELLED) {
                cancelledColumnIsLast = column.getPosition() == columns.size() - 1;
            }
        }

        if (!finalColumnIsPenultimate) throw new IllegalArgumentException("FINAL column must be the penultimate column");
        if (!cancelledColumnIsLast) throw new IllegalArgumentException("CANCELLED column must be the last column");

        return boardRepository.save(board);
    }

    public void deleteBoard(Long id) {
        boardRepository.deleteById(id);
    }

    public Board saveBoard(Board board) {
        return boardRepository.save(board);
    }
}
