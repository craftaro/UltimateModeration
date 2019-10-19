package com.songoda.ultimatemoderation.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.utils.gui.AbstractGUI;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class GUIPlayer extends AbstractGUI {

    private final UltimateModeration plugin;

    private final OfflinePlayer toModerate;

    private boolean punish, tickets, punishments, notes, moderate;

    public GUIPlayer(UltimateModeration plugin, OfflinePlayer toModerate, Player player) {
        super(player);
        this.plugin = plugin;
        this.toModerate = toModerate;
        this.punish = player.hasPermission("um.punish");
        this.tickets = player.hasPermission("um.tickets");
        this.punishments = player.hasPermission("um.punishments");
        this.notes = player.hasPermission("um.notes");
        this.moderate = player.hasPermission("um.moderation");

        init(plugin.getLocale().getMessage("gui.player.title")
                .processPlaceholder("toModerate", toModerate.getName()).getMessage(), 54);
    }

    @Override
    protected void constructGUI() {
        ItemStack head = new ItemStack(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), 1, (byte) 3);
        SkullMeta meta = ((SkullMeta) head.getItemMeta());
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13))
            meta.setOwningPlayer(toModerate);
        else
            meta.setOwner(toModerate.getName());
        head.setItemMeta(meta);

        createButton(13, head, "&7&l" + toModerate.getName(),
                player.isOnline() ? "&a" + plugin.getLocale().getMessage("gui.players.online.online") : "&c" + plugin.getLocale().getMessage("gui.players.online.offline"));

        createButton(8, CompatibleMaterial.OAK_DOOR.getMaterial(), plugin.getLocale().getMessage("gui.general.back").getMessage());

        if (punish) createButton(38, Material.ANVIL, plugin.getLocale().getMessage("gui.player.punish").getMessage());
        if (tickets) createButton(30, Material.CHEST, plugin.getLocale().getMessage("gui.player.tickets").getMessage());
        if (player.isOnline() && punishments)
            createButton(32, Material.DIAMOND_SWORD, plugin.getLocale().getMessage("gui.player.punishments").getMessage());
        if (notes) createButton(42, Material.MAP, plugin.getLocale().getMessage("gui.player.notes").getMessage());
        if (moderate)
            createButton(40, Material.DIAMOND_CHESTPLATE, plugin.getLocale().getMessage("gui.player.moderate").getMessage());
    }

    @Override
    protected void registerClickables() {
        registerClickable(8, ((player1, inventory1, cursor, slot, type) ->
                new GUIPlayers(plugin, player1)));

        if (punish) {
            registerClickable(38, ((player1, inventory1, cursor, slot, type) ->
                    new GUIPunish(plugin, toModerate, null, player1)));
        }

        if (tickets) {
            registerClickable(30, ((player1, inventory1, cursor, slot, type) ->
                    new GUITicketManager(plugin, toModerate, player1)));
        }

        if (punishments) {
            registerClickable(32, ((player1, inventory1, cursor, slot, type) ->
                    new GUIPunishments(plugin, toModerate, player1)));
        }

        if (notes) {
            registerClickable(42, ((player1, inventory1, cursor, slot, type) ->
                    new GUINotesManager(plugin, toModerate, player1)));
        }

        if (moderate) {
            registerClickable(40, ((player1, inventory1, cursor, slot, type) ->
                    new GUIModerate(plugin, toModerate, player1)));
        }
    }

    @Override
    protected void registerOnCloses() {

    }
}
