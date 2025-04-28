package it.polimi.ingsw.network.socket.client;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketClient implements Runnable, VirtualView, AutoCloseable {
    
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream  in;
    private final BlockingQueue<String> incoming = new LinkedBlockingQueue<>();
    private final VirtualSocketServer proxy;

    public SocketClient(String host,int port) throws IOException {
        socket = new Socket(host,port);
        out = new ObjectOutputStream(socket.getOutputStream()); 
        out.flush();
        in  = new ObjectInputStream (socket.getInputStream());
        proxy = new VirtualSocketServer(out);
        startConsumer();
    }

    private void startConsumer() {
        Thread t=new Thread(() -> {
            try { while(true) System.out.println("[Server] "+incoming.take()); }
            catch(InterruptedException ignored) {}
        },"cli-consumer"); t.setDaemon(true); t.start();
    }

    @Override public void run() {
        try {
            while(true){
                Object o = in.readObject();
                if(o instanceof String s) incoming.offer(s);
            }
        } catch(Exception e){ 
            System.out.println("Connecction closed: "+e.getMessage()); 
        }
    }

    public void sendX() throws IOException { 
        proxy.x(); 
    }

    public void sendY() throws IOException { 
        proxy.y(); 
    }

    @Override public void showUpdate(String m){ 
        incoming.offer(m); 
    }

    @Override public void close() throws Exception { 
        socket.close(); 
    }
    
}