package com.hyuse.com.board.model;

import jakarta.persistence.*;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Entity
@Table(name = "block_history")
public class BlockHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private boolean blocked; // true = block event, false = unblock event

    @Column(nullable = false, length = 1000)
    private String reason;

    @ManyToOne
    @JoinColumn(name = "card_id")
    private Card card;

    public BlockHistory() {
        this.timestamp = LocalDateTime.now();
    }

    public BlockHistory(boolean blocked, String reason) {
        this();
        this.blocked = blocked;
        this.reason = reason;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public String getReason() {
        return reason;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }
}
