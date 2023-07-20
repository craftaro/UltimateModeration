package com.songoda.ultimatemoderation.gui;

import com.craftaro.core.gui.AnvilGui;
import com.craftaro.core.gui.Gui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.utils.TextUtils;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.settings.Settings;
import com.songoda.ultimatemoderation.tickets.Ticket;
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

// FIXME: Pagination not working (probably in other GUIs too) (copy own one from TemplateManagerGui)
public class TicketManagerGui extends Gui {
    private final UltimateModeration plugin;

    private final OfflinePlayer toModerate;

    private TicketStatus status = TicketStatus.OPEN;

    private final Player player;

    public TicketManagerGui(UltimateModeration plugin, OfflinePlayer toModerate, Player player) {
        super(6);
        setDefaultItem(null);
        this.plugin = plugin;
        this.toModerate = toModerate;
        this.player = player;

        setTitle(plugin.getLocale().getMessage(toModerate != null ? "gui.tickets.titlesingle" : "gui.tickets.title")
                .processPlaceholder("toModerate", toModerate != null ? toModerate.getName() : "").getMessage());
        showPage();
    }

    private void showPage() {
        if (this.inventory != null) {
            this.inventory.clear();
        }
        setActionForRange(0, 53, null);

        List<Ticket> tickets = this.toModerate != null
                ? this.plugin.getTicketManager().getTicketsAbout(this.toModerate, this.status)
                : this.plugin.getTicketManager().getTickets(this.status);

        int numTickets = tickets.size();
        this.pages = (int) Math.floor(numTickets / 28.0);

        tickets = tickets.stream().skip((this.page - 1) * 28).limit(28).collect(Collectors.toList());

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

        setButton(5, 3, GuiUtils.createButtonItem(XMaterial.LEVER, TextUtils.formatText("&6" + this.status.getStatus())),
                (event) -> {
                    this.status = this.status == TicketStatus.OPEN ? TicketStatus.CLOSED : TicketStatus.OPEN;
                    this.page = 1;
                    showPage();
                });

        if (this.toModerate != null && this.player.hasPermission("um.tickets.create")) {
            setButton(5, 5, GuiUtils.createButtonItem(XMaterial.REDSTONE,
                            this.plugin.getLocale().getMessage("gui.tickets.create").getMessage()),
                    (event) -> createNew(this.player, this.toModerate));
        }

        if (this.player.hasPermission("um.ticket")) {
            setButton(5, 4, GuiUtils.createButtonItem(XMaterial.OAK_DOOR,
                            this.plugin.getLocale().getMessage("gui.general.back").getMessage()),
                    (event) -> {
                        if (this.toModerate == null) {
                            this.plugin.getGuiManager().showGUI(this.player, new MainGui(this.plugin, this.player));
                        } else {
                            this.plugin.getGuiManager().showGUI(event.player, new PlayerGui(this.plugin, this.toModerate, event.player));
                        }
                    });
        }

        int num = 11;
        for (Ticket ticket : tickets) {
            if (num == 16 || num == 36) {
                num = num + 2;
            }

            String subjectStr = ticket.getSubject();

            ArrayList<String> lore = new ArrayList<>();
            int lastIndex = 0;
            for (int n = 0; n < subjectStr.length(); n++) {
                if (n - lastIndex < 20) {
                    continue;
                }

                if (subjectStr.charAt(n) == ' ') {
                    lore.add("&6" + subjectStr.substring(lastIndex, n).trim());
                    lastIndex = n;
                }
            }

            if (lastIndex - subjectStr.length() < 20) {
                lore.add("&6" + subjectStr.substring(lastIndex).trim() + " &7- " + ticket.getId());
            }

            String name = lore.get(0);
            lore.remove(0);

            lore.add("");

            SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");

            lore.add(this.plugin.getLocale().getMessage("gui.ticket.status")
                    .processPlaceholder("status", ticket.getStatus().getStatus()).getMessage());

            if (this.toModerate != null) {
                lore.add(this.plugin.getLocale().getMessage("gui.tickets.player")
                        .processPlaceholder("player", Bukkit.getOfflinePlayer(ticket.getVictim()).getName()).getMessage());
            }
            lore.add(this.plugin.getLocale().getMessage("gui.ticket.type")
                    .processPlaceholder("type", ticket.getType()).getMessage());
            lore.add(this.plugin.getLocale().getMessage("gui.ticket.createdon")
                    .processPlaceholder("sent", format.format(new Date(ticket.getCreationDate()))).getMessage());
            lore.add(this.plugin.getLocale().getMessage("gui.tickets.click").getMessage());

            setButton(num, GuiUtils.createButtonItem(XMaterial.MAP,
                            TextUtils.formatText(name), TextUtils.formatText(lore)),
                    (event) -> this.guiManager.showGUI(this.player, new TicketGui(this.plugin, ticket, this.toModerate, this.player)));
            num++;
        }
    }

    public static void createNew(Player player, OfflinePlayer toModerate) {
        UltimateModeration plugin = UltimateModeration.getPlugin(UltimateModeration.class);

        AnvilGui gui = new AnvilGui(player);
        gui.setAction((event) ->
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
                        plugin.getGuiManager().showGUI(player,
                                new TicketTypeGui(plugin, toModerate, player, gui.getInputText())), 1L));

        ItemStack item = GuiUtils.createButtonItem(XMaterial.PAPER,
                plugin.getLocale().getMessage("gui.tickets.subject").getMessage());

        gui.setInput(item);
        plugin.getGuiManager().showGUI(player, gui);
    }
}
