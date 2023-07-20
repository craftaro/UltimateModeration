package com.songoda.ultimatemoderation.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.locale.Message;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.listeners.ChatListener;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandToggleChat extends AbstractCommand {
    private final UltimateModeration plugin;

    /*
     * Chat is enabled by default ;)
     */
    private boolean toggled = true;

    public CommandToggleChat(UltimateModeration plugin) {
        super(CommandType.PLAYER_ONLY, "togglechat");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        this.toggled = !this.toggled;

        Message message = this.toggled ? this.plugin.getLocale().getMessage("command.togglechat.toggledOn")
                : this.plugin.getLocale().getMessage("command.togglechat.toggledOff");

        ChatListener.setChatToggled(this.toggled);

        for (Player player : Bukkit.getOnlinePlayers()) {
            message.sendPrefixedMessage(player);

            if (!player.hasPermission(getPermissionNode() + ".bypass")) {
                continue;
            }
            this.plugin.getLocale().getMessage("command.togglechat.bypass").sendMessage(player);
        }

        if (!(sender instanceof Player)) {
            message.sendPrefixedMessage(sender);
        }

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "um.togglechat";
    }

    @Override
    public String getSyntax() {
        return "/ToggleChat";
    }

    @Override
    public String getDescription() {
        return "Toggle chat for the entire server";
    }
}
