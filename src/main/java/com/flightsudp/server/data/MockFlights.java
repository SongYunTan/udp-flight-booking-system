package com.flightsudp.server.data;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class MockFlights {

    static Flight flight1 = Flight.builder()
            .id(1L)
            .source("New York")
            .destination("London")
            .departureTime(LocalDateTime.of(2022, 4, 1, 12, 0))
            .airfare(1000L)
            .totalSeats(200)
            .build();
    static Flight flight2 = Flight.builder()
            .id(2L)
            .source("London")
            .destination("New York")
            .departureTime(LocalDateTime.of(2022, 4, 2, 12, 0))
            .airfare(1100L)
            .totalSeats(220)
            .build();
    static Flight flight3 = Flight.builder()
            .id(3L)
            .source("Singapore")
            .destination("Tokyo")
            .departureTime(LocalDateTime.of(2022, 4, 3, 12, 0))
            .airfare(800L)
            .totalSeats(150)
            .build();
    static Flight flight4 = Flight.builder()
            .id(4L)
            .source("Tokyo")
            .destination("Singapore")
            .departureTime(LocalDateTime.of(2022, 4, 4, 12, 0))
            .airfare(900L)
            .totalSeats(170)
            .build();
    static Flight flight5 = Flight.builder()
            .id(5L)
            .source("Sydney")
            .destination("Melbourne")
            .departureTime(LocalDateTime.of(2022, 4, 5, 12, 0))
            .airfare(200L)
            .totalSeats(100)
            .build();

    private static List<Flight> allFlights = Arrays.asList(flight1, flight2, flight3, flight4, flight5);

    public static List<Flight> getAllFlights() {
        return allFlights;
    }
}
