package com.flightsudp.server;

import org.json.JSONObject;

public interface FlightService {
    JSONObject execute(JSONObject params) throws Exception;
}
