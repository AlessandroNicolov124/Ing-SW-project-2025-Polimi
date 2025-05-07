package it.polimi.ingsw.network.socket.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

import it.polimi.ingsw.network.socket.messages.GenericMessage;
import it.polimi.ingsw.network.socket.messages.AddPlayerMessage;
import it.polimi.ingsw.network.socket.server.VirtualServer;
import it.polimi.ingsw.network.socket.messages.PlayerAddedMessage;


public class VirtualSocketServer implements VirtualServer {
    
    private final ObjectOutputStream out;
    private final BlockingDeque<GenericMessage> responses;

    public VirtualSocketServer(ObjectOutputStream out, BlockingDeque<GenericMessage> responses) { 
        this.out = out; 
        this.responses = responses;
    }

    private void send(GenericMessage m) throws IOException {
        synchronized (out) { 
            out.writeObject(m);
            out.flush(); 
        }
    }

    public <T extends GenericMessage> T waitFor(Class<T> type) throws InterruptedException {
        while (true) {
            GenericMessage m = responses.poll(100, TimeUnit.MILLISECONDS);
            if (m == null){
                continue;
            }
            if (type.isInstance(m))
                return type.cast(m);

            responses.offerLast(m);
        }
    }

    @Override
    public int addPlayer(String username) throws IOException {
        send(new AddPlayerMessage(username));
        try {
            return waitFor(PlayerAddedMessage.class).playerId();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for response", e);
        }
    }

    @Override 
    public void stopServer() throws IOException { 
        out.close(); 
    }

}