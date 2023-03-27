package com.flightsudp.server.services;

import com.flightsudp.server.data.DateTimeString;
import com.flightsudp.server.data.Flight;
import com.flightsudp.server.data.UserMonitoring;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class MonitorUpdatesService extends AbstractService {
    private List<Flight> allFlights;
    private Map<Long, Flight> allFlightsMap;

    public MonitorUpdatesService(List<Flight> allFlights) {
        super(allFlights);
    }

    @Override
    public JSONObject execute(JSONObject jsonRequest, String address, String port) {
        return addToMonitor(jsonRequest, address, port);
    }

    private JSONObject addToMonitor(JSONObject jsonRequest, String address, String port) {
        JSONObject data = jsonRequest.getJSONObject("data");
        String user_id = address + "@" + port;
        Long f_id = Long.valueOf(data.getString("flightid"));
//        String dateTimeString = "2022-04-01T12:00:00";
        LocalDateTime monitorEndDateTime = new DateTimeString(data.getString("expiryDate")).getLocalDateTime();

        if (allFlightsMap.containsKey(f_id)) {
            UserMonitoring userMonitoring = new UserMonitoring();
            userMonitoring.setU_id(user_id);
            userMonitoring.setDatetime(monitorEndDateTime);
            this.getEventManager().addSubscriber(f_id, userMonitoring);

            JSONObject json = new JSONObject();
            json.put("status", "SUCCESS");
            return json;
        }
        return this.createErrorJSONObject("No flight found");


    }
}