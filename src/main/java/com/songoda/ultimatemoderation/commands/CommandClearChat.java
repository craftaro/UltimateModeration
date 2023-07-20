package com.songoda.ultimatemoderation.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimatemoderation.UltimateModeration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandClearChat extends AbstractCommand {
    private final UltimateModeration plugin;

    public CommandClearChat(UltimateModeration plugin) {
        super(CommandType.PLAYER_ONLY, "ClearChat");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length != 0 && !args[0].equalsIgnoreCase("force")) {
            return ReturnType.SYNTAX_ERROR;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("um.clearchat.bypass") || isForced(args)) {
                String[] toSend = new String[250];
                Arrays.fill(toSend, "");
                player.sendMessage(toSend);
            }

            this.plugin.getLocale().getMessage("command.clearchat.cleared")
                    .processPlaceholder("player", sender.getName()).sendPrefixedMessage(player);

            if (player.hasPermission("um.clearchat.bypass") && !isForced(args)) {
                this.plugin.getLocale().getMessage("command.clearchat.immune").sendMessage(player);
            }
        }

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
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
