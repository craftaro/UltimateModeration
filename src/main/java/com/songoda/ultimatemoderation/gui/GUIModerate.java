package com.songoda.ultimatemoderation.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.commands.CommandFreeze;
import com.songoda.ultimatemoderation.commands.CommandRevive;
import com.songoda.ultimatemoderation.commands.CommandSpy;
import com.songoda.ultimatemoderation.utils.gui.AbstractGUI;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class GUIModerate extends AbstractGUI {

    private final UltimateModeration plugin;
    private final OfflinePlayer toModerate;
    private boolean freeze, spy, invsee, enderview, revive;

    public GUIModerate(UltimateModeration plugin, OfflinePlayer toModerate, Player player) {
        super(player);
        this.plugin = plugin;
        this.toModerate = toModerate;
        this.freeze = !toModerate.getPlayer().hasPermission("um.freeze.exempt") && player.hasPermission("um.freeze");
        this.spy = !toModerate.getPlayer().hasPermission("um.spy.exempt") && player.hasPermission("um.spy");
        this.invsee = !toModerate.getPlayer().hasPermission("um.invsee.exempt") && player.hasPermission("um.invsee");
        this.enderview = !toModerate.getPlayer().hasPermission("um.viewenderchest.exempt") && player.hasPermission("um.viewenderchest");
        this.revive = player.hasPermission("um.revive");

        init(plugin.getLocale().getMessage("gui.moderate.title")
                .processPlaceholder("toModerate", toModerate.getName()).getMessage(), 45);
    }

    @Override
    protected void constructGUI() {

        createButton(8, CompatibleMaterial.OAK_DOOR.getMaterial(), plugin.getLocale().getMessage("gui.general.back").getMessage());

        if (freeze)
            createButton(10, ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.BLUE_ICE : Material.valueOf("PACKED_ICE"), "&6&lFreeze", "&7Stop this player from moving.", "", "&7Currently:&6 " + (CommandFreeze.isFrozen(toModerate) ? "Frozen" : "Unfrozen"));
        if (spy) createButton(12, Material.SADDLE, "&6&lSpy", "&7Spy on this player");
        if (invsee) createButton(14, Material.CHEST, "&c&lInventory", "&7Access this players Inventory.");
        if (enderview) createButton(16, Material.ENDER_CHEST, "&a&lEnderchest", "&7Access this players Enderchest");
        if (revive)
            createButton(28, ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPLASH_POTION : Material.valueOf("POTION"), "&c&lRevive", "&7Revive this player.");
    }

    @Override
    protected void registerClickables() {
        registerClickable(8, ((player1, inventory1, cursor, slot, type) ->
                new GUIPlayer(plugin, toModerate, player1)));

        if (freeze) {
            registerClickable(10, ((player1, inventory1, cursor, slot, type) -> {
                CommandFreeze.freeze(toModerate, player);
                constructGUI();
            }));
        }

        if (spy) {
            registerClickable(12, ((player1, inventory1, cursor, slot, type) -> {
                CommandSpy.spy(toModerate, player);
                player.closeInventory();
            }));
        }

        if (invsee) {
            registerClickable(14, ((player1, inventory1, cursor, slot, type) ->
                    player.openInventory(toModerate.getPlayer().getInventory())));
        }

        if (enderview) {
            registerClickable(16, ((player1, inventory1, cursor, slot, type) ->
                    player.openInventory(toModerate.getPlayer().getEnderChest())));
        }

        if (revive) {
            registerClickable(28, ((player1, inventory1, cursor, slot, type) ->
                    CommandRevive.revive(toModerate.getPlayer(), player)));
        }
    }

    @Override
    protected void registerOnCloses() {
    }
}
