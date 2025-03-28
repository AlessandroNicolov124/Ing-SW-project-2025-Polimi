package it.polimi.ingsw;

import it.polimi.ingsw.AdventureCards.*;
import it.polimi.ingsw.Components.*;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        GameModel gameModel = new GameModel();

        //Aggiungi alcune carte all'adventureDeck
        gameModel.getAdventureDeck().add(new Planets());
        gameModel.getAdventureDeck().add(new AbandonedStation());

        ShipBuilder shipBuilder1 = new ShipBuilder(6,6);
        Player player1 = new Player(0,"Giocatore 1",true, shipBuilder1);
        gameModel.getPlayers().add(player1);

        // Il giocatore pesca una carta
        player1.pickCard(gameModel);

        // Creazione di componenti
        Cabin startingCabin = new Cabin(true); // cabina di comando
        Engine engine = new SingleEngine();
        engine.initializeConnectors(ConnectorType.SINGLE, ConnectorType.NULL, ConnectorType.NULL, ConnectorType.NULL);
        Cannon cannon = new SingleCannon();
        cannon.initializeConnectors(ConnectorType.NULL, ConnectorType.NULL, ConnectorType.SINGLE, ConnectorType.NULL);
        BatteryCompartment battery = new BatteryCompartment();
        battery.initializeConnectors(ConnectorType.NULL, ConnectorType.UNIVERSAL, ConnectorType.UNIVERSAL, ConnectorType.NULL);
        LifeSupportSystem lifeSupportSystem = new LifeSupportSystem(LifeSupportSystemType.PURPLE);
        lifeSupportSystem.initializeConnectors(ConnectorType.UNIVERSAL, ConnectorType.NULL, ConnectorType.NULL, ConnectorType.UNIVERSAL);


        Position pos1 = player1.getShip().getStartingPosition();
        player1.getShip().placeComponent(startingCabin, pos1);

        pos1 = player1.getShip().getStartingPosition().getAdjacentPosition(Direction.SOUTH);
        player1.getShip().placeComponent(engine, pos1);

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

//        // Create a list of all components
//        List<Component> shipComponents = new ArrayList<>();
//        shipComponents.add(engine);
//        shipComponents.add(startingCabin);
//        shipComponents.add(cannon);
//        shipComponents.add(battery);
//        shipComponents.add(lifeSupportSystem);
//
//        // Use visitors to analyze the ship
//        ShipValueVisitor valueVisitor = new ShipValueVisitor();
//        DurabilityVisitor durabilityVisitor = new DurabilityVisitor();
//        VulnerabilityVisitor vulnerabilityVisitor = new VulnerabilityVisitor();
//        ConnectionValidityVisitor validityVisitor = new ConnectionValidityVisitor();
//
//        // Apply visitors to all components
//        for (Component component : shipComponents) {
//            component.accept(valueVisitor);
//            component.accept(durabilityVisitor);
//            component.accept(vulnerabilityVisitor);
//            component.accept(validityVisitor);
//        }
//
//        // Display results
//        System.out.println("SHIP ANALYSIS");
//        System.out.println("Total ship value: " + valueVisitor.getTotalValue() + " credits");
//        System.out.println("\nDurability Analysis:");
//        System.out.println("- Structural integrity: " + durabilityVisitor.getStructuralIntegrity());
//        System.out.println("- Shield strength: " + durabilityVisitor.getShieldStrength());
//        System.out.println("- Total durability: " + durabilityVisitor.getTotalDurability());
//
//        System.out.println("\nVulnerability Analysis:");
//        System.out.println("- Meteor vulnerability: " + vulnerabilityVisitor.getMeteorVulnerability());
//        System.out.println("- Pirate vulnerability: " + vulnerabilityVisitor.getPirateVulnerability());
//        System.out.println("- Cosmic dust vulnerability: " + vulnerabilityVisitor.getCosmicDustVulnerability());
//
//        System.out.println("\nConnection Validity:");
//        System.out.println("- Is ship valid: " + validityVisitor.isValid());
//        if (!validityVisitor.isValid()) {
//            System.out.println("- Validation errors: " + validityVisitor.getValidationReport());
//        }
    }
}
