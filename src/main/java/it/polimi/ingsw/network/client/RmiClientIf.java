package it.polimi.ingsw.network.client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.rmi.messages.UpdateMessage;
import it.polimi.ingsw.network.socket.messages.RotationType;

public interface RmiClientIf extends GeneralClientIf, Remote {

    @Override
    int addPlayer(String username) throws RemoteException;

    @Override
    void placeComponent(Component component, Position position) throws RemoteException;

    @Override
    void showUpdate(String message) throws RemoteException;

    @Override
    void validateShip() throws RemoteException;

    @Override
    void rotateComponent(Position position, RotationType rotationType) throws RemoteException;

    @Override
    void setLocalPlayer(Player player) throws RemoteException;

    @Override
    Component pickComponentCard() throws RemoteException;

    @Override
    void showUpdate(UpdateMessage msg) throws RemoteException;

    @Override
    boolean isPositionSelectionValid(int position) throws RemoteException;

    @Override
    List<Integer> requestAvailablePositions() throws RemoteException;

    @Override
    boolean selectPosition(int position) throws RemoteException;

    @Override
    boolean areAllPlayersPositioned() throws RemoteException;

}