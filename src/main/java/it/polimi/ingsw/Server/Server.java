package it.polimi.ingsw.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


/**
 * Server for the game.
 */
public class Server
{

    public static void main(String[] args)
    {
        ServerSocket socket;
        Scanner scanner = new Scanner(System.in);
        // Allow the user to set the socket port
        System.out.println("Socket port of server?");
        String ip = scanner.nextLine();
        try {
            socket = new ServerSocket(Integer.parseInt(ip));
        } catch (IOException e) {
            System.out.println("cannot open server socket");
            System.exit(1);
            return;
        }

        while (true) {
            try {
                /* accepts connections; for every connection we accept,
                 * create a new Thread executing a ClientHandler */
                Socket client = socket.accept();
                ClientHandler clientHandler = new ClientHandler(client);
                Thread thread = new Thread(clientHandler, "server_" + client.getInetAddress());
                thread.start();
            } catch (IOException e) {
                System.out.println("connection dropped");
            }
        }
    }
}
