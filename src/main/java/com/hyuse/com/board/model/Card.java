package com.hyuse.com.board.model;

import jakarta.persistence.*;
import jakarta.persistence.Column;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private final LocalDateTime creationDate;

    @Column(nullable = false)
    private boolean blocked;

    @Column
    private String blockReason;

    @ManyToOne
    @JoinColumn(name = "column_id")
    private Columns columns;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL)
    private List<BlockHistory> blockHistory = new ArrayList<>();

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL)
    @OrderBy("movementDate ASC")
    private List<CardMovement> movements = new ArrayList<>();

    public Card() {
        this.creationDate = LocalDateTime.now();
        this.blocked = false;
    }

    public Card(String title, String description) {
        this();
        this.title = title;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getBlockReason() {
        return blockReason;
    }

    public void setBlockReason(String blockReason) {
        this.blockReason = blockReason;
    }

    public Columns getColumn() {
        return columns;
    }

    public void setColumn(Columns columns) {
        this.columns = columns;
    }

    public List<BlockHistory> getBlockHistory() {
        return blockHistory;
    }

    public void addBlockHistory(BlockHistory history) {
        blockHistory.add(history);
        history.setCard(this);
    }

    public List<CardMovement> getMovements() {
        return movements;
    }

    public void addMovement(CardMovement movement) {
        movements.add(movement);
        movement.setCard(this);
    }

    @Override
    public String toString() {
        return "Card #" + id + ": " + title + (blocked ? " [BLOCKED]" : "");
    }
}
