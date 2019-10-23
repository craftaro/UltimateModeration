package com.songoda.ultimatemoderation.gui;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.tickets.Ticket;
import com.songoda.ultimatemoderation.tickets.TicketResponse;
import com.songoda.ultimatemoderation.utils.AbstractChatConfirm;
import com.songoda.ultimatemoderation.utils.gui.AbstractGUI;
import com.songoda.ultimatemoderation.settings.Settings;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import com.songoda.ultimatemoderation.staffchat.*;

import java.util.List;
import org.bukkit.Bukkit;

public class GUITicketType extends AbstractGUI {

    private final UltimateModeration plugin;
    private StaffChatManager chatManager = UltimateModeration.getInstance().getStaffChatManager();

    private final OfflinePlayer toModerate;
    private final String subject;

    public GUITicketType(UltimateModeration plugin, OfflinePlayer toModerate, Player player, String subject) {
        super(player);
        this.plugin = plugin;
        this.toModerate = toModerate;
        this.subject = subject;

        init(plugin.getLocale().getMessage("gui.ticket.picktype").getMessage(), 27);
    }

    @Override
    protected void constructGUI() {
        inventory.clear();
        resetClickables();
        registerClickables();

        List<String> types = Settings.TICKET_TYPES.getStringList();

        for (int i = 0; i < types.size(); i ++) {
            createButton(i, Material.PAPER, types.get(i));
            final int fi = i;
            registerClickable(i, (player1, inventory1, cursor, slot, type) -> {
                Ticket ticket = new Ticket(toModerate, subject, types.get(fi));
                player.sendMessage(plugin.getLocale().getMessage("gui.tickets.what").getMessage());
                AbstractChatConfirm abstractChatConfirm = new AbstractChatConfirm(player, event2 -> {
                    plugin.getTicketManager().addTicket(ticket);
                    // Notify staff
                    chatManager.getChat("ticket").messageAll(UltimateModeration.getInstance().getLocale().getMessage("notify.ticket.created").getMessage().replace("%tid%", ""+ticket.getTicketId()).replace("%type%", ticket.getType()).replace("%player%", Bukkit.getPlayer(ticket.getVictim()).getDisplayName()));
                    if (player == toModerate)
                        ticket.setLocation(player.getLocation());
                    ticket.addResponse(new TicketResponse(player, event2.getMessage(), System.currentTimeMillis()));
                });

                abstractChatConfirm.setOnClose(() ->
                        new GUITicket(plugin, ticket, toModerate, player));
            });
        }
    }

    @Override
    protected void registerClickables() {
    }

    @Override
    protected void registerOnCloses() {
    }
}
