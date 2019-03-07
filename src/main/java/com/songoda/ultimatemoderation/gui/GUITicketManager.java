package com.songoda.ultimatemoderation.gui;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.PunishmentNote;
import com.songoda.ultimatemoderation.tickets.Ticket;
import com.songoda.ultimatemoderation.tickets.TicketResponse;
import com.songoda.ultimatemoderation.tickets.TicketStatus;
import com.songoda.ultimatemoderation.utils.AbstractChatConfirm;
import com.songoda.ultimatemoderation.utils.Methods;
import com.songoda.ultimatemoderation.utils.gui.AbstractAnvilGUI;
import com.songoda.ultimatemoderation.utils.gui.AbstractGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GUITicketManager extends AbstractGUI {

    private final UltimateModeration plugin;

    private final OfflinePlayer toModerate;

    private TicketStatus status = TicketStatus.OPEN;

    public GUITicketManager(UltimateModeration plugin, OfflinePlayer toModerate, Player player) {
        super(player);
        this.plugin = plugin;
        this.toModerate = toModerate;

        init(plugin.getLocale().getMessage(toModerate != null ? "gui.tickets.titlesingle" : "gui.tickets.title", player.getName()), 54);
    }

    @Override
    protected void constructGUI() {
        inventory.clear();
        resetClickables();
        registerClickables();

        createButton(1, Material.ARROW, plugin.getLocale().getMessage("gui.general.previous"));

        createButton(3 ,Material.DIAMOND_SWORD, Methods.formatText("&6" + status.getStatus()));

        createButton(5, Material.ARROW, plugin.getLocale().getMessage("gui.general.next"));

        if (toModerate != null)
        createButton(7, Material.REDSTONE, plugin.getLocale().getMessage("gui.tickets.create"));

        if (player.hasPermission("um.ticket"))
            createButton(8, Material.OAK_DOOR, plugin.getLocale().getMessage("gui.general.back"));

        for (int i = 0; i < 9; i++)
            createButton(9 + i, Material.GRAY_STAINED_GLASS_PANE, "&1");

        List<Ticket> tickets = toModerate != null ? plugin.getTicketManager().getTicketsAbout(toModerate, status) : plugin.getTicketManager().getTickets(status);

        for (int i = 0; i < tickets.size(); i++) {
            Ticket ticket = tickets.get(i);

            String subjectStr = ticket.getSubject();

            ArrayList<String> lore = new ArrayList<>();
            int lastIndex = 0;
            for (int n = 0; n < subjectStr.length(); n++) {
                if (n - lastIndex < 20)
                    continue;

                if (subjectStr.charAt(n) == ' ') {
                    lore.add("&6" +subjectStr.substring(lastIndex, n).trim());
                    lastIndex = n;
                }
            }

            if (lastIndex - subjectStr.length() < 20)
                lore.add("&6" + subjectStr.substring(lastIndex, subjectStr.length()).trim() + " &7- " + ticket.getTicketId());

            String name = lore.get(0);
            lore.remove(0);

            lore.add("");

            SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");

            lore.add(plugin.getLocale().getMessage("gui.ticket.status", ticket.getStatus().getStatus()));

            if (toModerate != null)
                lore.add(plugin.getLocale().getMessage("gui.tickets.player", Bukkit.getOfflinePlayer(ticket.getVictim()).getName()));
            lore.add(plugin.getLocale().getMessage("gui.ticket.createdon", format.format(new Date(ticket.getCreationDate()))));
            lore.add(plugin.getLocale().getMessage("gui.tickets.click"));

            createButton(18 + i, Material.MAP, name, lore);

            registerClickable(18 + i, ((player1, inventory1, cursor, slot, type) ->
                    new GUITicket(plugin, ticket, toModerate, player)));
        }

    }

    @Override
    protected void registerClickables() {
        if (player.hasPermission("um.ticket")) {
            registerClickable(8, ((player1, inventory1, cursor, slot, type) -> {
                if (toModerate == null)
                    new GUIPlayers(plugin, player);
                else
                    new GUIPlayer(plugin, toModerate, player1);
            }));
        }

        registerClickable(3, ((player1, inventory1, cursor, slot, type) -> {
            this.status = status == TicketStatus.OPEN ? TicketStatus.CLOSED : TicketStatus.OPEN;
            constructGUI();
        }));

        if (toModerate != null) {
                registerClickable(7, ((player1, inventory1, cursor, slot, type) ->
                        createNew(player, toModerate)));
        }
    }

    public static void createNew(Player player, OfflinePlayer toModerate) {
        UltimateModeration plugin = UltimateModeration.getInstance();

        AbstractAnvilGUI gui = new AbstractAnvilGUI(player, event -> {
            Ticket ticket = new Ticket(toModerate, event.getName());

            player.sendMessage(plugin.getLocale().getMessage("gui.tickets.what"));
            AbstractChatConfirm abstractChatConfirm = new AbstractChatConfirm(player, event2 -> {
                plugin.getTicketManager().addTicket(ticket);

                ticket.addResponse(new TicketResponse(player, event2.getMessage(), System.currentTimeMillis()));
            });

            abstractChatConfirm.setOnClose(() ->
                    new GUITicket(plugin, ticket, toModerate, player));
        });

        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.getLocale().getMessage("gui.tickets.subject"));
        item.setItemMeta(meta);

        gui.setSlot(AbstractAnvilGUI.AnvilSlot.INPUT_LEFT, item);
        gui.open();
    }

    @Override
    protected void registerOnCloses() {
    }
}
