package com.flightsudp.server.data;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class Flight {
    Long id;
    String source;
    String destination;
    LocalDateTime departureTime;
    Long airfare;
    int totalSeats;
    Map<String, Integer> passengersInfoMap = new HashMap<String, Integer>();

    public int reserveSeats(String userId, Integer numSeats) {
        if (totalSeats - numSeats >= 0) {
            totalSeats = totalSeats - numSeats;
            // similar to defaultdict in python
            passengersInfoMap.compute(userId, (k, v) -> (v == null) ? numSeats : v + numSeats);
            return totalSeats;
        }
        return -1;
    }

    public int cancelSeats(String userId, Integer numSeats) {
        if (passengersInfoMap.containsKey(userId) && passengersInfoMap.get(userId) >= numSeats) {
            // Update the map with the new value
            passengersInfoMap.computeIfPresent(userId, (k, v) -> v - numSeats);
            totalSeats = totalSeats - numSeats;
            return totalSeats;
        }
        return -1;
    }


}
