package com.flightsudp.server.data;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserMonitoring {
    private String u_id;
    private LocalDateTime datetime;
}
