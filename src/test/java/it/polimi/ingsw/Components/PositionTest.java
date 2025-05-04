package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;
import it.polimi.ingsw.Position;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

    @Test
    void testPositionCreation() {
        Position pos = new Position(3, 5);
        assertNotNull(pos);
        assertEquals(3, pos.getX()); // Assuming getters exist
        assertEquals(5, pos.getY()); // Assuming getters exist
    }

    @Test
    void testEqualsAndHashCode() {
        Position pos1 = new Position(2, 4);
        Position pos2 = new Position(2, 4);
        Position pos3 = new Position(1, 4);
        Position pos4 = new Position(2, 5);

        assertEquals(pos1, pos2, "Positions with same coordinates should be equal.");
        assertNotEquals(pos1, pos3, "Positions with different X should not be equal.");
        assertNotEquals(pos1, pos4, "Positions with different Y should not be equal.");
        assertNotEquals(pos1, null, "Position should not be equal to null.");
        assertNotEquals(pos1, new Object(), "Position should not be equal to a different object type.");

        assertEquals(pos1.hashCode(), pos2.hashCode(), "Hash codes should be equal for equal objects.");
        // Hash codes *might* be different for non-equal objects, but it's not guaranteed.
        // assertNotEquals(pos1.hashCode(), pos3.hashCode()); // Not a strict requirement
    }

} 