package com.songoda.ultimatemoderation.gui;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.utils.ServerVersion;
import com.songoda.ultimatemoderation.utils.gui.AbstractGUI;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class GUIPlayer extends AbstractGUI {

    private final UltimateModeration plugin;

    private final OfflinePlayer toModerate;

    public GUIPlayer(UltimateModeration plugin, OfflinePlayer toModerate, Player player) {
        super(player);
        this.plugin = plugin;
        this.toModerate = toModerate;

        init(plugin.getLocale().getMessage("gui.player.title", toModerate.getName()), 54);
    }

    @Override
    protected void constructGUI() {
        ItemStack head = new ItemStack(plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), 1, (byte) 3);
        SkullMeta meta = ((SkullMeta) head.getItemMeta());
        if (plugin.isServerVersionAtLeast(ServerVersion.V1_13))
            meta.setOwningPlayer(toModerate);
        else
            meta.setOwner(toModerate.getName());
        head.setItemMeta(meta);

        createButton(13, head, "&7&l" + toModerate.getName());

        createButton(8, plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.OAK_DOOR : Material.valueOf("WOOD_DOOR"), plugin.getLocale().getMessage("gui.general.back"));

        createButton(38, Material.ANVIL, plugin.getLocale().getMessage("gui.player.punish"));
        createButton(30, Material.CHEST, plugin.getLocale().getMessage("gui.player.tickets"));
        createButton(32, Material.DIAMOND_SWORD, plugin.getLocale().getMessage("gui.player.punishments"));
        createButton(42, Material.MAP, plugin.getLocale().getMessage("gui.player.notes"));
        createButton(40, Material.DIAMOND_CHESTPLATE, plugin.getLocale().getMessage("gui.player.moderate"));
    }

    @Override
    protected void registerClickables() {
        registerClickable(8, ((player1, inventory1, cursor, slot, type) ->
                new GUIPlayers(plugin, player1)));

        registerClickable(38, ((player1, inventory1, cursor, slot, type) ->
                new GUIPunish(plugin, toModerate, null, player1)));

        registerClickable(30, ((player1, inventory1, cursor, slot, type) ->
                new GUITicketManager(plugin, toModerate, player1)));

        registerClickable(32, ((player1, inventory1, cursor, slot, type) ->
                new GUIPunishments(plugin, toModerate, player1)));

        registerClickable(42, ((player1, inventory1, cursor, slot, type) ->
                new GUINotesManager(plugin, toModerate, player1)));

        registerClickable(40, ((player1, inventory1, cursor, slot, type) ->
                new GUIModerate(plugin, toModerate, player1)));
    }

    @Override
    protected void registerOnCloses() {

    }
}
