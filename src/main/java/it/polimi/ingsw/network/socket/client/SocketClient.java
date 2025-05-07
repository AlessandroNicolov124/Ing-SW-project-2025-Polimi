package it.polimi.ingsw.network.socket.client;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import it.polimi.ingsw.network.client.GameClient;
import it.polimi.ingsw.network.commands.PlayerReadyCommand;
import it.polimi.ingsw.network.socket.messages.PlayerAddedMessage;


public class SocketClient implements Runnable, VirtualView, GameClient {
    
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream  in;
    private final BlockingQueue<String> incoming = new LinkedBlockingQueue<>();
    private final VirtualSocketServer proxy;
    private final BlockingQueue<Object> responses = new LinkedBlockingQueue<>();
    private int playerId = -1;

    public SocketClient(String host,int port) throws IOException {
        socket = new Socket(host,port);
        out = new ObjectOutputStream(socket.getOutputStream()); 
        out.flush();
        in  = new ObjectInputStream (socket.getInputStream());
        proxy = new VirtualSocketServer(out, responses);
        startConsumer();
    }

    private void startConsumer() {
        Thread t=new Thread(() -> {
            try {
                while(true)
                System.out.println("[Server] " + incoming.take());
            }
            catch(InterruptedException ignored) {

            }
        },"cli-consumer");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void run() {
        try {
            while(true){
                Object o = in.readObject();

                if (o instanceof PlayerAddedMessage playerAddedMessage) {
                    playerAddedMessage.handleClient(this);
                    responses.offer(playerAddedMessage);
                } else if (o instanceof String s) {
                    incoming.offer(s);
                }
            }
        } catch (Exception e) {
            System.out.println("Connection closed: " + e);
        }
    }

    public void sendObject(Object o) throws IOException {
        out.writeObject(o);
        out.flush();
    }

    @Override public void showUpdate(String m){ 
        incoming.offer(m); 
    }

    @Override public void close() throws Exception { 
        socket.close(); 
    }

    @Override
    public int addPlayer(String username) throws Exception {
        return proxy.addPlayer(username);
    }

    public void socketSetPlayerReady(PlayerReadyCommand cmd) {
        try {
            out.writeObject(cmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPlayerId(int id) {
        this.playerId = id;
    }

}