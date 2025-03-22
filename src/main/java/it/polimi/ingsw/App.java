package it.polimi.ingsw;

import it.polimi.ingsw.Components.*;
import it.polimi.ingsw.Visitors.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        //GameModel gameModel = new GameModel();

        // Aggiungi alcune carte all'adventureDeck
//        gameModel.getAdventureDeck().add(new Planets());
//        gameModel.getAdventureDeck().add(new AbandonedStation());
//
//        Player player1 = new Player(0,"Giocatore 1",true,null);
//        gameModel.getPlayers().add(player1);
//
//        // Il giocatore pesca una carta
//        player1.pickCard(gameModel);
//
//        ShipBuilder shipBuilder = new ShipBuilder();
//
//        // Creazione di componenti
//        Cabin cabin = new Cabin();
//        cabin.setType("Cabina di comando");
//
//        Engine engine = new Engine();
//        engine.setType("Motore a reazione");
//
//        Component cargo = new CargoBay("Storage", new ArrayList<>());
//
//        // Aggiunta di componenti alla nave
//        shipBuilder.addComponent(cabin, new Position(0, 0));
//        shipBuilder.addComponent(engine, new Position(1, 0));
//        shipBuilder.addComponent(cargo, new Position(0, 1));
//
//        // Validazione della nave
//        if (shipBuilder.validateShip()) {
//            System.out.println("La nave è assemblata correttamente.");
//        } else {
//            System.out.println("La nave non è assemblata correttamente.");
//        }
//
//        // Rimozione di un componente
//        shipBuilder.removeComponent(engine);
//


        //test visitor
        // Create components
        Component engine = new SingleEngine();
        Component cabin = new Cabin("Command");
        Component cannon = new SingleCannon();
        Component battery = new BatteryCompartment(100, "High Capacity");
        Component structure = new StructuralModule();

        //!!!!occorre inserire le connection nei component

//        // Connect components (simplified for demo)
//        engine.connectTo(structure, Direction.NORTH);
//        cabin.connectTo(structure, Direction.SOUTH);
//        cannon.connectTo(structure, Direction.EAST);
//        battery.connectTo(structure, Direction.WEST);

        // Create a list of all components
        List<Component> shipComponents = new ArrayList<>();
        shipComponents.add(engine);
        shipComponents.add(cabin);
        shipComponents.add(cannon);
        shipComponents.add(battery);
        shipComponents.add(structure);

        // Use visitors to analyze the ship
        ShipValueVisitor valueVisitor = new ShipValueVisitor();
        DurabilityVisitor durabilityVisitor = new DurabilityVisitor();
        VulnerabilityVisitor vulnerabilityVisitor = new VulnerabilityVisitor();
        ConnectionValidityVisitor validityVisitor = new ConnectionValidityVisitor();

        // Apply visitors to all components
        for (Component component : shipComponents) {
            component.accept(valueVisitor);
            component.accept(durabilityVisitor);
            component.accept(vulnerabilityVisitor);
            component.accept(validityVisitor);
        }

        // Display results
        System.out.println("SHIP ANALYSIS");
        System.out.println("Total ship value: " + valueVisitor.getTotalValue() + " credits");
        System.out.println("\nDurability Analysis:");
        System.out.println("- Structural integrity: " + durabilityVisitor.getStructuralIntegrity());
        System.out.println("- Shield strength: " + durabilityVisitor.getShieldStrength());
        System.out.println("- Total durability: " + durabilityVisitor.getTotalDurability());

        System.out.println("\nVulnerability Analysis:");
        System.out.println("- Meteor vulnerability: " + vulnerabilityVisitor.getMeteorVulnerability());
        System.out.println("- Pirate vulnerability: " + vulnerabilityVisitor.getPirateVulnerability());
        System.out.println("- Cosmic dust vulnerability: " + vulnerabilityVisitor.getCosmicDustVulnerability());

        System.out.println("\nConnection Validity:");
        System.out.println("- Is ship valid: " + validityVisitor.isValid());
        if (!validityVisitor.isValid()) {
            System.out.println("- Validation errors: " + validityVisitor.getValidationReport());
        }
    }
}
