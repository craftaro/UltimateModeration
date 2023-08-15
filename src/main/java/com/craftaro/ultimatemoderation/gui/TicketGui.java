package com.craftaro.ultimatemoderation.gui;

import com.craftaro.core.gui.Gui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.core.input.ChatPrompt;
import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.ultimatemoderation.UltimateModeration;
import com.craftaro.ultimatemoderation.settings.Settings;
import com.craftaro.ultimatemoderation.staffchat.StaffChatManager;
import com.craftaro.ultimatemoderation.tickets.Ticket;
import com.craftaro.ultimatemoderation.tickets.TicketResponse;
import com.craftaro.ultimatemoderation.tickets.TicketStatus;
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
    private final StaffChatManager chatManager;

    private final Ticket ticket;

    private final Player player;
    private final OfflinePlayer toModerate;


    public TicketGui(UltimateModeration plugin, Ticket ticket, OfflinePlayer toModerate, Player player) {
        super(6);
        setDefaultItem(null);
        this.ticket = ticket;
        this.plugin = plugin;
        this.chatManager = plugin.getStaffChatManager();
        this.player = player;
        this.toModerate = toModerate;

        setTitle(plugin.getLocale().getMessage("gui.ticket.title")
                .processPlaceholder("subject", ticket.getSubject())
                .processPlaceholder("id", ticket.getId()).getMessage());

        showPage();
    }

    private void showPage() {
        if (this.inventory != null) {
            this.inventory.clear();
        }
        setActionForRange(0, 53, null);

        int numNotes = this.ticket.getResponses().size();
        this.pages = (int) Math.floor(numNotes / 28.0);

        List<TicketResponse> responses = this.ticket.getResponses().stream().skip((this.page - 1) * 28).limit(28)
                .collect(Collectors.toList());

        // decorate the edges
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(XMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        // edges will be type 3
        mirrorFill(0, 2, true, true, glass3);
        mirrorFill(1, 1, true, true, glass3);

        // decorate corners with type 2
        mirrorFill(0, 0, true, true, glass2);
        mirrorFill(1, 0, true, true, glass2);
        mirrorFill(0, 1, true, true, glass2);

        // enable page event
        setNextPage(4, 7, GuiUtils.createButtonItem(XMaterial.ARROW, this.plugin.getLocale().getMessage("gui.general.next").getMessage()));
        setPrevPage(4, 1, GuiUtils.createButtonItem(XMaterial.ARROW, this.plugin.getLocale().getMessage("gui.general.back").getMessage()));
        setOnPage((event) -> showPage());

        if (this.player.hasPermission("um.tickets.openclose")) {
            setButton(5, 3, GuiUtils.createButtonItem(XMaterial.LEVER, TextUtils.formatText("&6" + this.ticket.getStatus().getStatus())),
                    (event) -> {
                        this.ticket.setStatus(this.ticket.getStatus() == TicketStatus.OPEN ? TicketStatus.CLOSED : TicketStatus.OPEN);
                        this.plugin.getDataHelper().updateTicket(this.ticket);
                        // Notify staff of ticket status
                        this.chatManager.getChat("ticket").messageAll(this.plugin.getLocale().getMessage("notify.ticket.status").getMessage().replace("%tid%", String.valueOf(this.ticket.getId())).replace("%type%", this.ticket.getType()).replace("%player%", Bukkit.getPlayer(this.ticket.getVictim()).getDisplayName()).replace("%status%", this.ticket.getStatus().toString()));
                        showPage();
                    });
        }

        setButton(4, GuiUtils.createButtonItem(XMaterial.OAK_DOOR,
                        this.plugin.getLocale().getMessage("gui.general.back").getMessage()),
                (event) -> {
                    this.plugin.getGuiManager().showGUI(event.player, new TicketManagerGui(this.plugin, this.toModerate, event.player));
                });

        if (this.player.hasPermission("um.ticket.clicktotele") && this.ticket.getLocation() != null) {
            setButton(5, 5, GuiUtils.createButtonItem(XMaterial.ENDER_PEARL,
                            this.plugin.getLocale().getMessage("gui.ticket.clicktotele").getMessage()),
                    (event) -> this.player.teleport(this.ticket.getLocation()));
        }

        if (this.player.hasPermission("um.tickets.respond")) {
            setButton(5, 4, GuiUtils.createButtonItem(XMaterial.WRITABLE_BOOK, this.plugin.getLocale().getMessage("gui.ticket.respond").getMessage()),
                    (event) -> {
                        ChatPrompt.showPrompt(this.plugin, this.player, this.plugin.getLocale().getMessage("gui.ticket.what").getMessage(), (evnt) -> {
                            TicketResponse response = this.ticket.addResponse(new TicketResponse(this.player, evnt.getMessage(), System.currentTimeMillis()));
                            this.plugin.getDataHelper().createTicketResponse(response);
                            // Notify staff of ticket response.
                            this.chatManager.getChat("ticket").messageAll(this.plugin.getLocale().getMessage("notify.ticket.response").getMessage().replace("%tid%", "" + this.ticket.getId()).replace("%type%", this.ticket.getType()).replace("%player%", Bukkit.getPlayer(this.ticket.getVictim()).getDisplayName()));
                            showPage();
                        }).setOnClose(() -> this.guiManager.showGUI(event.player, this));
                    });
        }


        int num = 11;
        for (TicketResponse ticketResponse : responses) {
            if (num == 16 || num == 36) {
                num = num + 2;
            }

            String subjectStr = ticketResponse.getMessage();

            ArrayList<String> lore = new ArrayList<>();
            int lastIndex = 0;
            for (int n = 0; n < subjectStr.length(); n++) {
                if (n - lastIndex < 20) {
                    continue;
                }

                if (subjectStr.charAt(n) == ' ') {
                    lore.add(TextUtils.formatText("&6" + subjectStr.substring(lastIndex, n).trim()));
                    lastIndex = n;
                }
            }

            if (lastIndex - subjectStr.length() < 20) {
                lore.add(TextUtils.formatText("&6" + subjectStr.substring(lastIndex).trim()));
            }

            String name = lore.get(0);
            lore.remove(0);

            lore.add("");

            SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");

            lore.add(this.plugin.getLocale().getMessage("gui.ticket.postedby")
                    .processPlaceholder("player", Bukkit.getOfflinePlayer(ticketResponse.getAuthor()).getName()).getMessage());
            lore.add(this.plugin.getLocale().getMessage("gui.ticket.createdon")
                    .processPlaceholder("sent", format.format(new Date(ticketResponse.getPostedDate()))).getMessage());

            setItem(num, GuiUtils.createButtonItem(XMaterial.MAP, TextUtils.formatText(name), lore));
            num++;
        }
    }
}
