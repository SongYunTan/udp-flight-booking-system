package com.flightsudp.server.services;

import com.flightsudp.server.data.UserMonitoring;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EventManagerSingleton {

    private static EventManagerSingleton INSTANCE;
    private Map<Long, HashMap<String, LocalDateTime>> subscribers;


    public EventManagerSingleton() {
        subscribers = new HashMap<>();
    }

    public static EventManagerSingleton getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EventManagerSingleton();
        }
        return INSTANCE;
    }


    public void addSubscriber(Long f_id, UserMonitoring userMonitoring) {
        if (subscribers.containsKey(f_id)) {
            subscribers.get(f_id).put(userMonitoring.getU_id(), userMonitoring.getDatetime());
        } else {
            HashMap<String, LocalDateTime> userMonitoringMap = new HashMap<>();
            userMonitoringMap.put(userMonitoring.getU_id(), userMonitoring.getDatetime());
            subscribers.put(f_id, userMonitoringMap);
        }
    }

    private Map<Long, HashMap<String, LocalDateTime>> removeExpiredDates(LocalDateTime currentDateTime) {
        Map<Long, HashMap<String, LocalDateTime>> updatedSubscribers = new HashMap<>();

        for (Map.Entry<Long, HashMap<String, LocalDateTime>> entry : subscribers.entrySet()) {
            HashMap<String, LocalDateTime> userMonitoringMap = entry.getValue();
            userMonitoringMap.entrySet().removeIf(item -> item.getValue().isBefore(currentDateTime));
            if (!userMonitoringMap.isEmpty()) {
                updatedSubscribers.put(entry.getKey(), userMonitoringMap);
            }
        }
        return updatedSubscribers;
    }

    public void notifySubscribers(Long f_id, Integer seats) {
        List<String> u_ids = new ArrayList<>();
        LocalDateTime currentDateTime = LocalDateTime.now();

        Map<Long, HashMap<String, LocalDateTime>> updatedSubscribers = removeExpiredDates(currentDateTime);

        HashMap<String, LocalDateTime> userMonitoringMap = updatedSubscribers.get(f_id);
        if (userMonitoringMap != null) {
            for (Map.Entry<String, LocalDateTime> entry : userMonitoringMap.entrySet()) {
                u_ids.add(entry.getKey());
            }
            for (String u_id : u_ids) {
                System.out.println("Number of seats left for Flight " + f_id + " is " + seats);
            }
        }
    }
}