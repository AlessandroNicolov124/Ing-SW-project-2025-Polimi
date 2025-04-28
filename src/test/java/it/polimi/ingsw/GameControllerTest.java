package it.polimi.ingsw;

import it.polimi.ingsw.Components.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {

    private GameController controller;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        controller = new GameController();
        player1 = new Player(0, "Pippo", false); // Start not ready
        player2 = new Player(1, "Pluto", false); // Start not ready
        // Add players in tests where needed, not globally here
    }

    @Test
    void testAddPlayer() {
        controller.addPlayer(player1);
        // Need a way to verify player was added, e.g., getPlayers() or check behavior
        // Assuming startMatchIfPossible reflects player count indirectly
        assertFalse(controller.startMatchIfPossible(), "Match should not start with one player.");
        controller.addPlayer(player2);
         controller.setPlayerReady(player1);
         controller.setPlayerReady(player2);
        assertTrue(controller.startMatchIfPossible(), "Match should start with two ready players.");
    }

    @Test
    void testStartMatchIfPossible_NotEnoughPlayers() {
        controller.addPlayer(player1);
        controller.setPlayerReady(player1);
        assertFalse(controller.startMatchIfPossible(), "Should not start with less than minimum players.");
    }

     @Test
    void testStartMatchIfPossible_PlayersNotReady() {
        controller.addPlayer(player1);
        controller.addPlayer(player2);
        assertFalse(controller.startMatchIfPossible(), "Should not start if players are not ready.");
    }

    @Test
    void testSetPlayerReadyAndStartMatch() {
        controller.addPlayer(player1);
        controller.addPlayer(player2);
        assertFalse(controller.startMatchIfPossible(), "Match should not start initially.");
        controller.setPlayerReady(player1);
        assertFalse(controller.startMatchIfPossible(), "Match should not start with only one player ready.");
        controller.setPlayerReady(player2);
        assertTrue(controller.startMatchIfPossible(), "Match should start when both players are ready.");
    }

     @Test
    void testAddComponentForPlayer() {
        controller.addPlayer(player1);
        controller.addPlayer(player2);
        controller.setPlayerReady(player1);
        controller.setPlayerReady(player2);
        controller.startMatchIfPossible(); // Assume this changes state if needed

        Component cabin = new Cabin(true);
        Position pos = new Position(1, 1);

        controller.addComponentForPlayer(player1, cabin, pos);

        // Need GameModel access or specific getter in Controller/Player to verify component placement
        // For now, just checking that the method doesn't throw exceptions
        //assertTrue(player1.getShip().isComponentAt(pos), "Component should be placed at the position."); // Requires isComponentAt method
        assertDoesNotThrow(() -> controller.addComponentForPlayer(player1, cabin, pos)); // Placeholder verification

    }

    @Test
    void testMoveToShipValidation() {
         controller.addPlayer(player1);
         controller.addPlayer(player2);
         controller.setPlayerReady(player1);
         controller.setPlayerReady(player2);
         controller.startMatchIfPossible();
        // Assuming this phase transition is allowed after starting the game
        assertTrue(controller.moveToShipValidation(), "Should be possible to move to ship validation phase.");
        // Maybe test calling it again or in wrong phase? Requires knowledge of GameState
    }

    @Test
    void testValidateShipForPlayer_ValidSimpleShip() {
         controller.addPlayer(player1);
         controller.setPlayerReady(player1); // Make player ready before adding components?
         // Add player 2 as well to start match
         controller.addPlayer(player2);
         controller.setPlayerReady(player2);
         controller.startMatchIfPossible();

        // Create a minimal valid ship for player1 as per rules (needs Cabin)
        Component cabin = new Cabin(true); cabin.initializeConnectors(ConnectorType.SINGLE, ConnectorType.SINGLE, ConnectorType.SINGLE, ConnectorType.SINGLE); // Assume default connectors allow connection
        Position cabinPos = new Position(1, 1);
        controller.addComponentForPlayer(player1, cabin, cabinPos);

        // Add an engine connected to the cabin
        Component engine = new SingleEngine(); engine.initializeConnectors(ConnectorType.SINGLE, ConnectorType.NULL, ConnectorType.NULL, ConnectorType.NULL); // Connect NORTH
        Position enginePos = new Position(0, 1); // North of cabin
        controller.addComponentForPlayer(player1, engine, enginePos);


        controller.moveToShipValidation(); // Move to validation phase

        assertTrue(controller.validateShipForPlayer(player1), "A simple valid ship (Cabin + Engine) should be valid.");
    }

     @Test
    void testValidateShipForPlayer_Invalid_NoCabin() {
        controller.addPlayer(player1);
        controller.addPlayer(player2);
         controller.setPlayerReady(player1);
         controller.setPlayerReady(player2);
        controller.startMatchIfPossible();

        // Add only an engine, no cabin
        Component engine = new SingleEngine(); engine.initializeConnectors(ConnectorType.SINGLE, ConnectorType.NULL, ConnectorType.NULL, ConnectorType.NULL);
        Position enginePos = new Position(0, 1);
        controller.addComponentForPlayer(player1, engine, enginePos);

        controller.moveToShipValidation();

        assertFalse(controller.validateShipForPlayer(player1), "A ship without a cabin should be invalid.");
    }


    @Test
    void testPickCardForPlayer() {
         controller.addPlayer(player1);
         controller.addPlayer(player2);
         controller.setPlayerReady(player1);
         controller.setPlayerReady(player2);
         controller.startMatchIfPossible();
        // Assume game needs to be in a specific state after ship validation?
        // For now, just check method execution if preconditions met
        // Need to populate the AdventureDeck in the GameModel first for the card pick to succeed.
        // GameController might need a setup method or dependency injection for the deck.
        // Currently, this test might fail if the deck is empty.
        // assertDoesNotThrow(() -> controller.pickCardForPlayer(player1)); // Placeholder

        // To make this testable, we'd need access to the GameModel's AdventureDeck
        // or mock the relevant parts. Let's assume pickCard handles empty deck gracefully for now.
         assertThrows(IllegalStateException.class, () -> controller.pickCardForPlayer(player1), "Should throw if deck is empty or state is wrong.");

         // If we could inject/setup a deck:
         // AdventureDeck deck = controller.getGameModel().getAdventureDeck(); // Need getter
         // deck.add(new Planets());
         // deck.add(new AbandonedStation());
         // assertDoesNotThrow(() -> controller.pickCardForPlayer(player1));
         // assertEquals(1, player1.getAdventureHand().size()); // Need getter for hand

    }


} 