package it.polimi.ingsw.network.client;

import it.polimi.ingsw.Components.Component;

import java.util.UUID;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.messages.ResponseMessage;
import it.polimi.ingsw.network.messages.RotationType;

public interface GeneralClientIf extends AutoCloseable {
    
    void createPlayer(String username) throws Exception;

    void showUpdate(ResponseMessage message) throws Exception;

    void createMatch(String matchName, int playerId, String username) throws Exception;

    void getAvailableMatches() throws Exception;

    void startMatch(UUID matchId, int playerId, String username) throws Exception;

    void joinMatch(UUID matchId, int playerId, String username) throws Exception;

    void leaveMatch(UUID matchId, int playerId, String username) throws Exception;

    void placeComponent(Component component, Position position) throws Exception;

    void validateShip() throws Exception;

    void rotateComponent(Position position, RotationType rotationType) throws Exception;

    void setLocalPlayer(Player player) throws Exception;

    Component pickComponentCard() throws Exception;

}
