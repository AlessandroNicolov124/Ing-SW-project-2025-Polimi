

package it.polimi.ingsw.network.client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.messages.ResponseMessage;
import it.polimi.ingsw.network.messages.RotationType;

public interface RmiClientIf extends GeneralClientIf, Remote {

    @Override
    void createPlayer(String username) throws RemoteException;

    @Override
    void placeComponent(Component component, Position position) throws RemoteException;

    @Override
    void showUpdate(ResponseMessage message) throws RemoteException;

    @Override
    void validateShip() throws RemoteException;

    @Override
    void rotateComponent(Position position, RotationType rotationType) throws RemoteException;

    @Override
    void setLocalPlayer(Player player) throws RemoteException;

    @Override
    Component pickComponentCard() throws RemoteException;

    @Override
    void createMatch(String matchName, int playerId, String username) throws RemoteException;

    @Override
    void getAvailableMatches() throws RemoteException;

    @Override
    void joinMatch(UUID matchId, int playerId, String username) throws RemoteException;

    @Override
    void leaveMatch(UUID matchId, int playerId, String username) throws RemoteException;

    @Override
    void startMatch(UUID matchId, int playerId, String username) throws RemoteException;

    @Override
    void loadingAck(UUID matchId) throws RemoteException;

    @Override
    void playerReady(UUID matchId, int playerId) throws RemoteException;

}