package com.flightsudp.server.services;

import com.flightsudp.server.data.Flight;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FlightDetailsService extends AbstractService {
    private List<Flight> allFlights;
    private Map<Long, Flight> allFlightsMap;

    public FlightDetailsService(List<Flight> allFlights) {
        super(allFlights);
    }

    public JSONObject execute(JSONObject jsonRequest, String address, String port) {
        return getFlightDetails(jsonRequest, address, port);
    }

    private JSONObject getFlightDetails(JSONObject jsonRequest, String address, String port) {
        JSONObject data = jsonRequest.getJSONObject("data");
        Long f_id = Long.valueOf(data.getString("flightid"));

        Optional<Flight> flight = allFlights
                .stream()
                .filter(f -> (f.getId().equals(f_id))).findFirst();


        JSONObject json = new JSONObject();

        if (!flight.isPresent()) {
            return this.createErrorJSONObject("No flight found");
        } else {
            json.put("status", "SUCCESS");
            JSONObject flightJson = new JSONObject();
            flightJson.put("departure_time", flight.get().getDepartureTime());
            flightJson.put("air_fare", flight.get().getAirfare());
            json.put("seat_availability", flight.get().getTotalSeats());
            return json;
        }
    }
}
