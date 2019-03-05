package com.songoda.ultimatemoderation.gui;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.commands.CommandFreeze;
import com.songoda.ultimatemoderation.command.commands.CommandSpy;
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
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = ((SkullMeta) head.getItemMeta());
        meta.setOwningPlayer(toModerate);
        head.setItemMeta(meta);

        createButton(13, head, "&7&l" + toModerate.getName());

        createButton(8, Material.OAK_DOOR, "Back");

        createButton(28, Material.BLUE_ICE, "&6&lFreeze", "&7Stop a player from moving.", "", "&7Currently:&6 " + (CommandFreeze.isFrozen(toModerate) ? "Frozen" : "Unfrozen"));
        createButton(30, Material.SADDLE, "&6&lSpy", "&7Spy on a player");
        createButton(32, Material.CHEST, "&c&lInventory", "&7Access this users Inventory.");
        createButton(34, Material.ENDER_CHEST, "&a&lEnderchest", "&7Access this users Enderchest");
    }

    @Override
    protected void registerClickables() {
        registerClickable(8, ((player1, inventory1, cursor, slot, type) -> {
            new GUIPlayer(plugin, toModerate, player1);
        }));

        registerClickable(28, ((player1, inventory1, cursor, slot, type) -> {
            CommandFreeze.freeze(toModerate, player);
            constructGUI();
        }));

        registerClickable(30, ((player1, inventory1, cursor, slot, type) -> {
            CommandSpy.spy(toModerate, player);
            player.closeInventory();
        }));

        registerClickable(32, ((player1, inventory1, cursor, slot, type) -> {
            player.openInventory(player.getInventory());
        }));

        registerClickable(34, ((player1, inventory1, cursor, slot, type) -> {
            player.openInventory(player.getEnderChest());
        }));
    }

    @Override
    protected void registerOnCloses() {
    }
}
