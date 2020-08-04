package com.songoda.ultimatemoderation.moderate.moderations;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.moderate.AbstractModeration;
import com.songoda.ultimatemoderation.moderate.ModerationType;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FreezeModeration extends AbstractModeration {

    private static final List<UUID> frozen = new ArrayList<>();

    public FreezeModeration(UltimateModeration plugin) {
        super(plugin, false, true);
        registerCommand(plugin);
    }

    @Override
    public ModerationType getType() {
        return ModerationType.FREEZE;
    }

    @Override
    public CompatibleMaterial getIcon() {
        return CompatibleMaterial.BLUE_ICE;
    }

    @Override
    public String getProper() {
        return "Freeze";
    }

    @Override
    public String getDescription() {
        return "Allows you to freeze a player.";
    }

    @Override
    protected boolean runModeration(CommandSender runner, OfflinePlayer toModerate) {
        if (frozen.contains(toModerate.getUniqueId())) {
            frozen.remove(toModerate.getUniqueId());
            plugin.getLocale().getMessage("command.freeze.remove")
                    .processPlaceholder("player", toModerate.getPlayer().getDisplayName()).sendPrefixedMessage(runner);
            if (toModerate.isOnline())
                plugin.getLocale().getMessage("command.freeze.alertremove").sendPrefixedMessage(toModerate.getPlayer());
        } else {
            frozen.add(toModerate.getUniqueId());
            plugin.getLocale().getMessage("command.freeze.add")
                    .processPlaceholder("player", toModerate.getPlayer().getDisplayName()).sendPrefixedMessage(runner);
            if (toModerate.isOnline())
                plugin.getLocale().getMessage("command.freeze.alertadd").sendPrefixedMessage(toModerate.getPlayer());
        }
        return true;
    }

    public static boolean isFrozen(OfflinePlayer player) {
        return frozen.contains(player.getUniqueId());
    }
}
