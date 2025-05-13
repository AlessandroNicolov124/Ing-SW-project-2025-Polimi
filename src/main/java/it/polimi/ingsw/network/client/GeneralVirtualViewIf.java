package it.polimi.ingsw.network.client;

import java.rmi.RemoteException;

public interface GeneralVirtualViewIf extends AutoCloseable {
    void showUpdate(String message) throws Exception;
}
