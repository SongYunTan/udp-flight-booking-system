package com.flightsudp.server.data;

import java.time.LocalDateTime;

public class DateTimeString {
    String dateTimeString;

    public DateTimeString(String dateTimeString) {
        this.dateTimeString = dateTimeString;
    }

    public LocalDateTime getLocalDateTime() {
        return LocalDateTime.parse(this.dateTimeString);
    }
}
