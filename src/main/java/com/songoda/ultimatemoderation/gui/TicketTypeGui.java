package com.songoda.ultimatemoderation.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.input.ChatPrompt;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.settings.Settings;
import com.songoda.ultimatemoderation.staffchat.StaffChatManager;
import com.songoda.ultimatemoderation.tickets.Ticket;
import com.songoda.ultimatemoderation.tickets.TicketResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class TicketTypeGui extends Gui {

    private final StaffChatManager chatManager = UltimateModeration.getInstance().getStaffChatManager();

    public TicketTypeGui(UltimateModeration plugin, OfflinePlayer toModerate, Player player, String subject) {
        super(3);
        setDefaultItem(null);

        setTitle(plugin.getLocale().getMessage("gui.ticket.picktype").getMessage());


        List<String> types = Settings.TICKET_TYPES.getStringList();

        for (int i = 0; i < types.size(); i++) {
            final int fi = i;
            setButton(i, GuiUtils.createButtonItem(CompatibleMaterial.PAPER, types.get(i)),
                    (event) -> {
                        Ticket ticket = new Ticket(toModerate, subject, types.get(fi));
                        ChatPrompt.showPrompt(plugin,
                                player, plugin.getLocale().getMessage("gui.tickets.what").getMessage(),
                                event2 -> {
                                    plugin.getTicketManager().addTicket(ticket);

                                    // Notify staff
                                    chatManager.getChat("ticket").messageAll(UltimateModeration.getInstance().getLocale().getMessage("notify.ticket.created").getMessage().replace("%tid%", "" + ticket.getId()).replace("%type%", ticket.getType()).replace("%player%", Bukkit.getPlayer(ticket.getVictim()).getDisplayName()));
                                    if (player == toModerate)
                                        ticket.setLocation(player.getLocation());
                                    ticket.addResponse(new TicketResponse(player, event2.getMessage(), System.currentTimeMillis()));
                                    plugin.getDataManager().createTicket(ticket);
                                }).setOnClose(() ->
                                guiManager.showGUI(event.player, new TicketGui(plugin, ticket, toModerate, player)));
                    });
        }
    }
}
