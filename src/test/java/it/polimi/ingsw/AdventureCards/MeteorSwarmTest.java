package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Components.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class MeteorSwarmTest {
    private MeteorSwarm meteorSwarm;
    private Player player;
    private ShipBuilder ship;
    private GameModel gameModel;

    @BeforeEach
    void setUp() {
        List<MeteorSwarm.Meteor> meteors = List.of(
                new MeteorSwarm.Meteor(MeteorSwarm.MeteorSize.LARGE, Direction.NORTH),
                new MeteorSwarm.Meteor(MeteorSwarm.MeteorSize.SMALL,Direction.NORTH),
                new MeteorSwarm.Meteor(MeteorSwarm.MeteorSize.SMALL,Direction.EAST),
                new MeteorSwarm.Meteor(MeteorSwarm.MeteorSize.SMALL,Direction.SOUTH),
                new MeteorSwarm.Meteor(MeteorSwarm.MeteorSize.LARGE, Direction.EAST)
        );
        meteorSwarm = new MeteorSwarm(1, 1, new ArrayList<>(meteors));

        ship = new ShipBuilder(7, 5);
        ship.placeComponent(new Cabin(true), ship.getStartingPosition());
        Cabin cabin2 = new Cabin(false);
        Cabin cabin3 = new Cabin(false);
        Cabin cabin4 = new Cabin(false);
        cabin2.initializeConnectors();
        cabin3.initializeConnectors();
        cabin4.initializeConnectors();
        ship.placeComponent(cabin2, ship.getStartingPosition().getAdjacentPosition(Direction.EAST));
        ship.placeComponent(cabin3, ship.getStartingPosition().getAdjacentPosition(Direction.SOUTH));
        ship.placeComponent(cabin4, ship.getStartingPosition().getAdjacentPosition(Direction.WEST));

        player = new Player(1, "TestPlayer", true, ship);

        gameModel = new GameModel();
        gameModel.addPlayer(player);
        gameModel.setCredits(player, 10);
        gameModel.getFlightBoard().setInitialPosition(player, 1);

        assertEquals(6, gameModel.getFlightBoard().getPosition(player));
    }

    @Test
    void testProcessMeteorImpactsDestroysComponentIfNoCannon() {
        int initialComponents = player.getShip().getNonDamageComponents().size();
        assertEquals(4, initialComponents);
        meteorSwarm.processMeteorImpactFor(player, new MeteorSwarm.Meteor(MeteorSwarm.MeteorSize.SMALL, Direction.EAST), 2);
        int afterComponents = player.getShip().getNonDamageComponents().size();
        System.out.println(initialComponents +" "+ afterComponents);
        assertTrue(afterComponents <= initialComponents, "Un componente deve essere distrutto se non c'è cannone");
    }

    @Test
    void testRollDiceRange() throws Exception {
        java.lang.reflect.Method rollDice = MeteorSwarm.class.getDeclaredMethod("rollDice");
        rollDice.setAccessible(true);
        for (int i = 0; i < 100; i++) {
            int result = (int) rollDice.invoke(meteorSwarm);
            assertTrue(result >= 2 && result <= 12, "Il risultato del dado deve essere tra 2 e 12");
        }
    }

    @Test
    void testBigMeteorFromNorthDestroyedOnlyByForwardCannonInSameColumn() {
        // Meteora grande da nord nella colonna 3
        MeteorSwarm.Meteor meteor = new MeteorSwarm.Meteor(MeteorSwarm.MeteorSize.LARGE, Direction.NORTH);
        List<MeteorSwarm.Meteor> meteors = List.of(meteor);
        meteorSwarm = new MeteorSwarm(1, 1, new ArrayList<>(meteors));
        // Posiziona un cannone rivolto a nord nella stessa colonna (3, qualsiasi riga)

        SingleCannon cannon = new SingleCannon();
        cannon.initializeConnectors(new UniversalConnectorType(), new NullConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        ship.placeComponent(cannon, ship.getStartingPosition().getAdjacentPosition(Direction.NORTH));

        System.out.println("cannon position: "+ ship.getPositionOf(cannon));

        int initialComponents = player.getShip().getNonDamageComponents().size();
        meteorSwarm.processMeteorImpactFor(player, meteor, 3);
        int afterComponents = player.getShip().getNonDamageComponents().size();
        assertEquals(initialComponents, afterComponents, "Il cannone in avanti nella stessa colonna deve distruggere la meteora");
    }

    @Test
    void testBigMeteorFromNorthNotDestroyedBySideCannon() {
        // Meteora grande da nord nella colonna 3
        MeteorSwarm.Meteor meteor = new MeteorSwarm.Meteor(MeteorSwarm.MeteorSize.LARGE, Direction.NORTH);
        List<MeteorSwarm.Meteor> meteors = List.of(meteor);
        meteorSwarm = new MeteorSwarm(1, 1, new ArrayList<>(meteors));
        // Posiziona un cannone rivolto a est nella stessa colonna (3, qualsiasi riga)

        SingleCannon cannon = new SingleCannon();
        cannon.initializeConnectors(new NullConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        ship.placeComponent(cannon, ship.getStartingPosition().getAdjacentPosition(Direction.NORTH));

        int initialComponents = player.getShip().getNonDamageComponents().size();
        meteorSwarm.processMeteorImpactFor(player, meteor, 4);
        int afterComponents = player.getShip().getNonDamageComponents().size();

        assertTrue(afterComponents < initialComponents, "Un cannone laterale non difende da meteora frontale");
    }

    @Test
    void testBigMeteorFromEastDestroyedByCannonInSameDirection() {
        // Meteora grande da est nella riga 1
        MeteorSwarm.Meteor meteor = new MeteorSwarm.Meteor(MeteorSwarm.MeteorSize.LARGE, Direction.EAST);
        List<MeteorSwarm.Meteor> meteors = List.of(meteor);
        meteorSwarm = new MeteorSwarm(1, 1, new ArrayList<>(meteors));
        // Posiziona un cannone rivolto a est nella stessa riga (qualsiasi colonna, riga 1)

        SingleCannon cannon = new SingleCannon();
        cannon.initializeConnectors(new UniversalConnectorType(), new NullConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        cannon.setComponentDirection(Direction.EAST);
        ship.placeComponent(cannon, ship.getStartingPosition().getAdjacentPosition(Direction.NORTH));

        System.out.println("canno position: "+ ship.getPositionOf(cannon));

        int initialComponents = player.getShip().getNonDamageComponents().size();
        meteorSwarm.processMeteorImpactFor(player, meteor, 1);
        int afterComponents = player.getShip().getNonDamageComponents().size();
        assertEquals(initialComponents, afterComponents, "Il cannone laterale nella stessa direzione deve distruggere la meteora grande");

        // Ora testiamo una riga adiacente
        SingleCannon cannon1 = new SingleCannon();
        cannon1.initializeConnectors(new NullConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        cannon1.setComponentDirection(Direction.EAST);
        ship.placeComponent(cannon1, new Position(5, 2)); // Riga adiacente

        int initial2 = player.getShip().getNonDamageComponents().size();
        meteorSwarm.processMeteorImpactFor(player, meteor, 4);
        int after2 = player.getShip().getNonDamageComponents().size();
        assertEquals(initial2, after2, "Il cannone laterale nella stessa direzione deve distruggere la meteora grande anche se in riga adiacente");
    }
}
