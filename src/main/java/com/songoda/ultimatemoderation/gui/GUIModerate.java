package com.songoda.ultimatemoderation.gui;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.commands.CommandFreeze;
import com.songoda.ultimatemoderation.command.commands.CommandRevive;
import com.songoda.ultimatemoderation.command.commands.CommandSpy;
import com.songoda.ultimatemoderation.utils.ServerVersion;
import com.songoda.ultimatemoderation.utils.gui.AbstractGUI;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class GUIModerate extends AbstractGUI {

    private final UltimateModeration plugin;

    private final OfflinePlayer toModerate;

    public GUIModerate(UltimateModeration plugin, OfflinePlayer toModerate, Player player) {
        super(player);
        this.plugin = plugin;
        this.toModerate = toModerate;

        init(plugin.getLocale().getMessage("gui.moderate.title", toModerate.getName()), 45);
    }

    @Override
    protected void constructGUI() {

        createButton(8, plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.OAK_DOOR : Material.valueOf("WOOD_DOOR"), "Back");

        createButton(10, plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.BLUE_ICE : Material.valueOf("PACKED_ICE"), "&6&lFreeze", "&7Stop this player from moving.", "", "&7Currently:&6 " + (CommandFreeze.isFrozen(toModerate) ? "Frozen" : "Unfrozen"));
        createButton(12, Material.SADDLE, "&6&lSpy", "&7Spy on this player");
        createButton(14, Material.CHEST, "&c&lInventory", "&7Access this players Inventory.");
        createButton(16, Material.ENDER_CHEST, "&a&lEnderchest", "&7Access this players Enderchest");

        createButton(28, plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPLASH_POTION : Material.valueOf("POTION"), "&c&lRevive", "&7Revive this player.");
    }

    @Override
    protected void registerClickables() {
        registerClickable(8, ((player1, inventory1, cursor, slot, type) ->
                new GUIPlayer(plugin, toModerate, player1)));

        registerClickable(10, ((player1, inventory1, cursor, slot, type) -> {
            CommandFreeze.freeze(toModerate, player);
            constructGUI();
        }));

        registerClickable(12, ((player1, inventory1, cursor, slot, type) -> {
            CommandSpy.spy(toModerate, player);
            player.closeInventory();
        }));

        registerClickable(14, ((player1, inventory1, cursor, slot, type) ->
                player.openInventory(toModerate.getPlayer().getInventory())));

        registerClickable(16, ((player1, inventory1, cursor, slot, type) ->
                player.openInventory(toModerate.getPlayer().getEnderChest())));

        registerClickable(28, ((player1, inventory1, cursor, slot, type) ->
                CommandRevive.revive(toModerate.getPlayer(), player)));
    }

    @Override
    protected void registerOnCloses() {
    }
}
