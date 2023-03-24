package com.flightsudp.client;

import org.json.JSONObject;

import java.net.DatagramPacket;

public class FlightClientController {
    public byte[] processInput(String input) throws Exception {
        // Parse input to determine server side function to be invoked and necessary parameters
        String[] tokens = input.split(" ");
        String functionName = tokens[0];
        
        if (tokens.length % 2 != 1) {
            throw new Exception("Input should be <function> <parameter name> <parameter value>");
        }
        JSONObject params = new JSONObject();
        for (int i = 1; i < tokens.length; i += 2) {
            String paramName = tokens[i];
            String paramValue = tokens[i+1];
            params.put(paramName, paramValue);
        }

        // Construct request JSON with function name and parameters
        JSONObject requestJson = new JSONObject();
        requestJson.put("function", functionName);
        requestJson.put("data", params);

        // Convert request JSON to byte array
        byte[] requestData = requestJson.toString().getBytes();

        return requestData;
    }

    public String processResponse(DatagramPacket responsePacket) throws Exception {
        // Unmarshal response JSON from response packet
        byte[] responseData = responsePacket.getData();
        JSONObject responseJson = new JSONObject(new String(responseData, 0, responsePacket.getLength()));

        // Get response status and data
        String status = responseJson.getString("status");
        JSONObject data = responseJson.getJSONObject("data");

        // Check response status and return data or error message
        if (status.equals("success")) {
            return data.toString();
        } else {
            return "Error: " + data.getString("message");
        }
    }
}
