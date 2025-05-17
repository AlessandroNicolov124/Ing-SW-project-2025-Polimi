

package it.polimi.ingsw.network.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
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

}