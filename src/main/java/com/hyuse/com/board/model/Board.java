package com.hyuse.com.board.model;

import com.hyuse.com.board.enums.ColumnType;
import jakarta.persistence.*;
import jakarta.persistence.Column;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "boards")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("position ASC")
    private List<Columns> columns = new ArrayList<>();

    public Board() {
    }

    public Board(String name) {
        this.name = name;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Columns> getColumns() {
        return columns;
    }

    public void setColumns(List<Columns> columns) {
        this.columns = columns;
    }

    public void addColumn(Columns column) {
        columns.add(column);
        column.setBoard(this);
    }

    public Columns getInitialColumn() {
        return columns.stream()
                .filter(col -> col.getType() == ColumnType.INITIAL)
                .findFirst()
                .orElse(null);
    }

    public Columns getFinalColumn() {
        return columns.stream()
                .filter(col -> col.getType() == ColumnType.FINAL)
                .findFirst()
                .orElse(null);
    }

    public Columns getCancelledColumn() {
        return columns.stream()
                .filter(col -> col.getType() == ColumnType.CANCELLED)
                .findFirst()
                .orElse(null);
    }

    public Columns getNextColumn(Columns currentColumn) {
        int currentPosition = currentColumn.getPosition();
        return columns.stream()
                .filter(col -> col.getPosition() == currentPosition + 1)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return name;
    }
}