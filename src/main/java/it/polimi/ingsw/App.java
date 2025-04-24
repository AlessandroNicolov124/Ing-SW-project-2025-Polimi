package it.polimi.ingsw;

import it.polimi.ingsw.AdventureCards.*;
import it.polimi.ingsw.Components.*;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        //GameModel gameModel = new GameModel();

        // THIS APP.java is pretending to be the view right now
        GameController controller = new GameController();

        //Aggiungi alcune carte all'adventureDeck
        //gameModel.getAdventureDeck().add(new Planets());
        //gameModel.getAdventureDeck().add(new AbandonedStation());

        //ShipBuilder shipBuilder1 = new ShipBuilder(6,6);
        Player player1 = new Player(0,"Pippo",true);
        Player player2 = new Player(1,"Pluto",true);
        //gameModel.getPlayers().add(player1);

        controller.addPlayer(player1);

        if (!controller.startMatchIfPossible()){
            System.out.println("Impossible to start mathc, player(s) not ready yet.");
        }

        controller.setPlayerReady(player1);
        controller.setPlayerReady(player2);

        if (controller.startMatchIfPossible()) {

            System.out.println("Game started correctly");
        }

        // Creazione di componenti
        Component startingCabin = new Cabin(true); // cabina di comando
        Engine engine = new SingleEngine();
        engine.initializeConnectors(ConnectorType.SINGLE, ConnectorType.NULL, ConnectorType.NULL, ConnectorType.NULL);
        Cannon cannon = new SingleCannon();
        cannon.initializeConnectors(ConnectorType.NULL, ConnectorType.NULL, ConnectorType.SINGLE, ConnectorType.NULL);
        BatteryCompartment battery = new BatteryCompartment();
        battery.initializeConnectors(ConnectorType.NULL, ConnectorType.UNIVERSAL, ConnectorType.UNIVERSAL, ConnectorType.NULL);
        LifeSupportSystem lifeSupportSystem = new LifeSupportSystem(LifeSupportSystemType.PURPLE);
        lifeSupportSystem.initializeConnectors(ConnectorType.UNIVERSAL, ConnectorType.NULL, ConnectorType.NULL, ConnectorType.UNIVERSAL);


        Position pos1 = new Position(1,1); //Magic numbers will be removed
        Position pos2 = new Position(2,1);
        Position pos3 = new Position(3,1);
        Position pos4 = new Position(4,1);

        controller.addComponentForPlayer(player1, startingCabin, pos1);
        controller.addComponentForPlayer(player1, cannon, pos2);
        controller.addComponentForPlayer(player1, battery, pos3);
        controller.addComponentForPlayer(player1, lifeSupportSystem, pos4);

        controller.addComponentForPlayer(player2, startingCabin, pos4);
        controller.addComponentForPlayer(player2, cannon, pos3);
        controller.addComponentForPlayer(player2, battery, pos2);
        controller.addComponentForPlayer(player2, lifeSupportSystem, pos1);
        //player1.getShip().placeComponent(startingCabin, pos1);

//        pos1 = player1.getShip().getStartingPosition().getAdjacentPosition(Direction.SOUTH);
//
//        pos1 = player1.getShip().getStartingPosition().getAdjacentPosition(Direction.NORTH);
//        player1.getShip().placeComponent(cannon, pos1);
//
//        pos1 = player1.getShip().getStartingPosition().getAdjacentPosition(Direction.WEST);
//        player1.getShip().placeComponent(battery, pos1);
//
//        pos1 = player1.getShip().getStartingPosition().getAdjacentPosition(Direction.EAST);
//        player1.getShip().placeComponent(lifeSupportSystem, pos1);

        if (controller.moveToShipValidation())
        {
            System.out.println("OK ship bilding finished, moving to ship validation");
        }
        else
        {
            System.out.println("Fatal error, moving to ship validation");
        }

        // Validazione della nave
        if (controller.validateShipForPlayer(player1)){
            System.out.println("Pl1: La nave è assemblata correttamente.");
        } else {
            System.out.println("Pl1: La nave non è assemblata correttamente.");
        }

        if (controller.validateShipForPlayer(player2)){
            System.out.println("Pl2 :La nave è assemblata correttamente.");
        } else {
            System.out.println("Pl2: La nave non è assemblata correttamente.");
        }

        // Carica il cannone
        boolean charged = cannon.charge(battery);  // true
        boolean fired = cannon.fire();  // true



        controller.pickCardForPlayer(player1);
        controller.pickCardForPlayer(player2);

    }
}
