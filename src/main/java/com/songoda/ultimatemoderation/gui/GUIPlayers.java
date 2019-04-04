package com.songoda.ultimatemoderation.gui;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.punish.player.PlayerPunishData;
import com.songoda.ultimatemoderation.tickets.TicketStatus;
import com.songoda.ultimatemoderation.utils.gui.AbstractGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GUIPlayers extends AbstractGUI {

    private final UltimateModeration plugin;

    private int task;
    private int page = 0;

    public GUIPlayers(UltimateModeration plugin, Player player) {
        super(player);
        this.plugin = plugin;

        init(plugin.getLocale().getMessage("gui.players.title"), 54);
        runTask();
    }

    @Override
    protected void constructGUI() {
        inventory.clear();
        resetClickables();
        registerClickables();


        int numNotes = Bukkit.getOnlinePlayers().size();
        int maxPage = (int) Math.floor(numNotes / 36.0);

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                .skip(page * 36).limit(36).collect(Collectors.toList());

        if (page != 0) {
            createButton(46, Material.ARROW, plugin.getLocale().getMessage("gui.general.previous"));
            registerClickable(46, ((player1, inventory1, cursor, slot, type) -> {
                page --;
                constructGUI();
            }));
        }

        if (maxPage != page) {
            createButton(48, Material.ARROW, plugin.getLocale().getMessage("gui.general.next"));
            registerClickable(48, ((player1, inventory1, cursor, slot, type) -> {
                page ++;
                constructGUI();
            }));
        }

        for (int i = 0; i < players.size(); i++) {
            Player pl = players.get(i);

            PlayerPunishData playerPunishData = plugin.getPunishmentManager().getPlayer(pl);

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = ((SkullMeta) head.getItemMeta());
            meta.setOwningPlayer(pl);
            head.setItemMeta(meta);

            ArrayList<String> lore = new ArrayList<>();
            lore.add(plugin.getLocale().getMessage("gui.players.click"));
            lore.add("");

            int ticketAmt = (int) plugin.getTicketManager().getTicketsAbout(pl).stream()
                    .filter(t -> t.getStatus() == TicketStatus.OPEN).count();

            if (ticketAmt == 0)
                lore.add(plugin.getLocale().getMessage("gui.players.notickets"));
            else {
                if (ticketAmt == 1)
                    lore.add(plugin.getLocale().getMessage("gui.players.ticketsone"));
                else
                    lore.add(plugin.getLocale().getMessage("gui.players.tickets",ticketAmt));
            }

            int warningAmt = playerPunishData.getActivePunishments(PunishmentType.WARNING).size();

            if (warningAmt == 0)
                lore.add(plugin.getLocale().getMessage("gui.players.nowarnings"));
            else {
                if (warningAmt == 1)
                    lore.add(plugin.getLocale().getMessage("gui.players.warningsone"));
                else
                    lore.add(plugin.getLocale().getMessage("gui.players.warnings",warningAmt));
            }


            createButton(i, head, "&7&l" + pl.getName(), lore);
            registerClickable(i, (player1, inventory1, cursor, slot, type) -> new GUIPlayer(plugin, pl, player));
        }

        for (int i = 0; i < 9; i++)
            createButton(36 + i, Material.GRAY_STAINED_GLASS_PANE, "&1");

        createButton(51, Material.CHEST, "&7Tickets");
        createButton(52, Material.MAP, plugin.getLocale().getMessage("gui.players.button.templatemanager"));
    }

    private void runTask() {
        task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::constructGUI, 5L, 5L);
    }

    @Override
    protected void registerClickables() {
        registerClickable(51, (player1, inventory1, cursor, slot, type) ->
                new GUITicketManager(plugin, null, player));

        registerClickable(52, (player1, inventory1, cursor, slot, type) ->
                new GUITemplateManager(plugin, player));
    }

    @Override
    protected void registerOnCloses() {
        registerOnClose(((player1, inventory1) -> Bukkit.getScheduler().cancelTask(task)));
    }
}
