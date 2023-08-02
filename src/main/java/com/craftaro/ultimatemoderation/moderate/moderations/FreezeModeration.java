package com.craftaro.ultimatemoderation.moderate.moderations;

import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.ultimatemoderation.UltimateModeration;
import com.craftaro.ultimatemoderation.moderate.AbstractModeration;
import com.craftaro.ultimatemoderation.moderate.ModerationType;
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
    public XMaterial getIcon() {
        return XMaterial.BLUE_ICE;
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
            this.plugin.getLocale().getMessage("command.freeze.remove")
                    .processPlaceholder("player", toModerate.getPlayer().getDisplayName()).sendPrefixedMessage(runner);
            if (toModerate.isOnline()) {
                this.plugin.getLocale().getMessage("command.freeze.alertremove").sendPrefixedMessage(toModerate.getPlayer());
            }
        } else {
            frozen.add(toModerate.getUniqueId());
            this.plugin.getLocale().getMessage("command.freeze.add")
                    .processPlaceholder("player", toModerate.getPlayer().getDisplayName()).sendPrefixedMessage(runner);
            if (toModerate.isOnline()) {
                this.plugin.getLocale().getMessage("command.freeze.alertadd").sendPrefixedMessage(toModerate.getPlayer());
            }
        }
        return true;
    }

    public static boolean isFrozen(OfflinePlayer player) {
        return frozen.contains(player.getUniqueId());
    }
}
