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
        Player player1 = new Player(0,"Giocatore 1",true);
        //gameModel.getPlayers().add(player1);

        controller.addPlayer(player1);
        controller.pickCardForPlayer(player1);

        // Il giocatore pesca una carta
        //player1.pickCard(gameModel);

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

        controller.addComponentForPlayer(player1, startingCabin, pos1);
        //player1.getShip().placeComponent(startingCabin, pos1);

        //pos1 = player1.getShip().getStartingPosition().getAdjacentPosition(Direction.SOUTH);

        pos1 = player1.getShip().getStartingPosition().getAdjacentPosition(Direction.NORTH);
        player1.getShip().placeComponent(cannon, pos1);

        pos1 = player1.getShip().getStartingPosition().getAdjacentPosition(Direction.WEST);
        player1.getShip().placeComponent(battery, pos1);

        pos1 = player1.getShip().getStartingPosition().getAdjacentPosition(Direction.EAST);
        player1.getShip().placeComponent(lifeSupportSystem, pos1);


        // Validazione della nave
        if (player1.getShip().validateShip()) {
            System.out.println("La nave è assemblata correttamente.");
        } else {
            System.out.println("La nave non è assemblata correttamente.");
        }

        // Carica il cannone
        boolean charged = cannon.charge(battery);  // true
        boolean fired = cannon.fire();  // true

    }
}
