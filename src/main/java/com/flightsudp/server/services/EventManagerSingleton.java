package com.flightsudp.server.services;

import com.flightsudp.server.data.UserMonitoring;

import java.net.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

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

    public void notifySubscribers(Long f_id, Integer seats) throws Exception {
        List<String> u_ids = new ArrayList<>();
        LocalDateTime currentDateTime = LocalDateTime.now();

        Map<Long, HashMap<String, LocalDateTime>> updatedSubscribers = removeExpiredDates(currentDateTime);

        HashMap<String, LocalDateTime> userMonitoringMap = updatedSubscribers.get(f_id);
        if (userMonitoringMap != null) {
            for (Map.Entry<String, LocalDateTime> entry : userMonitoringMap.entrySet()) {
                u_ids.add(entry.getKey());
            }
            
            DatagramSocket socket = new DatagramSocket();
                
            for (String u_id : u_ids) {
                JSONObject response = new JSONObject();
                response.put("status", "SUCCESS");
                JSONObject data = new JSONObject();
                data.put("message", "Number of seats left for Flight " + f_id + " is " + seats);
                response.put("data", data);

                String[] clientInfo = u_id.split("@"); // address@port
                InetAddress clientAddress = InetAddress.getByName(clientInfo[0]);
                Integer clientPort = Integer.parseInt(clientInfo[1]);

                byte[] sendData = new byte[1024];
                sendData = response.toString().getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                socket.send(sendPacket);
            }

            socket.close();
        }
    }
}