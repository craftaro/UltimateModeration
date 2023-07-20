package com.songoda.ultimatemoderation.gui;

import com.craftaro.core.gui.AnvilGui;
import com.craftaro.core.gui.Gui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.utils.ItemUtils;
import com.craftaro.core.utils.TextUtils;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.punish.player.PlayerPunishData;
import com.songoda.ultimatemoderation.settings.Settings;
import com.songoda.ultimatemoderation.tickets.TicketStatus;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MainGui extends Gui {
    private final UltimateModeration plugin;

    private Online currentOnline = Online.ONLINE;

    private final List<UUID> players = new ArrayList<>();
    private final Player viewer;

    public MainGui(UltimateModeration plugin, Player viewer) {
        this.plugin = plugin;
        setRows(6);
        setDefaultItem(null);
        this.viewer = viewer;

        for (Player player : Bukkit.getOnlinePlayers()) {
            this.players.add(player.getUniqueId());
        }
        for (UUID uuid : plugin.getPunishmentManager().getPunishments().keySet()) {
            if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
                continue;
            }
            this.players.add(uuid);
        }

        setTitle(plugin.getLocale().getMessage("gui.players.title").getMessage());

        showPage();
    }

    private void showPage() {
        if (this.inventory != null) {
            this.inventory.clear();
        }
        setActionForRange(0, 53, null);

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

        setButton(5, 2, GuiUtils.createButtonItem(XMaterial.ENDER_PEARL,
                        this.plugin.getLocale().getMessage("gui.players.search").getMessage()),
                (event) -> {
                    AnvilGui gui = new AnvilGui(event.player, this);
                    gui.setAction(event2 -> {
                        List<UUID> players = new ArrayList<>(this.plugin.getPunishmentManager().getPunishments().keySet());

                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (players.contains(p.getUniqueId())) {
                                continue;
                            }
                            players.add(p.getUniqueId());
                        }

                        List<UUID> found = players.stream().filter(uuid -> Bukkit.getOfflinePlayer(uuid).getName().toLowerCase().contains(gui.getInputText().toLowerCase())).collect(Collectors.toList());

                        if (found.size() >= 1) {
                            this.players.clear();
                            this.players.addAll(found);
                            showPage();
                        } else {
                            this.plugin.getLocale().getMessage("gui.players.nonefound").sendMessage(event.player);
                        }
                        event2.player.closeInventory();
                    });

                    ItemStack item = new ItemStack(Material.PAPER);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(this.plugin.getLocale().getMessage("gui.players.name").getMessage());
                    item.setItemMeta(meta);

                    gui.setInput(item);
                    this.guiManager.showGUI(event.player, gui);
                });

        setButton(5, 3, GuiUtils.createButtonItem(XMaterial.HOPPER, TextUtils.formatText("&6" + this.currentOnline.getTranslation())),
                (event) -> {
                    this.currentOnline = this.currentOnline.next();
                    this.page = 1;
                    showPage();
                });


        if (this.viewer.hasPermission("um.tickets")) {
            setButton(5, 5, GuiUtils.createButtonItem(XMaterial.CHEST,
                            this.plugin.getLocale().getMessage("gui.players.button.tickets").getMessage()),
                    (event) -> this.guiManager.showGUI(event.player, new TicketManagerGui(this.plugin, null, this.viewer)));
        }

        if (this.viewer.hasPermission("um.templates")) {
            setButton(5, 6, GuiUtils.createButtonItem(XMaterial.MAP,
                            this.plugin.getLocale().getMessage("gui.players.button.templatemanager").getMessage()),
                    (events) -> this.guiManager.showGUI(events.player, new TemplateManagerGui(this.plugin, this.viewer)));
        }


        List<UUID> toUse = this.players.stream()
                .filter(u -> this.currentOnline == Online.BOTH
                        || this.currentOnline == Online.ONLINE && Bukkit.getOfflinePlayer(u).isOnline()
                        || this.currentOnline == Online.OFFLINE && !Bukkit.getOfflinePlayer(u).isOnline()).collect(Collectors.toList());

        this.pages = (int) Math.max(1, Math.ceil(toUse.size() / ((double) 28)));

        final List<UUID> toUseFinal = toUse.stream().skip((this.page - 1) * 28).limit(28).collect(Collectors.toList());

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            int num = 11;
            for (UUID uuid : toUseFinal) {
                if (num == 16 || num == 36) {
                    num = num + 2;
                }
                OfflinePlayer pl = Bukkit.getOfflinePlayer(uuid);
                ItemStack skull = ItemUtils.getPlayerSkull(pl);
                setItem(num, skull);

                PlayerPunishData playerPunishData = this.plugin.getPunishmentManager().getPlayer(pl);

                ArrayList<String> lore = new ArrayList<>();
                lore.add(this.plugin.getLocale().getMessage("gui.players.click").getMessage());
                lore.add("");

                int ticketAmt = (int) this.plugin.getTicketManager().getTicketsAbout(pl).stream()
                        .filter(t -> t.getStatus() == TicketStatus.OPEN).count();

                if (ticketAmt == 0) {
                    lore.add(this.plugin.getLocale().getMessage("gui.players.notickets").getMessage());
                } else {
                    if (ticketAmt == 1) {
                        lore.add(this.plugin.getLocale().getMessage("gui.players.ticketsone").getMessage());
                    } else {
                        lore.add(this.plugin.getLocale().getMessage("gui.players.tickets")
                                .processPlaceholder("amount", ticketAmt).getMessage());
                    }
                }

                int warningAmt = playerPunishData.getActivePunishments(PunishmentType.WARNING).size();

                if (warningAmt == 0) {
                    lore.add(this.plugin.getLocale().getMessage("gui.players.nowarnings").getMessage());
                } else {
                    if (warningAmt == 1) {
                        lore.add(this.plugin.getLocale().getMessage("gui.players.warningsone").getMessage());
                    } else {
                        lore.add(this.plugin.getLocale().getMessage("gui.players.warnings")
                                .processPlaceholder("amount", warningAmt).getMessage());
                    }
                }

                setButton(num, GuiUtils.createButtonItem(skull, TextUtils.formatText("&7&l" + pl.getName()), lore),
                        (event) -> this.guiManager.showGUI(event.player, new PlayerGui(this.plugin, pl, this.viewer)));

                num++;
            }
        });

        // enable page events
        setNextPage(4, 7, GuiUtils.createButtonItem(XMaterial.ARROW, this.plugin.getLocale().getMessage("gui.general.next").getMessage()));
        setPrevPage(4, 1, GuiUtils.createButtonItem(XMaterial.ARROW, this.plugin.getLocale().getMessage("gui.general.back").getMessage()));
        setOnPage((event) -> showPage());
    }

    private enum Online {
        ONLINE, OFFLINE, BOTH;

        public Online next() {
            return values()[(this.ordinal() != values().length - 1 ? this.ordinal() + 1 : 0)];
        }

        public String getTranslation() {
            return UltimateModeration.getPlugin(UltimateModeration.class)
                    .getLocale()
                    .getMessage("gui.players.online." + this.name().toLowerCase())
                    .getMessage();
        }
    }
}
