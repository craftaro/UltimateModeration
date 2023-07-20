package com.songoda.ultimatemoderation.tickets;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Ticket {
    private int id;

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

    public Ticket(int id, UUID victim, String subject, String type, TicketStatus status, Location location) {
        this.id = id;
        this.victim = victim;
        this.subject = subject;
        this.type = type;
        this.status = status;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<TicketResponse> getResponses() {
        return new ArrayList<>(tickets);
    }

    public TicketResponse addResponse(TicketResponse response) {
        response.setTicketId(id);
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
