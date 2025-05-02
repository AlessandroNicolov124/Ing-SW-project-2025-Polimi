package it.polimi.ingsw.network.rmi;

import java.rmi.RemoteException;
import java.rmi.Remote;

public interface VirtualView extends Remote {
    
    void showUpdate(String message) throws RemoteException;

}