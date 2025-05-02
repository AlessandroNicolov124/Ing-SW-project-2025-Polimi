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
        positions.put(player, 0);
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
            if (!player.equals(currentPlayer) && playerPosition == position && player.isActive()) {
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

        // Trova la prima posizione non occupata
        while (position > 0 && isPositionOccupied(position, currentPlayer)) {
            position++;
        }

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
        if (isPositionOccupied(newPosition, player)) {
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
        sortedPlayers.sort((p1, p2) -> positions.get(p2) - positions.get(p1));
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
            if (entry.getValue() > maxPosition) {
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
            if (entry.getKey().isActive() && entry.getValue() < minPosition) {
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
            if (entry.getValue() == position && entry.getKey().isActive()) {
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
        List<Player> finishedPlayers = positions.keySet().stream().toList();

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