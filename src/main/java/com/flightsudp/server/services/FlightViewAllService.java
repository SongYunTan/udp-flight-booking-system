package com.flightsudp.server.services;

import com.flightsudp.server.data.Flight;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlightViewAllService extends AbstractService {
    private List<Flight> allFlights;
    private Map<Long, Flight> allFlightsMap;


    public FlightViewAllService(List<Flight> allFlights) {
        super(allFlights);
        this.allFlights = allFlights;
        this.allFlightsMap = allFlightsMap;
    }

    @Override
    public JSONObject execute(JSONObject jsonRequest, String address, String port) {
        return getAllFlights(jsonRequest, address, port);
    }

    private JSONObject getAllFlights(JSONObject jsonRequest, String address, String port) {
        JSONObject json = new JSONObject();

        if (allFlights.isEmpty()) {
            return this.createErrorJSONObject("No flight found");
        } else {
            json.put("status", "SUCCESS");
            JSONObject flightJson = new JSONObject();
            flightJson.put("all_flights", allFlights.stream().map(Flight::getId).collect(Collectors.toList()));
            json.put("data", flightJson);
            return json;
        }
    }
}
