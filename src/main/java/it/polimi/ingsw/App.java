package it.polimi.ingsw;

import it.polimi.ingsw.AdventureCards.*;
import it.polimi.ingsw.Components.*;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {

        GameController controller = new GameController();
        //controller.testGameRound();

        System.out.println("=== TEST SIMULAZIONE ROUND DI GIOCO ===");

        Player player1 = new Player(0,"Giocatore 1",true);

        controller.addPlayer(player1);

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


        Position pos1 = new Position(1,1); //Magic numbers will be removed
        Position pos2 = new Position(2,2);
        Position pos3 = new Position(3,2);
        controller.addComponentForPlayer(player1, startingCabin, pos1);
        controller.addComponentForPlayer(player1, cannon, pos2);
        controller.addComponentForPlayer(player1, battery, pos3);


//        pos1 = player1.getShip().getStartingPosition().getAdjacentPosition(Direction.NORTH);
//        player1.getShip().placeComponent(cannon, pos1);
//
//        pos1 = player1.getShip().getStartingPosition().getAdjacentPosition(Direction.WEST);
//        player1.getShip().placeComponent(battery, pos1);
//
//        pos1 = player1.getShip().getStartingPosition().getAdjacentPosition(Direction.EAST);
//        player1.getShip().placeComponent(lifeSupportSystem, pos1);


        // Validazione della nave


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
