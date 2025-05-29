package it.polimi.ingsw.network.socket.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

import it.polimi.ingsw.network.socket.messages.*;
import it.polimi.ingsw.network.socket.server.VirtualServer;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Position;


public class VirtualSocketServer implements VirtualServer {
    
    private final ObjectOutputStream out;
    private final BlockingDeque<GenericMessage> receivedMessagesQueue;

    public VirtualSocketServer(ObjectOutputStream out, BlockingDeque<GenericMessage> receivedMessagesQueue) {
        this.out = out;
        this.receivedMessagesQueue = receivedMessagesQueue;
    }

    // Synchronous call: send AddPlayerMessage, wait for PlayerAddedMessage
    @Override
    public int sendSrvAddPlayerReqAndWaitResp(String username) throws IOException {
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

    // Asynchronous call: remove component
    public void removeComponent(int playerId, Position position) throws IOException {
        send(new RemoveComponentMessage(playerId, position));
        // Response handled asynchronously by ComponentRemovedMessage
    }

    public Component pickCompForShip(int playerId) throws IOException {
        send(new PickCompMessage(playerId));

        try {
            // Wait for server to respond
            while (true) {

                GenericMessage response = receivedMessagesQueue.takeFirst(); // Blocking call
                if (response instanceof CompPickedMessage resp) {
                    return resp.getComponent();
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

    // Asynchronous call: rotate component
    public void rotateComponent(int playerId, Position position, RotationType rotationType) throws IOException {
        send(new RotateComponentMessage(playerId, position, rotationType));
        // Response handled asynchronously by ComponentRotatedMessage
    }

    // Asynchronous call: validate ship
    public void validateShip(int playerId) throws IOException {
        send(new ValidateShipMessage(playerId));
        // Response handled asynchronously by ShipValidationResultMessage
    }

    // Method to request available positions asynchronously
    public void requestAvailablePositionsAsync(int playerId) throws IOException {
        System.out.println("[Client] Requesting available positions from server");
        synchronized (out) {
            out.writeObject(new GetAvailablePositionsMessage(playerId));
            out.flush();
        }
    }

    // Method to select a position asynchronously
    public void selectPositionAsync(int playerId, int position) throws IOException {
        if (position < 1 || position > 4) {
            throw new IllegalArgumentException("Position must be between 1 and 4");
        }
        System.out.println("[Client] Sending position selection request: " + position);
        synchronized (out) {
            out.writeObject(new SelectPositionMessage(playerId, position));
            out.flush();
        }
    }

    // Send a message to the server (synchronized to prevent concurrent writes from different threads if proxy is shared)
    private void send(GenericMessage msg) throws IOException {
        synchronized (out) { // Synchronize on the output stream for thread safety
            out.writeObject(msg);
            out.flush(); // Ensure the message is sent immediately
        }
    }
}