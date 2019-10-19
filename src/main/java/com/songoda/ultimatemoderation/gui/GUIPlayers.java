package com.songoda.ultimatemoderation.gui;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.punish.player.PlayerPunishData;
import com.songoda.ultimatemoderation.tickets.TicketStatus;
import com.songoda.ultimatemoderation.utils.gui.AbstractAnvilGUI;
import com.songoda.ultimatemoderation.utils.gui.AbstractGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GUIPlayers extends AbstractGUI {

    private final UltimateModeration plugin;

    private int task;
    private int page = 0;
    private Online currentOnline = Online.ONLINE;

    public GUIPlayers(UltimateModeration plugin, Player player) {
        super(player);
        this.plugin = plugin;

        init(plugin.getLocale().getMessage("gui.players.title").getMessage(), 54);
        runTask();
    }

    @Override
    protected void constructGUI() {
        inventory.clear();
        resetClickables();
        registerClickables();


        int numNotes = Bukkit.getOnlinePlayers().size();
        int maxPage = (int) Math.floor(numNotes / 36.0);

        List<UUID> players = new ArrayList<>();

        if (currentOnline == Online.ONLINE || currentOnline == Online.BOTH) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getUniqueId());
            }
        }
        if (currentOnline == Online.OFFLINE || currentOnline == Online.BOTH) {
            for (UUID uuid : plugin.getPunishmentManager().getPunishments().keySet()) {
                if (Bukkit.getOfflinePlayer(uuid).isOnline()) continue;
                players.add(uuid);
            }
        }

        players = players.stream()
                .skip(page * 36).limit(36).collect(Collectors.toList());

        if (page != 0) {
            createButton(46, Material.ARROW, plugin.getLocale().getMessage("gui.general.previous").getMessage());
            registerClickable(46, ((player1, inventory1, cursor, slot, type) -> {
                page--;
                constructGUI();
            }));
        }

        if (maxPage != page) {
            createButton(48, Material.ARROW, plugin.getLocale().getMessage("gui.general.next").getMessage());
            registerClickable(48, ((player1, inventory1, cursor, slot, type) -> {
                page++;
                constructGUI();
            }));
        }

        for (int i = 0; i < players.size(); i++) {
            OfflinePlayer pl = Bukkit.getOfflinePlayer(players.get(i));

            PlayerPunishData playerPunishData = plugin.getPunishmentManager().getPlayer(pl);

            ItemStack head = new ItemStack(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), 1, (byte) 3);
            SkullMeta meta = ((SkullMeta) head.getItemMeta());
            if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13))
                meta.setOwningPlayer(pl);
            else
                meta.setOwner(pl.getName());
            head.setItemMeta(meta);

            ArrayList<String> lore = new ArrayList<>();
            lore.add(plugin.getLocale().getMessage("gui.players.click").getMessage());
            lore.add("");

            int ticketAmt = (int) plugin.getTicketManager().getTicketsAbout(pl).stream()
                    .filter(t -> t.getStatus() == TicketStatus.OPEN).count();

            if (ticketAmt == 0)
                lore.add(plugin.getLocale().getMessage("gui.players.notickets").getMessage());
            else {
                if (ticketAmt == 1)
                    lore.add(plugin.getLocale().getMessage("gui.players.ticketsone").getMessage());
                else
                    lore.add(plugin.getLocale().getMessage("gui.players.tickets")
                            .processPlaceholder("amount", ticketAmt).getMessage());
            }

            int warningAmt = playerPunishData.getActivePunishments(PunishmentType.WARNING).size();

            if (warningAmt == 0)
                lore.add(plugin.getLocale().getMessage("gui.players.nowarnings").getMessage());
            else {
                if (warningAmt == 1)
                    lore.add(plugin.getLocale().getMessage("gui.players.warningsone").getMessage());
                else
                    lore.add(plugin.getLocale().getMessage("gui.players.warnings")
                            .processPlaceholder("amount", warningAmt).getMessage());
            }


            createButton(i, head, "&7&l" + pl.getName(), lore);
            registerClickable(i, (player1, inventory1, cursor, slot, type) -> new GUIPlayer(plugin, pl, player));
        }

        for (int i = 0; i < 9; i++)
            createButton(36 + i, ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.GRAY_STAINED_GLASS_PANE : new ItemStack(Material.valueOf("STAINED_GLASS_PANE")), "&1");

        createButton(46, Material.ENDER_PEARL, plugin.getLocale().getMessage("gui.players.search").getMessage());
        createButton(47, Material.HOPPER, "&6" + currentOnline.getTranslation());

        if (player.hasPermission("um.tickets"))
            createButton(51, Material.CHEST, plugin.getLocale().getMessage("gui.players.button.tickets").getMessage());

        if (player.hasPermission("um.templates"))
            createButton(52, Material.MAP, plugin.getLocale().getMessage("gui.players.button.templatemanager").getMessage());
    }


    private enum Online {

        ONLINE, OFFLINE, BOTH;

        private static Online[] vals = values();

        public Online next() {
            return vals[(this.ordinal() != vals.length - 1 ? this.ordinal() + 1 : 0)];
        }

        public String getTranslation() {
            return UltimateModeration.getInstance().getLocale()
                    .getMessage("gui.players.online." + this.name().toLowerCase()).getMessage();
        }
    }

    private void runTask() {
        task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::constructGUI, 5L, 5L);
    }

    @Override
    protected void registerClickables() {

        registerClickable(46, ((player1, inventory1, cursor, slot, type) -> {
            AbstractAnvilGUI gui = new AbstractAnvilGUI(player, event -> {
                List<UUID> players = new ArrayList<>(plugin.getPunishmentManager().getPunishments().keySet());

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (players.contains(player.getUniqueId())) continue;
                    players.add(player.getUniqueId());
                }

                List<UUID> found = players.stream().filter(uuid -> Bukkit.getOfflinePlayer(uuid).getName().equalsIgnoreCase(event.getName())).collect(Collectors.toList());

                if (found.size() == 1) {
                    new GUIPlayer(plugin, Bukkit.getOfflinePlayer(found.get(0)), player);
                } else {
                    plugin.getLocale().getMessage("gui.players.nonefound").sendMessage(player);
                }

            });

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(plugin.getLocale().getMessage("gui.players.name").getMessage());
            item.setItemMeta(meta);

            gui.setSlot(AbstractAnvilGUI.AnvilSlot.INPUT_LEFT, item);
            gui.open();
        }));

        registerClickable(47, ((player1, inventory1, cursor, slot, type) -> {
            this.currentOnline = currentOnline.next();
            this.page = 0;
            constructGUI();
        }));

        if (player.hasPermission("um.tickets")) {
            registerClickable(51, (player1, inventory1, cursor, slot, type) -> {
                new GUITicketManager(plugin, null, player);
            });
        }

        if (player.hasPermission("um.templates")) {
            registerClickable(52, (player1, inventory1, cursor, slot, type) -> {
                if (player.hasPermission("um.templates")) new GUITemplateManager(plugin, player);
            });
        }
    }

    @Override
    protected void registerOnCloses() {
        registerOnClose(((player1, inventory1) -> Bukkit.getScheduler().cancelTask(task)));
    }
}
