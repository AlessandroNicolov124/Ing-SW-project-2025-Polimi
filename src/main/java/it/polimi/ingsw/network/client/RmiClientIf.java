

package it.polimi.ingsw.network.client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.Difficulty;
import it.polimi.ingsw.network.messages.ResponseMessage;
import it.polimi.ingsw.network.messages.RotationType;

public interface RmiClientIf extends GeneralClientIf, Remote {

    @Override
    void createPlayer(String username) throws RemoteException;

    @Override
    void placeComponent(Component component, Position position, int playerId) throws RemoteException;

    @Override
    void removeComponent(Position position, int playerId) throws RemoteException;

    @Override
    void showUpdate(ResponseMessage message) throws RemoteException;

    @Override
    void validateShip(int playerId) throws RemoteException;

    @Override
    void rotateComponent(int playerId, Position position, RotationType rotationType) throws RemoteException;

    @Override
    void pickComponentCard(int playerId) throws RemoteException;

    @Override
    void createMatch(String matchName, int playerId, String username, Difficulty difficulty) throws RemoteException;

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

    void selectCabinColor(String color, int playerId) throws RemoteException;

    void readyForFlight(UUID matchId, int playerId) throws RemoteException;

}