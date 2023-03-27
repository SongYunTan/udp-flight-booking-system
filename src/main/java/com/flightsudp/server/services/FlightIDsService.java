package com.flightsudp.server.services;

import com.flightsudp.server.data.Flight;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlightIDsService extends AbstractService {
    private List<Flight> allFlights;
    private Map<Long, Flight> allFlightsMap;

    public FlightIDsService(List<Flight> allFlights) {
        super(allFlights);
    }
    @Override
    public JSONObject execute(JSONObject jsonRequest, String address, String port){
        return getFlightIds(jsonRequest, address, port);
    }

    private JSONObject getFlightIds(JSONObject jsonRequest, String address, String port) {
        JSONObject data = jsonRequest.getJSONObject("data");
        String source = data.getString("source");
        String destination = data.getString("destination");

        List<Long> flights = allFlights
                .stream()
                .filter(f -> (f.getSource().equalsIgnoreCase(source) && f.getDestination().equalsIgnoreCase(destination)))
                .map(Flight::getId)
                .collect(Collectors.toList());

        JSONObject json = new JSONObject();

        if (flights.isEmpty()) {
            return this.createErrorJSONObject("No flight found");
        } else {
            json.put("status", "SUCCESS");
            JSONObject flightJson = new JSONObject();
            flightJson.put("flightids", flights);
            json.put("data", flightJson);
            return json;
        }
    }
}
