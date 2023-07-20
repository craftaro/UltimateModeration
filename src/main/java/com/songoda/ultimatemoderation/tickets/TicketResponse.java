package com.songoda.ultimatemoderation.tickets;

import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class TicketResponse {
    private int ticketId;

    private final UUID author;
    private final String message;
    private final long posted;

    public TicketResponse(OfflinePlayer author, String message, long posted) {
        this.author = author.getUniqueId();
        this.message = message;
        this.posted = posted;
    }

    public TicketResponse(UUID author, String message, long posted) {
        this.author = author;
        this.message = message;
        this.posted = posted;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public UUID getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    public long getPostedDate() {
        return posted;
    }
}
