package com.hyuse.com.board.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import jakarta.persistence.Column;

@Entity
@Table(name = "card_movements")
public class CardMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime movementDate;

    @Column(nullable = false)
    private String fromColumnName;

    @Column(nullable = false)
    private String toColumnName;

    @ManyToOne
    @JoinColumn(name = "card_id")
    private Card card;

    public CardMovement() {
        this.movementDate = LocalDateTime.now();
    }

    public CardMovement(String fromColumnName, String toColumnName) {
        this();
        this.fromColumnName = fromColumnName;
        this.toColumnName = toColumnName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public LocalDateTime getMovementDate() {
        return movementDate;
    }

    public String getFromColumnName() {
        return fromColumnName;
    }

    public String getToColumnName() {
        return toColumnName;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }
}
