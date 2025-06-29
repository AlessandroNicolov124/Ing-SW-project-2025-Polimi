package it.polimi.ingsw.network.client.listeners;


import it.polimi.ingsw.AdventureCards.AdventureCard;
import it.polimi.ingsw.AdventureCards.Planets;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.Difficulty;
import it.polimi.ingsw.network.messages.RotationType;
import it.polimi.ingsw.Position;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface GameListener {
    
    void playerCreated(int playerId, String playerName);
    void usernameRejected(String errorMessage);
    
    void displayRefresh();

    void componentPlaced(Component component, Position position);
    void componentPlacedFailed(String errorMessage);

    void componentRemoved(Position position);
    void componentRemovedFailed(String errorMessage);

    void componentRotated(int playerId, Position position, RotationType rotationType, boolean isValid);
    void componentRotatedFailed(String errorMessage);

    void shipValidated(boolean isValid, String message);
    void shipValidationFailed(String errorMessage);

    boolean notifyNewPlayerPosition(int playerId, int position);
    void notifyNewPlayerPositionFailed(String errorMessage);

    void positionAssigned(int playerId, String playerName, int assignedPosition, boolean success, String message);

    void printTextMsg(String message);

    void matchCreated(UUID matchId, String matchName, String playerName, int playerId, Difficulty difficulty);
    void matchCreatedFailed(String errorMessage);

    void playerJoinedMatch(UUID matchId, String playerName);
    void playerJoinedMatchFailed(String errorMessage);

    void playerLeftMatch(UUID matchId, int playerId, String playerName);
    void playerLeftMatchFailed(String errorMessage);

    void updateAvailableMatches(List<String> matchNames);
    void updateAvailableMatchesFailed(String errorMessage);

    void updateHost(UUID matchId, int playerId, String playerName);
    void updateHostFailed(String errorMessage);

    void matchStarted(UUID matchId, String username);
    void matchStartFailed(String errorMessage);

    void loadingAckResponseFailed(String errorMessage);

    void loadingStatus(UUID matchId, int playersReady, int totalPlayers);
    void loadingStatusFailed(String errorMessage);

    void playersInMatch(UUID matchId, List<String> players, int hostId);
    void playersInMatchFailed(String errorMessage);

    void playerReady(UUID matchId, int readyPlayers, int totalPlayers);
    void playerReadyFailed(String errorMessage);

    void shipBuildingStart(UUID matchId, int readyPlayers);
    void shipBuildingStartFailed(String errorMessage);

    void componentPicked(int playerId, Component component, boolean success, String message);
    void componentPickFailed(String errorMessage);

    void cabinColorSelected(String color);
    void cabinColorSelectionFailed(String errorMsg);
    
    void flightPreparationStart(UUID matchId);
    void flightPreparationStartFailed(String errorMessage);

    void playerReadyForFlight(UUID matchId, int playerId, String playerName, boolean shipValid);
    void playerReadyForFlightFailed(String errorMessage);

    void adventurePhaseStarted(java.util.UUID matchId, java.util.Map<Integer, Integer> playerPositions, int firstPlayerToAct);
    void adventurePhaseComplete(java.util.UUID matchId, boolean gameComplete, String completionReason);

    /**
     * Called when an adventure card is picked and positions may have changed
     * @param playerId The player who picked the card
     * @param adventureCard The adventure card that was picked
     * @param success Whether the card pick was successful
     * @param message Any message associated with the pick
     * @param nextPlayerTurn Which player should pick next
     * @param updatedPositions Current positions after card effects (may be null)
     */
    void adventureCardPicked(int playerId, AdventureCard adventureCard, boolean success, String message, int nextPlayerTurn, java.util.Map<Integer, Integer> updatedPositions);

    /**
     * Called when a Planets card is drawn and players need to choose planets
     * @param matchId The match ID
     * @param availablePlanets List of available planets to choose from
     * @param flightDaysCost Cost in flight days for landing on a planet
     * @param currentPlayerId ID of the player who should choose next
     * @param currentPlayerName Name of the player who should choose next
     */
    void planetChoiceRequested(UUID matchId, List<Planets.Planet> availablePlanets, int flightDaysCost, int currentPlayerId, String currentPlayerName);

    /**
     * Called when a player selects a planet
     * @param playerId The player who made the selection
     * @param playerName Name of the player
     * @param planetIndex Index of the chosen planet (-1 if no planet chosen)
     * @param planetName Name of the chosen planet (null if no planet chosen)
     * @param success Whether the selection was successful
     * @param message Any message associated with the selection
     */
    void planetSelected(int playerId, String playerName, int planetIndex, String planetName, boolean success, String message);
}
