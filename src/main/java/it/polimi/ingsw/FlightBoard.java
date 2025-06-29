package it.polimi.ingsw;

import java.util.*;

/**
 * Represents the flight board for Galaxy Trucker with 24 positions (0-23) in a circular track.
 * Position system: Higher numbers = better positions, Lower numbers = worse positions
 * Initial positions: 1st place = position 6, 2nd = position 3, 3rd = position 1, 4th = position 0
 * Flight day costs move players to lower position numbers (worse positions)
 * 
 * Wrap-around behavior: If players go below position 0, they wrap around to the end of the track
 * (e.g., from position 0 losing 2 flight days goes to position 22, losing 3 goes to position 21, etc.)
 */
public class FlightBoard {

    /**
     * Map of players to their current flight board positions (0-23)
     * Position 0-23: Lower numbers are better positions
     */
    private Map<Player, Integer> positions;

    /**
     * Constructor
     */
    public FlightBoard() {
        positions = new HashMap<>();
    }

    /**
     * Aggiunge un giocatore al tabellone di volo nella posizione di default (-1 = non posizionato)
     *
     * @param player Il giocatore da aggiungere
     */
    public void addPlayer(Player player) {
        if (!positions.containsKey(player)) {
            positions.put(player, -1); // -1 indicates not yet positioned
            System.out.println("[FlightBoard] Player " + player.getNickName() + " added to flight board (not positioned yet)");
        }
    }

    /**
     * Sets the initial starting position for a player based on their completion order.
     * Position mapping: 1st finisher → position 6, 2nd → position 3, 3rd → position 1, 4th → position 0
     *
     * @param player The player to position
     * @param finishOrder The order in which the player finished ship building (1-4)
     * @return true if the position was successfully set, false otherwise.
     */
    public boolean setInitialPosition(Player player, int finishOrder) {
        if (finishOrder < 1 || finishOrder > 4) {
            System.out.println("Invalid finish order. Must be between 1 and 4.");
            return false;
        }

        // Map finish order to flight board position
        int flightPosition = getFlightPositionFromFinishOrder(finishOrder);
        
        // Check if the position is already occupied
        if (isPositionOccupied(flightPosition, player)) {
            System.out.println("Position " + flightPosition + " (finish order " + finishOrder + ") is already occupied");
            return false;
        }

        positions.put(player, flightPosition);
        System.out.println(player.getNickName() + " finished " + getOrdinalString(finishOrder) + 
                          " and is placed at flight position " + flightPosition);
        return true;
    }

    /**
     * Maps finish order to flight board position
     * @param finishOrder 1-4 (1st, 2nd, 3rd, 4th)
     * @return flight board position (6, 3, 1, 0)
     */
    private int getFlightPositionFromFinishOrder(int finishOrder) {
        switch (finishOrder) {
            case 1: return 6; // 1st finisher gets position 6 (best)
            case 2: return 3; // 2nd finisher gets position 3
            case 3: return 1; // 3rd finisher gets position 1
            case 4: return 0; // 4th finisher gets position 0 (worst)
            default: throw new IllegalArgumentException("Invalid finish order: " + finishOrder);
        }
    }

    /**
     * Gets available finish orders (1-4) that haven't been assigned yet
     */
    public List<Integer> getAvailableStartingPositions() {
        List<Integer> availableOrders = new ArrayList<>();
        
        // Check which finish orders are still available
        for (int order = 1; order <= 4; order++) {
            int flightPosition = getFlightPositionFromFinishOrder(order);
            if (!isPositionOccupied(flightPosition, null)) {
                availableOrders.add(order);
            }
        }
        
        return availableOrders;
    }

    /**
     * Checks if a specific flight board position is occupied
     */
    public boolean isPositionOccupied(int flightPosition, Player excludePlayer) {
        for (Map.Entry<Player, Integer> entry : positions.entrySet()) {
            Player player = entry.getKey();
            int playerPosition = entry.getValue();

            // Skip the excluded player and inactive players
            if (player.equals(excludePlayer) || !player.isActive()) {
                continue;
            }
            
            // Check if this position is occupied (and player is positioned, not -1)
            if (playerPosition == flightPosition && playerPosition != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the next available position when moving backwards (lower position numbers)
     * Used for flight day costs - finds next available position in the direction of movement
     * Supports wrap-around: positions below 0 continue from position 23
     */
    private int findNextAvailablePosition(int startPosition, Player movingPlayer) {
        // When losing flight days, we move to lower position numbers (worse positions)
        int position = startPosition;
        int maxPosition = 23; // Flight board has positions 0-23
        
        // Search for next available position going towards worse positions (lower numbers)
        // Check positions from startPosition down to 0, then wrap around from 23 downwards
        for (int i = 0; i < maxPosition + 1; i++) {
            // If position goes below 0, wrap around to the end of the track
            if (position < 0) {
                position = maxPosition + position + 1; // Wrap around (e.g., -1 becomes 23, -2 becomes 22)
            }
            
            // If this position is not occupied, we found our spot
            if (!isPositionOccupied(position, movingPlayer)) {
                return position;
            }
            
            // Move to next position backwards
            position--;
        }
        
        // If all positions are somehow occupied (shouldn't happen), return the original start position
        return startPosition;
    }

    /**
     * Updates a player's position, handling flight day costs and collisions
     * Supports wrap-around: if a player goes below position 0, they wrap around to position 23, 22, etc.
     * @param player The player whose position to update
     * @param flightDaysCost Flight days to lose (positive number = move to worse position)
     */
    public void updatePosition(Player player, int flightDaysCost) {
        if (!positions.containsKey(player)) {
            throw new IllegalArgumentException("Player is not on the flight board");
        }

        int currentPosition = positions.get(player);
        if (currentPosition == -1) {
            throw new IllegalArgumentException("Player has not been positioned yet");
        }

        // Calculate new position (losing flight days = moving to lower numbers = worse positions)
        int targetPosition = currentPosition - flightDaysCost;
        
        // Allow negative positions - the findNextAvailablePosition method will handle wrap-around
        // Ensure we don't go better than the best position (23)
        if (targetPosition > 23) {
            targetPosition = 23;
        }

        // Find next available position if target is occupied (handles wrap-around automatically)
        int finalPosition = findNextAvailablePosition(targetPosition, player);
        
        System.out.println("[FlightBoard] " + player.getNickName() + " moves from position " + 
                          currentPosition + " to position " + finalPosition + 
                          " (lost " + flightDaysCost + " flight days)" + 
                          (finalPosition > currentPosition ? " - wrapped around!" : ""));
        
        positions.put(player, finalPosition);
    }

    /**
     * Updates multiple players' positions in reverse order (worst position moves first)
     * This implements the rule: "If multiple players lose flight days at the same time, 
     * they lose them in reverse order (farthest behind moves back first)"
     */
    public void updatePositionsInReverseOrder(Map<Player, Integer> playerFlightDayCosts) {
        // Get players ordered from worst position to best position (highest to lowest position numbers)
        List<Player> playersInReverseOrder = getPlayersInReverseFlightOrder();
        
        // Update positions in reverse order (worst position player moves first)
        for (Player player : playersInReverseOrder) {
            if (playerFlightDayCosts.containsKey(player) && player.isActive()) {
                int flightDaysCost = playerFlightDayCosts.get(player);
                updatePosition(player, flightDaysCost);
            }
        }
    }

    /**
     * Gets players in flight order (leader first, then others in order)
     * Used for adventure card picking, planet selection, and determining turn order
     * The leader (highest position number) chooses first, then others follow
     * @return List of players ordered by position (highest position first = leader first)
     */
    public List<Player> getPlayersInFlightOrder() {
        List<Player> sortedPlayers = new ArrayList<>();
        
        // Only include positioned, active players
        for (Map.Entry<Player, Integer> entry : positions.entrySet()) {
            Player player = entry.getKey();
            int position = entry.getValue();
            if (player.isActive() && position != -1) {
                sortedPlayers.add(player);
            }
        }
        
        // Sort by position: higher position numbers = better positions = leader first
        // Leader (highest position) chooses first in adventure cards and planet selection
        sortedPlayers.sort((p1, p2) -> {
            Integer pos1 = positions.get(p1);
            Integer pos2 = positions.get(p2);
            return pos2.compareTo(pos1); // Descending order (23, 22, 21, ... 0)
        });
        
        return sortedPlayers;
    }

    /**
     * Gets players in reverse flight order (worst position to best position)
     * Used for applying flight day costs in correct order
     * @return List of players ordered from worst to best position (position 23 first, position 0 last)
     */
    public List<Player> getPlayersInReverseFlightOrder() {
        List<Player> sortedPlayers = new ArrayList<>();
        
        // Only include positioned, active players
        for (Map.Entry<Player, Integer> entry : positions.entrySet()) {
            Player player = entry.getKey();
            int position = entry.getValue();
            if (player.isActive() && position != -1) {
                sortedPlayers.add(player);
            }
        }
        
        // Sort by position: higher position numbers = worse positions = earlier in list
        sortedPlayers.sort((p1, p2) -> {
            Integer pos1 = positions.get(p1);
            Integer pos2 = positions.get(p2);
            return pos2.compareTo(pos1); // Descending order (23, 22, 21, ... 0)
        });
        
        return sortedPlayers;
    }

    /**
     * Gets the current position of a player
     */
    public int getPosition(Player player) {
        if (!positions.containsKey(player)) {
            throw new IllegalArgumentException("Player is not on the flight board");
        }
        return positions.get(player);
    }

    /**
     * Gets the leading player (player with the highest position number = best flight position)
     * This player is in 1st place
     */
    public Player getLeadingPlayer() {
        if (positions.isEmpty()) {
            return null;
        }

        Player leadingPlayer = null;
        int bestPosition = -1;

        for (Map.Entry<Player, Integer> entry : positions.entrySet()) {
            Player player = entry.getKey();
            int position = entry.getValue();
            
            // Only consider active, positioned players
            // Leading player = highest position number (best flight position)
            if (player.isActive() && position != -1 && position > bestPosition) {
                bestPosition = position;
                leadingPlayer = player;
            }
        }

        return leadingPlayer;
    }

    /**
     * Gets the adventure card picker (player with the highest position number = best flight position)
     * This player picks adventure cards that affect everyone - the leading player picks cards
     */
    public Player getAdventureCardPicker() {
        if (positions.isEmpty()) {
            return null;
        }

        Player cardPicker = null;
        int bestPosition = -1;

        for (Map.Entry<Player, Integer> entry : positions.entrySet()) {
            Player player = entry.getKey();
            int position = entry.getValue();
            
            // Only consider active, positioned players
            // Card picker = highest position number (best flight position = leading player)
            if (player.isActive() && position != -1 && position > bestPosition) {
                bestPosition = position;
                cardPicker = player;
            }
        }

        return cardPicker;
    }

    /**
     * Gets the player with the best race position (lowest position number)
     * This is used for determining final rankings and winners in the flight race
     * Note: This is different from adventure card picking where highest position = leader
     */
    public Player getBestPositionPlayer() {
        if (positions.isEmpty()) {
            return null;
        }

        Player bestPlayer = null;
        int bestPosition = Integer.MAX_VALUE;

        for (Map.Entry<Player, Integer> entry : positions.entrySet()) {
            Player player = entry.getKey();
            int position = entry.getValue();
            
            // Only consider active, positioned players
            // Best race position = lowest position number (closer to finish line)
            if (player.isActive() && position != -1 && position < bestPosition) {
                bestPosition = position;
                bestPlayer = player;
            }
        }

        return bestPlayer;
    }

    /**
     * Gets the player furthest behind in the race (highest position number)
     * Note: This is the same player who leads adventure card picking
     */
    public Player getLastPlayer() {
        if (positions.isEmpty()) {
            return null;
        }

        Player lastPlayer = null;
        int worstPosition = -1;

        for (Map.Entry<Player, Integer> entry : positions.entrySet()) {
            Player player = entry.getKey();
            int position = entry.getValue();
            
            // Only consider active, positioned players
            // Highest position number = furthest behind in race = adventure card leader
            if (player.isActive() && position != -1 && position > worstPosition) {
                worstPosition = position;
                lastPlayer = player;
            }
        }

        return lastPlayer;
    }

    /**
     * Gets the player at a specific position (null if no one is there)
     */
    public Player getPlayerAtPosition(int position) {
        for (Map.Entry<Player, Integer> entry : positions.entrySet()) {
            Player player = entry.getKey();
            int playerPosition = entry.getValue();
            
            if (player.isActive() && playerPosition == position && position != -1) {
                return player;
            }
        }
        return null;
    }

    /**
     * Gets all current positions
     */
    public Map<Player, Integer> getAllPositions() {
        return new HashMap<>(positions);
    }

    /**
     * Checks if a player has reached a specific position or better
     */
    public boolean hasReachedPosition(Player player, int position) {
        if (!positions.containsKey(player)) {
            throw new IllegalArgumentException("Player is not on the flight board");
        }
        
        int playerPosition = positions.get(player);
        return playerPosition != -1 && playerPosition <= position; // Lower or equal position = reached or better
    }

    /**
     * Resets all players to unpositioned state
     */
    public void resetPositions() {
        for (Player player : positions.keySet()) {
            positions.put(player, -1);
        }
    }

    /**
     * Removes a player from the flight board
     */
    public void removePlayer(Player player) {
        positions.remove(player);
    }

    /**
     * Calculate flight bonuses based on final positions
     */
    public Map<Player, Integer> calculateFlightBonuses(int roundNumber) {
        Map<Player, Integer> bonuses = new HashMap<>();
        List<Player> playersInOrder = getPlayersInFlightOrder(); // Best to worst
        
        // Award bonuses based on final position
        for (int i = 0; i < playersInOrder.size(); i++) {
            Player player = playersInOrder.get(i);
            int bonus = 0;

            // Bonuses depend on round number and position
            switch (roundNumber) {
                case 1:
                    if (i == 0) bonus = 4;      // 1st place
                    else if (i == 1) bonus = 2; // 2nd place
                    else if (i == 2) bonus = 1; // 3rd place
                    break;
                case 2:
                    if (i == 0) bonus = 6;      // 1st place
                    else if (i == 1) bonus = 4; // 2nd place
                    else if (i == 2) bonus = 2; // 3rd place
                    else if (i == 3) bonus = 1; // 4th place
                    break;
                case 3:
                    if (i == 0) bonus = 8;      // 1st place
                    else if (i == 1) bonus = 6; // 2nd place
                    else if (i == 2) bonus = 4; // 3rd place
                    else if (i == 3) bonus = 2; // 4th place
                    else if (i == 4) bonus = 1; // 5th place
                    break;
            }

            bonuses.put(player, bonus);
        }

        return bonuses;
    }

    /**
     * Helper method to convert numbers to ordinal strings
     */
    private String getOrdinalString(int number) {
        switch (number) {
            case 1: return "1st";
            case 2: return "2nd";
            case 3: return "3rd";
            case 4: return "4th";
            default: return number + "th";
        }
    }

    /**
     * Updates positions in normal order (best position to worst position)
     * Used for general position updates that don't require reverse order
     */
    public void updatePositionsInOrder(Map<Player, Integer> playerMoves) {
        List<Player> orderedPlayers = getPlayersInFlightOrder();
        
        for (Player player : orderedPlayers) {
            if (playerMoves.containsKey(player) && player.isActive()) {
                updatePosition(player, playerMoves.get(player));
            }
        }
    }
}