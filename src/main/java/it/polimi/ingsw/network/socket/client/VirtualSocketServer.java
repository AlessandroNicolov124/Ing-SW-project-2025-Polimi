package it.polimi.ingsw.network.socket.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;

import it.polimi.ingsw.network.socket.messages.GenericMessage;
import it.polimi.ingsw.network.socket.messages.AddPlayerMessage;
import it.polimi.ingsw.network.socket.server.VirtualServer;
import it.polimi.ingsw.network.socket.messages.PlayerAddedMessage;


public class VirtualSocketServer implements VirtualServer {
    
    private final ObjectOutputStream out;
    private final BlockingQueue<Object> responses;

    public VirtualSocketServer(ObjectOutputStream out, BlockingQueue<Object> responses) { 
        this.out = out; 
        this.responses = responses;
    }

    private void send(GenericMessage m) throws IOException {
        synchronized (out) { 
            out.writeObject(m);
            out.flush(); 
        }
    }

    @Override
    public int addPlayer(String username) throws IOException {
        send(new AddPlayerMessage(username));
        try {
            Object resp = responses.take(); // use poll instead of take to avoid blocking and 
                                            // handle the case where the response is not available
                                            // also in the TUI. Like responses.poll(5, TimeUnit.SECONDS);     
            if (resp instanceof PlayerAddedMessage p)
                return p.playerId();
            throw new IOException("Unexpected response " + resp);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted", ie);
        }
    }

    @Override 
    public void stopServer() throws IOException { 
        out.close(); 
    }

}