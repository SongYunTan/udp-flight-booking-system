package com.flightsudp.client;

import org.json.JSONObject;
import java.net.DatagramPacket;
import java.util.UUID;

public class FlightClientController {

    public JSONObject obtainUUID(JSONObject requestJson) {
        UUID uuid = UUID.randomUUID();
        String UUIDString = uuid.toString();

        // Construct request JSON with function name and parameters
        requestJson.put("uuid", UUIDString);
        return requestJson;
    }

    public byte[] generateRequest(JSONObject requestJson) {
        // Convert request JSON to byte array
        return requestJson.toString().getBytes();
    }

    public JSONObject processResponse(DatagramPacket responsePacket) throws Exception {
        // Unmarshal response JSON from response packet
        byte[] responseData = responsePacket.getData();
        JSONObject responseJson = new JSONObject(new String(responseData, 0, responsePacket.getLength()));

        // Get response status and data
        String status = responseJson.getString("status");
        JSONObject data = responseJson.getJSONObject("data");

        // Check response status and return data or error message
        if (status.equals("success")) {
            return data;
        } else {
            throw new Exception(data.getString("message"));
        }
    }
}
