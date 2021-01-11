package com.songoda.ultimatemoderation.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.input.ChatPrompt;
import com.songoda.core.utils.TextUtils;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.settings.Settings;
import com.songoda.ultimatemoderation.staffchat.StaffChatManager;
import com.songoda.ultimatemoderation.tickets.Ticket;
import com.songoda.ultimatemoderation.tickets.TicketResponse;
import com.songoda.ultimatemoderation.tickets.TicketStatus;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TicketGui extends Gui {

    private final UltimateModeration plugin;
    private final StaffChatManager chatManager = UltimateModeration.getInstance().getStaffChatManager();

    private final Ticket ticket;

    private final Player player;
    private final OfflinePlayer toModerate;


    public TicketGui(UltimateModeration plugin, Ticket ticket, OfflinePlayer toModerate, Player player) {
        super(6);
        setDefaultItem(null);
        this.ticket = ticket;
        this.plugin = plugin;
        this.player = player;
        this.toModerate = toModerate;

        setTitle(plugin.getLocale().getMessage("gui.ticket.title")
                .processPlaceholder("subject", ticket.getSubject())
                .processPlaceholder("id", ticket.getId()).getMessage());

        showPage();
    }

    private void showPage() {
        if (inventory != null)
            inventory.clear();
        setActionForRange(0, 53, null);

        int numNotes = ticket.getResponses().size();
        this.pages = (int) Math.floor(numNotes / 28.0);

        List<TicketResponse> responses = ticket.getResponses().stream().skip((page - 1) * 28).limit(28)
                .collect(Collectors.toList());

        // decorate the edges
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(CompatibleMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(CompatibleMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        // edges will be type 3
        mirrorFill(0, 2, true, true, glass3);
        mirrorFill(1, 1, true, true, glass3);

        // decorate corners with type 2
        mirrorFill(0, 0, true, true, glass2);
        mirrorFill(1, 0, true, true, glass2);
        mirrorFill(0, 1, true, true, glass2);

        // enable page event
        setNextPage(4, 7, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, plugin.getLocale().getMessage("gui.general.next").getMessage()));
        setPrevPage(4, 1, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, plugin.getLocale().getMessage("gui.general.back").getMessage()));
        setOnPage((event) -> showPage());

        if (player.hasPermission("um.tickets.openclose"))
            setButton(5, 3, GuiUtils.createButtonItem(CompatibleMaterial.LEVER, TextUtils.formatText("&6" + ticket.getStatus().getStatus())),
                    (event) -> {
                        ticket.setStatus(ticket.getStatus() == TicketStatus.OPEN ? TicketStatus.CLOSED : TicketStatus.OPEN);
                        plugin.getDataManager().updateTicket(ticket);
                        // Notify staff of ticket status
                        chatManager.getChat("ticket").messageAll(UltimateModeration.getInstance().getLocale().getMessage("notify.ticket.status").getMessage().replace("%tid%", "" + ticket.getId()).replace("%type%", ticket.getType()).replace("%player%", Bukkit.getPlayer(ticket.getVictim()).getDisplayName()).replace("%status%", ticket.getStatus().toString()));
                        showPage();
                    });

        setButton(4, GuiUtils.createButtonItem(CompatibleMaterial.OAK_DOOR,
                plugin.getLocale().getMessage("gui.general.back").getMessage()),
                (event) -> {
                    plugin.getGuiManager().showGUI(event.player, new TicketManagerGui(plugin, toModerate, event.player));
                });

        if (player.hasPermission("um.ticket.clicktotele") && ticket.getLocation() != null)
            setButton(5, 5, GuiUtils.createButtonItem(CompatibleMaterial.ENDER_PEARL,
                    plugin.getLocale().getMessage("gui.ticket.clicktotele").getMessage()),
                    (event) -> player.teleport(ticket.getLocation()));

        if (player.hasPermission("um.tickets.respond"))
            setButton(5, 4, GuiUtils.createButtonItem(CompatibleMaterial.WRITABLE_BOOK, plugin.getLocale().getMessage("gui.ticket.respond").getMessage()),
                    (event) -> {
                        ChatPrompt.showPrompt(plugin, player, plugin.getLocale().getMessage("gui.ticket.what").getMessage(), (evnt) -> {
                            TicketResponse response = ticket.addResponse(new TicketResponse(player, evnt.getMessage(), System.currentTimeMillis()));
                            plugin.getDataManager().createTicketResponse(response);
                            // Notify staff of ticket response.
                            chatManager.getChat("ticket").messageAll(UltimateModeration.getInstance().getLocale().getMessage("notify.ticket.response").getMessage().replace("%tid%", "" + ticket.getId()).replace("%type%", ticket.getType()).replace("%player%", Bukkit.getPlayer(ticket.getVictim()).getDisplayName()));
                            showPage();
                        }).setOnClose(() -> guiManager.showGUI(event.player, this));
                    });


        int num = 11;
        for (TicketResponse ticketResponse : responses) {
            if (num == 16 || num == 36)
                num = num + 2;

            String subjectStr = ticketResponse.getMessage();

            ArrayList<String> lore = new ArrayList<>();
            int lastIndex = 0;
            for (int n = 0; n < subjectStr.length(); n++) {
                if (n - lastIndex < 20)
                    continue;

                if (subjectStr.charAt(n) == ' ') {
                    lore.add(TextUtils.formatText("&6" + subjectStr.substring(lastIndex, n).trim()));
                    lastIndex = n;
                }
            }

            if (lastIndex - subjectStr.length() < 20)
                lore.add(TextUtils.formatText("&6" + subjectStr.substring(lastIndex).trim()));

            String name = lore.get(0);
            lore.remove(0);

            lore.add("");

            SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");


            lore.add(plugin.getLocale().getMessage("gui.ticket.postedby")
                    .processPlaceholder("player", Bukkit.getOfflinePlayer(ticketResponse.getAuthor()).getName()).getMessage());
            lore.add(plugin.getLocale().getMessage("gui.ticket.createdon")
                    .processPlaceholder("sent", format.format(new Date(ticketResponse.getPostedDate()))).getMessage());

            setItem(num, GuiUtils.createButtonItem(CompatibleMaterial.MAP, TextUtils.formatText(name), lore));
            num++;
        }
    }
}
