package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Components.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class StardustTest {
    private GameModel model;
    private Player player1;
    private Player player2;
    private ShipBuilder ship1;
    private ShipBuilder ship2;

    @BeforeEach
    void setUp() {
        model = new GameModel();
        ship1 = new ShipBuilder(7, 5);
        ship2 = new ShipBuilder(7, 5);
        player1 = new Player(1, "P1", true, ship1);
        player2 = new Player(2, "P2", true, ship2);
        model.addPlayer(player1);
        model.addPlayer(player2);
        model.getFlightBoard().setInitialPosition(player1, 1);
        model.getFlightBoard().setInitialPosition(player2, 2);
    }

    @Test
    void testStardustMovesPlayersBackByExposedConnectors() {
        // Setup: nave di player1 con 3 connettori esposti, player2 con 4
        Cabin cabin1 = new Cabin(true);
        ship1.placeComponent(cabin1, new Position(3, 2));
        // Aggiungo un componente solo a destra, lasciando 0 connettori esposti per il cannone e 3 per la navicella
        SingleCannon cannon1 = new SingleCannon();
        cannon1.initializeConnectors(new NullConnectorType(), new NullConnectorType(), new NullConnectorType(), new UniversalConnectorType());
        ship1.placeComponent(cannon1, new Position(4, 2));
        // ship2: solo la cabina, tutti i connettori esposti
        Cabin cabin2 = new Cabin(true);
        ship2.placeComponent(cabin2, new Position(3, 2));

        Stardust stardust = new Stardust();
        stardust.applyStardust(model);

        // player2 è ultimo, quindi sarà primo a essere spostato
        assertEquals(3, stardust.countExposedConnectors(ship1), "3 connettori esposti");
        assertEquals(4, stardust.countExposedConnectors(ship2), "4 connettori esposti");
        //System.out.println("Expected positions: P1=" + expected1 + ", P2=" + expected2);
        assertEquals(23, model.getFlightBoard().getPosition(player2));
        assertEquals(2, model.getFlightBoard().getPosition(player1));
    }

    @Test
    void testNoExposedConnectorsNoMovement() {
        // Cabina circondata da componenti su tutti i lati
        Cabin cabin = new Cabin(true);
        ship1.placeComponent(cabin, new Position(3, 2));

        SingleCannon c = new SingleCannon();
        c.initializeConnectors(new NullConnectorType(), new NullConnectorType(), new UniversalConnectorType(), new NullConnectorType());
        ship1.placeComponent(c, ship1.getStartingPosition().getAdjacentPosition(Direction.NORTH));

        c = new SingleCannon();
        c.initializeConnectors(new NullConnectorType(), new UniversalConnectorType(), new NullConnectorType(), new NullConnectorType());
        ship1.placeComponent(c, ship1.getStartingPosition().getAdjacentPosition(Direction.WEST));

        c = new SingleCannon();
        c.initializeConnectors(new NullConnectorType(), new NullConnectorType(), new NullConnectorType(), new UniversalConnectorType());
        ship1.placeComponent(c, ship1.getStartingPosition().getAdjacentPosition(Direction.EAST));

        SingleEngine s1 = new SingleEngine();
        s1.initializeConnectors(new UniversalConnectorType(), new NullConnectorType(), new NullConnectorType(), new NullConnectorType());
        ship1.placeComponent(s1, ship1.getStartingPosition().getAdjacentPosition(Direction.SOUTH));

        int pos = model.getFlightBoard().getPosition(player1);
        Stardust stardust = new Stardust();
        // Non ci sono connettori esposti, quindi non ci si aspetta movimento
        assertEquals(0, stardust.countExposedConnectors(ship1), "Nessun connettore esposto, nessun movimento atteso");
        stardust.applyStardust(model);
        assertEquals(pos, model.getFlightBoard().getPosition(player1));
    }

    @Test
    void testCountExposedConnectorsVariousCases() {
        // Caso 1: solo cabina, tutti i connettori esposti
        ShipBuilder testShip = new ShipBuilder(7, 5);
        Cabin cabin = new Cabin(true);
        testShip.placeComponent(cabin, new Position(3, 2));
        Stardust stardust = new Stardust();
        int exposed = stardust.countExposedConnectors(testShip);
        assertEquals(4, exposed, "La cabina da sola ha connettori esposti");

        // Caso 2: cabina circondata da componenti su tutti i lati
        for (Direction dir : Direction.values()) {
            BatteryCompartment c = new BatteryCompartment();
            c.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
            Position adj = dir.getAdjacentPosition(new Position(3, 2));
            testShip.placeComponent(c, adj);
        }
        exposed = stardust.countExposedConnectors(testShip);
        assertEquals(12, exposed, "tutti connettore esposto se la cabina è circondata da componenti con connettori non nulli");

        // Caso 3: cabina circondata da componenti con connettori nulli
        testShip = new ShipBuilder(7, 5);
        testShip.placeComponent(new Cabin(true), new Position(3, 2));

        SingleCannon c = new SingleCannon();
        c.initializeConnectors(new NullConnectorType(), new NullConnectorType(), new UniversalConnectorType(), new NullConnectorType());
        testShip.placeComponent(c, testShip.getStartingPosition().getAdjacentPosition(Direction.NORTH));

        c = new SingleCannon();
        c.initializeConnectors(new NullConnectorType(), new UniversalConnectorType(), new NullConnectorType(), new NullConnectorType());
        testShip.placeComponent(c, testShip.getStartingPosition().getAdjacentPosition(Direction.WEST));

        c = new SingleCannon();
        c.initializeConnectors(new NullConnectorType(), new NullConnectorType(), new NullConnectorType(), new UniversalConnectorType());
        testShip.placeComponent(c, testShip.getStartingPosition().getAdjacentPosition(Direction.EAST));

        SingleEngine s1 = new SingleEngine();
        s1.initializeConnectors(new UniversalConnectorType(), new NullConnectorType(), new NullConnectorType(), new NullConnectorType());
        testShip.placeComponent(s1, testShip.getStartingPosition().getAdjacentPosition(Direction.SOUTH));

        exposed = stardust.countExposedConnectors(testShip);
        assertEquals(0, exposed, "NON Dovrebbero esserci connettori esposti");
    }

    @Test
    void testStardustOrderIsReverse() {
        // Creo 3 giocatori con posizioni diverse
        ShipBuilder s1 = new ShipBuilder(7, 5);
        ShipBuilder s2 = new ShipBuilder(7, 5);
        ShipBuilder s3 = new ShipBuilder(7, 5);
        Player p1 = new Player(1, "P1", true, s1);
        Player p2 = new Player(2, "P2", true, s2);
        Player p3 = new Player(3, "P3", true, s3);
        GameModel m = new GameModel();
        m.addPlayer(p1);
        m.addPlayer(p2);
        m.addPlayer(p3);
        m.getFlightBoard().setInitialPosition(p1, 3);
        m.getFlightBoard().setInitialPosition(p2, 1); // p2 è più indietro
        m.getFlightBoard().setInitialPosition(p3, 4); // p3 è più avanti

        // Espongo 1 connettore per tutti
        Cabin cab1 = new Cabin(true);
        cab1.deleteConnection();
        cab1.initializeConnectors(new UniversalConnectorType(), new NullConnectorType(), new NullConnectorType(), new NullConnectorType());
        s1.placeComponent(cab1, new Position(3,2));
        Cabin cab2 = new Cabin(true);
        cab2.deleteConnection();
        cab2.initializeConnectors(new UniversalConnectorType(), new NullConnectorType(), new NullConnectorType(), new NullConnectorType());
        s2.placeComponent(cab2, new Position(3,2));
        Cabin cab3 = new Cabin(true);
        cab3.deleteConnection();
        cab3.initializeConnectors(new UniversalConnectorType(), new NullConnectorType(), new NullConnectorType(), new NullConnectorType());
        s3.placeComponent(cab3, new Position(3,2));

        Stardust stardust = new Stardust();
        //System.out.println("Count Exposed Connectors: (s1, s2, s3) " + stardust.countExposedConnectors(s1) + ", " + stardust.countExposedConnectors(s2) + ", " + stardust.countExposedConnectors(s3));

        int pos1 = m.getFlightBoard().getPosition(p1);
        int pos2 = m.getFlightBoard().getPosition(p2);
        int pos3 = m.getFlightBoard().getPosition(p3);

        List<Player> players = stardust.getPlayersInReverseOrder(m);
        assertEquals(p2, players.get(0), "Il primo giocatore nella lista deve essere p2 (più indietro)");
        assertEquals(p1, players.get(1), "Il secondo giocatore nella lista deve essere p1");
        assertEquals(p3, players.get(2), "L'ultimo giocatore nella lista deve essere p3 (più avanti)");

        stardust.applyStardust(m);
        // Dopo l'effetto, controllo che ognuno sia arretrato di 1 (cabina con 1 connettori esposti)
        assertEquals(5, m.getFlightBoard().getPosition(p2));
        assertEquals(23, m.getFlightBoard().getPosition(p1));
        assertEquals(22, m.getFlightBoard().getPosition(p3));
    }
}
