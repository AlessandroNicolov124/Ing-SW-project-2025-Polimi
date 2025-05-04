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
    public static void main(String[] args) {

        GeneralServer srv = new GeneralServer();
        srv.serverRun();

        GameController controller = new GameController();
        //controller.testGameRound();

        System.out.println("=== TEST SIMULAZIONE ROUND DI GIOCO ===");

        Player player1 = new Player(0,"Giocatore 0",true);
        Player player2 = new Player(1,"Giocatore 1",true);

        controller.addPlayer(player1);
        controller.addPlayer(player2);

        // Creazione di componenti
        Component startingCabin = new Cabin(true); // cabina di comando
        Engine engine = new SingleEngine();
        NullConnectorType nullConn = new  NullConnectorType();
        engine.initializeConnectors(new SingleConnectorType(), nullConn, nullConn, nullConn);
        Cannon cannon = new SingleCannon();
        cannon.initializeConnectors(nullConn, nullConn, new SingleConnectorType(), nullConn);
        BatteryCompartment battery = new BatteryCompartment();
        battery.initializeConnectors(nullConn, new UniversalConnectorType(), new UniversalConnectorType(), nullConn);
        LifeSupportSystem lifeSupportSystem = new LifeSupportSystem(LifeSupportSystemType.PURPLE);
        lifeSupportSystem.initializeConnectors(new UniversalConnectorType(), nullConn, nullConn, new UniversalConnectorType());


        Position pos = player1.getShip().getStartingPosition();
        controller.addComponentForPlayer(player1, startingCabin, pos);
        pos = player1.getShip().getStartingPosition().getAdjacentPosition(Direction.NORTH);
        controller.addComponentForPlayer(player1, cannon, pos);
        pos = player1.getShip().getStartingPosition().getAdjacentPosition(Direction.SOUTH);
        controller.addComponentForPlayer(player1, engine, pos);
        pos = player1.getShip().getStartingPosition().getAdjacentPosition(Direction.WEST);
        controller.addComponentForPlayer(player1, battery, pos);
        pos = player1.getShip().getStartingPosition().getAdjacentPosition(Direction.EAST);
        controller.addComponentForPlayer(player1, lifeSupportSystem, pos);


        //player1.shipAssembled();
        //player2.shipAssembled();

        controller.printBoardStatus();
        controller.printShipStatus();


        controller.setPlayerReady(player1);
        controller.validateShipForPlayer(player1);

        if(controller.startMatchIfPossible())
        {
            System.out.println("Match started correctly.");
            controller.pickCardForPlayer(player1);
            controller.setCredits(player1, 10);
            controller.setCurrentPlayer(player1);
            controller.playTurn();

            controller.pickCardForPlayer(player1);

        }
    }

}
