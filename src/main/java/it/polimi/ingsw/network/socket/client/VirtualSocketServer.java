package it.polimi.ingsw.network.socket.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

import it.polimi.ingsw.network.socket.messages.GenericMessage;
import it.polimi.ingsw.network.socket.messages.AddPlayerMessage;
import it.polimi.ingsw.network.socket.server.VirtualServer;
import it.polimi.ingsw.network.socket.messages.PlayerAddedMessage;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.socket.messages.PlaceComponentMessage;


public class VirtualSocketServer implements VirtualServer {
    
    private final ObjectOutputStream out;
    private final BlockingDeque<GenericMessage> receivedMessagesQueue;

    public VirtualSocketServer(ObjectOutputStream out, BlockingDeque<GenericMessage> receivedMessagesQueue) {
        this.out = out;
        this.receivedMessagesQueue = receivedMessagesQueue;
    }

    // Synchronous call: send AddPlayerMessage, wait for PlayerAddedMessage
    public int addPlayer(String username) throws IOException {
        send(new AddPlayerMessage(username));
        try {
            // Wait for the PlayerAddedMessage response
            while (true) {
                // Check if the queue is empty before polling to avoid blocking indefinitely if message never comes
                // This part needs careful design to handle timeouts or errors if expected message isn't received.
                GenericMessage response = receivedMessagesQueue.takeFirst(); // Blocking call
                if (response instanceof PlayerAddedMessage pam) {
                    return pam.playerId();
                } else {
                    // If it's not the message we're waiting for, put it back at the end of the deque
                    // or handle it if it's an unexpected but valid message (e.g. a broadcast)
                    receivedMessagesQueue.offerLast(response); 
                    Thread.sleep(50); // Small delay to prevent busy spinning if other messages are in queue
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for player ID from server", e);
        }
    }

    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'close'");
    }

    // Asynchronous call: place component
    public void placeComponent(int playerId, Component component, Position position) throws IOException {
        send(new PlaceComponentMessage(playerId, component, position));
        // The client will receive ComponentPlacedMessage asynchronously via its run loop
        // and that message's handleClient method will be invoked.
    }

    // Send a message to the server (synchronized to prevent concurrent writes from different threads if proxy is shared)
    private void send(GenericMessage msg) throws IOException {
        synchronized (out) { // Synchronize on the output stream for thread safety
            out.writeObject(msg);
            out.flush(); // Ensure the message is sent immediately
        }
    }
}