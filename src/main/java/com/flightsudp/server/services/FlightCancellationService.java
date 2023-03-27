package com.flightsudp.server.services;

import com.flightsudp.server.data.Flight;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class FlightCancellationService extends AbstractService {

    private List<Flight> allFlights;
    private Map<Long, Flight> allFlightsMap;

    public FlightCancellationService(List<Flight> allFlights) {
        super(allFlights);
        this.allFlights = allFlights;
        this.allFlightsMap = this.getAllFlightsMap();
    }
    @Override
    public JSONObject execute(JSONObject jsonRequest, String address, String port){
        return cancelFlight(jsonRequest, address, port);
    }

    private JSONObject cancelFlight(JSONObject jsonRequest, String address, String port) {
        JSONObject data = jsonRequest.getJSONObject("data");
        String user_id = address + "@" + port;
        Long f_id = Long.valueOf(data.getString("flightid"));
        int numSeats = Integer.parseInt(data.getString("numSeats"));

        Flight flight = allFlightsMap.get(f_id);
        if (flight != null) {
            int seatsAvail = flight.cancelSeats(user_id, numSeats);
            if (seatsAvail == -1) {
                return this.createErrorJSONObject("You're trying to cancel too many seats");
            }
            JSONObject json = new JSONObject();
            json.put("status", "SUCCESS");
            JSONObject flightJson = new JSONObject();
            flightJson.put("seats_available", seatsAvail);

            try {
                this.getEventManager().notifySubscribers(f_id, seatsAvail);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
            

            return json;
        }
        return this.createErrorJSONObject("No flight found");

    }
}
