package edu.mit.sidpac.flightsearch.util;

import edu.mit.sidpac.flightsearch.entity.*;
import edu.mit.sidpac.flightsearch.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Test database setup utility class
 * Creates comprehensive test data matching the init-database.sql script
 * Used by integration tests to set up consistent test data
 */
@Component
public class TestDatabaseSetup {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private AirlineRepository airlineRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private FareRepository fareRepository;

    @Autowired
    private FareRestrictionRepository fareRestrictionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Loads the complete database schema and sample data
     * This provides a realistic test environment with:
     * - 5 admin users (1 super admin + 4 airline-specific admins)
     * - 20 airports (US and international)
     * - 16 airlines (US and international carriers)
     * - 18 flights with realistic schedules
     * - 25+ fare rules with complex pricing restrictions
     * - Codeshare relationships between airlines
     */
    public void loadFullDatabaseData() {
        createTestUsers();
        createTestAirports();
        createTestAirlines();
        createTestFlights();
        createTestFares();
        createTestFareRestrictions();
    }

    private void createTestUsers() {
        // Create super admin
        User superAdmin = new User();
        superAdmin.setId("user-admin-001");
        superAdmin.setUsername("admin");
        superAdmin.setEmail("admin@flightsearch.com");
        superAdmin.setPasswordHash(passwordEncoder.encode("password123"));
        superAdmin.setFirstName("Super");
        superAdmin.setLastName("Admin");
        superAdmin.setRole(UserRole.ADMIN);
        superAdmin.setAssignedAirlineCode(null);
        userRepository.save(superAdmin);

        // Create airline admins
        User aaAdmin = new User();
        aaAdmin.setId("user-aa-admin-001");
        aaAdmin.setUsername("aa_admin");
        aaAdmin.setEmail("aa_admin@flightsearch.com");
        aaAdmin.setPasswordHash(passwordEncoder.encode("password123"));
        aaAdmin.setFirstName("American");
        aaAdmin.setLastName("Admin");
        aaAdmin.setRole(UserRole.ADMIN);
        aaAdmin.setAssignedAirlineCode("AA");
        userRepository.save(aaAdmin);

        User dlAdmin = new User();
        dlAdmin.setId("user-dl-admin-001");
        dlAdmin.setUsername("dl_admin");
        dlAdmin.setEmail("dl_admin@flightsearch.com");
        dlAdmin.setPasswordHash(passwordEncoder.encode("password123"));
        dlAdmin.setFirstName("Delta");
        dlAdmin.setLastName("Admin");
        dlAdmin.setRole(UserRole.ADMIN);
        dlAdmin.setAssignedAirlineCode("DL");
        userRepository.save(dlAdmin);

        User uaAdmin = new User();
        uaAdmin.setId("user-ua-admin-001");
        uaAdmin.setUsername("ua_admin");
        uaAdmin.setEmail("ua_admin@flightsearch.com");
        uaAdmin.setPasswordHash(passwordEncoder.encode("password123"));
        uaAdmin.setFirstName("United");
        uaAdmin.setLastName("Admin");
        uaAdmin.setRole(UserRole.ADMIN);
        uaAdmin.setAssignedAirlineCode("UA");
        userRepository.save(uaAdmin);

        User b6Admin = new User();
        b6Admin.setId("user-b6-admin-001");
        b6Admin.setUsername("b6_admin");
        b6Admin.setEmail("b6_admin@flightsearch.com");
        b6Admin.setPasswordHash(passwordEncoder.encode("password123"));
        b6Admin.setFirstName("JetBlue");
        b6Admin.setLastName("Admin");
        b6Admin.setRole(UserRole.ADMIN);
        b6Admin.setAssignedAirlineCode("B6");
        userRepository.save(b6Admin);
    }

    private void createTestAirports() {
        // Create key airports for testing - matching init-database.sql
        String[][] airports = {
            // US Airports
            {"airport-bos-001", "BOS", "Logan International Airport", "Boston", "USA"},
            {"airport-lax-001", "LAX", "Los Angeles International Airport", "Los Angeles", "USA"},
            {"airport-jfk-001", "JFK", "John F. Kennedy International Airport", "New York", "USA"},
            {"airport-lga-001", "LGA", "LaGuardia Airport", "New York", "USA"},
            {"airport-ord-001", "ORD", "O'Hare International Airport", "Chicago", "USA"},
            {"airport-dfw-001", "DFW", "Dallas/Fort Worth International Airport", "Dallas", "USA"},
            {"airport-atl-001", "ATL", "Hartsfield-Jackson Atlanta International Airport", "Atlanta", "USA"},
            {"airport-mia-001", "MIA", "Miami International Airport", "Miami", "USA"},
            {"airport-sea-001", "SEA", "Seattle-Tacoma International Airport", "Seattle", "USA"},
            {"airport-den-001", "DEN", "Denver International Airport", "Denver", "USA"},
            {"airport-sfo-001", "SFO", "San Francisco International Airport", "San Francisco", "USA"},
            {"airport-las-001", "LAS", "McCarran International Airport", "Las Vegas", "USA"},
            
            // International Airports
            {"airport-lhr-001", "LHR", "London Heathrow Airport", "London", "UK"},
            {"airport-cdg-001", "CDG", "Charles de Gaulle Airport", "Paris", "France"},
            {"airport-fra-001", "FRA", "Frankfurt Airport", "Frankfurt", "Germany"},
            {"airport-nrt-001", "NRT", "Narita International Airport", "Tokyo", "Japan"},
            {"airport-hnd-001", "HND", "Haneda Airport", "Tokyo", "Japan"},
            {"airport-syd-001", "SYD", "Kingsford Smith Airport", "Sydney", "Australia"},
            {"airport-yyz-001", "YYZ", "Toronto Pearson International Airport", "Toronto", "Canada"},
            {"airport-yvr-001", "YVR", "Vancouver International Airport", "Vancouver", "Canada"}
        };

        for (String[] airport : airports) {
            Airport a = new Airport();
            a.setId(airport[0]);
            a.setCode(airport[1]);
            a.setName(airport[2]);
            a.setCity(airport[3]);
            a.setCountry(airport[4]);
            airportRepository.save(a);
        }
    }

    private void createTestAirlines() {
        // Create key airlines for testing - matching init-database.sql
        String[][] airlines = {
            // US Airlines
            {"airline-aa-001", "AA", "American Airlines", "USA"},
            {"airline-dl-001", "DL", "Delta Air Lines", "USA"},
            {"airline-ua-001", "UA", "United Airlines", "USA"},
            {"airline-b6-001", "B6", "JetBlue Airways", "USA"},
            {"airline-wn-001", "WN", "Southwest Airlines", "USA"},
            {"airline-as-001", "AS", "Alaska Airlines", "USA"},
            {"airline-f9-001", "F9", "Frontier Airlines", "USA"},
            {"airline-nk-001", "NK", "Spirit Airlines", "USA"},
            
            // International Airlines
            {"airline-ba-001", "BA", "British Airways", "UK"},
            {"airline-af-001", "AF", "Air France", "France"},
            {"airline-lh-001", "LH", "Lufthansa", "Germany"},
            {"airline-jl-001", "JL", "Japan Airlines", "Japan"},
            {"airline-nh-001", "NH", "All Nippon Airways", "Japan"},
            {"airline-qf-001", "QF", "Qantas Airways", "Australia"},
            {"airline-ac-001", "AC", "Air Canada", "Canada"},
            {"airline-kl-001", "KL", "KLM Royal Dutch Airlines", "Netherlands"}
        };

        for (String[] airline : airlines) {
            Airline a = new Airline();
            a.setId(airline[0]);
            a.setCode(airline[1]);
            a.setName(airline[2]);
            a.setCountry(airline[3]);
            airlineRepository.save(a);
        }
    }

    private void createTestFlights() {
        // Get airports and airlines
        var bosAirport = airportRepository.findByCode("BOS").orElse(null);
        var laxAirport = airportRepository.findByCode("LAX").orElse(null);
        var jfkAirport = airportRepository.findByCode("JFK").orElse(null);
        var lgaAirport = airportRepository.findByCode("LGA").orElse(null);
        var ordAirport = airportRepository.findByCode("ORD").orElse(null);
        var dfwAirport = airportRepository.findByCode("DFW").orElse(null);
        var atlAirport = airportRepository.findByCode("ATL").orElse(null);
        var miaAirport = airportRepository.findByCode("MIA").orElse(null);
        var seaAirport = airportRepository.findByCode("SEA").orElse(null);
        var denAirport = airportRepository.findByCode("DEN").orElse(null);
        var sfoAirport = airportRepository.findByCode("SFO").orElse(null);
        var lasAirport = airportRepository.findByCode("LAS").orElse(null);
        var lhrAirport = airportRepository.findByCode("LHR").orElse(null);
        var cdgAirport = airportRepository.findByCode("CDG").orElse(null);
        var nrtAirport = airportRepository.findByCode("NRT").orElse(null);

        var aaAirline = airlineRepository.findByCode("AA").orElse(null);
        var dlAirline = airlineRepository.findByCode("DL").orElse(null);
        var uaAirline = airlineRepository.findByCode("UA").orElse(null);
        var b6Airline = airlineRepository.findByCode("B6").orElse(null);
        var wnAirline = airlineRepository.findByCode("WN").orElse(null);
        var baAirline = airlineRepository.findByCode("BA").orElse(null);
        var afAirline = airlineRepository.findByCode("AF").orElse(null);
        var jlAirline = airlineRepository.findByCode("JL").orElse(null);

        // Create flights matching init-database.sql exactly
        if (bosAirport != null && laxAirport != null && aaAirline != null) {
            // American Airlines flights
            createFlightWithAirline("flight-aa123-001", "AA123", bosAirport, laxAirport, 
                        LocalDateTime.of(2024, 3, 20, 9, 30), LocalDateTime.of(2024, 3, 20, 15, 30), aaAirline);
            createFlightWithAirline("flight-aa456-001", "AA456", laxAirport, bosAirport, 
                        LocalDateTime.of(2024, 3, 20, 18, 0), LocalDateTime.of(2024, 3, 21, 0, 30), aaAirline);
            createFlightWithAirline("flight-aa789-001", "AA789", jfkAirport, miaAirport, 
                        LocalDateTime.of(2024, 3, 20, 7, 0), LocalDateTime.of(2024, 3, 20, 10, 30), aaAirline);
            createFlightWithAirline("flight-aa101-001", "AA101", dfwAirport, laxAirport, 
                        LocalDateTime.of(2024, 3, 20, 14, 0), LocalDateTime.of(2024, 3, 20, 16, 30), aaAirline);

            // Delta flights
            createFlightWithAirline("flight-dl789-001", "DL789", jfkAirport, laxAirport, 
                        LocalDateTime.of(2024, 3, 20, 10, 0), LocalDateTime.of(2024, 3, 20, 16, 30), dlAirline);
            createFlightWithAirline("flight-dl234-001", "DL234", atlAirport, laxAirport, 
                        LocalDateTime.of(2024, 3, 20, 11, 30), LocalDateTime.of(2024, 3, 20, 14, 45), dlAirline);
            createFlightWithAirline("flight-dl567-001", "DL567", seaAirport, jfkAirport, 
                        LocalDateTime.of(2024, 3, 20, 6, 0), LocalDateTime.of(2024, 3, 20, 14, 30), dlAirline);

            // United flights
            createFlightWithAirline("flight-ua101-001", "UA101", bosAirport, ordAirport, 
                        LocalDateTime.of(2024, 3, 20, 8, 0), LocalDateTime.of(2024, 3, 20, 10, 30), uaAirline);
            createFlightWithAirline("flight-ua102-001", "UA102", ordAirport, laxAirport, 
                        LocalDateTime.of(2024, 3, 20, 12, 0), LocalDateTime.of(2024, 3, 20, 15, 0), uaAirline);
            createFlightWithAirline("flight-ua303-001", "UA303", sfoAirport, ordAirport, 
                        LocalDateTime.of(2024, 3, 20, 13, 0), LocalDateTime.of(2024, 3, 20, 20, 30), uaAirline);
            createFlightWithAirline("flight-ua404-001", "UA404", denAirport, jfkAirport, 
                        LocalDateTime.of(2024, 3, 20, 15, 30), LocalDateTime.of(2024, 3, 20, 22, 0), uaAirline);

            // JetBlue flights
            createFlightWithAirline("flight-b6-501-001", "B6-501", jfkAirport, laxAirport, 
                        LocalDateTime.of(2024, 3, 20, 16, 0), LocalDateTime.of(2024, 3, 20, 19, 30), b6Airline);
            createFlightWithAirline("flight-b6-502-001", "B6-502", bosAirport, lasAirport, 
                        LocalDateTime.of(2024, 3, 20, 17, 30), LocalDateTime.of(2024, 3, 20, 21, 0), b6Airline);

            // Southwest flights
            createFlightWithAirline("flight-wn-601-001", "WN-601", lasAirport, laxAirport, 
                        LocalDateTime.of(2024, 3, 20, 9, 0), LocalDateTime.of(2024, 3, 20, 10, 15), wnAirline);
            createFlightWithAirline("flight-wn-602-001", "WN-602", denAirport, lasAirport, 
                        LocalDateTime.of(2024, 3, 20, 12, 30), LocalDateTime.of(2024, 3, 20, 13, 45), wnAirline);

            // International flights
            createFlightWithAirline("flight-ba-701-001", "BA-701", lhrAirport, jfkAirport, 
                        LocalDateTime.of(2024, 3, 20, 10, 30), LocalDateTime.of(2024, 3, 20, 13, 30), baAirline);
            createFlightWithAirline("flight-af-801-001", "AF-801", cdgAirport, jfkAirport, 
                        LocalDateTime.of(2024, 3, 20, 11, 0), LocalDateTime.of(2024, 3, 20, 14, 0), afAirline);
            createFlightWithAirline("flight-jl-901-001", "JL-901", nrtAirport, laxAirport, 
                        LocalDateTime.of(2024, 3, 20, 14, 30), LocalDateTime.of(2024, 3, 20, 9, 30), jlAirline);

            // Add codeshare relationship for AA123 with JetBlue (matching init-database.sql)
            if (b6Airline != null) {
                var aa123Flight = flightRepository.findById("flight-aa123-001").orElse(null);
                if (aa123Flight != null) {
                    FlightAirline flightAirline = new FlightAirline(aa123Flight, b6Airline);
                    aa123Flight.getFlightAirlines().add(flightAirline);
                    flightRepository.save(aa123Flight);
                }
            }
        }
    }

    private void createFlightWithAirline(String id, String flightNumber, Airport source, Airport destination, 
                                       LocalDateTime departure, LocalDateTime arrival, Airline airline) {
        Flight flight = new Flight();
        flight.setId(id);
        flight.setFlightNumber(flightNumber);
        flight.setSourceAirport(source);
        flight.setDestinationAirport(destination);
        flight.setDepartureTime(departure);
        flight.setArrivalTime(arrival);
        flight = flightRepository.save(flight);

        // Add airline relationship
        if (airline != null) {
            FlightAirline flightAirline = new FlightAirline(flight, airline);
            flight.getFlightAirlines().add(flightAirline);
            flightRepository.save(flight);
        }
    }

    private void createTestFares() {
        // Get airlines
        var aaAirline = airlineRepository.findByCode("AA").orElse(null);
        var dlAirline = airlineRepository.findByCode("DL").orElse(null);
        var uaAirline = airlineRepository.findByCode("UA").orElse(null);
        var b6Airline = airlineRepository.findByCode("B6").orElse(null);
        var wnAirline = airlineRepository.findByCode("WN").orElse(null);
        var baAirline = airlineRepository.findByCode("BA").orElse(null);
        var afAirline = airlineRepository.findByCode("AF").orElse(null);
        var jlAirline = airlineRepository.findByCode("JL").orElse(null);

        // Create fares matching init-database.sql exactly
        if (aaAirline != null) {
            // American Airlines fares
            createFare("fare-aa-standard-001", aaAirline, new BigDecimal("200.00"), "Standard Fare", "Any flight has fare $200");
            createFare("fare-aa-bos-special-001", aaAirline, new BigDecimal("150.00"), "BOS Special", "Any flight departing/arriving at BOS has fare $150");
            createFare("fare-aa-early-bird-001", aaAirline, new BigDecimal("175.00"), "Early Bird", "Any flight leaving before 09:00 has fare $175");
            createFare("fare-aa-multi-leg-001", aaAirline, new BigDecimal("160.00"), "Multi-Leg Discount", "Any flight as part of a 2-or-more leg trip has fare $160");
            createFare("fare-aa-dfw-hub-001", aaAirline, new BigDecimal("130.00"), "DFW Hub Special", "Any flight departing/arriving at DFW has fare $130");
        }

        if (dlAirline != null) {
            // Delta fares
            createFare("fare-dl-standard-001", dlAirline, new BigDecimal("220.00"), "Standard Fare", "Any flight has fare $220");
            createFare("fare-dl-jfk-special-001", dlAirline, new BigDecimal("180.00"), "JFK Special", "Any flight departing/arriving at JFK has fare $180");
            createFare("fare-dl-atl-hub-001", dlAirline, new BigDecimal("140.00"), "ATL Hub Special", "Any flight departing/arriving at ATL has fare $140");
            createFare("fare-dl-west-coast-001", dlAirline, new BigDecimal("200.00"), "West Coast Special", "Any flight to/from LAX has fare $200");
        }

        if (uaAirline != null) {
            // United fares
            createFare("fare-ua-standard-001", uaAirline, new BigDecimal("190.00"), "Standard Fare", "Any flight has fare $190");
            createFare("fare-ua-ord-hub-001", uaAirline, new BigDecimal("140.00"), "ORD Hub", "Any flight departing/arriving at ORD has fare $140");
            createFare("fare-ua-sfo-hub-001", uaAirline, new BigDecimal("150.00"), "SFO Hub", "Any flight departing/arriving at SFO has fare $150");
            createFare("fare-ua-den-hub-001", uaAirline, new BigDecimal("160.00"), "DEN Hub", "Any flight departing/arriving at DEN has fare $160");
        }

        if (b6Airline != null) {
            // JetBlue fares
            createFare("fare-b6-standard-001", b6Airline, new BigDecimal("180.00"), "Standard Fare", "Any flight has fare $180");
            createFare("fare-b6-jfk-hub-001", b6Airline, new BigDecimal("120.00"), "JFK Hub Special", "Any flight departing/arriving at JFK has fare $120");
            createFare("fare-b6-bos-hub-001", b6Airline, new BigDecimal("130.00"), "BOS Hub Special", "Any flight departing/arriving at BOS has fare $130");
        }

        if (wnAirline != null) {
            // Southwest fares
            createFare("fare-wn-standard-001", wnAirline, new BigDecimal("160.00"), "Standard Fare", "Any flight has fare $160");
            createFare("fare-wn-las-hub-001", wnAirline, new BigDecimal("100.00"), "LAS Hub Special", "Any flight departing/arriving at LAS has fare $100");
            createFare("fare-wn-den-hub-001", wnAirline, new BigDecimal("110.00"), "DEN Hub Special", "Any flight departing/arriving at DEN has fare $110");
        }

        if (baAirline != null) {
            // International airline fares
            createFare("fare-ba-standard-001", baAirline, new BigDecimal("800.00"), "Standard Fare", "Any international flight has fare $800");
        }

        if (afAirline != null) {
            createFare("fare-af-standard-001", afAirline, new BigDecimal("750.00"), "Standard Fare", "Any international flight has fare $750");
        }

        if (jlAirline != null) {
            createFare("fare-jl-standard-001", jlAirline, new BigDecimal("900.00"), "Standard Fare", "Any international flight has fare $900");
        }
    }

    private void createFare(String id, Airline airline, BigDecimal basePrice, String fareName, String description) {
        Fare fare = new Fare();
        fare.setId(id);
        fare.setAirline(airline);
        fare.setBasePrice(basePrice);
        fare.setFareName(fareName);
        fare.setDescription(description);
        fareRepository.save(fare);
    }

    private void createTestFareRestrictions() {
        // Create fare restrictions matching init-database.sql exactly
        createFareRestriction("restriction-bos-special-001", "fare-aa-bos-special-001", RestrictionType.ENDPOINT, "BOS");
        createFareRestriction("restriction-early-bird-001", "fare-aa-early-bird-001", RestrictionType.DEPARTURE_TIME, "09:00");
        createFareRestriction("restriction-multi-leg-001", "fare-aa-multi-leg-001", RestrictionType.MULTI_LEG, "2");
        createFareRestriction("restriction-dfw-hub-001", "fare-aa-dfw-hub-001", RestrictionType.ENDPOINT, "DFW");

        createFareRestriction("restriction-jfk-special-001", "fare-dl-jfk-special-001", RestrictionType.ENDPOINT, "JFK");
        createFareRestriction("restriction-atl-hub-001", "fare-dl-atl-hub-001", RestrictionType.ENDPOINT, "ATL");
        createFareRestriction("restriction-west-coast-001", "fare-dl-west-coast-001", RestrictionType.ENDPOINT, "LAX");

        createFareRestriction("restriction-ord-hub-001", "fare-ua-ord-hub-001", RestrictionType.ENDPOINT, "ORD");
        createFareRestriction("restriction-sfo-hub-001", "fare-ua-sfo-hub-001", RestrictionType.ENDPOINT, "SFO");
        createFareRestriction("restriction-den-hub-001", "fare-ua-den-hub-001", RestrictionType.ENDPOINT, "DEN");

        createFareRestriction("restriction-jfk-hub-001", "fare-b6-jfk-hub-001", RestrictionType.ENDPOINT, "JFK");
        createFareRestriction("restriction-bos-hub-001", "fare-b6-bos-hub-001", RestrictionType.ENDPOINT, "BOS");

        createFareRestriction("restriction-las-hub-001", "fare-wn-las-hub-001", RestrictionType.ENDPOINT, "LAS");
        createFareRestriction("restriction-den-hub-wn-001", "fare-wn-den-hub-001", RestrictionType.ENDPOINT, "DEN");
    }

    private void createFareRestriction(String id, String fareId, RestrictionType restrictionType, String restrictionValue) {
        var fare = fareRepository.findById(fareId).orElse(null);
        if (fare != null) {
            FareRestriction restriction = new FareRestriction();
            restriction.setId(id);
            restriction.setFare(fare);
            restriction.setRestrictionType(restrictionType);
            restriction.setRestrictionValue(restrictionValue);
            fareRestrictionRepository.save(restriction);
            
            // Ensure the restriction is added to the fare's restrictions set
            fare.getRestrictions().add(restriction);
            fareRepository.save(fare);
        }
    }
}
