package com.craftaro.ultimatemoderation.gui;

import com.craftaro.core.gui.Gui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.core.input.ChatPrompt;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.ultimatemoderation.UltimateModeration;
import com.craftaro.ultimatemoderation.settings.Settings;
import com.craftaro.ultimatemoderation.staffchat.StaffChatManager;
import com.craftaro.ultimatemoderation.tickets.Ticket;
import com.craftaro.ultimatemoderation.tickets.TicketResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class TicketTypeGui extends Gui {
    private final StaffChatManager chatManager;

    public TicketTypeGui(UltimateModeration plugin, OfflinePlayer toModerate, Player player, String subject) {
        super(3);
        this.chatManager = plugin.getStaffChatManager();

        setDefaultItem(null);
        setTitle(plugin.getLocale().getMessage("gui.ticket.picktype").getMessage());

        List<String> types = Settings.TICKET_TYPES.getStringList();

        for (int i = 0; i < types.size(); i++) {
            final int fi = i;
            setButton(i, GuiUtils.createButtonItem(XMaterial.PAPER, types.get(i)),
                    (event) -> {
                        Ticket ticket = new Ticket(toModerate, subject, types.get(fi));
                        ChatPrompt.showPrompt(plugin,
                                player, plugin.getLocale().getMessage("gui.tickets.what").getMessage(),
                                event2 -> {
                                    plugin.getTicketManager().addTicket(ticket);

                                    // Notify staff
                                    this.chatManager.getChat("ticket").messageAll(plugin.getLocale().getMessage("notify.ticket.created").getMessage().replace("%tid%", String.valueOf(ticket.getId())).replace("%type%", ticket.getType()).replace("%player%", Bukkit.getPlayer(ticket.getVictim()).getDisplayName()));
                                    if (player == toModerate) {
                                        ticket.setLocation(player.getLocation());
                                    }
                                    ticket.addResponse(new TicketResponse(player, event2.getMessage(), System.currentTimeMillis()));
                                    plugin.getDataHelper().createTicket(ticket);
                                }).setOnClose(() ->
                                this.guiManager.showGUI(event.player, new TicketGui(plugin, ticket, toModerate, player)));
                    });
        }
    }
}
