package com.songoda.ultimatemoderation.tickets;

public enum TicketStatus {
    OPEN("Open"),
    CLOSED("Closed");

    private String status;

    TicketStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
