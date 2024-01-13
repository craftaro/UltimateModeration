package com.craftaro.ultimatemoderation.moderate;

import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.ultimatemoderation.UltimateModeration;
import com.craftaro.ultimatemoderation.utils.VaultPermissions;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class AbstractModeration {
    protected final UltimateModeration plugin;
    private final boolean requireOnline, allowConsole;

    protected AbstractModeration(UltimateModeration plugin, boolean requireOnline, boolean allowConsole) {
        this.plugin = plugin;
        this.requireOnline = requireOnline;
        this.allowConsole = allowConsole;
    }

    public abstract ModerationType getType();

    public abstract XMaterial getIcon();

    public abstract String getProper();

    public abstract String getDescription();

    public String getPermission() {
        return "um." + getProper().toLowerCase();
    }

    public boolean hasPermission(Player player) {
        return player.hasPermission(getPermission());
    }

    public boolean isExempt(OfflinePlayer player) {
        return VaultPermissions.hasPermission(player, getPermission() + ".exempt");
    }

    protected void registerCommand(UltimateModeration plugin) {
        plugin.getCommandManager().addCommand(new GenericModerationCommand(plugin, this));
    }

    public boolean runPreModeration(CommandSender runner, OfflinePlayer toModerate) {
        if (this.requireOnline && !toModerate.isOnline()) {
            this.plugin.getLocale().newMessage(toModerate.getName() + " must be online for this moderation.").sendPrefixedMessage(runner);
        }

        if (isExempt(toModerate)) {
            this.plugin.getLocale().newMessage(toModerate.getName() + " is exempt from this moderation.").sendPrefixedMessage(runner);
            return false;
        }

        return runModeration(runner, toModerate);
    }

    protected abstract boolean runModeration(CommandSender runner, OfflinePlayer toModerate);

    public boolean isRequireOnline() {
        return this.requireOnline;
    }

    public boolean isAllowConsole() {
        return this.allowConsole;
    }
}
