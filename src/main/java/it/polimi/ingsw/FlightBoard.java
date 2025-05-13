package it.polimi.ingsw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the flight board that tracks player positions during the flight phase
 * of Galaxy Trucker.
 */
public class FlightBoard {
    /**
     * Map that stores each player's position on the flight board
     */
    private Map<Player, Integer> positions;

    /**
     * Constructor for the FlightBoard
     */
    public FlightBoard() {
        this.positions = new HashMap<>();
    }

    /**
     * Adds a player to the flight board at the starting position (0)
     *
     * @param player The player to add to the board
     */
    public void addPlayer(Player player) {
        positions.put(player, 0); // Default to 0, will be updated by setInitialPosition
    }

    /**
     * Sets the initial position for a player on the flight board.
     * The position must be between 1 and 4 (inclusive) and not occupied.
     *
     * @param player The player choosing the position.
     * @param position The chosen position (1-4).
     * @return true if the position was successfully set, false otherwise.
     */
    public boolean setInitialPosition(Player player, int position) {
        if (position < 1 || position > 4) {
            System.out.println("Invalid position. Please choose a position between 1 and 4.");
            return false;
        }

        // Check if the position is occupied by any other active player
        for (Map.Entry<Player, Integer> entry : positions.entrySet()) {
            // We are checking if *any* player is in that spot.
            // The player attempting to set the position shouldn't already have a non-zero position at this stage.
            if (!entry.getKey().equals(player) && entry.getValue() == position && entry.getKey().isActive()) {
                System.out.println("Position " + position + " is already occupied by " + entry.getKey().getNickName());
                return false;
            }
        }

        positions.put(player, position);
        System.out.println(player.getNickName() + " has chosen starting position: " + position);
        return true;
    }

    /**
     * Gets a list of available starting positions (1-4).
     * A position is available if it's not occupied by any active player.
     *
     * @return A list of integers representing available starting positions.
     */
    public List<Integer> getAvailableStartingPositions() {
        List<Integer> availablePositions = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            boolean occupied = false;
            for (Map.Entry<Player, Integer> entry : positions.entrySet()) {
                // Consider a position occupied if an active player is there and their position is not the default 0
                if (entry.getValue() == i && entry.getKey().isActive() && positions.get(entry.getKey()) != 0) {
                    occupied = true;
                    break;
                }
            }
            if (!occupied) {
                availablePositions.add(i);
            }
        }
        return availablePositions;
    }

    /**
     * Verifica se una posizione è già occupata da un altro giocatore
     *
     * @param position La posizione da verificare
     * @param currentPlayer Il giocatore attuale (da escludere dal controllo)
     * @return true se la posizione è occupata, false altrimenti
     */
    public boolean isPositionOccupied(int position, Player currentPlayer) {
        for (Map.Entry<Player, Integer> entry : positions.entrySet()) {
            Player player = entry.getKey();
            int playerPosition = entry.getValue();

            // Se non è il giocatore corrente e occupa la posizione richiesta
            // Also ensure the player occupying is active and not at the default unassigned position (0)
            if (!player.equals(currentPlayer) && playerPosition == position && player.isActive() && playerPosition != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Trova la prima posizione disponibile a partire da una posizione iniziale
     *
     * @param startPosition La posizione iniziale
     * @param currentPlayer Il giocatore attuale
     * @return La prima posizione disponibile
     */
    private int findFirstAvailablePosition(int startPosition, Player currentPlayer) {
        int position = startPosition;

        // Trova la prima posizione non occupata (e greater than 0)
        while (position > 0 && isPositionOccupied(position, currentPlayer)) {
            position++; // This logic might need review for edge cases if players can be pushed back to 0
        }
        // If after incrementing, it's still occupied, or if the initial check was for pos 0.
        // This method is generally for forward movement.
        // For setting initial positions, getAvailableStartingPositions and setInitialPosition are more direct.

        return position;
    }

    /**
     * Aggiorna la posizione di un giocatore sul tabellone di volo
     * considerando le posizioni già occupate
     *
     * @param player Il giocatore la cui posizione deve essere aggiornata
     * @param spaces Il numero di spazi da muovere (può essere positivo o negativo)
     */
    public void updatePosition(Player player, int spaces) {
        if (!positions.containsKey(player)) {
            throw new IllegalArgumentException("Il giocatore non è presente sul tabellone di volo");
        }

        int currentPosition = positions.get(player);
        int newPosition = currentPosition + spaces;

        // Applica la posizione minima (0)
        if (newPosition < 0) {
            newPosition = 0;
        }

        // Verifica se la nuova posizione è occupata
        if (newPosition > 0 && isPositionOccupied(newPosition, player)) {
            // Trova la prima posizione disponibile
            newPosition = findFirstAvailablePosition(newPosition, player);
        }

        positions.put(player, newPosition);
    }

    /**
     * Aggiorna le posizioni di tutti i giocatori in ordine di posizione attuale
     * (dal più avanzato al meno avanzato)
     *
     * @param playerMoves Mappa che contiene i giocatori e i loro movimenti
     */
    public void updatePositionsInOrder(Map<Player, Integer> playerMoves) {
        // Ottiene i giocatori in ordine di posizione decrescente
        List<Player> orderedPlayers = getPlayersInFlightOrder();

        // Aggiorna le posizioni nell'ordine corretto
        for (Player player : orderedPlayers) {
            if (playerMoves.containsKey(player) && player.isActive()) {
                updatePosition(player, playerMoves.get(player));
            }
        }
    }

    /**
     * Ottiene la posizione attuale di un giocatore on the board
     *
     * @param player Il giocatore di cui recuperare la posizione
     * @return La posizione attuale del giocatore
     */
    public int getPosition(Player player) {
        if (!positions.containsKey(player)) {
            throw new IllegalArgumentException("Il giocatore non è presente sul tabellone di volo");
        }

        return positions.get(player);
    }

    /**
     * Ottiene i giocatori in ordine della loro posizione (il più lontano per primo)
     *
     * @return Lista di giocatori ordinati per posizione
     */
    public List<Player> getPlayersInFlightOrder() {
        List<Player> sortedPlayers = new ArrayList<>(positions.keySet());
        // Filter out players at position 0 (uninitialized) before sorting for flight order
        sortedPlayers.removeIf(p -> positions.get(p) == 0 && p.isActive());
        sortedPlayers.sort((p1, p2) -> {
            // Handle cases where a player might not be active or not yet positioned
            Integer pos1 = positions.getOrDefault(p1, 0);
            Integer pos2 = positions.getOrDefault(p2, 0);
            return pos2.compareTo(pos1); // Descending order
        });
        return sortedPlayers;
    }

    /**
     * Checks if a player has reached a specific position on the board
     *
     * @param player The player to check
     * @param position The position to check against
     * @return true if the player has reached or passed the position, false otherwise
     */
    public boolean hasReachedPosition(Player player, int position) {
        if (!positions.containsKey(player)) {
            throw new IllegalArgumentException("Player is not on the flight board");
        }

        return positions.get(player) >= position;
    }

    /**
     * Gets the player with the most advanced position on the board
     *
     * @return The player in the lead, or null if the board is empty
     */
    public Player getLeadingPlayer() {
        if (positions.isEmpty()) {
            return null;
        }

        Player leadingPlayer = null;
        int maxPosition = -1;

        for (Map.Entry<Player, Integer> entry : positions.entrySet()) {
            if (entry.getKey().isActive() && entry.getValue() > maxPosition && entry.getValue() != 0) { // Ensure player is active and not at pos 0
                maxPosition = entry.getValue();
                leadingPlayer = entry.getKey();
            }
        }

        return leadingPlayer;
    }

    /**
     * Gets the current positions of all players
     *
     * @return A map with players as keys and their positions as values
     */
    public Map<Player, Integer> getAllPositions() {
        // Return a copy to prevent external modification
        return new HashMap<>(positions);
    }

    /**
     * Resets all players to the starting position (0)
     */
    public void resetPositions() {
        for (Player player : positions.keySet()) {
            positions.put(player, 0);
        }
    }

    /**
     * Removes a player from the flight board
     *
     * @param player The player to remove
     */
    public void removePlayer(Player player) {
        positions.remove(player);
    }

    /**
     * Ottiene il giocatore in ultima posizione
     *
     * @return Il giocatore con la posizione più bassa
     */
    public Player getLastPlayer() {
        if (positions.isEmpty()) {
            return null;
        }

        Player lastPlayer = null;
        int minPosition = Integer.MAX_VALUE;

        for (Map.Entry<Player, Integer> entry : positions.entrySet()) {
            // Ensure player is active and actually on the board (not at pos 0)
            if (entry.getKey().isActive() && entry.getValue() < minPosition && entry.getValue() != 0) {
                minPosition = entry.getValue();
                lastPlayer = entry.getKey();
            }
        }

        return lastPlayer;
    }

    /**
     * Ottiene i giocatori in una posizione specifica sul tabellone
     *
     * @param position La posizione da controllare
     * @return Lista di giocatori in quella posizione
     */
    public Player getPlayerAtPosition(int position) {
        for (Map.Entry<Player, Integer> entry : positions.entrySet()) {
            // Ensure player is active and at the specified position (and position is not 0)
            if (entry.getValue() == position && entry.getKey().isActive() && position != 0) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Calcola i bonus di posizione di volo per il punteggio in base al round
     *
     * @param roundNumber Il numero del round attuale (1-3)
     * @return Mappa dei giocatori con i loro punti bonus per la posizione di volo
     */
    public Map<Player, Integer> calculateFlightBonuses(int roundNumber) {
        Map<Player, Integer> bonuses = new HashMap<>();
        List<Player> finishedPlayers = getPlayersInFlightOrder(); // Use the updated method

        // Il primo giocatore ottiene il bonus più grande, e così via
        for (int i = 0; i < finishedPlayers.size(); i++) {
            Player player = finishedPlayers.get(i);
            int bonus = 0;

            // I bonus dipendono dal numero di round e dalla posizione
            switch (roundNumber) {
                case 1:
                    // Esempio: 1° ottiene 4, 2° ottiene 2, 3° ottiene 1, il resto ottiene 0
                    if (i == 0) bonus = 4;
                    else if (i == 1) bonus = 2;
                    else if (i == 2) bonus = 1;
                    break;
                case 2:
                    // Esempio: 1° ottiene 6, 2° ottiene 4, 3° ottiene 2, 4° ottiene 1
                    if (i == 0) bonus = 6;
                    else if (i == 1) bonus = 4;
                    else if (i == 2) bonus = 2;
                    else if (i == 3) bonus = 1;
                    break;
                case 3:
                    // Esempio: 1° ottiene 8, 2° ottiene 6, 3° ottiene 4, 4° ottiene 2, 5° ottiene 1
                    if (i == 0) bonus = 8;
                    else if (i == 1) bonus = 6;
                    else if (i == 2) bonus = 4;
                    else if (i == 3) bonus = 2;
                    else if (i == 4) bonus = 1;
                    break;
            }

            bonuses.put(player, bonus);
        }

        return bonuses;
    }

}