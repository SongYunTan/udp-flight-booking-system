package com.flightsudp.server;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ListFlightsService implements FlightService {

    // A hardcoded list of available flights
    private List<Flight> flights = new ArrayList<>();

    public ListFlightsService() {
        flights.add(new Flight("F101", "Seattle", "San Francisco", 150, 150));
        flights.add(new Flight("F102", "San Francisco", "Seattle", 175, 175));
        flights.add(new Flight("F103", "New York", "Los Angeles", 250, 250));
        flights.add(new Flight("F104", "Los Angeles", "New York", 225, 225));
    }

    @Override
    public JSONObject execute(JSONObject params) throws Exception {
        // Extract any necessary parameters from the request JSON
        // For example, you could use params.getString("source") to get the source airport code

        // Construct response JSON with a list of available flights
        JSONArray flightsJson = new JSONArray();
        for (Flight flight : flights) {
            JSONObject flightJson = new JSONObject();
            flightJson.put("flightNumber", flight.getFlightNumber());
            flightJson.put("source", flight.getSource());
            flightJson.put("destination", flight.getDestination());
            flightJson.put("price", flight.getPrice());
            flightJson.put("seats", flight.getSeats());
            flightsJson.put(flightJson);
        }
        JSONObject resultJson = new JSONObject();
        resultJson.put("flights", flightsJson);

        return resultJson;
    }
}
