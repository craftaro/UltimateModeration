package com.songoda.ultimatemoderation.command.commands;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.AbstractCommand;
import com.songoda.ultimatemoderation.listeners.ChatListener;
import com.songoda.ultimatemoderation.utils.Methods;
import com.songoda.ultimatemoderation.utils.locale.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandToggleChat extends AbstractCommand {

    /*
     * Chat is enabled by default ;)
     */
    private boolean toggled = true;

    public CommandToggleChat() {
        super(false, false, "togglechat");
    }

    @Override
    protected ReturnType runCommand(UltimateModeration instance, CommandSender sender, String... args) {
        toggled = !toggled;

        Message message = toggled ? instance.getLocale().getMessage("command.togglechat.toggledOn")
                : instance.getLocale().getMessage("command.togglechat.toggledOff");

        ChatListener.setChatToggled(toggled);

        for (Player player : Bukkit.getOnlinePlayers()) {

            message.sendPrefixedMessage(player);

            if (!player.hasPermission(getPermissionNode() + ".bypass"))
                continue;

            instance.getLocale().getMessage("command.togglechat.bypass").sendMessage(player);
        }

        if (!(sender instanceof Player))
            message.sendPrefixedMessage(sender);

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(UltimateModeration instance, CommandSender sender, String... args) {
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
