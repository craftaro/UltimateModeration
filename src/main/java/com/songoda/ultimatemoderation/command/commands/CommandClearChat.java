package com.songoda.ultimatemoderation.command.commands;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.AbstractCommand;
import com.songoda.ultimatemoderation.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandClearChat extends AbstractCommand {

    public CommandClearChat() {
        super(true, true, "ClearChat");
    }

    @Override
    protected ReturnType runCommand(UltimateModeration instance, CommandSender sender, String... args) {

        if (args.length != 0 && !args[0].equalsIgnoreCase("force"))
            return ReturnType.SYNTAX_ERROR;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("um.clearchat.bypass") || isForced(args)) {
                String[] toSend = new String[250];
                Arrays.fill(toSend, "");
                player.sendMessage(toSend);
            }

            player.sendMessage(instance.getReferences().getPrefix() + Methods.formatText(instance.getLocale().getMessage("command.clearchat.cleared", sender.getName())));

            if (player.hasPermission("um.clearchat.bypass") && !isForced(args)) {
                player.sendMessage(instance.getLocale().getMessage("command.clearchat.immune"));
            }
        }
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(UltimateModeration instance, CommandSender sender, String... args) {
        if (args.length == 1) {
            return Collections.singletonList("force");
        }
        return null;
    }

    private boolean isForced(String[] args) {
        return args.length != 0 && args[0].equals("force");
    }

    @Override
    public String getPermissionNode() {
        return "um.clearchat";
    }

    @Override
    public String getSyntax() {
        return "/clearChat [force]";
    }

    @Override
    public String getDescription() {
        return "Allows you to clear the chat.";
    }
}
