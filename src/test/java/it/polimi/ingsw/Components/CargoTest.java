package it.polimi.ingsw.Components;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class CargoTest {

    @Test
    @DisplayName("BlueCargo should have value 1")
    public void testBlueCargoValue() {
        BlueCargo blueCargo = new BlueCargo();
        assertEquals(1, blueCargo.getValue());
    }

    @Test
    @DisplayName("GreenCargo should have value 2")
    public void testGreenCargoValue() {
        GreenCargo greenCargo = new GreenCargo();
        assertEquals(2, greenCargo.getValue());
    }

    @Test
    @DisplayName("YellowCargo should have value 3")
    public void testYellowCargoValue() {
        YellowCargo yellowCargo = new YellowCargo();
        assertEquals(3, yellowCargo.getValue());
    }

    @Test
    @DisplayName("RedCargo should have value 4")
    public void testRedCargoValue() {
        RedCargo redCargo = new RedCargo();
        assertEquals(4, redCargo.getValue());
    }

    @Test
    @DisplayName("RedCargo should return true for isRed()")
    public void testRedCargoIsRed() {
        RedCargo redCargo = new RedCargo();
        assertTrue(redCargo.isRed());
    }

    @Test
    @DisplayName("BlueCargo should return false for isRed()")
    public void testBlueCargoIsNotRed() {
        BlueCargo blueCargo = new BlueCargo();
        assertFalse(blueCargo.isRed());
    }

    @Test
    @DisplayName("GreenCargo should return false for isRed()")
    public void testGreenCargoIsNotRed() {
        GreenCargo greenCargo = new GreenCargo();
        assertFalse(greenCargo.isRed());
    }

    @Test
    @DisplayName("YellowCargo should return false for isRed()")
    public void testYellowCargoIsNotRed() {
        YellowCargo yellowCargo = new YellowCargo();
        assertFalse(yellowCargo.isRed());
    }

    @Test
    @DisplayName("Test polymorphism - all cargo types should be instances of Cargo")
    public void testPolymorphism() {
        Cargo blueCargo = new BlueCargo();
        Cargo greenCargo = new GreenCargo();
        Cargo yellowCargo = new YellowCargo();
        Cargo redCargo = new RedCargo();

        assertInstanceOf(Cargo.class, blueCargo);
        assertInstanceOf(Cargo.class, greenCargo);
        assertInstanceOf(Cargo.class, yellowCargo);
        assertInstanceOf(Cargo.class, redCargo);
    }

    @Test
    @DisplayName("Test specific instance types")
    public void testInstanceTypes() {
        BlueCargo blueCargo = new BlueCargo();
        GreenCargo greenCargo = new GreenCargo();
        YellowCargo yellowCargo = new YellowCargo();
        RedCargo redCargo = new RedCargo();

        assertInstanceOf(BlueCargo.class, blueCargo);
        assertInstanceOf(GreenCargo.class, greenCargo);
        assertInstanceOf(YellowCargo.class, yellowCargo);
        assertInstanceOf(RedCargo.class, redCargo);
    }

    @Test
    @DisplayName("Test pattern matching with polymorphic references")
    public void testPatternMatchingPolymorphic() {
        Cargo redCargo = new RedCargo();
        Cargo blueCargo = new BlueCargo();
        Cargo greenCargo = new GreenCargo();
        Cargo yellowCargo = new YellowCargo();

        assertTrue(redCargo.isRed());
        assertFalse(blueCargo.isRed());
        assertFalse(greenCargo.isRed());
        assertFalse(yellowCargo.isRed());
    }

    @Test
    @DisplayName("Test cargo values are different and ordered")
    public void testCargoValuesOrdering() {
        BlueCargo blueCargo = new BlueCargo();
        GreenCargo greenCargo = new GreenCargo();
        YellowCargo yellowCargo = new YellowCargo();
        RedCargo redCargo = new RedCargo();

        assertTrue(blueCargo.getValue() < greenCargo.getValue());
        assertTrue(greenCargo.getValue() < yellowCargo.getValue());
        assertTrue(yellowCargo.getValue() < redCargo.getValue());
    }

    @Test
    @DisplayName("Test equals and hashCode consistency for same type instances")
    public void testEqualsAndHashCode() {
        BlueCargo blueCargo1 = new BlueCargo();
        BlueCargo blueCargo2 = new BlueCargo();

        // Since no custom equals is defined, should use reference equality
        assertNotEquals(blueCargo1, blueCargo2);

        // But values should be the same
        assertEquals(blueCargo1.getValue(), blueCargo2.getValue());
    }
}