package it.polimi.ingsw.network.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualServer extends Remote {

    void connect(VirtualView client) throws RemoteException;

    void stopServer() throws RemoteException;

    int addPlayer(String username) throws RemoteException;

}