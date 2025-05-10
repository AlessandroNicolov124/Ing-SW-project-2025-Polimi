package it.polimi.ingsw.network.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import it.polimi.ingsw.network.client.GameClient;

public interface VirtualServer extends Remote, GameClient {

    void connect(VirtualView client) throws RemoteException;

    void stopServer() throws RemoteException;

    @Override
    int addPlayer(String username) throws RemoteException;

}