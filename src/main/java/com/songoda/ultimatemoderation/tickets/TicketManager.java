package com.songoda.ultimatemoderation.tickets;

import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

public class TicketManager {
    private final TreeMap<Integer, Ticket> registeredTickets = new TreeMap<>();

    public Ticket addTicket(Ticket ticket) {
        int id = registeredTickets.isEmpty() ? 1 : registeredTickets.lastEntry().getValue().getId() + 1;
        ticket.setId(id);
        return addTicket(ticket, id);
    }

    public Ticket addTicket(Ticket ticket, int id) {
        registeredTickets.put(id, ticket);
        return ticket;
    }

    public Ticket getTicket(int id) {
        return registeredTickets.get(id);
    }

    public List<Ticket> getTickets() {
        return new ArrayList<>(registeredTickets.values());
    }

    public List<Ticket> getTickets(TicketStatus status) {
        return registeredTickets.values().stream().filter(ticket -> ticket.getStatus() == status).collect(Collectors.toList());
    }

    public List<Ticket> getTicketsAbout(OfflinePlayer player) {
        return getTicketsAbout(player.getUniqueId());
    }

    public List<Ticket> getTicketsAbout(UUID player) {
        return registeredTickets.values().stream()
                .filter(ticket -> ticket.getVictim().equals(player)).collect(Collectors.toList());
    }

    public List<Ticket> getTicketsAbout(OfflinePlayer player, TicketStatus status) {
        return getTicketsAbout(player.getUniqueId(), status);
    }

    public List<Ticket> getTicketsAbout(UUID player, TicketStatus status) {
        return registeredTickets.values().stream()
                .filter(ticket -> ticket.getVictim().equals(player) && ticket.getStatus() == status).collect(Collectors.toList());
    }
}
