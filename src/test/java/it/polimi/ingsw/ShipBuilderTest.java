package it.polimi.ingsw;

import it.polimi.ingsw.Components.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShipBuilderTest {
    private ShipBuilder shipBuilder;

    @BeforeEach
    void setUp() {
        shipBuilder = new ShipBuilder(7, 5);
    }

    @Test
    @DisplayName("removeCrewOfType rimuove il numero corretto di crew dalle cabine")
    void testRemoveCrewOfType() {
        Cabin cabin1 = new Cabin(true);
        Cabin cabin2 = new Cabin(false);
        cabin2.initializeConnectors();
        Crew human1 = new HumanCrew();
        Crew human2 = new HumanCrew();
        Crew alien = new BrownAlienCrew();
        cabin1.addCrew(human1);
        cabin1.addCrew(human2);
        cabin2.addCrew(alien);
//        System.out.println(cabin1.addCrew(human1));
//        System.out.println(cabin1.addCrew(human2));
//        System.out.println(cabin2.addCrew(alien));

        // Aggiungi le cabine alla nave tramite placeComponent
        shipBuilder.placeComponent(cabin1, shipBuilder.getStartingPosition());
        shipBuilder.placeComponent(cabin2, shipBuilder.getStartingPosition().getAdjacentPosition(Direction.EAST));

        int removed = shipBuilder.removeCrewOfType(CrewType.HUMAN, 2);
        assertEquals(2, removed);
        assertTrue(cabin1.getCrew().isEmpty());
        assertTrue(cabin2.getCrew().isEmpty()); //cabin2 non ha supporto vitale marrone collegato

        LifeSupportSystem ss = new LifeSupportSystem(LifeSupportSystemType.BROWN);
        ss.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(),
                new UniversalConnectorType(), new UniversalConnectorType());
        shipBuilder.placeComponent(ss, shipBuilder.getPositionOf(cabin2).getAdjacentPosition(Direction.SOUTH));
        shipBuilder.updateLifeSupportConnections(ss);
        System.out.println(cabin2.addCrew(alien));

        assertFalse(cabin2.getCrew().isEmpty()); //cabin2 ha supporto vitale marrone collegato
    }

    @Test
    @DisplayName("removeCrewOfType si ferma quando ha rimosso il numero richiesto")
    void testRemoveCrewOfTypeStopsAtCount() {
        Cabin cabin1 = new Cabin(true);
        Crew human1 = new HumanCrew();
        Crew human2 = new HumanCrew();
        cabin1.addCrew(human1);
        cabin1.addCrew(human2);

        shipBuilder.placeComponent(cabin1, shipBuilder.getStartingPosition());

        int removed = shipBuilder.removeCrewOfType(CrewType.HUMAN, 1);
        assertEquals(1, removed);
        assertEquals(1, cabin1.getCrew().size());
    }

    @Test
    @DisplayName("removeCrewOfType non rimuove nulla se non trova crew del tipo richiesto")
    void testRemoveCrewOfTypeNoneFound() {
        Cabin cabin1 = new Cabin();
        cabin1.initializeConnectors();
        Crew alien = new BrownAlienCrew();

        shipBuilder.placeComponent(cabin1, shipBuilder.getStartingPosition());

        LifeSupportSystem ss = new LifeSupportSystem(LifeSupportSystemType.BROWN);
        ss.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(),
                new UniversalConnectorType(), new UniversalConnectorType());
        shipBuilder.placeComponent(ss, shipBuilder.getPositionOf(cabin1).getAdjacentPosition(Direction.SOUTH));
        shipBuilder.updateLifeSupportConnections(ss);

        cabin1.addCrew(alien);

        int removed = shipBuilder.removeCrewOfType(CrewType.HUMAN, 2);
        assertEquals(0, removed);
        assertEquals(1, cabin1.getCrew().size());
    }

    @Test
    @DisplayName("updateLifeSupportConnections collega correttamente la cabina al supporto vitale")
    void testUpdateLifeSupportConnections() {
        Cabin cabin = new Cabin();
        cabin.initializeConnectors();
        shipBuilder.placeComponent(cabin, shipBuilder.getStartingPosition());

        LifeSupportSystem ss = new LifeSupportSystem(LifeSupportSystemType.PURPLE);
        ss.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(),
                new UniversalConnectorType(), new UniversalConnectorType());
        shipBuilder.placeComponent(ss, shipBuilder.getStartingPosition().getAdjacentPosition(Direction.EAST));
        shipBuilder.updateLifeSupportConnections(ss);

        assertTrue(cabin.hasConnectedLifeSupport(LifeSupportSystemType.PURPLE));
    }

    @Test
    @DisplayName("addCrewToCabin rispetta i limiti di equipaggio per alieni e umani")
    void testAddCrewToCabin() {
        Cabin cabin = new Cabin();
        cabin.initializeConnectors();
        shipBuilder.placeComponent(cabin, shipBuilder.getStartingPosition());

        Crew human1 = new HumanCrew();
        Crew human2 = new HumanCrew();
        Crew brownAlien1 = new BrownAlienCrew();
        Crew brownAlien2 = new BrownAlienCrew();

        LifeSupportSystem ss = new LifeSupportSystem(LifeSupportSystemType.BROWN);
        ss.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(),
                new UniversalConnectorType(), new UniversalConnectorType());
        shipBuilder.placeComponent(ss, shipBuilder.getPositionOf(cabin).getAdjacentPosition(Direction.SOUTH));
        shipBuilder.updateLifeSupportConnections(ss);

        assertTrue(shipBuilder.addCrewToCabin(human1, cabin));
        assertTrue(shipBuilder.addCrewToCabin(human2, cabin));
        // Non si può aggiungere alieno marrone
        assertFalse(shipBuilder.addCrewToCabin(brownAlien1, cabin));

        shipBuilder.removeCrewOfType(CrewType.HUMAN, 2);

        // Ora la cabina è vuota, si può aggiungere un alieno marrone
        assertTrue(shipBuilder.addCrewToCabin(brownAlien1, cabin));
        // Non si può aggiungere un secondo alieno marrone
        assertFalse(shipBuilder.addCrewToCabin(brownAlien2, cabin));
    }

    @Test
    @DisplayName("distributeCrewAutomatically distribuisce correttamente l'equipaggio")
    void testDistributeCrewAutomatically() {
        Cabin cabin1 = new Cabin(true);
        Cabin cabin2 = new Cabin(false);
        cabin1.initializeConnectors();
        cabin2.initializeConnectors();
        shipBuilder.placeComponent(cabin1, shipBuilder.getStartingPosition());
        shipBuilder.placeComponent(cabin2, shipBuilder.getStartingPosition().getAdjacentPosition(Direction.EAST));

        LifeSupportSystem ss = new LifeSupportSystem(LifeSupportSystemType.BROWN);
        ss.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(),
                new UniversalConnectorType(), new UniversalConnectorType());
        shipBuilder.placeComponent(ss, shipBuilder.getPositionOf(cabin2).getAdjacentPosition(Direction.SOUTH));
        shipBuilder.updateLifeSupportConnections(ss);

        shipBuilder.distributeCrewAutomatically();

        // La cabina 2 ha supporto marrone, quindi dovrebbe avere un alieno marrone
        assertTrue(cabin2.getCrew().stream().anyMatch(c -> c.getType() == CrewType.BROWN_ALIEN));
        // La cabina 1 dovrebbe avere almeno un umano
        assertTrue(cabin1.getCrew().stream().anyMatch(c -> c.getType() == CrewType.HUMAN));
    }

    @Test
    @DisplayName("distributeCrewAutomatically rispetta tutte le regole di assegnazione equipaggio")
    void testDistributeCrewAutomaticallySpec() {
        // Cabina di pilotaggio (sempre 2 umani)
        Cabin cockpit = new Cabin(true);
        shipBuilder.placeComponent(cockpit, shipBuilder.getStartingPosition());

        // Cabina senza supporto vitale (2 umani)
        Cabin noLifeSupport = new Cabin(false);
        noLifeSupport.initializeConnectors();
        shipBuilder.placeComponent(noLifeSupport, shipBuilder.getStartingPosition().getAdjacentPosition(Direction.SOUTH));

        // Cabina con supporto marrone
        Cabin brownCabin = new Cabin(false);
        brownCabin.initializeConnectors();
        shipBuilder.placeComponent(brownCabin, shipBuilder.getStartingPosition().getAdjacentPosition(Direction.EAST));
        LifeSupportSystem brownSS = new LifeSupportSystem(LifeSupportSystemType.BROWN);
        brownSS.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(),
                new UniversalConnectorType(), new UniversalConnectorType());
        shipBuilder.placeComponent(brownSS, shipBuilder.getPositionOf(brownCabin).getAdjacentPosition(Direction.SOUTH));
        shipBuilder.updateLifeSupportConnections(brownSS);

        // Cabina con supporto viola
        Cabin purpleCabin = new Cabin(false);
        purpleCabin.initializeConnectors();
        shipBuilder.placeComponent(purpleCabin, shipBuilder.getStartingPosition().getAdjacentPosition(Direction.WEST));
        LifeSupportSystem purpleSS = new LifeSupportSystem(LifeSupportSystemType.PURPLE);
        purpleSS.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(),
                new UniversalConnectorType(), new UniversalConnectorType());
        shipBuilder.placeComponent(purpleSS, shipBuilder.getPositionOf(purpleCabin).getAdjacentPosition(Direction.SOUTH));
        shipBuilder.updateLifeSupportConnections(purpleSS);

        // Cabina con doppio supporto (marrone e viola)
        Cabin doubleCabin = new Cabin(false);
        doubleCabin.initializeConnectors();
        shipBuilder.placeComponent(doubleCabin, shipBuilder.getStartingPosition().getAdjacentPosition(Direction.NORTH));
        LifeSupportSystem brownSS2 = new LifeSupportSystem(LifeSupportSystemType.BROWN);
        brownSS2.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(),
                new UniversalConnectorType(), new UniversalConnectorType());
        shipBuilder.placeComponent(brownSS2, shipBuilder.getPositionOf(doubleCabin).getAdjacentPosition(Direction.EAST));
        shipBuilder.updateLifeSupportConnections(brownSS2);
        LifeSupportSystem purpleSS2 = new LifeSupportSystem(LifeSupportSystemType.PURPLE);
        purpleSS2.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(),
                new UniversalConnectorType(), new UniversalConnectorType());
        shipBuilder.placeComponent(purpleSS2, shipBuilder.getPositionOf(doubleCabin).getAdjacentPosition(Direction.WEST));
        shipBuilder.updateLifeSupportConnections(purpleSS2);

        shipBuilder.distributeCrewAutomatically();

        // 1. Cabina di pilotaggio: sempre 2 umani
        assertEquals(2, cockpit.getCrew().size());
        assertTrue(cockpit.getCrew().stream().allMatch(c -> c.getType() == CrewType.HUMAN));

        // 2. Cabina senza supporto vitale: 2 umani
        assertEquals(2, noLifeSupport.getCrew().size());
        assertTrue(noLifeSupport.getCrew().stream().allMatch(c -> c.getType() == CrewType.HUMAN));

        // 3. Cabina con supporto marrone: 2 umani OPPURE 1 alieno marrone
        long brownAliens = brownCabin.getCrew().stream().filter(c -> c.getType() == CrewType.BROWN_ALIEN).count();
        if (brownAliens == 1) {
            assertEquals(1, brownCabin.getCrew().size());
        } else {
            assertEquals(2, brownCabin.getCrew().size());
            assertTrue(brownCabin.getCrew().stream().allMatch(c -> c.getType() == CrewType.HUMAN));
        }

        // 4. Cabina con supporto viola: 2 umani OPPURE 1 alieno viola
        long purpleAliens = purpleCabin.getCrew().stream().filter(c -> c.getType() == CrewType.PURPLE_ALIEN).count();
        if (purpleAliens == 1) {
            assertEquals(1, purpleCabin.getCrew().size());
        } else {
            assertEquals(2, purpleCabin.getCrew().size());
            assertTrue(purpleCabin.getCrew().stream().allMatch(c -> c.getType() == CrewType.HUMAN));
        }

        // 5. Cabina con doppio supporto: 2 umani OPPURE 1 alieno marrone OPPURE 1 alieno viola
        long brownAliensDouble = doubleCabin.getCrew().stream().filter(c -> c.getType() == CrewType.BROWN_ALIEN).count();
        long purpleAliensDouble = doubleCabin.getCrew().stream().filter(c -> c.getType() == CrewType.PURPLE_ALIEN).count();
        if (brownAliensDouble == 1 || purpleAliensDouble == 1) {
            assertEquals(1, doubleCabin.getCrew().size());
        } else {
            assertEquals(2, doubleCabin.getCrew().size());
            assertTrue(doubleCabin.getCrew().stream().allMatch(c -> c.getType() == CrewType.HUMAN));
        }

        // 6. La nave non può ospitare più di 1 alieno per colore
        int totalBrownAliens = shipBuilder.getAllComponents().stream()
                .filter(c -> c instanceof Cabin)
                .map(c -> ((Cabin) c).getCrew())
                .flatMap(List::stream)
                .filter(c -> c.getType() == CrewType.BROWN_ALIEN)
                .toList().size();
        int totalPurpleAliens = shipBuilder.getAllComponents().stream()
                .filter(c -> c instanceof Cabin)
                .map(c -> ((Cabin) c).getCrew())
                .flatMap(List::stream)
                .filter(c -> c.getType() == CrewType.PURPLE_ALIEN)
                .toList().size();
        assertTrue(totalBrownAliens <= 1);
        assertTrue(totalPurpleAliens <= 1);
    }
}
