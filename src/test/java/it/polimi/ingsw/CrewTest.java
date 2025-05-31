package it.polimi.ingsw;

import it.polimi.ingsw.Components.Cabin;
import it.polimi.ingsw.Components.LifeSupportSystemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CrewTest {

    @Mock
    private Cabin mockCabin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
            when(mockCabin.hasConnectedLifeSupport(LifeSupportSystemType.BROWN)).thenReturn(true);

            boolean result = brownAlienCrew.canLiveIn(mockCabin);

            assertTrue(result);
            verify(mockCabin, times(1)).hasConnectedLifeSupport(LifeSupportSystemType.BROWN);
        }

        @Test
        @DisplayName("Test canLiveIn without brown life support - should return false")
        void testCanLiveInWithoutBrownLifeSupport() {
            when(mockCabin.hasConnectedLifeSupport(LifeSupportSystemType.BROWN)).thenReturn(false);

            boolean result = brownAlienCrew.canLiveIn(mockCabin);

            assertFalse(result);
            verify(mockCabin, times(1)).hasConnectedLifeSupport(LifeSupportSystemType.BROWN);
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
            when(mockCabin.hasConnectedLifeSupport(LifeSupportSystemType.BROWN)).thenReturn(true);

            assertTrue(brownAlienCrew.canLiveIn(mockCabin));
            assertTrue(brownAlienCrew.canLiveIn(mockCabin));

            verify(mockCabin, times(2)).hasConnectedLifeSupport(LifeSupportSystemType.BROWN);
        }

        @Test
        @DisplayName("Test BrownAlienCrew inheritance")
        void testBrownAlienCrewInheritance() {
            assertTrue(brownAlienCrew instanceof Crew);
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
            boolean result = humanCrew.canLiveIn(mockCabin);

            assertTrue(result);
            // Verify that no methods are called on the cabin mock
            verifyNoInteractions(mockCabin);
        }

        @Test
        @DisplayName("Test canLiveIn with different cabin states")
        void testCanLiveInWithDifferentCabinStates() {
            // Test with various mock cabin setups to ensure it always returns true
            when(mockCabin.hasConnectedLifeSupport(any())).thenReturn(false);
            assertTrue(humanCrew.canLiveIn(mockCabin));

            when(mockCabin.hasConnectedLifeSupport(any())).thenReturn(true);
            assertTrue(humanCrew.canLiveIn(mockCabin));

            // The human crew should not even check life support
            verify(mockCabin, never()).hasConnectedLifeSupport(any());
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
            assertTrue(humanCrew.canLiveIn(mockCabin));
            assertTrue(humanCrew.canLiveIn(mockCabin));
            assertTrue(humanCrew.canLiveIn(mockCabin));

            verifyNoInteractions(mockCabin);
        }

        @Test
        @DisplayName("Test HumanCrew inheritance")
        void testHumanCrewInheritance() {
            assertTrue(humanCrew instanceof Crew);
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
            when(mockCabin.hasConnectedLifeSupport(LifeSupportSystemType.PURPLE)).thenReturn(true);

            boolean result = purpleAlienCrew.canLiveIn(mockCabin);

            assertTrue(result);
            verify(mockCabin, times(1)).hasConnectedLifeSupport(LifeSupportSystemType.PURPLE);
        }

        @Test
        @DisplayName("Test canLiveIn without purple life support - should return false")
        void testCanLiveInWithoutPurpleLifeSupport() {
            when(mockCabin.hasConnectedLifeSupport(LifeSupportSystemType.PURPLE)).thenReturn(false);

            boolean result = purpleAlienCrew.canLiveIn(mockCabin);

            assertFalse(result);
            verify(mockCabin, times(1)).hasConnectedLifeSupport(LifeSupportSystemType.PURPLE);
        }

        @Test
        @DisplayName("Test canLiveIn with wrong life support type")
        void testCanLiveInWithWrongLifeSupportType() {
            // Mock cabin has brown life support but not purple
            when(mockCabin.hasConnectedLifeSupport(LifeSupportSystemType.PURPLE)).thenReturn(false);
            when(mockCabin.hasConnectedLifeSupport(LifeSupportSystemType.BROWN)).thenReturn(true);

            boolean result = purpleAlienCrew.canLiveIn(mockCabin);

            assertFalse(result);
            verify(mockCabin, times(1)).hasConnectedLifeSupport(LifeSupportSystemType.PURPLE);
            verify(mockCabin, never()).hasConnectedLifeSupport(LifeSupportSystemType.BROWN);
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
            when(mockCabin.hasConnectedLifeSupport(LifeSupportSystemType.PURPLE)).thenReturn(true);

            assertTrue(purpleAlienCrew.canLiveIn(mockCabin));
            assertTrue(purpleAlienCrew.canLiveIn(mockCabin));

            verify(mockCabin, times(2)).hasConnectedLifeSupport(LifeSupportSystemType.PURPLE);
        }

        @Test
        @DisplayName("Test PurpleAlienCrew inheritance")
        void testPurpleAlienCrewInheritance() {
            assertTrue(purpleAlienCrew instanceof Crew);
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
            when(mockCabin.hasConnectedLifeSupport(any())).thenReturn(false);

            // Human should always be able to live in any cabin
            assertTrue(humanCrew.canLiveIn(mockCabin));

            // Aliens should not be able to live without their specific life support
            assertFalse(brownAlienCrew.canLiveIn(mockCabin));
            assertFalse(purpleAlienCrew.canLiveIn(mockCabin));
        }

        @Test
        @DisplayName("Test crew behavior with specific life supports")
        void testCrewBehaviorWithSpecificLifeSupports() {
            // Setup cabin with brown life support only
            when(mockCabin.hasConnectedLifeSupport(LifeSupportSystemType.BROWN)).thenReturn(true);
            when(mockCabin.hasConnectedLifeSupport(LifeSupportSystemType.PURPLE)).thenReturn(false);

            assertTrue(humanCrew.canLiveIn(mockCabin)); // Humans can live anywhere
            assertTrue(brownAlienCrew.canLiveIn(mockCabin)); // Brown aliens can live with brown support
            assertFalse(purpleAlienCrew.canLiveIn(mockCabin)); // Purple aliens need purple support

            // Setup cabin with purple life support only
            when(mockCabin.hasConnectedLifeSupport(LifeSupportSystemType.BROWN)).thenReturn(false);
            when(mockCabin.hasConnectedLifeSupport(LifeSupportSystemType.PURPLE)).thenReturn(true);

            assertTrue(humanCrew.canLiveIn(mockCabin)); // Humans can live anywhere
            assertFalse(brownAlienCrew.canLiveIn(mockCabin)); // Brown aliens need brown support
            assertTrue(purpleAlienCrew.canLiveIn(mockCabin)); // Purple aliens can live with purple support
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