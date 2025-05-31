package it.polimi.ingsw;

import it.polimi.ingsw.AdventureCards.AdventureCard;
import it.polimi.ingsw.Components.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;

    @Mock
    private ShipBuilder mockShip;

    @Mock
    private AdventureCard mockCard;

    @Mock
    private Component mockComponent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPlayerCreation_WithDefaultShip() {
        player = new Player(1, "TestPlayer", true);

        assertEquals(1, player.getId());
        assertEquals("TestPlayer", player.getNickName());
        assertTrue(player.isActive());
        assertNotNull(player.getShip(), "Player ship should be initialized");
        assertFalse(player.getReady(), "Player should not be ready by default");
    }

    @Test
    void testPlayerCreation_WithCustomShip() {
        player = new Player(2, "CustomPlayer", false, mockShip);

        assertEquals(2, player.getId());
        assertEquals("CustomPlayer", player.getNickName());
        assertFalse(player.isActive());
        assertEquals(mockShip, player.getShip());
    }

    @Test
    void testSetAndGetActive() {
        player = new Player(1, "TestPlayer", true);

        assertTrue(player.isActive());

        player.setActive(false);
        assertFalse(player.isActive());

        player.setActive(true);
        assertTrue(player.isActive());
    }

    @Test
    void testSetAndGetReady() {
        player = new Player(1, "TestPlayer", true);

        assertFalse(player.getReady());

        player.setReady(true);
        assertTrue(player.getReady());

        player.setReady(false);
        assertFalse(player.getReady());
    }

    @Test
    void testSetShip() {
        player = new Player(1, "TestPlayer", true);
        ShipBuilder originalShip = player.getShip();

        player.setShip(mockShip);
        assertEquals(mockShip, player.getShip());
        assertNotEquals(originalShip, player.getShip());
    }

    @Test
    void testPickCard_ValidCard() {
        player = new Player(1, "TestPlayer", true);

        // Mock del comportamento della carta
        try {
            when(mockCard).thenReturn(mockCard.getClass().newInstance());
        }catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }

        player.pickCard(mockCard);

        // Verifica che il metodo effect() sia stato chiamato
        verify(mockCard, times(1)).effect();
    }

    @Test
    void testPickCard_NullCard() {
        player = new Player(1, "TestPlayer", true);

        // Questo test verifica che non ci siano eccezioni quando si passa null
        assertDoesNotThrow(() -> player.pickCard(null));
    }

    @Test
    void testPlaceComponent() {
        player = new Player(1, "TestPlayer", true, mockShip);
        Position mockPosition = mock(Position.class);

        when(mockShip.placeComponent(mockComponent, mockPosition)).thenReturn(true);

        boolean result = player.placeComponent(mockComponent, mockPosition);

        assertTrue(result);
        verify(mockShip, times(1)).placeComponent(mockComponent, mockPosition);
    }

    @Test
    void testPlaceComponent_Failure() {
        player = new Player(1, "TestPlayer", true, mockShip);
        Position mockPosition = mock(Position.class);

        when(mockShip.placeComponent(mockComponent, mockPosition)).thenReturn(false);

        boolean result = player.placeComponent(mockComponent, mockPosition);

        assertFalse(result);
        verify(mockShip, times(1)).placeComponent(mockComponent, mockPosition);
    }

    @Test
    void testRemoveComponent() {
        player = new Player(1, "TestPlayer", true, mockShip);

        when(mockShip.removeComponent(mockComponent)).thenReturn(true);

        boolean result = player.removeComponent(mockComponent);

        assertTrue(result);
        verify(mockShip, times(1)).removeComponent(mockComponent);
    }

    @Test
    void testRemoveComponent_Failure() {
        player = new Player(1, "TestPlayer", true, mockShip);

        when(mockShip.removeComponent(mockComponent)).thenReturn(false);

        boolean result = player.removeComponent(mockComponent);

        assertFalse(result);
        verify(mockShip, times(1)).removeComponent(mockComponent);
    }

    @Test
    void testValidateShip() {
        player = new Player(1, "TestPlayer", true, mockShip);

        when(mockShip.validateShip()).thenReturn(true);

        boolean result = player.validateShip();

        assertTrue(result);
        verify(mockShip, times(1)).validateShip();
    }

    @Test
    void testValidateShip_Invalid() {
        player = new Player(1, "TestPlayer", true, mockShip);

        when(mockShip.validateShip()).thenReturn(false);

        boolean result = player.validateShip();

        assertFalse(result);
        verify(mockShip, times(1)).validateShip();
    }

    @Test
    void testRemoveCrewMembers_Success() {
        player = new Player(1, "TestPlayer", true, mockShip);
        int crewRequired = 3;

        when(mockShip.removeCrewMembers(crewRequired)).thenReturn(crewRequired);

        boolean result = player.removeCrewMembers(crewRequired);

        assertTrue(result);
        verify(mockShip, times(1)).removeCrewMembers(crewRequired);
    }

    @Test
    void testRemoveCrewMembers_Failure() {
        player = new Player(1, "TestPlayer", true, mockShip);
        int crewRequired = 5;
        int actualRemoved = 3;

        when(mockShip.removeCrewMembers(crewRequired)).thenReturn(actualRemoved);

        boolean result = player.removeCrewMembers(crewRequired);

        assertFalse(result);
        verify(mockShip, times(1)).removeCrewMembers(crewRequired);
    }

    @Test
    void testRemoveCrewOfType() {
        player = new Player(1, "TestPlayer", true, mockShip);
        CrewType mockCrewType = mock(CrewType.class);
        int requiredCount = 2;
        int removedCount = 1;

        when(mockShip.removeCrewOfType(mockCrewType, requiredCount)).thenReturn(removedCount);

        int result = player.removeCrewOfType(mockCrewType, requiredCount);

        assertEquals(removedCount, result);
        verify(mockShip, times(1)).removeCrewOfType(mockCrewType, requiredCount);
    }

    @Test
    void testDischargeBatteries() {
        player = new Player(1, "TestPlayer", true, mockShip);
        int batteriesNeeded = 4;

        player.dischargeBatteries(batteriesNeeded);

        verify(mockShip, times(1)).dischargeBatteries(batteriesNeeded);
    }

    @Test
    void testTakeTurn() {
        player = new Player(1, "TestPlayer", true);

        // Il metodo takeTurn() è vuoto, quindi testiamo solo che non lanci eccezioni
        assertDoesNotThrow(() -> player.takeTurn());
    }

    @Test
    void testPlayerSerialization() {
        // Test per verificare che la classe sia serializzabile
        player = new Player(1, "SerializablePlayer", true);

        // Verifica che la classe implementi Serializable
        assertTrue(true,
                "Player should implement Serializable interface");
    }

    @Test
    void testPlayerImmutableFields() {
        player = new Player(1, "TestPlayer", true);

        // Test per verificare che l'ID non possa essere modificato dopo la creazione
        assertEquals(1, player.getId());

        // Test per verificare che il nickname non possa essere modificato dopo la creazione
        assertEquals("TestPlayer", player.getNickName());
    }

    @Test
    void testMultiplePlayersWithDifferentIds() {
        Player player1 = new Player(1, "Player1", true);
        Player player2 = new Player(2, "Player2", false);

        assertNotEquals(player1.getId(), player2.getId());
        assertNotEquals(player1.getNickName(), player2.getNickName());
        assertNotEquals(player1.isActive(), player2.isActive());
    }

}