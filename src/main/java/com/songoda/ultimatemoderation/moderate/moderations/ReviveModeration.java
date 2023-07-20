package com.songoda.ultimatemoderation.moderate.moderations;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.utils.PlayerUtils;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.listeners.DeathListener;
import com.songoda.ultimatemoderation.moderate.AbstractModeration;
import com.songoda.ultimatemoderation.moderate.ModerationType;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ReviveModeration extends AbstractModeration {
    private final UltimateModeration plugin;

    public ReviveModeration(UltimateModeration plugin) {
        super(plugin, true, true);
        this.plugin = plugin;

        registerCommand(plugin);
    }

    @Override
    public ModerationType getType() {
        return ModerationType.REVIVE;
    }

    @Override
    public CompatibleMaterial getIcon() {
        return CompatibleMaterial.POTION;
    }

    @Override
    public String getProper() {
        return "Revive";
    }

    @Override
    public String getDescription() {
        return "Allows you to revive a player.";
    }

    @Override
    protected boolean runModeration(CommandSender runner, OfflinePlayer toModerate) {
        Player toModeratePlayer = (Player) toModerate;
        List<ItemStack> drops = DeathListener.getLastDrop(toModeratePlayer);

        if (drops == null) {
            this.plugin.getLocale().getMessage("command.revive.noloot").sendPrefixedMessage(runner);
            return false;
        }

        ItemStack[] dropArr = new ItemStack[drops.size()];
        dropArr = drops.toArray(dropArr);

        PlayerUtils.giveItem(toModeratePlayer, dropArr);

        this.plugin.getLocale().getMessage("command.revive.revived").sendPrefixedMessage(toModeratePlayer);
        this.plugin.getLocale().getMessage("command.revive.success")
                .processPlaceholder("player", toModerate.getName()).sendPrefixedMessage(runner);
        return true;
    }
}
