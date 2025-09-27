package edu.mit.sidpac.flightsearch.service;

import edu.mit.sidpac.flightsearch.entity.*;
import edu.mit.sidpac.flightsearch.repository.*;
import edu.mit.sidpac.flightsearch.util.TestDatabaseSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import edu.mit.sidpac.flightsearch.config.TestJpaAuditingConfig;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive numerical tests for fare calculation logic
 * Tests the actual pricing calculations with expected values
 * Verifies fare restriction logic and fare selection algorithms
 * Uses full database data to test realistic pricing scenarios
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestJpaAuditingConfig.class)
class FareCalculationTest {

    @Autowired
    private FlightSearchService flightSearchService;

    @Autowired
    private FareRepository fareRepository;

    @Autowired
    private FareRestrictionRepository fareRestrictionRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private AirlineRepository airlineRepository;

    @Autowired
    private TestDatabaseSetup testDatabaseSetup;

    private Airport bosAirport;
    private Airport laxAirport;
    private Airport jfkAirport;
    private Airport dfwAirport;
    private Airport atlAirport;
    private Airport ordAirport;
    private Airline aaAirline;
    private Airline dlAirline;
    private Airline uaAirline;
    private Airline b6Airline;

    @BeforeEach
    void setUp() throws Exception {
        // Load the complete database schema and data
        testDatabaseSetup.loadFullDatabaseData();
        
        // Get airports and airlines for testing
        bosAirport = airportRepository.findByCode("BOS").orElseThrow();
        laxAirport = airportRepository.findByCode("LAX").orElseThrow();
        jfkAirport = airportRepository.findByCode("JFK").orElseThrow();
        dfwAirport = airportRepository.findByCode("DFW").orElseThrow();
        atlAirport = airportRepository.findByCode("ATL").orElseThrow();
        ordAirport = airportRepository.findByCode("ORD").orElseThrow();
        
        aaAirline = airlineRepository.findByCode("AA").orElseThrow();
        dlAirline = airlineRepository.findByCode("DL").orElseThrow();
        uaAirline = airlineRepository.findByCode("UA").orElseThrow();
        b6Airline = airlineRepository.findByCode("B6").orElseThrow();
    }

    /**
     * Test: Standard fare calculation (no restrictions)
     * Verifies that standard fares are applied when no special restrictions apply
     */
    @Test
    void testFareCalculation_StandardFare() {
        // Given: AA flight from LAX to SFO (no hub specials apply)
        // LAX and SFO are not AA hubs, so no endpoint restrictions apply
        Flight flight = createTestFlight("AA123", laxAirport, airportRepository.findByCode("SFO").orElseThrow(),
                LocalDateTime.of(2024, 3, 20, 14, 0), // 2:00 PM departure (not early bird - after 09:00)
                LocalDateTime.of(2024, 3, 20, 16, 0), aaAirline);

        // When: Calculate fare price for single-leg trip
        BigDecimal price = calculateFarePrice(flight, aaAirline, 1);

        // Then: Should return standard fare of $200.00
        // Expected: $200.00 (AA Standard Fare - no restrictions apply)
        // Available fares: Standard $200, BOS Special $150 (not applicable), Early Bird $175 (not applicable), 
        //                  Multi-Leg $160 (not applicable), DFW Hub $130 (not applicable)
        assertNotNull(price, "Price should not be null");
        assertEquals(new BigDecimal("200.00"), price, "Should return standard AA fare of $200.00");
    }

    /**
     * Test: Debug - Show all AA fares and their restrictions
     * This test helps understand what fares are available and why certain prices are returned
     */
    @Test
    void testDebug_ShowAllAAFares() {
        // Get all AA fares
        List<Fare> aaFares = fareRepository.findFaresByAirlineCode("AA");
        
        System.out.println("=== AA Fares Debug ===");
        for (Fare fare : aaFares) {
            System.out.println("Fare: " + fare.getFareName() + " - $" + fare.getBasePrice());
            System.out.println("  Restrictions: " + fare.getRestrictions().size());
            for (FareRestriction restriction : fare.getRestrictions()) {
                System.out.println("    " + restriction.getRestrictionType() + ": " + restriction.getRestrictionValue());
            }
        }
        
        // This test always passes - it's just for debugging
        assertTrue(true, "Debug test always passes");
    }

    /**
     * Test: Debug - Show which fares apply to a specific flight
     * This test helps understand why certain prices are returned for specific flights
     */
    @Test
    void testDebug_ShowApplicableFares() {
        // Create a test flight
        Flight flight = createTestFlight("AA123", laxAirport, airportRepository.findByCode("SFO").orElseThrow(),
                LocalDateTime.of(2024, 3, 20, 14, 0), // 2:00 PM departure (not early bird)
                LocalDateTime.of(2024, 3, 20, 16, 0), aaAirline);

        // Get all AA fares
        List<Fare> aaFares = fareRepository.findFaresByAirlineCode("AA");
        
        System.out.println("=== Applicable Fares for LAX→SFO Flight ===");
        for (Fare fare : aaFares) {
            boolean applicable = isFareApplicable(fare, flight, 1);
            System.out.println("Fare: " + fare.getFareName() + " - $" + fare.getBasePrice() + " - Applicable: " + applicable);
            if (applicable) {
                System.out.println("  ✓ This fare applies to the flight");
            } else {
                System.out.println("  ✗ This fare does not apply to the flight");
                for (FareRestriction restriction : fare.getRestrictions()) {
                    boolean restrictionSatisfied = isRestrictionSatisfied(restriction, flight, 1);
                    System.out.println("    Restriction " + restriction.getRestrictionType() + "=" + restriction.getRestrictionValue() + ": " + restrictionSatisfied);
                }
            }
        }
        
        // This test always passes - it's just for debugging
        assertTrue(true, "Debug test always passes");
    }

    /**
     * Test: BOS Hub Special fare calculation
     * Verifies that hub specials are applied for flights departing from BOS
     */
    @Test
    void testFareCalculation_BOSHubSpecial() {
        // Given: AA flight from BOS to LAX (BOS hub special should apply)
        // BOS is an AA hub, so BOS Special fare restriction applies
        Flight flight = createTestFlight("AA123", bosAirport, laxAirport,
                LocalDateTime.of(2024, 3, 20, 14, 0), // 2:00 PM departure (not early bird - after 09:00)
                LocalDateTime.of(2024, 3, 20, 20, 0), aaAirline);

        // When: Calculate fare price for single-leg trip
        BigDecimal price = calculateFarePrice(flight, aaAirline, 1);

        // Then: Should return BOS hub special of $150.00
        // Expected: $150.00 (AA BOS Special - lowest applicable fare)
        // Available fares: Standard $200, BOS Special $150 ✓ (applicable), Early Bird $175 (not applicable), 
        //                  Multi-Leg $160 (not applicable), DFW Hub $130 (not applicable)
        assertNotNull(price, "Price should not be null");
        assertEquals(new BigDecimal("150.00"), price, "Should return BOS hub special of $150.00");
    }

    /**
     * Test: DFW Hub Special fare calculation
     * Verifies that DFW hub specials are applied for flights departing from DFW
     */
    @Test
    void testFareCalculation_DFWHubSpecial() {
        // Given: AA flight from DFW to LAX (DFW hub special should apply)
        // DFW is an AA hub, so DFW Hub Special fare restriction applies
        Flight flight = createTestFlight("AA123", dfwAirport, laxAirport,
                LocalDateTime.of(2024, 3, 20, 14, 0), // 2:00 PM departure (not early bird - after 09:00)
                LocalDateTime.of(2024, 3, 20, 16, 0), aaAirline);

        // When: Calculate fare price for single-leg trip
        BigDecimal price = calculateFarePrice(flight, aaAirline, 1);

        // Then: Should return DFW hub special of $130.00 (lowest AA fare)
        // Expected: $130.00 (AA DFW Hub Special - lowest applicable fare)
        // Available fares: Standard $200, BOS Special $150 (not applicable), Early Bird $175 (not applicable), 
        //                  Multi-Leg $160 (not applicable), DFW Hub $130 ✓ (applicable)
        assertNotNull(price, "Price should not be null");
        assertEquals(new BigDecimal("130.00"), price, "Should return DFW hub special of $130.00");
    }

    /**
     * Test: Early Bird Special fare calculation
     * Verifies that early bird discounts are applied for morning departures
     */
    @Test
    void testFareCalculation_EarlyBirdSpecial() {
        // Given: AA flight from LAX to SFO departing at 8:30 AM (before 9:00 AM)
        // Departure time is before 09:00, so Early Bird fare restriction applies
        Flight flight = createTestFlight("AA123", laxAirport, airportRepository.findByCode("SFO").orElseThrow(),
                LocalDateTime.of(2024, 3, 20, 8, 30), // 8:30 AM departure (early bird - before 09:00)
                LocalDateTime.of(2024, 3, 20, 10, 30), aaAirline);

        // When: Calculate fare price for single-leg trip
        BigDecimal price = calculateFarePrice(flight, aaAirline, 1);

        // Then: Should return early bird special of $175.00
        // Expected: $175.00 (AA Early Bird - lowest applicable fare)
        // Available fares: Standard $200, BOS Special $150 (not applicable), Early Bird $175 ✓ (applicable), 
        //                  Multi-Leg $160 (not applicable), DFW Hub $130 (not applicable)
        assertNotNull(price, "Price should not be null");
        assertEquals(new BigDecimal("175.00"), price, "Should return early bird special of $175.00");
    }

    /**
     * Test: Multi-Leg Discount fare calculation
     * Verifies that multi-leg discounts are applied for connecting flights
     */
    @Test
    void testFareCalculation_MultiLegDiscount() {
        // Given: AA flight as part of a 2-leg trip
        // legCount = 2, so Multi-Leg Discount fare restriction applies (2+ legs)
        Flight flight = createTestFlight("AA123", bosAirport, laxAirport,
                LocalDateTime.of(2024, 3, 20, 14, 0), // 2:00 PM departure (not early bird - after 09:00)
                LocalDateTime.of(2024, 3, 20, 20, 0), aaAirline);

        // When: Calculate fare price for multi-leg trip (legCount = 2)
        BigDecimal price = calculateFarePrice(flight, aaAirline, 2);

        // Then: Should return BOS hub special of $150.00 (not multi-leg discount $160.00)
        // Expected: $150.00 (AA BOS Special - lowest applicable fare)
        // Available fares: Standard $200, BOS Special $150 ✓ (applicable), Early Bird $175 (not applicable), 
        //                  Multi-Leg $160 ✓ (applicable), DFW Hub $130 (not applicable)
        // Note: BOS Special $150 is lower than Multi-Leg $160, so it wins
        assertNotNull(price, "Price should not be null");
        assertEquals(new BigDecimal("150.00"), price, "Should return BOS hub special of $150.00");
    }

    /**
     * Test: Lowest fare selection when multiple fares apply
     * Verifies that the system selects the lowest applicable fare
     */
    @Test
    void testFareCalculation_LowestFareSelection() {
        // Given: AA flight from DFW to LAX (multiple fares apply: Standard $200, DFW Hub $130, Early Bird $175)
        // DFW is an AA hub AND departure is before 09:00, so both DFW Hub and Early Bird restrictions apply
        Flight flight = createTestFlight("AA123", dfwAirport, laxAirport,
                LocalDateTime.of(2024, 3, 20, 8, 30), // 8:30 AM departure (early bird + DFW hub)
                LocalDateTime.of(2024, 3, 20, 10, 30), aaAirline);

        // When: Calculate fare price for single-leg trip
        BigDecimal price = calculateFarePrice(flight, aaAirline, 1);

        // Then: Should return the lowest applicable fare: DFW Hub $130.00
        // Expected: $130.00 (AA DFW Hub Special - lowest applicable fare)
        // Available fares: Standard $200, BOS Special $150 (not applicable), Early Bird $175 ✓ (applicable), 
        //                  Multi-Leg $160 (not applicable), DFW Hub $130 ✓ (applicable)
        // Note: DFW Hub $130 is lower than Early Bird $175, so it wins
        assertNotNull(price, "Price should not be null");
        assertEquals(new BigDecimal("130.00"), price, "Should return lowest applicable fare: DFW Hub $130.00");
    }

    /**
     * Test: Delta JFK Special fare calculation
     * Verifies that Delta's JFK special is applied correctly
     */
    @Test
    void testFareCalculation_DeltaJFKSpecial() {
        // Given: DL flight from JFK to LAX (JFK special should apply)
        // JFK is a Delta hub, so JFK Special fare restriction applies
        Flight flight = createTestFlight("DL789", jfkAirport, laxAirport,
                LocalDateTime.of(2024, 3, 20, 14, 0), // 2:00 PM departure (not early bird - after 09:00)
                LocalDateTime.of(2024, 3, 20, 20, 0), dlAirline);

        // When: Calculate fare price for single-leg trip
        BigDecimal price = calculateFarePrice(flight, dlAirline, 1);

        // Then: Should return JFK special of $180.00 (not standard $220.00)
        // Expected: $180.00 (DL JFK Special - lowest applicable fare)
        // Available fares: Standard $220, JFK Special $180 ✓ (applicable), ATL Hub $140 (not applicable), 
        //                  West Coast $200 (not applicable)
        assertNotNull(price, "Price should not be null");
        assertEquals(new BigDecimal("180.00"), price, "Should return Delta JFK special of $180.00");
    }

    /**
     * Test: Delta ATL Hub Special fare calculation
     * Verifies that Delta's ATL hub special is applied correctly
     */
    @Test
    void testFareCalculation_DeltaATLHubSpecial() {
        // Given: DL flight from ATL to LAX (ATL hub special should apply)
        Flight flight = createTestFlight("DL456", atlAirport, laxAirport,
                LocalDateTime.of(2024, 3, 20, 14, 0), // 2:00 PM departure
                LocalDateTime.of(2024, 3, 20, 16, 0), dlAirline);

        // When: Calculate fare price
        BigDecimal price = calculateFarePrice(flight, dlAirline, 1);

        // Then: Should return ATL hub special of $140 (not standard $220)
        assertNotNull(price, "Price should not be null");
        assertEquals(new BigDecimal("140.00"), price, "Should return Delta ATL hub special of $140");
    }

    /**
     * Test: JetBlue BOS Hub Special fare calculation
     * Verifies that JetBlue's BOS hub special is applied correctly
     */
    @Test
    void testFareCalculation_JetBlueBOSHubSpecial() {
        // Given: B6 flight from BOS to LAX (BOS hub special should apply)
        Flight flight = createTestFlight("B6501", bosAirport, laxAirport,
                LocalDateTime.of(2024, 3, 20, 14, 0), // 2:00 PM departure
                LocalDateTime.of(2024, 3, 20, 20, 0), b6Airline);

        // When: Calculate fare price
        BigDecimal price = calculateFarePrice(flight, b6Airline, 1);

        // Then: Should return BOS hub special of $130 (not standard $180)
        assertNotNull(price, "Price should not be null");
        assertEquals(new BigDecimal("130.00"), price, "Should return JetBlue BOS hub special of $130");
    }

    /**
     * Test: JetBlue JFK Hub Special fare calculation
     * Verifies that JetBlue's JFK hub special is applied correctly
     */
    @Test
    void testFareCalculation_JetBlueJFKHubSpecial() {
        // Given: B6 flight from JFK to LAX (JFK hub special should apply)
        // JFK is a JetBlue hub, so JFK Hub Special fare restriction applies
        Flight flight = createTestFlight("B6502", jfkAirport, laxAirport,
                LocalDateTime.of(2024, 3, 20, 14, 0), // 2:00 PM departure (not early bird - after 09:00)
                LocalDateTime.of(2024, 3, 20, 20, 0), b6Airline);

        // When: Calculate fare price for single-leg trip
        BigDecimal price = calculateFarePrice(flight, b6Airline, 1);

        // Then: Should return JFK hub special of $120.00 (lowest B6 fare)
        // Expected: $120.00 (B6 JFK Hub Special - lowest applicable fare)
        // Available fares: Standard $180, JFK Hub $120 ✓ (applicable), BOS Hub $130 (not applicable)
        assertNotNull(price, "Price should not be null");
        assertEquals(new BigDecimal("120.00"), price, "Should return JetBlue JFK hub special of $120.00");
    }

    /**
     * Test: United ORD Hub Special fare calculation
     * Verifies that United's ORD hub special is applied correctly
     */
    @Test
    void testFareCalculation_UnitedORDHubSpecial() {
        // Given: UA flight from ORD to LAX (ORD hub special should apply)
        Flight flight = createTestFlight("UA101", ordAirport, laxAirport,
                LocalDateTime.of(2024, 3, 20, 14, 0), // 2:00 PM departure
                LocalDateTime.of(2024, 3, 20, 16, 0), uaAirline);

        // When: Calculate fare price
        BigDecimal price = calculateFarePrice(flight, uaAirline, 1);

        // Then: Should return ORD hub special of $140 (not standard $190)
        assertNotNull(price, "Price should not be null");
        assertEquals(new BigDecimal("140.00"), price, "Should return United ORD hub special of $140");
    }

    /**
     * Test: Complex scenario with multiple applicable fares
     * Verifies that the system correctly handles complex pricing scenarios
     */
    @Test
    void testFareCalculation_ComplexScenario() {
        // Given: AA flight from BOS to LAX departing at 8:30 AM (BOS hub + early bird + multi-leg)
        // BOS is an AA hub, departure is before 09:00, and legCount = 2, so multiple restrictions apply
        Flight flight = createTestFlight("AA123", bosAirport, laxAirport,
                LocalDateTime.of(2024, 3, 20, 8, 30), // 8:30 AM departure (early bird - before 09:00)
                LocalDateTime.of(2024, 3, 20, 14, 30), aaAirline);

        // When: Calculate fare price for multi-leg trip (legCount = 2)
        BigDecimal price = calculateFarePrice(flight, aaAirline, 2);

        // Then: Should return the lowest applicable fare: BOS Hub Special $150.00
        // Expected: $150.00 (AA BOS Special - lowest applicable fare)
        // Available fares: Standard $200, BOS Special $150 ✓ (applicable), Early Bird $175 ✓ (applicable), 
        //                  Multi-Leg $160 ✓ (applicable), DFW Hub $130 (not applicable)
        // Note: BOS Special $150 is lower than Early Bird $175 and Multi-Leg $160, so it wins
        assertNotNull(price, "Price should not be null");
        assertEquals(new BigDecimal("150.00"), price, "Should return lowest applicable fare: BOS Hub Special $150.00");
    }

    /**
     * Test: No applicable fares scenario
     * Verifies that the system handles cases where no fares apply
     */
    @Test
    void testFareCalculation_NoApplicableFares() {
        // Given: Flight with airline that has no fares
        Airline testAirline = new Airline("TST", "Test Airline", "USA");
        testAirline = airlineRepository.save(testAirline);
        
        Flight flight = createTestFlight("TEST123", bosAirport, laxAirport,
                LocalDateTime.of(2024, 3, 20, 14, 0),
                LocalDateTime.of(2024, 3, 20, 20, 0), testAirline);

        // When: Calculate fare price
        BigDecimal price = calculateFarePrice(flight, testAirline, 1);

        // Then: Should return null (no applicable fares)
        assertNull(price, "Should return null when no fares are available");
    }

    /**
     * Test: Fare restriction validation - ENDPOINT restriction
     * Verifies that ENDPOINT restrictions are correctly validated
     */
    @Test
    void testFareRestrictionValidation_EndpointRestriction() {
        // Given: BOS Hub Special fare with ENDPOINT restriction
        Fare bosSpecialFare = fareRepository.findFaresByAirlineCode("AA").stream()
                .filter(fare -> "BOS Special".equals(fare.getFareName()))
                .findFirst().orElseThrow();

        Flight bosFlight = createTestFlight("AA123", bosAirport, laxAirport,
                LocalDateTime.of(2024, 3, 20, 14, 0),
                LocalDateTime.of(2024, 3, 20, 20, 0), aaAirline);

        Flight nonBosFlight = createTestFlight("AA456", laxAirport, airportRepository.findByCode("SFO").orElseThrow(),
                LocalDateTime.of(2024, 3, 20, 14, 0),
                LocalDateTime.of(2024, 3, 20, 16, 0), aaAirline);

        // When & Then: BOS flight should be applicable, non-BOS flight should not
        assertTrue(isFareApplicable(bosSpecialFare, bosFlight, 1), "BOS Special should apply to BOS flight");
        assertFalse(isFareApplicable(bosSpecialFare, nonBosFlight, 1), "BOS Special should not apply to non-BOS flight");
    }

    /**
     * Test: Fare restriction validation - DEPARTURE_TIME restriction
     * Verifies that DEPARTURE_TIME restrictions are correctly validated
     */
    @Test
    void testFareRestrictionValidation_DepartureTimeRestriction() {
        // Given: Early Bird fare with DEPARTURE_TIME restriction (before 09:00)
        Fare earlyBirdFare = fareRepository.findFaresByAirlineCode("AA").stream()
                .filter(fare -> "Early Bird".equals(fare.getFareName()))
                .findFirst().orElseThrow();

        Flight earlyFlight = createTestFlight("AA123", bosAirport, laxAirport,
                LocalDateTime.of(2024, 3, 20, 8, 30), // 8:30 AM (before 09:00)
                LocalDateTime.of(2024, 3, 20, 14, 30), aaAirline);

        Flight lateFlight = createTestFlight("AA456", bosAirport, laxAirport,
                LocalDateTime.of(2024, 3, 20, 10, 0), // 10:00 AM (after 09:00)
                LocalDateTime.of(2024, 3, 20, 16, 0), aaAirline);

        // When & Then: Early flight should be applicable, late flight should not
        assertTrue(isFareApplicable(earlyBirdFare, earlyFlight, 1), "Early Bird should apply to 8:30 AM flight");
        assertFalse(isFareApplicable(earlyBirdFare, lateFlight, 1), "Early Bird should not apply to 10:00 AM flight");
    }

    /**
     * Test: Fare restriction validation - MULTI_LEG restriction
     * Verifies that MULTI_LEG restrictions are correctly validated
     */
    @Test
    void testFareRestrictionValidation_MultiLegRestriction() {
        // Given: Multi-Leg Discount fare with MULTI_LEG restriction (2+ legs)
        Fare multiLegFare = fareRepository.findFaresByAirlineCode("AA").stream()
                .filter(fare -> "Multi-Leg Discount".equals(fare.getFareName()))
                .findFirst().orElseThrow();

        Flight singleLegFlight = createTestFlight("AA123", bosAirport, laxAirport,
                LocalDateTime.of(2024, 3, 20, 14, 0),
                LocalDateTime.of(2024, 3, 20, 20, 0), aaAirline);

        Flight multiLegFlight = createTestFlight("AA456", bosAirport, ordAirport,
                LocalDateTime.of(2024, 3, 20, 14, 0),
                LocalDateTime.of(2024, 3, 20, 16, 0), aaAirline);

        // When & Then: Multi-leg fare should apply to 2+ leg trips, not single leg
        assertFalse(isFareApplicable(multiLegFare, singleLegFlight, 1), "Multi-Leg should not apply to single leg trip");
        assertTrue(isFareApplicable(multiLegFare, multiLegFlight, 2), "Multi-Leg should apply to 2+ leg trip");
    }

    /**
     * Test: Multiple restrictions validation
     * Verifies that fares with multiple restrictions are correctly validated
     */
    @Test
    void testFareRestrictionValidation_MultipleRestrictions() {
        // Given: A fare with multiple restrictions (if such exists in test data)
        // For this test, we'll create a scenario where a fare has both ENDPOINT and DEPARTURE_TIME restrictions
        
        // Create a custom fare with multiple restrictions for testing
        Fare customFare = new Fare();
        customFare.setAirline(aaAirline);
        customFare.setBasePrice(new BigDecimal("100.00"));
        customFare.setFareName("Custom Multi-Restriction Fare");
        customFare.setDescription("Test fare with multiple restrictions");
        customFare = fareRepository.save(customFare);

        // Add ENDPOINT restriction (BOS)
        FareRestriction endpointRestriction = new FareRestriction();
        endpointRestriction.setFare(customFare);
        endpointRestriction.setRestrictionType(RestrictionType.ENDPOINT);
        endpointRestriction.setRestrictionValue("BOS");
        fareRestrictionRepository.save(endpointRestriction);
        customFare.getRestrictions().add(endpointRestriction);

        // Add DEPARTURE_TIME restriction (before 09:00)
        FareRestriction timeRestriction = new FareRestriction();
        timeRestriction.setFare(customFare);
        timeRestriction.setRestrictionType(RestrictionType.DEPARTURE_TIME);
        timeRestriction.setRestrictionValue("09:00");
        fareRestrictionRepository.save(timeRestriction);
        customFare.getRestrictions().add(timeRestriction);
        
        // Save the fare with the restrictions
        fareRepository.save(customFare);

        Flight qualifyingFlight = createTestFlight("AA123", bosAirport, laxAirport,
                LocalDateTime.of(2024, 3, 20, 8, 30), // BOS departure at 8:30 AM
                LocalDateTime.of(2024, 3, 20, 14, 30), aaAirline);

        Flight nonQualifyingFlight1 = createTestFlight("AA456", laxAirport, airportRepository.findByCode("SFO").orElseThrow(),
                LocalDateTime.of(2024, 3, 20, 8, 30), // Non-BOS departure at 8:30 AM
                LocalDateTime.of(2024, 3, 20, 10, 30), aaAirline);

        Flight nonQualifyingFlight2 = createTestFlight("AA789", bosAirport, laxAirport,
                LocalDateTime.of(2024, 3, 20, 10, 0), // BOS departure at 10:00 AM
                LocalDateTime.of(2024, 3, 20, 16, 0), aaAirline);

        // When & Then: Only the flight meeting ALL restrictions should be applicable
        assertTrue(isFareApplicable(customFare, qualifyingFlight, 1), "Should apply to flight meeting all restrictions");
        assertFalse(isFareApplicable(customFare, nonQualifyingFlight1, 1), "Should not apply to flight missing ENDPOINT restriction");
        assertFalse(isFareApplicable(customFare, nonQualifyingFlight2, 1), "Should not apply to flight missing DEPARTURE_TIME restriction");
    }

    // Helper methods to access private methods from FlightSearchService
    private BigDecimal calculateFarePrice(Flight flight, Airline airline, int legCount) {
        try {
            java.lang.reflect.Method method = FlightSearchService.class.getDeclaredMethod("calculateFarePrice", Flight.class, Airline.class, int.class);
            method.setAccessible(true);
            return (BigDecimal) method.invoke(flightSearchService, flight, airline, legCount);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke calculateFarePrice method", e);
        }
    }

    private boolean isFareApplicable(Fare fare, Flight flight, int legCount) {
        try {
            java.lang.reflect.Method method = FlightSearchService.class.getDeclaredMethod("isFareApplicable", Fare.class, Flight.class, int.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(flightSearchService, fare, flight, legCount);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke isFareApplicable method", e);
        }
    }

    private boolean isRestrictionSatisfied(FareRestriction restriction, Flight flight, int legCount) {
        try {
            java.lang.reflect.Method method = FlightSearchService.class.getDeclaredMethod("isRestrictionSatisfied", FareRestriction.class, Flight.class, int.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(flightSearchService, restriction, flight, legCount);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke isRestrictionSatisfied method", e);
        }
    }

    private Flight createTestFlight(String flightNumber, Airport source, Airport destination, 
                                  LocalDateTime departure, LocalDateTime arrival, Airline airline) {
        Flight flight = new Flight();
        flight.setFlightNumber(flightNumber);
        flight.setSourceAirport(source);
        flight.setDestinationAirport(destination);
        flight.setDepartureTime(departure);
        flight.setArrivalTime(arrival);
        
        // Add airline relationship
        FlightAirline flightAirline = new FlightAirline();
        flightAirline.setFlight(flight);
        flightAirline.setAirline(airline);
        flight.getFlightAirlines().add(flightAirline);
        
        return flight;
    }
}
