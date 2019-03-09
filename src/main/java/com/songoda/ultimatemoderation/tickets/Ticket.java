package com.songoda.ultimatemoderation.tickets;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Ticket {

    private int ticketId;

    private TicketStatus status = TicketStatus.OPEN;
    private Location location = null;

    private String type;

    private final List<TicketResponse> tickets = new ArrayList<>();
    private final UUID victim;
    private final String subject;

    public Ticket(OfflinePlayer victim, String subject, String type) {
        this.victim = victim.getUniqueId();
        this.subject = subject;
        this.type = type;
    }

    public Ticket(UUID victim, String subject, String type) {
        this.victim = victim;
        this.subject = subject;
        this.type = type;
    }

    public Ticket(OfflinePlayer victim, String subject, String type, TicketResponse response) {
        this.victim = victim.getUniqueId();
        this.subject = subject;
        this.type = type;
        this.tickets.add(response);
    }

    public Ticket(UUID victim, String subject, String type, TicketResponse response) {
        this.victim = victim;
        this.subject = subject;
        this.type = type;
        this.tickets.add(response);
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public List<TicketResponse> getResponses() {
        return new ArrayList<>(tickets);
    }

    public TicketResponse addResponse(TicketResponse response) {
        response.setTicketId(ticketId);
        tickets.add(response);
        return response;
    }

    public TicketResponse removeResponse(TicketResponse response) {
        tickets.remove(response);
        return response;
    }

    public UUID getVictim() {
        return victim;
    }

    public String getSubject() {
        return subject;
    }

    public long getCreationDate() {
        return tickets.get(0).getPostedDate();
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
