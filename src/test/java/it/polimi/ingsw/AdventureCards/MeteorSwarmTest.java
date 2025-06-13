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
        player = new Player(1, "TestPlayer", true, ship);

        gameModel = new GameModel();
        gameModel.addPlayer(player);
        gameModel.setCredits(player, 10);

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

        gameModel.getFlightBoard().setInitialPosition(player, 4);
        assertEquals(4, gameModel.getFlightBoard().getPosition(player));
    }

    @Test
    void testProcessMeteorImpactsDestroysComponentIfNoCannon() {
        int initialComponents = player.getShip().getNonDamageComponents().size();
        assertEquals(4, initialComponents);
        meteorSwarm.processMeteorImpacts(gameModel);
        int afterComponents = player.getShip().getNonDamageComponents().size();
        System.out.println(afterComponents);
        assertTrue(afterComponents < initialComponents, "Un componente deve essere distrutto se non c'è cannone");
    }

    @Test
    void testProcessMeteorImpactsWithCannonPreventsDestruction() {
        // Aggiungi un cannone rivolto a nord nella posizione centrale
        Cannon sc = new SingleCannon();
        sc.initializeConnectors(new NullConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        ship.placeComponent(sc, ship.getStartingPosition().getAdjacentPosition(Direction.NORTH));

        int initialComponents = player.getShip().getComponents().size();
        meteorSwarm.processMeteorImpacts(gameModel);
        int afterComponents = player.getShip().getComponents().size();
        assertEquals(initialComponents, afterComponents, "Il cannone dovrebbe prevenire la distruzione");
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
}
