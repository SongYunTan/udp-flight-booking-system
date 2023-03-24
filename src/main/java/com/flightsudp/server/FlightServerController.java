package com.flightsudp.server;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FlightServerController {
    private Map<String, FlightService> serviceMap;

    public FlightServerController() {
        serviceMap = new HashMap<>();
        // Add FlightService implementations to the service map
        serviceMap.put("listFlights", new ListFlightsService());
        // serviceMap.put("bookFlight", new BookFlightService());
        // Add additional services as necessary
    }

    public String processInput(String input) throws Exception {
        // Parse request JSON from input data
        JSONObject requestJson = new JSONObject(input);
        String functionName = requestJson.getString("function");
        JSONObject params = requestJson.getJSONObject("data");

        // Invoke the appropriate service method based on the function name
        FlightService service = serviceMap.get(functionName);
        if (service == null) {
            return generateErrorResponse("Invalid function name: " + functionName);
        }
        JSONObject result = service.execute(params);

        // Construct response JSON with status and result data
        JSONObject responseJson = new JSONObject();
        responseJson.put("status", "success");
        responseJson.put("data", result);

        return responseJson.toString();
    }

    private String generateErrorResponse(String message) {
        // Construct response JSON with error status and error message
        JSONObject responseJson = new JSONObject();
        responseJson.put("status", "error");
        JSONObject errorData = new JSONObject();
        errorData.put("message", message);
        responseJson.put("data", errorData);

        return responseJson.toString();
    }
}
