package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Components.StructuralModule;
import it.polimi.ingsw.Components.UniversalConnectorType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import it.polimi.ingsw.Components.Cabin;

import java.util.List;

public class EpidemicTest {
    @Test
    void testApplyEpidemicWithAdjacentOccupiedCabins() {
        // Crea una nave con due cabine adiacenti e occupate
        ShipBuilder ship = new ShipBuilder(7, 5);
        Cabin cabin1 = new Cabin(true);
        Cabin cabin2 = new Cabin(false);
        cabin2.initializeConnectors();
        Crew crew1 = new HumanCrew();
        Crew crew2 = new HumanCrew();
        ship.addCrewToCabin(crew1, cabin1);
        ship.addCrewToCabin(crew2, cabin2);
        System.out.println("total crew " + ship.getTotalCrewCount());
        ship.placeComponent(cabin1, ship.getStartingPosition());
        ship.placeComponent(cabin2, ship.getStartingPosition().getAdjacentPosition(Direction.EAST));
        Player player = new Player(1, "test", true, ship);

        Epidemic epidemic = new Epidemic();
        int lost = epidemic.applyEpidemic(player);

        assertEquals(2, lost, "Devono essere rimossi 2 membri dell'equipaggio (uno per cabina adiacente)");
        assertTrue(cabin1.getCrew().isEmpty(), "La cabina 1 deve essere vuota");
        assertTrue(cabin2.getCrew().isEmpty(), "La cabina 2 deve essere vuota");
    }

    @Test
    void testApplyEpidemicWithNonAdjacentCabins() {
        // Crea una nave con due cabine NON adiacenti
        ShipBuilder ship = new ShipBuilder(7, 5); // 2x1 nave
        Cabin cabin1 = new Cabin(true);
        Cabin cabin2 = new Cabin(false);
        cabin2.initializeConnectors();
        StructuralModule structuralModule = new StructuralModule();
        structuralModule.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        Crew crew1 = new HumanCrew();
        Crew crew2 = new HumanCrew();
        cabin1.addCrew(crew1);
        cabin2.addCrew(crew2);
        ship.placeComponent(cabin1, ship.getStartingPosition());
        ship.placeComponent(structuralModule, ship.getStartingPosition().getAdjacentPosition(Direction.EAST));
        ship.placeComponent(cabin2, ship.getPositionOf(structuralModule).getAdjacentPosition(Direction.EAST));
        Player player = new Player(2, "test2", true, ship);

        Epidemic epidemic = new Epidemic();
        int lost = epidemic.applyEpidemic(player);

        assertEquals(0, lost, "Non devono essere rimossi membri dell'equipaggio (cabine non adiacenti)");
        assertFalse(cabin1.getCrew().isEmpty(), "La cabina 1 deve essere ancora occupata");
        assertFalse(cabin2.getCrew().isEmpty(), "La cabina 2 deve essere ancora occupata");
    }
}
