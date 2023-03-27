package com.flightsudp.server.services;

import com.flightsudp.server.data.Flight;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractService {

    private List<Flight> allFlights;
    private Map<Long, Flight> allFlightsMap;
    private EventManagerSingleton eventManager = EventManagerSingleton.getInstance();


    public AbstractService(List<Flight> allFlights) {
        this.allFlights = allFlights;
        this.allFlightsMap = allFlights
                .stream()
                .collect(Collectors.toMap(Flight::getId, Function.identity()));
    }

    EventManagerSingleton getEventManager() {
        return eventManager;
    }

    public JSONObject createErrorJSONObject(String errorMessage){
        JSONObject json = new JSONObject();
        json.put("status", "FAILURE");
        json.put("message", errorMessage);
        return json;
    }

    public abstract JSONObject execute(JSONObject jsonRequest, String address, String port);
}
