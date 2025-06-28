package it.polimi.ingsw.network.client;

import it.polimi.ingsw.Components.Component;

import java.util.UUID;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.Difficulty;
import it.polimi.ingsw.network.messages.ResponseMessage;
import it.polimi.ingsw.network.messages.RotationType;

public interface GeneralClientIf extends AutoCloseable {
    
    void createPlayer(String username) throws Exception;

    void showUpdate(ResponseMessage message) throws Exception;

    void createMatch(String matchName, int playerId, String username, Difficulty difficulty) throws Exception;

    void getAvailableMatches() throws Exception;

    void startMatch(UUID matchId, int playerId, String username) throws Exception;

    void joinMatch(UUID matchId, int playerId, String username) throws Exception;

    void leaveMatch(UUID matchId, int playerId, String username) throws Exception;

    void placeComponent(Component component, Position position, int playerId) throws Exception;

    void removeComponent(Position position, int playerId) throws Exception;

    void validateShip(int playerId) throws Exception;

    void rotateComponent(int playerId, Position position, RotationType rotationType) throws Exception;

    void pickComponentCard(int playerId) throws Exception;

    void loadingAck(UUID matchId) throws Exception;

    void playerReady(UUID matchId, int playerId) throws Exception;
    
    void pickAdventureCard(int currentPlayer) throws Exception;

    void selectCabinColor(String color, int playerId) throws Exception;

    void readyForFlight(UUID matchId, int playerId) throws Exception;

    void getPlayersOrderForFlightboard(UUID matchId, int playerId) throws Exception;


}
