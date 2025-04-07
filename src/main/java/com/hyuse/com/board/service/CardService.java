package com.hyuse.com.board.service;

import com.hyuse.com.board.enums.ColumnType;
import com.hyuse.com.board.model.*;
import com.hyuse.com.board.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    public Card createCard(String title, String description, Columns initialColumns) {
        Card card = new Card(title, description);
        initialColumns.addCard(card);

        // Add initial movement
        CardMovement movement = new CardMovement("Created", initialColumns.getName());
        card.addMovement(movement);

        return cardRepository.save(card);
    }

    public Card moveCardToNextColumn(Card card) {
        if (card.isBlocked()) {
            throw new IllegalStateException("Cannot move a blocked card");
        }

        Columns currentColumns = (Columns) card.getColumn();
        Columns nextColumns = currentColumns.getBoard().getNextColumn(currentColumns);

        if (nextColumns == null) {
            throw new IllegalStateException("There is no next column");
        }

        // Record movement
        CardMovement movement = new CardMovement(currentColumns.getName(), nextColumns.getName());
        card.addMovement(movement);

        // Move card
        currentColumns.getCards().remove(card);
        nextColumns.addCard(card);

        return cardRepository.save(card);
    }

    public Card cancelCard(Card card) {
        if (card.isBlocked()) {
            throw new IllegalStateException("Cannot cancel a blocked card");
        }

        Columns currentColumns = (Columns) card.getColumn();
        Columns cancelledColumns = currentColumns.getBoard().getCancelledColumn();

        // Check if card is already in final column
        if (currentColumns.getType() == ColumnType.FINAL) {
            throw new IllegalStateException("Cannot cancel a card that is already in the final column");
        }

        // Record movement
        CardMovement movement = new CardMovement(currentColumns.getName(), cancelledColumns.getName());
        card.addMovement(movement);

        // Move card
        currentColumns.getCards().remove(card);
        cancelledColumns.addCard(card);

        return cardRepository.save(card);
    }

    public Card blockCard(Card card, String reason) {
        if (card.isBlocked()) {
            throw new IllegalStateException("Card is already blocked");
        }

        card.setBlocked(true);
        card.setBlockReason(reason);

        // Record block history
        BlockHistory history = new BlockHistory(true, reason);
        card.addBlockHistory(history);

        return cardRepository.save(card);
    }

    public Card unblockCard(Card card, String reason) {
        if (!card.isBlocked()) {
            throw new IllegalStateException("Card is not blocked");
        }

        card.setBlocked(false);
        card.setBlockReason(null);

        // Record unblock history
        BlockHistory history = new BlockHistory(false, reason);
        card.addBlockHistory(history);

        return cardRepository.save(card);
    }

    public Map<String, Object> generateTimeReport(Board board) {
        Map<String, Object> report = new HashMap<>();
        List<Map<String, Object>> cardReports = new ArrayList<>();

        for (Columns columns : board.getColumns()) {
            for (Card card : columns.getCards()) {
                Map<String, Object> cardReport = new HashMap<>();
                cardReport.put("cardId", card.getId());
                cardReport.put("title", card.getTitle());

                // Calculate time in each column
                Map<String, Duration> timeInColumns = calculateTimeInColumns(card);
                cardReport.put("timeInColumns", timeInColumns);

                // Calculate total time
                Duration totalTime = timeInColumns.values().stream().reduce(Duration.ZERO, Duration::plus);
                cardReport.put("totalTime", totalTime);

                cardReports.add(cardReport);
            }
        }

        report.put("boardName", board.getName());
        report.put("cards", cardReports);

        return report;
    }

    private Map<String, Duration> calculateTimeInColumns(Card card) {
        Map<String, Duration> result = new HashMap<>();
        List<CardMovement> movements = card.getMovements();

        if (movements.isEmpty()) {
            return result;
        }

        // Process all movements
        for (int i = 0; i < movements.size() - 1; i++) {
            CardMovement current = movements.get(i);
            CardMovement next = movements.get(i + 1);

            String columnName = current.getToColumnName();
            Duration time = Duration.between(current.getMovementDate(), next.getMovementDate());

            result.put(columnName, result.getOrDefault(columnName, Duration.ZERO).plus(time));
        }

        // Handle current column
        CardMovement lastMovement = movements.get(movements.size() - 1);
        String currentColumn = lastMovement.getToColumnName();
        Duration timeInCurrentColumn = Duration.between(lastMovement.getMovementDate(), LocalDateTime.now());
        result.put(currentColumn, result.getOrDefault(currentColumn, Duration.ZERO).plus(timeInCurrentColumn));

        return result;
    }

    public Map<String, Object> generateBlockReport(Board board) {
        Map<String, Object> report = new HashMap<>();
        List<Map<String, Object>> cardReports = new ArrayList<>();

        for (Columns columns : board.getColumns()) {
            for (Card card : columns.getCards()) {
                if (!card.getBlockHistory().isEmpty()) {
                    Map<String, Object> cardReport = new HashMap<>();
                    cardReport.put("cardId", card.getId());
                    cardReport.put("title", card.getTitle());

                    List<Map<String, Object>> blockEvents = new ArrayList<>();
                    List<BlockHistory> history = card.getBlockHistory();

                    // Process block/unblock pairs
                    for (int i = 0; i < history.size() - 1; i += 2) {
                        if (i + 1 < history.size()) {
                            BlockHistory blockEvent = history.get(i);
                            BlockHistory unblockEvent = history.get(i + 1);

                            if (blockEvent.isBlocked() && !unblockEvent.isBlocked()) {
                                Map<String, Object> event = new HashMap<>();
                                event.put("blockReason", blockEvent.getReason());
                                event.put("unblockReason", unblockEvent.getReason());
                                event.put("blockTime", blockEvent.getTimestamp());
                                event.put("unblockTime", unblockEvent.getTimestamp());
                                event.put("duration", Duration.between(blockEvent.getTimestamp(), unblockEvent.getTimestamp()));

                                blockEvents.add(event);
                            }
                        }
                    }

                    // Check if card is currently blocked
                    if (card.isBlocked()) {
                        BlockHistory lastBlock = history.stream()
                                .filter(BlockHistory::isBlocked)
                                .max(Comparator.comparing(BlockHistory::getTimestamp))
                                .orElse(null);

                        if (lastBlock != null) {
                            Map<String, Object> event = new HashMap<>();
                            event.put("blockReason", lastBlock.getReason());
                            event.put("unblockReason", "Still blocked");
                            event.put("blockTime", lastBlock.getTimestamp());
                            event.put("unblockTime", null);
                            event.put("duration", Duration.between(lastBlock.getTimestamp(), LocalDateTime.now()));

                            blockEvents.add(event);
                        }
                    }

                    cardReport.put("blockEvents", blockEvents);
                    cardReport.put("totalBlockCount", blockEvents.size());

                    Duration totalBlockTime = blockEvents.stream()
                            .map(event -> (Duration) event.get("duration"))
                            .reduce(Duration.ZERO, Duration::plus);
                    cardReport.put("totalBlockTime", totalBlockTime);

                    cardReports.add(cardReport);
                }
            }
        }

        report.put("boardName", board.getName());
        report.put("cards", cardReports);

        return report;
    }
}
