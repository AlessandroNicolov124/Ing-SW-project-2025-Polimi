package it.polimi.ingsw.network.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import it.polimi.ingsw.network.client.ClientCommunicateToSrvIf;

public interface VirtualServer extends Remote, ClientCommunicateToSrvIf{

    void connect(VirtualView client) throws RemoteException;

    void stopServer() throws RemoteException;

    @Override
    int addPlayer(String username) throws RemoteException;

}