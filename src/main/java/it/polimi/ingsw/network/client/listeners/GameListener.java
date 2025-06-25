package it.polimi.ingsw.network.client.listeners;


import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.Difficulty;
import it.polimi.ingsw.network.messages.RotationType;
import it.polimi.ingsw.Position;

import java.util.List;
import java.util.UUID;

public interface GameListener {
    
    void playerCreated(int playerId, String playerName);
    
    void displayRefresh();
    void componentPlaced(Component component, Position position);
    void componentRemoved(Position position);
    void componentRotated(int playerId, Position position, RotationType rotationType, boolean isValid);
    void shipValidated(boolean isValid, String message);
    boolean notifyNewPlayerPosition(int playerId, int position);

    void printTextMsg(String message);

    void matchCreated(UUID matchId, String matchName, String playerName, int playerId, Difficulty difficulty);
    void playerJoinedMatch(UUID matchId, String playerName);
    void playerLeftMatch(UUID matchId, int playerId, String playerName);
    void updateAvailableMatches(List<String> matchNames);
    void updateHost(UUID matchId, int playerId, String playerName);
    void matchStarted(UUID matchId, String username);
    void loadingStatus(UUID matchId, int playersReady, int totalPlayers);
    void playersInMatch(UUID matchId, List<String> players, int hostId);
    void playerReady(UUID matchId, int readyPlayers, int totalPlayers);
    void shipBuildingStart(UUID matchId, int readyPlayers);
    void usernameRejected(String errorMessage);
    void componentPicked(int playerId, Component component, boolean success, String message);


    void cabinColorSelected(String color);
    void flightPreparationStart(UUID matchId);
    void allPlayersReadyForFlight(UUID matchId);
    void playerReadyForFlight(UUID matchId, int playerId, String playerName, boolean shipValid);

}