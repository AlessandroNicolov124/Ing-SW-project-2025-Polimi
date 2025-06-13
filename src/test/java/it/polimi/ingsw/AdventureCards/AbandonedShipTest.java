package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AbandonedShipTest {
    private AbandonedShip card;
    private Player player;
    private GameModel gameModel;
    private ShipBuilder ship;

    @BeforeEach
    void setUp() {
        // Richiede 2 crew umani per ottenere 5 crediti e perdere 1 flight day
        Map<CrewType, Integer> req = new HashMap<>();
        req.put(CrewType.HUMAN, 2);
        card = new AbandonedShip(1, 1, req, 5, 1);
        ship = new ShipBuilder(5, 5);
        player = new Player(1, "Test", true, ship);
        gameModel = new GameModel();
        gameModel.addPlayer(player);
        gameModel.setCredits(player, 10);
        // Aggiungi 2 umani alla nave
        ship.addCrewOfType(CrewType.HUMAN, 2);
    }

    @Test
    @DisplayName("useOpportunity: successo con crew sufficiente")
    void testUseOpportunitySuccess() {
        boolean result = card.useOpportunity(player, gameModel);
        assertTrue(result);
        assertTrue(card.isOpportunityUsed());
        assertEquals(15, gameModel.getCredits(player));
        // Verifica che i crew siano stati rimossi
        assertEquals(0, ship.getCrewCounts().getOrDefault(CrewType.HUMAN, 0));
    }

    @Test
    @DisplayName("useOpportunity: fallisce se già usata")
    void testUseOpportunityAlreadyUsed() {
        assertTrue(card.useOpportunity(player, gameModel));
        // Prova a usarla di nuovo
        assertFalse(card.useOpportunity(player, gameModel));
    }

    @Test
    @DisplayName("useOpportunity: fallisce se crew insufficiente")
    void testUseOpportunityNotEnoughCrew() {
        // Rimuovi un umano
        ship.removeCrewOfType(CrewType.HUMAN, 1);
        assertFalse(card.useOpportunity(player, gameModel));
        assertFalse(card.isOpportunityUsed());
        assertEquals(10, gameModel.getCredits(player));
    }

    @Test
    @DisplayName("useOpportunity: non modifica nulla se fallisce rimozione crew")
    void testUseOpportunityRemoveCrewFails() {
        // Simula nave che non rimuove crew (override removeCrewOfType)
        ShipBuilder fakeShip = new ShipBuilder(5, 5) {
            @Override
            public int removeCrewOfType(CrewType type, int n) { return 0; }
        };
        Player p2 = new Player(2, "Fake", true, fakeShip);
        fakeShip.addCrewOfType(CrewType.HUMAN, 2);
        gameModel.setCredits(p2, 20);
        assertFalse(card.useOpportunity(p2, gameModel));
        assertFalse(card.isOpportunityUsed());
        assertEquals(20, gameModel.getCredits(p2));
    }

    @Test
    @DisplayName("useOpportunity: non modifica flight days se fallisce")
    void testUseOpportunityNoFlightDayChangeOnFail() {
        // Rimuovi tutti i crew
        ship.removeCrewOfType(CrewType.HUMAN, 2);
        int before = gameModel.getFlightBoard().getPosition(player);
        assertFalse(card.useOpportunity(player, gameModel));
        assertEquals(before, gameModel.getFlightBoard().getPosition(player));
    }
}

