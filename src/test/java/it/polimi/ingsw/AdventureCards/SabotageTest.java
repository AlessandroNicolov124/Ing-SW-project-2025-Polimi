package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Components.Cabin;
import it.polimi.ingsw.Components.Component;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class SabotageTest {
    @Test
    void testSabotageDestroysComponentOnSmallestCrew() {
        ShipBuilder s1 = new ShipBuilder(7, 5);
        ShipBuilder s2 = new ShipBuilder(7, 5);
        s1.placeComponent(new Cabin(true), s1.getStartingPosition());
        Cabin c1= new Cabin(false);
        c1.initializeConnectors();
        s1.placeComponent(c1, s1.getStartingPosition().getAdjacentPosition(Direction.SOUTH));
        Cabin c2= new Cabin(false);
        c2.initializeConnectors();
        s2.placeComponent(new Cabin(true), s2.getStartingPosition());
        s2.placeComponent(c2, s2.getStartingPosition().getAdjacentPosition(Direction.SOUTH));


        Player p1 = new Player(1, "A", true, s1);
        Player p2 = new Player(2, "B", true, s2);
        // Simula crew: s1 ha meno crew
        s1.addCrewOfType(CrewType.HUMAN,1);
        s2.addCrewOfType(CrewType.HUMAN,2);
        GameModel model = new GameModel();
        model.addPlayer(p1);
        model.addPlayer(p2);
        model.getFlightBoard().setInitialPosition(p1, 1);
        model.getFlightBoard().setInitialPosition(p2, 2);

        Sabotage sabotage = new Sabotage(1, 1, 100);
        int pre = s1.getNonDamageComponents().size();
        boolean result = sabotage.applySabotage(model);
        if(result) {
            assertTrue(pre > s1.getNonDamageComponents().size(), "Sabotage should have destroyed a component");
        } else {
            assertEquals(pre, s1.getNonDamageComponents().size(), "Sabotage should not have destroyed a component");
        }
    }

    @Test
    void testSabotageTiesFurthestAhead() {
        ShipBuilder s1 = new ShipBuilder(7, 5);
        ShipBuilder s2 = new ShipBuilder(7, 5);
        s1.placeComponent(new Cabin(true), s1.getStartingPosition());
        s2.placeComponent(new Cabin(true), s2.getStartingPosition());

        Cabin c1= new Cabin(false);
        c1.initializeConnectors();
        s1.placeComponent(c1, s1.getStartingPosition().getAdjacentPosition(Direction.SOUTH));
        Cabin c2= new Cabin(false);
        c2.initializeConnectors();
        s2.placeComponent(c2, s2.getStartingPosition().getAdjacentPosition(Direction.SOUTH));


        Player p1 = new Player(1, "A", true, s1);
        Player p2 = new Player(2, "B", true, s2);
        s1.addCrewOfType(CrewType.HUMAN,1);
        s2.addCrewOfType(CrewType.HUMAN,1);

        GameModel model = new GameModel();
        model.addPlayer(p1);
        model.addPlayer(p2);
        model.getFlightBoard().setInitialPosition(p1, 1);
        model.getFlightBoard().setInitialPosition(p2, 2);

        Sabotage sabotage = new Sabotage(1, 1, 100);
        int pre = s2.getNonDamageComponents().size();
        boolean result = sabotage.applySabotage(model);
        if(result) {
            assertTrue(pre > s2.getNonDamageComponents().size(), "Sabotage should have destroyed a component");
        } else {
            assertEquals(pre, s2.getNonDamageComponents().size(), "Sabotage should not have destroyed a component");
        }
    }
}
