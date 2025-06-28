package it.polimi.ingsw;

import it.polimi.ingsw.Components.Cabin;
import it.polimi.ingsw.Components.LifeSupportSystemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

class CrewTest {

    private Cabin testCabin;

    @BeforeEach
    void setUp() {
        testCabin = new Cabin();
    }

    @Nested
    @DisplayName("BrownAlienCrew Tests")
    class BrownAlienCrewTest {

        private BrownAlienCrew brownAlienCrew;

        @BeforeEach
        void setUp() {
            brownAlienCrew = new BrownAlienCrew();
        }

        @Test
        @DisplayName("Test BrownAlienCrew creation")
        void testBrownAlienCrewCreation() {
            assertNotNull(brownAlienCrew);
            assertEquals(CrewType.BROWN_ALIEN, brownAlienCrew.getType());
        }

        @Test
        @DisplayName("Test canLiveIn with brown life support - should return true")
        void testCanLiveInWithBrownLifeSupport() {
            testCabin.connectLifeSupport(LifeSupportSystemType.BROWN);
            boolean result = brownAlienCrew.canLiveIn(testCabin);
            assertTrue(result);
        }

        @Test
        @DisplayName("Test canLiveIn without brown life support - should return false")
        void testCanLiveInWithoutBrownLifeSupport() {
            // Nessun supporto vitale connesso
            boolean result = brownAlienCrew.canLiveIn(testCabin);
            assertFalse(result);
        }

        @Test
        @DisplayName("Test canLiveIn with null cabin - should throw exception")
        void testCanLiveInWithNullCabin() {
            assertThrows(NullPointerException.class, () -> {
                brownAlienCrew.canLiveIn(null);
            });
        }

        @Test
        @DisplayName("Test multiple calls to canLiveIn")
        void testMultipleCallsToCanLiveIn() {
            testCabin.connectLifeSupport(LifeSupportSystemType.BROWN);
            assertTrue(brownAlienCrew.canLiveIn(testCabin));
            assertTrue(brownAlienCrew.canLiveIn(testCabin));
        }

        @Test
        @DisplayName("Test BrownAlienCrew inheritance")
        void testBrownAlienCrewInheritance() {
            assertInstanceOf(Crew.class, brownAlienCrew);
        }
    }

    @Nested
    @DisplayName("HumanCrew Tests")
    class HumanCrewTest {

        private HumanCrew humanCrew;

        @BeforeEach
        void setUp() {
            humanCrew = new HumanCrew();
        }

        @Test
        @DisplayName("Test HumanCrew creation")
        void testHumanCrewCreation() {
            assertNotNull(humanCrew);
            assertEquals(CrewType.HUMAN, humanCrew.getType());
        }

        @Test
        @DisplayName("Test canLiveIn always returns true")
        void testCanLiveInAlwaysReturnsTrue() {
            boolean result = humanCrew.canLiveIn(testCabin);
            assertTrue(result);
        }

        @Test
        @DisplayName("Test canLiveIn with different cabin states")
        void testCanLiveInWithDifferentCabinStates() {
            // Test con vari stati della cabina, sempre true
            assertTrue(humanCrew.canLiveIn(testCabin));
            testCabin.connectLifeSupport(LifeSupportSystemType.BROWN);
            assertTrue(humanCrew.canLiveIn(testCabin));
        }

        @Test
        @DisplayName("Test canLiveIn with null cabin - should still return true")
        void testCanLiveInWithNullCabin() {
            boolean result = humanCrew.canLiveIn(null);
            assertTrue(result);
        }

        @Test
        @DisplayName("Test multiple calls to canLiveIn")
        void testMultipleCallsToCanLiveIn() {
            assertTrue(humanCrew.canLiveIn(testCabin));
            assertTrue(humanCrew.canLiveIn(testCabin));
            assertTrue(humanCrew.canLiveIn(testCabin));
        }

        @Test
        @DisplayName("Test HumanCrew inheritance")
        void testHumanCrewInheritance() {
            assertInstanceOf(Crew.class, humanCrew);
        }
    }

    @Nested
    @DisplayName("PurpleAlienCrew Tests")
    class PurpleAlienCrewTest {

        private PurpleAlienCrew purpleAlienCrew;

        @BeforeEach
        void setUp() {
            purpleAlienCrew = new PurpleAlienCrew();
        }

        @Test
        @DisplayName("Test PurpleAlienCrew creation")
        void testPurpleAlienCrewCreation() {
            assertNotNull(purpleAlienCrew);
            assertEquals(CrewType.PURPLE_ALIEN, purpleAlienCrew.getType());
        }

        @Test
        @DisplayName("Test canLiveIn with purple life support - should return true")
        void testCanLiveInWithPurpleLifeSupport() {
            testCabin.connectLifeSupport(LifeSupportSystemType.PURPLE);

            boolean result = purpleAlienCrew.canLiveIn(testCabin);

            assertTrue(result);
        }

        @Test
        @DisplayName("Test canLiveIn without purple life support - should return false")
        void testCanLiveInWithoutPurpleLifeSupport() {
            // Nessun supporto vitale connesso
            boolean result = purpleAlienCrew.canLiveIn(testCabin);

            assertFalse(result);
        }

        @Test
        @DisplayName("Test canLiveIn with wrong life support type")
        void testCanLiveInWithWrongLifeSupportType() {
            // La cabina ha solo il supporto vitale marrone connesso
            testCabin.connectLifeSupport(LifeSupportSystemType.BROWN);

            boolean result = purpleAlienCrew.canLiveIn(testCabin);

            assertFalse(result);
        }

        @Test
        @DisplayName("Test canLiveIn with null cabin - should throw exception")
        void testCanLiveInWithNullCabin() {
            assertThrows(NullPointerException.class, () -> {
                purpleAlienCrew.canLiveIn(null);
            });
        }

        @Test
        @DisplayName("Test multiple calls to canLiveIn")
        void testMultipleCallsToCanLiveIn() {
            testCabin.connectLifeSupport(LifeSupportSystemType.PURPLE);

            assertTrue(purpleAlienCrew.canLiveIn(testCabin));
            assertTrue(purpleAlienCrew.canLiveIn(testCabin));
        }

        @Test
        @DisplayName("Test PurpleAlienCrew inheritance")
        void testPurpleAlienCrewInheritance() {
            assertInstanceOf(Crew.class, purpleAlienCrew);
        }
    }

    @Nested
    @DisplayName("Crew Comparison Tests")
    class CrewComparisonTest {

        private BrownAlienCrew brownAlienCrew;
        private HumanCrew humanCrew;
        private PurpleAlienCrew purpleAlienCrew;

        @BeforeEach
        void setUp() {
            brownAlienCrew = new BrownAlienCrew();
            humanCrew = new HumanCrew();
            purpleAlienCrew = new PurpleAlienCrew();
        }

        @Test
        @DisplayName("Test different crew types have different CrewType")
        void testDifferentCrewTypes() {
            assertNotEquals(brownAlienCrew.getType(), humanCrew.getType());
            assertNotEquals(brownAlienCrew.getType(), purpleAlienCrew.getType());
            assertNotEquals(humanCrew.getType(), purpleAlienCrew.getType());
        }

        @Test
        @DisplayName("Test crew behavior differences")
        void testCrewBehaviorDifferences() {
            // Setup cabin with no life support
            testCabin.disconnectLifeSupport(LifeSupportSystemType.BROWN);
            testCabin.disconnectLifeSupport(LifeSupportSystemType.PURPLE);

            // Human should always be able to live in any cabin
            assertTrue(humanCrew.canLiveIn(testCabin));

            // Aliens should not be able to live without their specific life support
            assertFalse(brownAlienCrew.canLiveIn(testCabin));
            assertFalse(purpleAlienCrew.canLiveIn(testCabin));
        }

        @Test
        @DisplayName("Test crew behavior with specific life supports")
        void testCrewBehaviorWithSpecificLifeSupports() {
            // Setup cabin with brown life support only
            testCabin.connectLifeSupport(LifeSupportSystemType.BROWN);

            assertTrue(humanCrew.canLiveIn(testCabin)); // Humans can live anywhere
            assertTrue(brownAlienCrew.canLiveIn(testCabin)); // Brown aliens can live with brown support
            assertFalse(purpleAlienCrew.canLiveIn(testCabin)); // Purple aliens need purple support

            // Setup cabin with purple life support only
            testCabin.disconnectLifeSupport(LifeSupportSystemType.BROWN);
            testCabin.connectLifeSupport(LifeSupportSystemType.PURPLE);

            assertTrue(humanCrew.canLiveIn(testCabin)); // Humans can live anywhere
            assertFalse(brownAlienCrew.canLiveIn(testCabin)); // Brown aliens need brown support
            assertTrue(purpleAlienCrew.canLiveIn(testCabin)); // Purple aliens can live with purple support
        }

        @Test
        @DisplayName("Test all crew types can be created multiple times")
        void testMultipleCrewCreation() {
            BrownAlienCrew brownAlien1 = new BrownAlienCrew();
            BrownAlienCrew brownAlien2 = new BrownAlienCrew();

            HumanCrew human1 = new HumanCrew();
            HumanCrew human2 = new HumanCrew();

            PurpleAlienCrew purpleAlien1 = new PurpleAlienCrew();
            PurpleAlienCrew purpleAlien2 = new PurpleAlienCrew();

            // All should have the same crew type but be different instances
            assertEquals(brownAlien1.getType(), brownAlien2.getType());
            assertEquals(human1.getType(), human2.getType());
            assertEquals(purpleAlien1.getType(), purpleAlien2.getType());

            assertNotSame(brownAlien1, brownAlien2);
            assertNotSame(human1, human2);
            assertNotSame(purpleAlien1, purpleAlien2);
        }
    }
}