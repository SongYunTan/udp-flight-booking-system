package com.flightsudp.server;

import com.flightsudp.server.data.Flight;
import com.flightsudp.server.data.MockFlights;
import com.flightsudp.server.services.*;
import org.json.JSONObject;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FlightServerController {
    private Map<String, AbstractService> serviceMap;
    private List<Flight> allFlights = MockFlights.getAllFlights();
    private Map<UUID, String> seenMap;

    public FlightServerController() {
        serviceMap = new HashMap<>();
        // Add FlightService implementations to the service map
        serviceMap.put("flightids", new FlightIDsService(allFlights));
        serviceMap.put("flightdetails", new FlightDetailsService(allFlights));
        serviceMap.put("flightreservation", new FlightReservationService(allFlights));
        serviceMap.put("monitorupdates", new MonitorUpdatesService(allFlights));
        serviceMap.put("listFlights", new FlightViewAllService(allFlights));
        serviceMap.put("flightcancellation", new FlightCancellationService(allFlights));

        seenMap = new HashMap<>();
    }

    public String processInput(String input, InetAddress clientAddress, Integer clientPort) throws Exception {
        // Parse request JSON from input data
        JSONObject requestJson = new JSONObject(input);
        String functionName = requestJson.getString("function");

        // Check against seenMap
        String str_uuid = requestJson.getString("uuid");
        UUID uuid = UUID.fromString(str_uuid);
        String cachedResponse = seenMap.get(uuid);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        // Invoke the appropriate service method based on the function name
        AbstractService service = serviceMap.get(functionName);
        if (service == null) {
            return generateErrorResponse("Invalid function name: " + functionName);
        }
        JSONObject result = service.execute(requestJson, clientAddress.getHostAddress(), clientPort.toString());

        // Construct response JSON with status and result data
        JSONObject responseJson = new JSONObject();
        responseJson.put("status", "SUCCESS");
        responseJson.put("data", result);
        String responseString = responseJson.toString();

        seenMap.put(uuid, responseString);

        return responseString;
    }

    private String generateErrorResponse(String message) {
        // Construct response JSON with error status and error message
        JSONObject responseJson = new JSONObject();
        responseJson.put("status", "ERROR");
        JSONObject errorData = new JSONObject();
        errorData.put("message", message);
        responseJson.put("data", errorData);

        return responseJson.toString();
    }



    // TODO: DUANKAI handle duplicate req messages (at-least-once semantics) via histories - DONE
    // TODO: DUANKAI handle monitoring -> return "monitoring closed" as result when time is up
}
