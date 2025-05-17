package it.polimi.ingsw.network.client;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.socket.messages.RotationType;
public interface GeneralClientIf extends AutoCloseable {
    
    int addPlayer(String username) throws Exception;

    void placeComponent(Component component, Position position) throws Exception;

    void showUpdate(String message) throws Exception;

    void validateShip() throws Exception;

    void rotateComponent(Position position, RotationType rotationType) throws Exception;

    void setLocalPlayer(Player player) throws Exception;

    Component pickComponentCard() throws Exception;

}
