package com.hyuse.com.board.controller;

import com.hyuse.com.board.enums.ColumnType;
import com.hyuse.com.board.model.Board;
import com.hyuse.com.board.model.Card;
import com.hyuse.com.board.model.Columns;
import com.hyuse.com.board.service.BoardService;
import com.hyuse.com.board.service.CardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class ConsoleController {

    @Autowired
    private BoardService boardService;

    @Autowired
    private CardService cardService;

    private Scanner scanner = new Scanner(System.in);

    public void startApplication() {
        boolean running = true;

        while (running) {
            System.out.println("\n===== TASK BOARD MANAGER =====");
            System.out.println("1. Criar novo board");
            System.out.println("2. Selecionar board");
            System.out.println("3. Excluir boards");
            System.out.println("4. Sair");
            System.out.print("Escolha uma opção: ");

            int option = getUserChoice();

            switch (option) {
                case 1:
                    createBoard();
                    break;
                case 2:
                    selectBoard();
                    break;
                case 3:
                    deleteBoards();
                    break;
                case 4:
                    System.out.println("Saindo do programa...");
                    running = false;
                    break;
                default:
                    System.out.println("Opção inválida!");
            }
        }
    }

    private int getUserChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void createBoard() {
        try {
            System.out.println("\n===== CRIAR NOVO BOARD =====");
            System.out.print("Digite o nome do board: ");
            String name = scanner.nextLine();

            List<Columns> columns = new ArrayList<>();

            // Create initial column
            System.out.print("Digite o nome da coluna inicial: ");
            String initialColumnName = scanner.nextLine();
            columns.add(new Columns(initialColumnName, 0, ColumnType.INITIAL));

            // Add pending columns
            int position = 1;
            boolean addMoreColumns = true;

            while (addMoreColumns && position < Integer.MAX_VALUE - 2) { // Leave room for FINAL and CANCELLED
                System.out.print("Deseja adicionar uma coluna intermediária? (S/N): ");
                if (scanner.nextLine().equalsIgnoreCase("S")) {
                    System.out.print("Digite o nome da coluna: ");
                    String colName = scanner.nextLine();
                    columns.add(new Columns(colName, position++, ColumnType.PENDING));
                } else {
                    addMoreColumns = false;
                }
            }

            // Create final column
            System.out.print("Digite o nome da coluna final (tarefas concluídas): ");
            String finalColumnName = scanner.nextLine();
            columns.add(new Columns(finalColumnName, position++, ColumnType.FINAL));

            // Create cancelled column
            System.out.print("Digite o nome da coluna de cancelamento: ");
            String cancelledColumnName = scanner.nextLine();
            columns.add(new Columns(cancelledColumnName, position, ColumnType.CANCELLED));

            // Create board
            Board board = boardService.createBoard(name, columns);
            System.out.println("Board \"" + board.getName() + "\" criado com sucesso!");

        } catch (Exception e) {
            System.out.println("Erro ao criar board: " + e.getMessage());
        }
    }

    private void selectBoard() {
        List<Board> boards = boardService.getAllBoards();

        if (boards.isEmpty()) {
            System.out.println("Não há boards disponíveis.");
            return;
        }

        System.out.println("\n===== SELECIONAR BOARD =====");
        for (int i = 0; i < boards.size(); i++) {
            System.out.println((i + 1) + ". " + boards.get(i).getName());
        }

        System.out.print("Selecione um board (0 para voltar): ");
        int choice = getUserChoice();

        if (choice > 0 && choice <= boards.size()) {
            Board selectedBoard = boards.get(choice - 1);
            boardMenu(selectedBoard);
        } else if (choice != 0) {
            System.out.println("Opção inválida!");
        }
    }
    private void deleteBoards() {
        List<Board> boards = boardService.getAllBoards();

        if (boards.isEmpty()) {
            System.out.println("Não há boards disponíveis para excluir.");
            return;
        }

        System.out.println("\n===== EXCLUIR BOARDS =====");
        for (int i = 0; i < boards.size(); i++) {
            System.out.println((i + 1) + ". " + boards.get(i).getName());
        }

        System.out.print("Selecione um board para excluir (0 para voltar): ");
        int choice = getUserChoice();

        if (choice > 0 && choice <= boards.size()) {
            Board selectedBoard = boards.get(choice - 1);
            System.out.print("Tem certeza que deseja excluir o board \"" + selectedBoard.getName() + "\"? (S/N): ");
            if (scanner.nextLine().equalsIgnoreCase("S")) {
                boardService.deleteBoard(selectedBoard.getId());
                System.out.println("Board excluído com sucesso!");
            }
        } else if (choice != 0) {
            System.out.println("Opção inválida!");
        }
    }

    private void boardMenu(Board board) {
        boolean managing = true;

        while (managing) {
            displayBoard(board);

            System.out.println("\n===== MENU DO BOARD: " + board.getName() + " =====");
            System.out.println("1. Criar card");
            System.out.println("2. Mover card para próxima coluna");
            System.out.println("3. Cancelar card");
            System.out.println("4. Bloquear card");
            System.out.println("5. Desbloquear card");
            System.out.println("6. Gerar relatório de tempo");
            System.out.println("7. Gerar relatório de bloqueios");
            System.out.println("8. Voltar ao menu principal");
            System.out.print("Escolha uma opção: ");

            int option = getUserChoice();

            switch (option) {
                case 1:
                    createCard(board);
                    break;
                case 2:
                    moveCard(board);
                    break;
                case 3:
                    cancelCard(board);
                    break;
                case 4:
                    blockCard(board);
                    break;
                case 5:
                    unblockCard(board);
                    break;
                case 6:
                    generateTimeReport(board);
                    break;
                case 7:
                    generateBlockReport(board);
                    break;
                case 8:
                    managing = false;
                    break;
                default:
                    System.out.println("Opção inválida!");
            }

            // Refresh board data from database
            if (managing) {
                Optional<Board> refreshedBoard = boardService.getBoardById(board.getId());
                if (refreshedBoard.isPresent()) {
                    board = refreshedBoard.get();
                } else {
                    System.out.println("O board foi excluído!");
                    managing = false;
                }
            }
        }
    }

    private void displayBoard(Board board) {
        System.out.println("\n===== BOARD: " + board.getName() + " =====");

        // Find the column with the most cards to calculate display height
        int maxCards = 0;
        for (Columns columns : board.getColumns()) {
            maxCards = Math.max(maxCards, columns.getCards().size());
        }

        // Print column headers
        for (Columns columns : board.getColumns()) {
            String header = columns.getName() + " (" + columns.getCards().size() + ")";
            System.out.print(padString(header, 20) + " | ");
        }
        System.out.println();

        for (Columns columns : board.getColumns()) {
            System.out.print(padString("", 20, '-') + " | ");
        }
        System.out.println();

        // Print cards
        for (int i = 0; i < maxCards; i++) {
            for (Columns columns : board.getColumns()) {
                if (i < columns.getCards().size()) {
                    Card card = columns.getCards().get(i);
                    String cardDisplay = "#" + card.getId() + ": " + truncateString(card.getTitle(), 13);
                    if (card.isBlocked()) {
                        cardDisplay += " [B]";
                    }
                    System.out.print(padString(cardDisplay, 20) + " | ");
                } else {
                    System.out.print(padString("", 20) + " | ");
                }
            }
            System.out.println();
        }
    }

    private String padString(String str, int length) {
        return padString(str, length, ' ');
    }

    private String padString(String str, int length, char padChar) {
        if (str.length() >= length) {
            return str;
        }

        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < length) {
            sb.append(padChar);
        }
        return sb.toString();
    }

    private String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    private void createCard(Board board) {
        try {
            System.out.println("\n===== CRIAR CARD =====");
            System.out.print("Digite o título do card: ");
            String title = scanner.nextLine();

            System.out.print("Digite a descrição do card: ");
            String description = scanner.nextLine();

            Columns initialColumns = board.getInitialColumn();
            Card card = cardService.createCard(title, description, initialColumns);

            System.out.println("Card \"" + card.getTitle() + "\" criado com sucesso na coluna " + initialColumns.getName() + "!");

        } catch (Exception e) {
            System.out.println("Erro ao criar card: " + e.getMessage());
        }
    }

    private Card selectCard(Board board) {
        System.out.println("\n===== SELECIONAR CARD =====");

        List<Card> allCards = new ArrayList<>();
        Map<Integer, Card> cardMap = new HashMap<>();

        int index = 1;
        for (Columns columns : board.getColumns()) {
            for (Card card : columns.getCards()) {
                allCards.add(card);
                cardMap.put(index++, card);
                System.out.println(index - 1 + ". [" + columns.getName() + "] #" + card.getId() + ": " + card.getTitle() +
                        (card.isBlocked() ? " [BLOCKED]" : ""));
            }
        }

        if (allCards.isEmpty()) {
            System.out.println("Não há cards disponíveis neste board.");
            return null;
        }

        System.out.print("Selecione um card (0 para voltar): ");
        int choice = getUserChoice();

        if (choice > 0 && choice < index) {
            return cardMap.get(choice);
        } else if (choice != 0) {
            System.out.println("Opção inválida!");
        }

        return null;
    }

    private void moveCard(Board board) {
        Card card = selectCard(board);

        if (card != null) {
            try {
                cardService.moveCardToNextColumn(card);
                System.out.println("Card #" + card.getId() + " movido com sucesso!");
            } catch (Exception e) {
                System.out.println("Erro ao mover card: " + e.getMessage());
            }
        }
    }

    private void cancelCard(Board board) {
        Card card = selectCard(board);

        if (card != null) {
            try {
                cardService.cancelCard(card);
                System.out.println("Card #" + card.getId() + " cancelado com sucesso!");
            } catch (Exception e) {
                System.out.println("Erro ao cancelar card: " + e.getMessage());
            }
        }
    }

    private void blockCard(Board board) {
        Card card = selectCard(board);

        if (card != null) {
            try {
                System.out.print("Digite o motivo do bloqueio: ");
                String reason = scanner.nextLine();

                cardService.blockCard(card, reason);
                System.out.println("Card #" + card.getId() + " bloqueado com sucesso!");
            } catch (Exception e) {
                System.out.println("Erro ao bloquear card: " + e.getMessage());
            }
        }
    }

    private void unblockCard(Board board) {
        // Filter only blocked cards
        List<Card> blockedCards = new ArrayList<>();
        Map<Integer, Card> cardMap = new HashMap<>();

        System.out.println("\n===== DESBLOQUEAR CARD =====");

        int index = 1;
        for (Columns columns : board.getColumns()) {
            for (Card card : columns.getCards()) {
                if (card.isBlocked()) {
                    blockedCards.add(card);
                    cardMap.put(index++, card);
                    System.out.println(index - 1 + ". [" + columns.getName() + "] #" + card.getId() + ": " + card.getTitle());
                }
            }
        }

        if (blockedCards.isEmpty()) {
            System.out.println("Não há cards bloqueados neste board.");
            return;
        }

        System.out.print("Selecione um card para desbloquear (0 para voltar): ");
        int choice = getUserChoice();

        if (choice > 0 && choice < index) {
            Card card = cardMap.get(choice);

            try {
                System.out.print("Digite o motivo do desbloqueio: ");
                String reason = scanner.nextLine();

                cardService.unblockCard(card, reason);
                System.out.println("Card #" + card.getId() + " desbloqueado com sucesso!");
            } catch (Exception e) {
                System.out.println("Erro ao desbloquear card: " + e.getMessage());
            }
        }
    }

    private void generateTimeReport(Board board) {
        System.out.println("\n===== RELATÓRIO DE TEMPO =====");

        try {
            Map<String, Object> report = cardService.generateTimeReport(board);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cardReports = (List<Map<String, Object>>) report.get("cards");

            if (cardReports.isEmpty()) {
                System.out.println("Não há dados disponíveis para gerar o relatório.");
                return;
            }

            System.out.println("Board: " + board.getName());
            System.out.println("---------------------------------------------");

            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            for (Map<String, Object> cardReport : cardReports) {
                System.out.println("Card #" + cardReport.get("cardId") + ": " + cardReport.get("title"));

                @SuppressWarnings("unchecked")
                Map<String, Duration> timeInColumns = (Map<String, Duration>) cardReport.get("timeInColumns");

                System.out.println("  Tempo em cada coluna:");
                for (Map.Entry<String, Duration> entry : timeInColumns.entrySet()) {
                    System.out.println("    " + entry.getKey() + ": " + formatDuration(entry.getValue()));
                }

                Duration totalTime = (Duration) cardReport.get("totalTime");
                System.out.println("  Tempo total: " + formatDuration(totalTime));
                System.out.println("---------------------------------------------");
            }

        } catch (Exception e) {
            System.out.println("Erro ao gerar relatório: " + e.getMessage());
        }
    }

    private void generateBlockReport(Board board) {
        System.out.println("\n===== RELATÓRIO DE BLOQUEIOS =====");

        try {
            Map<String, Object> report = cardService.generateBlockReport(board);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cardReports = (List<Map<String, Object>>) report.get("cards");

            if (cardReports.isEmpty()) {
                System.out.println("Não há dados disponíveis para gerar o relatório.");
                return;
            }

            System.out.println("Board: " + board.getName());
            System.out.println("---------------------------------------------");

            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            for (Map<String, Object> cardReport : cardReports) {
                System.out.println("Card #" + cardReport.get("cardId") + ": " + cardReport.get("title"));

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> blockEvents = (List<Map<String, Object>>) cardReport.get("blockEvents");

                System.out.println("  Histórico de bloqueios:");
                for (Map<String, Object> event : blockEvents) {
                    System.out.println("    Bloqueado em: " + event.get("blockTime"));
                    System.out.println("    Motivo: " + event.get("blockReason"));

                    if (event.get("unblockTime") != null) {
                        System.out.println("    Desbloqueado em: " + event.get("unblockTime"));
                        System.out.println("    Motivo do desbloqueio: " + event.get("unblockReason"));
                    } else {
                        System.out.println("    Status: Ainda bloqueado");
                    }

                    System.out.println("    Duração: " + formatDuration((Duration) event.get("duration")));
                    System.out.println();
                }

                System.out.println("  Total de bloqueios: " + cardReport.get("totalBlockCount"));
                System.out.println("  Tempo total bloqueado: " + formatDuration((Duration) cardReport.get("totalBlockTime")));
                System.out.println("---------------------------------------------");
            }

        } catch (Exception e) {
            System.out.println("Erro ao gerar relatório: " + e.getMessage());
        }
    }

    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days).append(" dia(s) ");
        }

        sb.append(String.format("%02d:%02d:%02d", hours, minutes, seconds));

        return sb.toString();
    }
}