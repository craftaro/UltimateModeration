package com.songoda.ultimatemoderation.moderate.moderations;

import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.moderate.AbstractModeration;
import com.songoda.ultimatemoderation.moderate.ModerationType;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ViewEnderChestModeration extends AbstractModeration {
    public ViewEnderChestModeration(UltimateModeration plugin) {
        super(plugin, true, false);
        registerCommand(plugin);
    }

    @Override
    public ModerationType getType() {
        return ModerationType.ENDER_VIEW;
    }

    @Override
    public XMaterial getIcon() {
        return XMaterial.ENDER_CHEST;
    }

    @Override
    public String getProper() {
        return "ViewEnderChest";
    }

    @Override
    public String getDescription() {
        return "Allows you to see inside of a players ender chest.";
    }

    @Override
    protected boolean runModeration(CommandSender runner, OfflinePlayer toModerate) {
        Player toModeratePlayer = (Player) toModerate;

        ((Player) runner).openInventory(toModeratePlayer.getEnderChest());
        return false;
    }
}
