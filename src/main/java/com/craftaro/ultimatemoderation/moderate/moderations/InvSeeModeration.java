package com.craftaro.ultimatemoderation.moderate.moderations;

import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.ultimatemoderation.UltimateModeration;
import com.craftaro.ultimatemoderation.moderate.AbstractModeration;
import com.craftaro.ultimatemoderation.moderate.ModerationType;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InvSeeModeration extends AbstractModeration {
    public InvSeeModeration(UltimateModeration plugin) {
        super(plugin, true, false);
        registerCommand(plugin);
    }

    @Override
    public ModerationType getType() {
        return ModerationType.INV_SEE;
    }

    @Override
    public XMaterial getIcon() {
        return XMaterial.CHEST;
    }

    @Override
    public String getProper() {
        return "InvSee";
    }

    @Override
    public String getDescription() {
        return "Allows you to see inside of a players inventory.";
    }

    @Override
    protected boolean runModeration(CommandSender runner, OfflinePlayer toModerate) {
        Player toModeratePlayer = (Player) toModerate;

        ((Player) runner).openInventory(toModeratePlayer.getInventory());
        return false;
    }
}
