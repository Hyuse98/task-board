package com.hyuse.com.board.model;

import com.hyuse.com.board.enums.ColumnType;
import jakarta.persistence.*;
import jakarta.persistence.Column;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "columns")
public class Columns {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ColumnType type;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;

    @OneToMany(mappedBy = "columns", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Card> cards = new ArrayList<>();

    public Columns() {
    }

    public Columns(String name, int position, ColumnType type) {
        this.name = name;
        this.position = position;
        this.type = type;
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

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public ColumnType getType() {
        return type;
    }

    public void setType(ColumnType type) {
        this.type = type;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public void addCard(Card card) {
        cards.add(card);
        card.setColumn((Columns) this);
    }

    @Override
    public String toString() {
        return name + " (" + type + ")";
    }
}
