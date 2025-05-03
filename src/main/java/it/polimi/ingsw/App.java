package it.polimi.ingsw;

import it.polimi.ingsw.AdventureCards.*;
import it.polimi.ingsw.Components.*;
import it.polimi.ingsw.network.GeneralServer;
import it.polimi.ingsw.network.socket.client.SocketClient;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws IOException {

        //GameController controller = new GameController();
        //controller.testGameRound();
//        Player player1 = new Player(0,"Giocatore 1",true);
//
//        controller.addPlayer(player1);
//        controller.pickCardForPlayer(player1);
//
//        // Creazione di componenti
//        Component startingCabin = new Cabin(true); // cabina di comando
//        Engine engine = new SingleEngine();
//        engine.initializeConnectors(ConnectorType.SINGLE, ConnectorType.NULL, ConnectorType.NULL, ConnectorType.NULL);
//        Cannon cannon = new SingleCannon();
//        cannon.initializeConnectors(ConnectorType.NULL, ConnectorType.NULL, ConnectorType.SINGLE, ConnectorType.NULL);
//        BatteryCompartment battery = new BatteryCompartment();
//        battery.initializeConnectors(ConnectorType.NULL, ConnectorType.UNIVERSAL, ConnectorType.UNIVERSAL, ConnectorType.NULL);
//        LifeSupportSystem lifeSupportSystem = new LifeSupportSystem(LifeSupportSystemType.PURPLE);
//        lifeSupportSystem.initializeConnectors(ConnectorType.UNIVERSAL, ConnectorType.NULL, ConnectorType.NULL, ConnectorType.UNIVERSAL);
//
//
//        Position pos1 = new Position(1,1); //Magic numbers will be removed
//
//        controller.addComponentForPlayer(player1, startingCabin, pos1);
//
//        pos1 = player1.getShip().getStartingPosition().getAdjacentPosition(Direction.NORTH);
//        player1.getShip().placeComponent(cannon, pos1);
//
//        pos1 = player1.getShip().getStartingPosition().getAdjacentPosition(Direction.WEST);
//        player1.getShip().placeComponent(battery, pos1);
//
//        pos1 = player1.getShip().getStartingPosition().getAdjacentPosition(Direction.EAST);
//        player1.getShip().placeComponent(lifeSupportSystem, pos1);
//
//
//        // Validazione della nave
//        if (player1.getShip().validateShip()) {
//            System.out.println("La nave è assemblata correttamente.");
//        } else {
//            System.out.println("La nave non è assemblata correttamente.");
//        }

        GeneralServer srv = new GeneralServer();
        srv.serverRun();
        //SocketClient client = new SocketClient("localhost",7777);
        //new Thread(client).start();
    }

}
