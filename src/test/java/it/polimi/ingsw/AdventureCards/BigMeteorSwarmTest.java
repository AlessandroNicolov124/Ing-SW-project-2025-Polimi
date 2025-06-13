package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BigMeteorSwarmTest {
    private BigMeteorSwarm bigMeteorSwarm;
    private Player player;

    @BeforeEach
    void setUp() {
        bigMeteorSwarm = new BigMeteorSwarm(2, 3, 5);
        player = new Player(1, "TestPlayer", true, null); // ShipBuilder null per test base
    }

    @Test
    void testAllMeteorsDestroyComponentWhenNoDefense() {
        // Poiché defendAgainstMeteor restituisce sempre false e checkMeteorHit sempre true,
        // ogni meteorite dovrebbe distruggere un componente
        int destroyed = bigMeteorSwarm.processMeteorImpacts(player);
        assertEquals(5, destroyed, "Tutte le meteore dovrebbero distruggere un componente");
    }

    @Test
    void testRollDiceRange() throws Exception {
        // Usa reflection per accedere al metodo privato rollDice
        java.lang.reflect.Method rollDice = BigMeteorSwarm.class.getDeclaredMethod("rollDice");
        rollDice.setAccessible(true);
        for (int i = 0; i < 100; i++) {
            int result = (int) rollDice.invoke(bigMeteorSwarm);
            assertTrue(result >= 2 && result <= 12, "Il risultato del dado deve essere tra 2 e 12");
        }
    }
}
